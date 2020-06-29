package com.vgrazi.study.completablefuture.completablefuturestudy;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class WhichThreadStudy {
    private final static Logger logger = LoggerFactory.getLogger(WhichThreadStudy.class);

    @Test
    public void whichThreadForThenRun() {
        logger.debug("Starting");
        CompletableFuture.runAsync(() ->
            logger.debug("In the runAsync()")).
            thenRun(() -> logger.debug("In the thenRun()"));
    }

    @Test
    public void whichThreadForThenRunAsync() {
        logger.debug("Starting");
        CompletableFuture.runAsync(() ->
            logger.debug("In the runAsync()")).
            thenRunAsync(() -> logger.debug("In the thenRunAsync()"));
    }

    @Test
    public void whichThreadForRunAfterBoth() {
        logger.debug("Starting");
        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> logger.debug("In the runAsync(1)"));
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> logger.debug("In the runAsync(2)"));
//        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> logger.debug("In the runAsync(1)"), CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
//        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> logger.debug("In the runAsync(2)"), CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        logger.debug("Done");
        cf1.runAfterBoth(cf2, () -> logger.debug("In the runAfterBoth()"));
    }

    @After
    public void keepAlive() throws InterruptedException {
        Thread.sleep(2_000);
    }
}
