package com.vgrazi.study.completablefuture.completablefuturestudy;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class exceptionallyStudy {
    @Test
     public void testExceptionally() throws InterruptedException, ExecutionException {
        System.out.println("-- running CompletableFuture --");
        CompletableFuture<Integer> completableFuture = CompletableFuture
            .supplyAsync(() -> {
                System.out.println("running task");
                return 10 / 0;
            }).exceptionally(exception -> {
                System.err.println("exception: " + exception);
                return 1;
            });
        Thread.sleep(3000);//let the stages complete
        System.out.println("-- checking exceptions --");
        boolean b = completableFuture.isCompletedExceptionally();
        System.out.println("completedExceptionally: " + b);
        System.out.println("-- getting results --");
        int result = completableFuture.get();
        System.out.println(result);
    }

    @Test
     public void testExceptionally1() {
//            runTasks(2);
            runTasks(0);
        }

        private static void runTasks(int i) {
            System.out.printf("-- input: %s --%n", i);
            CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> 16 / i)
                .thenApply(
                    input -> input + 1)
                .thenApply(
                    input -> input + 3)
                .exceptionally(exception -> {
                    System.out.println("in exceptionally");
                    System.err.println(exception);
                    return 1;
                })
                .thenApply(input -> input * 3)
                .thenAccept(System.out::println);
            System.out.println(cf.isCompletedExceptionally());
        }
}
