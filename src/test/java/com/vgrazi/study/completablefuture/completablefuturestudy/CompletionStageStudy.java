package com.vgrazi.study.completablefuture.completablefuturestudy;


import org.junit.Test;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class CompletionStageStudy {
    @Test
    public void general() {
        sout(Thread.currentThread());
        CompletionStage<Double> stage = CompletableFuture.completedStage(10.);
        stage.thenApply(x -> square(x))
                .thenAccept(x -> sout(x))
                .thenRun(() -> sout(Thread.currentThread()));
    }

    @Test
    public void either() {
        sout(Thread.currentThread());
        CompletionStage<Double> stage1 = CompletableFuture.completedStage(20.);
        CompletionStage<Double> stage2 = CompletableFuture.completedStage(10.);
        stage1.acceptEither(stage2, x -> sout(x)).
                thenRun(() -> sout(Thread.currentThread()));
    }

    @Test
    public void async() throws ExecutionException, InterruptedException {
        sout(Thread.currentThread());
        Executor executor = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);
        CompletableFuture<Void> stage1 = CompletableFuture.runAsync(() -> {
            sout("done");
        }, executor);
        CompletableFuture<Double> stage2 = CompletableFuture.completedFuture(10.).thenApply(CompletionStageStudy::square);
        CompletableFuture combo = stage2.thenApply(this::soutd);
//        CompletableFuture combo = CompletableFuture.allOf(stage1, stage2);
//        combo.thenAccept(System.out::println);
//        Object rval = stage2.get();
//        sout(rval);
        stage1.join();
    }

    private double soutd(double x) {
        sout(x);
        return x;
    }

    private final static Object MUTEX = new Object();
//    @After
    public void delay() {
        synchronized (MUTEX) {
            try {
                MUTEX.wait(10_000);
                sout();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sout(Object... message) {
        System.out.print(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        for (Object s : message) {
            System.out.print(" " + s);
        }
        System.out.println();
    }

    private static double square(double x) {
        return x * x;
    }
}
