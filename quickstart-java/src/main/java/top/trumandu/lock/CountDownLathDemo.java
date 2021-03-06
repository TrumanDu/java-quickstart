package top.trumandu.lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Truman.P.Du
 * @date 2021/07/24
 * @description
 */
public class CountDownLathDemo {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("execute success.");
            latch.countDown();
        });
        thread.setDaemon(true);
        thread.start();

        boolean result = latch.await(10, TimeUnit.SECONDS);
        if(!result){
            System.out.println("timeout.");
        }

    }
}
