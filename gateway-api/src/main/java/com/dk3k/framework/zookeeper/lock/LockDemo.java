package com.dk3k.framework.zookeeper.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.github.zkclient.ZkClient;

public class LockDemo {

	public static void main(String[] args) throws Exception {

		// test com.github.zkclient.ZkClient
		// new LockDemo().testZkClient("192.168.1.14:2181", "zkLock-xwt-tmp");

//		new LockDemo().testZkClientOnce("192.168.1.14:2181", "zkLock-xwt-tmp");
		 new LockDemo().testZkClientSequence("192.168.1.14:2181",
		 "zkLock-xwt-tmp");

		// test org.apache.curator.framework.CuratorFramework
		// new LockDemo().testCuratorFramework("192.168.1.14:2181",
		// "zkLock-xwt-tmp");

		// test org.I0Itec.zkclient.ZkConnection
		// new LockDemo().testZkConnection("192.168.1.14:2181",
		// "zkLock-xwt-tmp");

	}

	public void testZkClientSequence(final String server, final String groupName) throws Exception {
		ZkClient client = new ZkClient(server);
		final ZooKeeper zooKeeper = client.getZooKeeper();
		final ZookeeperLock lock = new ZookeeperLock();
		Thread thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobSequence(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobSequence(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread3 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobSequence(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread4 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobSequence(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread5 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobSequence(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();
		thread5.start();
		Thread.sleep(Integer.MAX_VALUE);
		client.close();
	}

	public void testZkClientOnce(String server, final String groupName) throws Exception {
		ZkClient client = new ZkClient(server);
		final ZooKeeper zooKeeper = client.getZooKeeper();
		final ZookeeperLock lock = new ZookeeperLock();
		Thread thread1 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobOnce(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread2 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobOnce(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread3 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobOnce(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread4 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobOnce(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Thread thread5 = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.getLockAndDoJobOnce(zooKeeper, groupName, new Executor() {
						@Override
						public void execute() {
							job();
						}
					});
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();
		thread5.start();
		Thread.sleep(Integer.MAX_VALUE);
		client.close();
	}

	public void testZkClient(String server, String groupName) throws Exception {
		ZkClient client = new ZkClient(server);
		ZooKeeper zooKeeper = client.getZooKeeper();
		ZookeeperLock lock = new ZookeeperLock();
		lock.getLockAndDoJobSequence(zooKeeper, groupName, new Executor() {
			@Override
			public void execute() {
				job();
			}
		});
		Thread.sleep(Integer.MAX_VALUE);
		client.close();
	}

	public void testCuratorFramework(String server, String groupName) throws Exception {
		RetryPolicy retryUntilElapsedPolicy = new RetryUntilElapsed(1000, 50);
		CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString(server).retryPolicy(retryUntilElapsedPolicy).build();
		curatorFramework.start();
		ZooKeeper zooKeeper = curatorFramework.getZookeeperClient().getZooKeeper();
		ZookeeperLock lock = new ZookeeperLock();
		lock.getLockAndDoJobSequence(zooKeeper, groupName, new Executor() {
			@Override
			public void execute() {
				job();
			}
		});
		Thread.sleep(Integer.MAX_VALUE);
		curatorFramework.close();
	}

	public void testZkConnection(String server, String groupName) throws Exception {
		// 这个比较特殊,无法获取到zookeeper对象,只能使用外部包装后的对象,不建议使用
		org.I0Itec.zkclient.ZkClient client = new org.I0Itec.zkclient.ZkClient(server);
		ZkClientLock lock = new ZkClientLock();
		lock.getLockAndDoJobSequence(client, groupName, new Executor() {
			@Override
			public void execute() {
				job();
			}
		});
		Thread.sleep(Integer.MAX_VALUE);
		client.close();
	}

	private void job() {
		System.out.println("do ===============>");
		System.out.println("do ===============>");
		System.out.println("do ===============>");
	}
}
