package top.trumandu.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Truman.P.Du
 * @date 2021/07/03
 * @description
 */
public class LockTest {

    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private int num = 0;



    public static void main(String[] args) throws InterruptedException {
        LockTest lockTest = new LockTest();
        CountDownLatch countDownLatch  = new CountDownLatch(2);
        Lock lock = new ReentrantLock();
        new Thread(()->{
            for (int i = 0; i < 100_000; i++) {
                lock.lock();
                try {
                    lockTest.num = lockTest.num+1;
                } finally {
                    lock.unlock();
                }
            }
            countDownLatch.countDown();
        },"thread 1").start();

        new Thread(()->{
            for (int i = 0; i < 100_000; i++) {
                lock.lock();
                try {
                    lockTest.num = lockTest.num+1;
                } finally {
                    lock.unlock();
                }
            }
            countDownLatch.countDown();
        },"thread 2").start();

        countDownLatch.await(1, TimeUnit.MINUTES);
        System.out.println("end:"+lockTest.num);
    }
}
