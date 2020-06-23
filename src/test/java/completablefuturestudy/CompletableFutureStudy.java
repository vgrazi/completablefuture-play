package completablefuturestudy;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class CompletableFutureStudy {
    private static CountDownLatch latch;
    private static final Logger logger = LoggerFactory.getLogger(CompletableFutureStudy.class);

    @Test
    public void canceledAfterDoneIsNotCanceled() {
        CompletableFuture done = CompletableFuture.completedFuture("done");
        done.cancel(true);
        logger.debug("Is canceled:" + done.isCancelled());
        assertFalse("cancel after completed produces canceled!!", done.isCancelled());
    }

    @Test
    public void getNowAfterCanceledShouldThrowException() {
        CompletableFuture cf = CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
            logger.debug("Exiting");
        });

        cf.cancel(true);
        try {
            Object value = cf.getNow("ValueIfAbsent");
            logger.debug(String.valueOf(value));
            fail("Expected CancellationException");
        } catch (CancellationException e) {
            // success
        }
    }

    @Test
    public void cancelAfterCanceledShouldNotThrowException() {
        CompletableFuture cf = CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
            logger.debug("Exiting");
        });

        try {
            cf.cancel(true);
            Object value = cf.cancel(true);
            logger.debug(String.valueOf(value));
        } catch (CancellationException e) {
            fail("CancellationException not expected " + e);
        }
    }

    @Test
    public void anyOfAfterOneCanceledShouldThrowException() {
        latch = new CountDownLatch(1);
        CompletableFuture cf = CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
            logger.debug("Exiting");
        });
        CompletableFuture cf2 = CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
            logger.debug("Exiting");
        });

//        cf.cancel(true);
        cf2.cancel(true);
        CompletableFuture cfAnyOf = CompletableFuture.anyOf(cf, cf2);
        logger.debug("Canceled:" + cfAnyOf.isCancelled());
        logger.debug("Done:" + cfAnyOf.isDone());
        try {
            Object value = cfAnyOf.join();
            fail("join after cancel did not throw exception");
        } catch (CompletionException e) {
            logger.debug(">>>>>>>>>>>Caught it! " + e);
        }
        latch.countDown();
    }
    @Test
    public void allOfAfterAllCanceledShouldThrowException() {
        latch = new CountDownLatch(1);
        CompletableFuture cf = CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
            logger.debug("Exiting");
        });
        CompletableFuture cf2 = CompletableFuture.runAsync(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.yield();
            }
            logger.debug("Exiting");
        });

        cf.cancel(true);
        cf2.cancel(true);
        CompletableFuture AllOf = CompletableFuture.allOf(cf, cf2);
        logger.debug("Canceled:" + AllOf.isCancelled());
        logger.debug("Done:" + AllOf.isDone());
        try {
            Object value = AllOf.join();
            fail("join after boh canceled did not throw exception");
        } catch (CompletionException e) {
            logger.debug(">>>>>>>>>>>Caught it! " + e);
        } catch (Exception e) {
            logger.debug(">>>>>>>>>>>Caught it! " + e);
        }
        latch.countDown();
    }

    @Test
    public void thenCompose() {
        latch = new CountDownLatch(1);
logger.debug("Creating hello");
        CompletableFuture cfHello = CompletableFuture.supplyAsync(() -> "Hello",
            CompletableFuture.delayedExecutor(1000, TimeUnit.MILLISECONDS));

logger.debug("Creating Composition");
        CompletableFuture cfComposed = cfHello.thenCompose(x -> {
            logger.debug("Creating World");
            return CompletableFuture.supplyAsync(() -> x + ", World");
        });

logger.debug("Joining");
        Object join = cfComposed.join();
logger.debug("Joined: " + join);

        latch.countDown();
    }

    @Test
    public void testEquals() {
        System.out.println("".equals(null));
    }

    @After
    public void keepAlive() throws InterruptedException {
        latch.await(5_000, TimeUnit.MILLISECONDS);
    }
}
