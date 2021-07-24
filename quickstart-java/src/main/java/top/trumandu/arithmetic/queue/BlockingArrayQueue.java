package top.trumandu.arithmetic.queue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Truman.P.Du
 * @date 2021/07/23
 * @description
 */
public class BlockingArrayQueue<E> {
    private int size = 0;
    private Object[] array;
    private Lock lock;
    private Condition notEmpty;
    private Condition notFull;
    private int head;
    private int tail;

    public BlockingArrayQueue(int capacity) {
        this.array = new Object[capacity];
        lock = new ReentrantLock(false);
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    /**
     * 阻塞出队
     *
     * @return
     * @throws InterruptedException
     */
    public E take() throws InterruptedException {
        lock.lock();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            E e = (E) array[head];
            size--;
            head = (head + 1) % array.length;
            notFull.signal();
            return e;

        } catch (Exception error) {
            notFull.signal();
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * 阻塞入队
     *
     * @param e
     * @return
     * @throws InterruptedException
     */
    public boolean offer(E e) throws InterruptedException {
        lock.lock();
        try {
            while (size == array.length) {
                notFull.await();
            }
            array[tail] = e;
            tail = (tail + 1) % array.length;
            size++;
            notEmpty.signal();
            return true;
        } catch (Exception error) {
            notEmpty.signal();
            return false;
        } finally {
            lock.unlock();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        BlockingArrayQueue<String> blockingArrayQueue = new BlockingArrayQueue<>(100);
        CountDownLatch countDownLatch = new CountDownLatch(2);

        new Thread(()->{
            for (int i = 0; i <1_000 ; i++) {
                try {
                    blockingArrayQueue.offer(""+i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            countDownLatch.countDown();
        },"thread-1").start();

        new Thread(()->{
            for (int i = 0; i <1_000 ; i++) {
                try {
                    System.out.println(blockingArrayQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            countDownLatch.countDown();
        },"thread-2").start();


        countDownLatch.await(1, TimeUnit.MINUTES);
    }

}
