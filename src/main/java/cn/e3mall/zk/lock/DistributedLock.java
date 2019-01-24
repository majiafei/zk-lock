package cn.e3mall.zk.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @ProjectName: house
 * @Package: cn.e3mall.zk.lock
 * @ClassName: DistributedLock
 * @Author: majiafei
 * @Description:
 * @Date: 2019/1/24 11:38
 */
public class DistributedLock implements Lock, Watcher {

    private ZooKeeper zooKeeper;
    private final int timeOut = 3000;
    private String prefixLock;
    private static final String ROOT_LOCK = "/locks";
    // 等待的前一个锁
    private final ThreadLocal<String> WAIT_LOCK = new ThreadLocal<String>();
    // 当前锁
    private final ThreadLocal<String> CURRENT_LOCK = new ThreadLocal<String>();
    private ConcurrentHashMap<String, CountDownLatch> countDownLatch = new ConcurrentHashMap<String, CountDownLatch>();
    private int sessionTimeout = 30000;
    private volatile static String currentLockStr;

    public DistributedLock(String connetString, String prefixForLock) {
        this.prefixLock = prefixForLock;
        try {
            // 创建zookeeper客户端
            zooKeeper = new ZooKeeper(connetString, sessionTimeout, this);
            // 初始化锁的根节点
            Stat stat = zooKeeper.exists(ROOT_LOCK, false);
            // 若不存在，则创建
            if (stat == null) {
                zooKeeper.create(ROOT_LOCK, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上锁
     */
    public void lock() {
        // 获取到了锁，就锁住
        if (tryLock()) {
            System.out.println(Thread.currentThread().getName() + "获得了锁" + CURRENT_LOCK.get());
            return;
        }
        // 没有获得锁，就等待锁（等待前一个节点删除了，就可以获得锁）
        // 等待锁
        try {
            waitForLock(WAIT_LOCK.get(), sessionTimeout);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 等待锁
    private boolean waitForLock(String prev, long waitTime) throws KeeperException, InterruptedException {
        // 监听前一个节点事件
        Stat stat = zooKeeper.exists(ROOT_LOCK + "/" + prev, true);

        if (stat != null) {
            System.out.println(Thread.currentThread().getName() + "等待锁 " + ROOT_LOCK + "/" + prev);
            countDownLatch.put(prev, new CountDownLatch(1));
            // 计数等待，若等到前一个节点消失，则precess中进行countDown，停止等待，获取锁
            this.countDownLatch.get(prev).await();
            // 修改当前线程str
            currentLockStr = CURRENT_LOCK.get().substring(CURRENT_LOCK.get().lastIndexOf("/") + 1);
            System.out.println(Thread.currentThread().getName() + " 获得了锁");
        }
        return true;
    }
    public void lockInterruptibly() throws InterruptedException {

    }

    /**
     * 尝试获取锁
     *
     * @return
     */
    public boolean tryLock() {
        try {
            // 创建临时有序节点 /locks/test_lock_00000000
            CURRENT_LOCK.set(zooKeeper.create(ROOT_LOCK + "/" + prefixLock + "_lock_", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
            System.out.println(Thread.currentThread().getName() + "创建了锁：" + CURRENT_LOCK.get());
            // 获取根节点下的子节点，将其排序
            List<String> children = zooKeeper.getChildren(ROOT_LOCK, false);
            // 存放所有创建的锁
            List<String> childrenList = new ArrayList<String>();
            for (String string : children) {
                // 如果string是以prefixLock开头的，就是创建的锁
                if (string.startsWith(prefixLock)) {
                    childrenList.add(string);
                }
            }
            // 将锁的集合从小到大排序
            Collections.sort(childrenList);
            System.out.println(childrenList);
            System.out.println(Thread.currentThread().getName() + " 的锁是 " + CURRENT_LOCK.get());
            // 判断当前创建的节点是否是最小的节点,如果是，获取锁成功
            if (CURRENT_LOCK.get().equals(ROOT_LOCK + "/" + childrenList.get(0))) {
                currentLockStr = childrenList.get(0);
                return true;
            }
            // 去掉根节点的路径
            String substring = CURRENT_LOCK.get().substring(CURRENT_LOCK.get().lastIndexOf("/") + 1);
            // 获取前一个节点,Collections.binarySearch(childrenList, substring)为当前节点的下标
            WAIT_LOCK.set(childrenList.get(Collections.binarySearch(childrenList, substring) - 1));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /**
     * 解锁
     */
    public void unlock() {
        try {
            System.out.println(Thread.currentThread().getName() + "释放锁" + CURRENT_LOCK.get() );
            zooKeeper.delete(CURRENT_LOCK.get(), -1);
//            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public Condition newCondition() {
        return null;
    }

    /**
     * 监控前一个节点是否删除，删除了，就countdownlatch，接触阻塞
     *
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        // 由于countDownLatch不可重用，只能监听一个节点，创建一次
        if (watchedEvent.getPath() != null && countDownLatch.get(watchedEvent.getPath().substring(watchedEvent.getPath().lastIndexOf("/") + 1)) != null) {
            countDownLatch.get(currentLockStr).countDown();
        }
    }
}
