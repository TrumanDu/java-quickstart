package top.trumandu.thread;

import java.util.concurrent.*;

/**
 * @author Truman.P.Du
 * @date 2021/07/02
 * @description
 */
public class Threads {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "thread_pool_" + r.hashCode());
            }
        };


        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5, 50, TimeUnit.SECONDS, new ArrayBlockingQueue<>(3), Executors.defaultThreadFactory());
        executor.allowCoreThreadTimeOut(true);
        for (int i = 0; i < 7; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Future<Integer> future = executor.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            return 1;
        });
        System.out.println(future.get());


        while (executor.getActiveCount() > 0) {
            System.out.println(executor.getActiveCount());
        }

        //executorService.shutdown();

    }
}
