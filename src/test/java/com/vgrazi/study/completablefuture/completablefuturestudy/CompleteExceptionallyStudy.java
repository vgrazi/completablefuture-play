package com.vgrazi.study.completablefuture.completablefuturestudy;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CompleteExceptionallyStudy {
    private final Logger logger = LoggerFactory.getLogger(CompleteExceptionallyStudy.class);
    @Test
    public void completeExceptionallyExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(String::toUpperCase,
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));


        CompletableFuture<String> exceptionHandler = cf.handle((s, throwable) ->
            (throwable != null) ? "message upon cancel" : "");

        cf.completeExceptionally(new RuntimeException("completed exceptionally"));
        assertTrue("Was not completed exceptionally", cf.isCompletedExceptionally());
        try {
            cf.join();
            fail("Should have thrown an exception");
        } catch (CompletionException ex) { // just for testing
            assertEquals("completed exceptionally", ex.getCause().getMessage());
        }
        assertEquals("message upon cancel", exceptionHandler.join());
    }

    @After
    public void keepAlive() throws InterruptedException {
        Thread.sleep(5_000);
    }
}
