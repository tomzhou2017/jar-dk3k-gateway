package com.dk3k.framework.zookeeper.lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Description:为org.I0Itec.zkclient.ZkClient提供锁,提供两种方式,
 * 一个是竞争锁之后顺序执行,一个是竞争锁后只执行一次,竞争失败的不执行
 * zk对象可以创建新的竞争锁之后关闭,也可以使用同一个zk对象
 * zk对象创建临时对象,可能会导致该任务还未结束,对象就被关闭
 * 如果使用同一个,会创建比较多的watcher监听器
 * 该对象可能会导致死锁问题,不建议使用
 * 
 */
public class ZkClientLock {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZkClientLock.class);

	public String join(ZkClient zk, String groupPath) throws KeeperException, InterruptedException {
		// 创建父节点
		createParentPath(zk, getParentPath(groupPath));
		// 子节点
		String path = getParentPath(groupPath) + "/" + ZkLockConstant.NODE_PREFIX + ZkLockConstant.NODE_NAME_SEPARATOR + zk.hashCode()
				+ ZkLockConstant.NODE_NAME_SEPARATOR;
		// 建立一个顺序临时节点
		String createdPath = zk.create(path, null, CreateMode.EPHEMERAL_SEQUENTIAL);
		LOGGER.debug("Created node " + createdPath);
		// 返回最后的子节点
		return createdPath.substring(createdPath.lastIndexOf('/'));
	}

	/**
	 * 
	 * Description:获取完整的父节点
	 * 
	 */
	private String getParentPath(String groupPath) {
		return ZkLockConstant.ROOT_NODE + "/" + groupPath;
	}

	/**
	 * 
	 * Description:层级创建永久父节点
	 */
	private void createParentPath(ZkClient zk, String path) throws KeeperException, InterruptedException {
		if (path == null || path == "") {
			return;
		}
		if (!zk.exists(path)) {
			// 创建父节点
			createParentPath(zk, path.substring(0, path.lastIndexOf('/')));
			// 创建本节点
			zk.create(path, null, CreateMode.PERSISTENT);
		}
	}

	/**
	 * 
	 * Description:检查本客户端是否得到了分布式锁
	 * 
	 */
	public boolean checkState(ZkClient zk, String groupPath, String myName) throws KeeperException, InterruptedException {
		// 获取所有的子节点
		List<String> childList = zk.getChildren(getParentPath(groupPath));
		String[] myStr = myName.split(ZkLockConstant.NODE_NAME_SEPARATOR);
		long myId = Long.parseLong(myStr[2]);
		// 遍历判断本节点是否是最小的子节点
		boolean minId = true;
		for (String childName : childList) {
			String[] str = childName.split(ZkLockConstant.NODE_NAME_SEPARATOR);
			long id = Long.parseLong(str[2]);
			if (id < myId) {
				minId = false;
				break;
			}
		}
		if (minId) {
			// 是,获取锁
			LOGGER.debug("Get the lock , myid is " + myId);
			return true;
		} else {
			// 不是,没有获取锁
			LOGGER.debug("Can not get the lock , myid is " + myId);
			return false;
		}
	}

	/**
	 * 
	 * Description:若本客户端没有得到分布式锁，则进行监听本节点前面的节点（避免羊群效应）
	 */
	public void listenNode(final ZkClient zk, final String groupPath, final String myName, final Executor executor) throws KeeperException,
			InterruptedException {
		// 获取比myid小的最大id
		List<String> childList = zk.getChildren(getParentPath(groupPath));
		String[] myStr = myName.split(ZkLockConstant.NODE_NAME_SEPARATOR);
		long myId = Long.parseLong(myStr[2]);
		List<Long> idList = new ArrayList<Long>();
		Map<Long, String> sessionMap = new HashMap<Long, String>();

		// 把所有的is和session封装到list和map中
		for (String childName : childList) {
			String[] str = childName.split(ZkLockConstant.NODE_NAME_SEPARATOR);
			long id = Long.parseLong(str[2]);
			idList.add(id);
			sessionMap.put(id, str[1] + ZkLockConstant.NODE_NAME_SEPARATOR + str[2]);
		}

		// 把id排序
		Collections.sort(idList);
		// 获取myid的位置
		int i = idList.indexOf(myId);
		// i为0代表我已经是最小的id,此时应该已经获取锁,不允许进行watcher绑定
		if (i <= 0) {
			// 拿到锁,执行功能
			try {
				executor.execute();
			} catch (Exception e) {
				LOGGER.error("execute error", e);
			}
			// 主动删除节点
			deleteNode3Times(zk, groupPath, myName);
			return;
		}
		// 得到myid前面的一个节点
		long headId = idList.get(i - 1);
		String headPath = getParentPath(groupPath) + "/" + ZkLockConstant.NODE_PREFIX + ZkLockConstant.NODE_NAME_SEPARATOR + sessionMap.get(headId);
		LOGGER.debug("Add watcher at " + headPath);
		// TODO 没有办法在绑定watcher的同时做节点存在性校验,存在死锁风险,如果在绑定之后做存在新校验,则可能出现重复执行风险
		zk.subscribeDataChanges(headPath, new IZkDataListener() {

			@Override
			public void handleDataDeleted(String dataPath) throws Exception {
				LOGGER.debug("Triggering event handleDataDeleted");
				try {
					// 只有当前一个节点被删除,即本节点需要重新检测锁
					if (checkState(zk, groupPath, myName)) {
						// 拿到锁,执行功能
						try {
							executor.execute();
						} catch (Exception e) {
							LOGGER.error("execute error", e);
						}
						// 主动删除节点
						deleteNode3Times(zk, groupPath, myName);
						return;
					}
					listenNode(zk, groupPath, myName, executor);
				} catch (KeeperException e) {
					LOGGER.error("Watcher error", e);
				} catch (InterruptedException e) {
					LOGGER.error("Watcher error", e);
				}
			}

			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				LOGGER.debug("Triggering event handleDataChange");
			}
		});
	}

	/**
	 * 
	 * Description:竞争锁并执行
	 */
	public void getLockAndDoJobSequence(ZkClient zk, String groupPath, Executor executor) throws KeeperException, InterruptedException {
		if (zk == null || groupPath == null || executor == null) {
			return;
		}
		// 创建节点
		String myName = join(zk, groupPath);
		// 绑定watcher,里面会判断锁
		listenNode(zk, groupPath, myName, executor);
	}

	/**
	 * 
	 * Description:竞争锁只有一个执行,即拿到锁的执行,拿不到锁的直接结束
	 */
	public void getLockAndDoJobOnce(ZkClient zk, String groupPath, Executor executor) throws KeeperException, InterruptedException {
		if (zk == null || groupPath == null || executor == null) {
			return;
		}
		// 创建节点
		String myName = join(zk, groupPath);
		// 检测状态
		if (checkState(zk, groupPath, myName)) {
			// 执行功能
			try {
				executor.execute();
			} catch (Exception e) {
				LOGGER.error("execute error", e);
			}
		}
		// 删除节点
		deleteNode3Times(zk, groupPath, myName);
	}

	/**
	 * 
	 * Description:删除指定节点,重试3次
	 */
	private void deleteNode3Times(ZkClient zk, String groupPath, String myName) {
		for (int i = 0; i < 3; i++) {
			try {
				zk.delete(getParentPath(groupPath) + myName);
				// 删除成功,结束
				return;
			} catch (Exception e) {
				// 出异常,循环重试
				LOGGER.error("execute error", e);
			}
		}
	}
}
