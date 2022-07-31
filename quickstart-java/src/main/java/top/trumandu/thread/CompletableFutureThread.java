package top.trumandu.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Truman.P.Du
 * @date 2022/07/31
 * @description
 */
public class CompletableFutureThread {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> oneFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 1;
        });


        CompletableFuture<Integer> twoFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 2;
        });
        CompletableFuture.allOf(oneFuture, twoFuture).join();

        System.out.println("one = " + oneFuture.get());
        System.out.println("two = " + twoFuture.get());

    }
}
