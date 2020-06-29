package com.vgrazi.study.completablefuture.completablefuturestudy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class ForkJoinPoolCommonPoolStudy {
    private static final Logger logger = LoggerFactory.getLogger(ForkJoinPoolCommonPoolStudy.class);

    public static void main(String[] args) {
        logger.debug("CPU Core: " + Runtime.getRuntime().availableProcessors());
        logger.debug("CommonPool Parallelism: " + ForkJoinPool.commonPool().getParallelism());
        logger.debug("CommonPool Common Parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        long start = System.nanoTime();
        CompletableFuture.allOf(IntStream.range(0, 100)
            .mapToObj(i -> CompletableFuture
                .runAsync(ForkJoinPoolCommonPoolStudy::blockingOperation))
            .toArray(CompletableFuture[]::new)).join();
        logger.debug("Processed in " + Duration.ofNanos(System.nanoTime() - start).toSeconds() + " sec");
    }

    private static void blockingOperation() {
        try {
            logger.debug("Sleeping");
            Thread.sleep(1000);
//            logger.debug("Waking");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
