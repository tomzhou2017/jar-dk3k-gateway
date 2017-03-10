package com.dk3k.framework.zookeeper.lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Description:为org.apache.zookeeper.ZooKeeper提供锁,提供两种方式,
 * 一个是竞争锁之后顺序执行,一个是竞争锁后只执行一次,竞争失败的不执行
 * zk对象可以创建新的竞争锁之后关闭,也可以使用同一个zk对象
 * zk对象创建临时对象,可能会导致该任务还未结束,对象就被关闭
 * 如果使用同一个,会创建比较多的watcher监听器
 * 
 * @PackageName:com.mobanker.zk.lock
 * @ClassName:ZookeeperLock
 * @author xiongweitao
 * @date 2016年9月19日 上午11:13:35
 */
public class ZookeeperLock {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperLock.class);

	public String join(ZooKeeper zk, String groupPath) throws KeeperException, InterruptedException {
		// 创建父节点
		createParentPath(zk, getParentPath(groupPath));
		// 子节点
		String path = getParentPath(groupPath) + "/" + ZkLockConstant.NODE_PREFIX + ZkLockConstant.NODE_NAME_SEPARATOR + zk.getSessionId()
				+ ZkLockConstant.NODE_NAME_SEPARATOR;
		// 建立一个顺序临时节点,
		// 创建临时节点,任务执行完成,手动删除该节点,如果任务执行过程中服务器出现异常,zk连接断开,依旧自动删除,避免死锁
		String createdPath = zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		LOGGER.debug("Created node " + createdPath);
		// 返回最后的子节点
		return createdPath.substring(createdPath.lastIndexOf('/'));
	}

	/**
	 * 
	 * Description:获取完整的父节点
	 * 
	 * @param groupPath
	 * @return
	 * @author xiongweitao
	 * @date 2016年9月18日 下午4:07:27
	 */
	private String getParentPath(String groupPath) {
		return ZkLockConstant.ROOT_NODE + "/" + groupPath;
	}

	/**
	 * 
	 * Description:层级创建永久父节点
	 * 
	 * @param zk
	 * @param path
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @author xiongweitao
	 * @date 2016年9月18日 下午4:18:31
	 */
	private void createParentPath(ZooKeeper zk, String path) throws KeeperException, InterruptedException {
		if (path == null || path == "") {
			return;
		}
		Stat stat = zk.exists(path, true);
		if (stat == null) {
			// 创建父节点
			createParentPath(zk, path.substring(0, path.lastIndexOf('/')));
			// 创建本节点
			zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
	}

	/**
	 * 
	 * Description:检查本客户端是否得到了分布式锁
	 * 
	 * @param zk
	 * @param groupPath
	 * @param myName
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @author xiongweitao
	 * @date 2016年9月18日 下午1:39:49
	 */
	public boolean checkState(ZooKeeper zk, String groupPath, String myName) throws KeeperException, InterruptedException {
		// 获取所有的子节点
		List<String> childList = zk.getChildren(getParentPath(groupPath), false);
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
	 * 
	 * @param zk
	 * @param groupPath
	 * @param myName
	 * @param executor
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @author xiongweitao
	 * @date 2016年9月18日 下午1:41:28
	 */
	public void listenNode(final ZooKeeper zk, final String groupPath, final String myName, final Executor executor) throws KeeperException,
			InterruptedException {
		// 获取比myid小的最大id
		List<String> childList = zk.getChildren(getParentPath(groupPath), false);
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
		// i为0代表我已经是最小的id,此时已经获取锁,不允许进行watcher绑定
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
		Stat stat = zk.exists(headPath, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				LOGGER.debug("Triggering event " + event.getType());
				try {
					// 只有当前一个节点被删除,即本节点需要重新检测锁
					if (Watcher.Event.EventType.NodeDeleted.equals(event.getType())) {
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
					}
					// 没拿到锁,或者节点其他状态改动,重新绑定watcher
					listenNode(zk, groupPath, myName, executor);
				} catch (KeeperException e) {
					LOGGER.error("Watcher error", e);
				} catch (InterruptedException e) {
					LOGGER.error("Watcher error", e);
				}
			}
		});
		LOGGER.debug("Add watcher stat is " + stat);
		// 前一个节点不存在,拿到锁,执行功能
		if (stat == null) {
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
	}

	/**
	 * 
	 * Description:竞争锁并依次执行
	 * 
	 * @param zk
	 * @param groupPath
	 * @param executor
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @author xiongweitao
	 * @date 2016年9月18日 下午5:12:14
	 */
	public void getLockAndDoJobSequence(ZooKeeper zk, String groupPath, Executor executor) throws KeeperException, InterruptedException {
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
	 * 
	 * @param zk
	 * @param groupPath
	 * @param executor
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @author xiongweitao
	 * @date 2016年9月19日 下午3:21:24
	 */
	public void getLockAndDoJobOnce(ZooKeeper zk, String groupPath, Executor executor) throws KeeperException, InterruptedException {
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
	 * 
	 * @param zk
	 * @param groupPath
	 * @param myName
	 * @author xiongweitao
	 * @date 2016年9月19日 下午6:22:01
	 */
	private void deleteNode3Times(ZooKeeper zk, String groupPath, String myName) {
		for (int i = 0; i < 3; i++) {
			try {
				zk.delete(getParentPath(groupPath) + myName, -1);
				// zk.close();
				// 删除成功,结束
				return;
			} catch (Exception e) {
				// 出异常,循环重试
				LOGGER.error("execute error", e);
			}
		}
	}
}
