package top.trumandu.lock;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Truman.P.Du
 * @date 2021/07/03
 * @description
 */
public class LockTest {
    Lock lock = new ReentrantLock();

    public void  increase(Integer i) {
        lock.lock();
        try {
            i=i+1;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        LockTest lockTest = new LockTest();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 200,
                50L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());;
        threadPoolExecutor.allowCoreThreadTimeOut(true);
                Integer i = 0;
        for (int j = 0; j < 1_000; j++) {
            Integer finalI = i;
            threadPoolExecutor.execute(()->{
                lockTest.increase(finalI);
            });
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (threadPoolExecutor.getActiveCount() > 0) {

        }
        System.out.println(i);


        for (int j = 0; j < 100; j++) {
            i = i+1;
            System.out.println(i.hashCode());
        }
    }
}
