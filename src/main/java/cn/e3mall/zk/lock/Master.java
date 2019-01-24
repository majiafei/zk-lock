package cn.e3mall.zk.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ProjectName: house
 * @Package: cn.e3mall.zk.lock
 * @ClassName: Master
 * @Author: majiafei
 * @Description:
 * @Date: 2019/1/24 12:52
 */
public class Master {

    private static int n = 10;
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        final DistributedLock distributedLock = new DistributedLock("192.168.221.100:2181", "test");;
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Runnable() {
                public void run() {
                    try {
                        distributedLock.lock();
                        n--;
                        System.out.println(n);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        distributedLock.unlock();
                    }

                }
            });
        }
        executorService.shutdown();
    }

}
