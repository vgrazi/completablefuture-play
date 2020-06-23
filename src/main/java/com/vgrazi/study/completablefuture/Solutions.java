package com.vgrazi.study.completablefuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgrazi.study.completablefuture.parser.Dataset;
import com.vgrazi.study.completablefuture.parser.Fields;
import com.vgrazi.study.completablefuture.parser.GeoPoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class Solutions {
    private final Logger logger = LoggerFactory.getLogger("");
    private CompletableFuture<Void> latch = new CompletableFuture();

    /**
     * Create a completed CompletableFuture, which can be used as the starting stage in a computation
     */
    @Test
    public void completedCompletableFutureExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Done");
        assertTrue(cf.isDone());
        logger.debug("Starting");
        try {
            // this is how we get the results :)
            // But we have to handle the exceptions
            String get = cf.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        // Join is like get, but it wraps the exceptions as runtime exceptions
        String join = cf.join();
        logger.debug("Done:" + join);

        // finally, getNow returns the default if not complete
        String orValueIfAbsent = cf.getNow("Or value if absent");
        logger.debug("GetNow:" + orValueIfAbsent);

        latch.complete(null);
    }

    @Test
    public void completeOnTimeoutExample() {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "completed value",
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));

        CompletableFuture<String> dfTOtimedOut = cf.completeOnTimeout("timed out value", 500, TimeUnit.MILLISECONDS);
        CompletableFuture<String> dfTOCompleted = cf.completeOnTimeout("timed out value", 2, TimeUnit.SECONDS);

        String joinTimedOut = dfTOtimedOut.join();
        logger.debug("Join (timed out):" + joinTimedOut);

        String joinCompleted = dfTOCompleted.join();
        logger.debug("Join (completed):" + joinCompleted);
//        latch.complete(null);
    }

    /**
     * What happens when a CompletableFuture exceptions out???
     */
    @Test
    public void completedExceptionallyFutureExample() {
        CompletableFuture<String> cf = new CompletableFuture();
        cf.completeExceptionally(new Exception("test"));
        assertTrue(cf.isDone());
        logger.debug("Starting");
        try {
            String get = cf.get();
            logger.debug("Get:" + get);
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Uh oh, Exception");
            e.printStackTrace();
        }
        logger.debug("Trying join...");
        String join = cf.join();
        logger.debug("Join:" + join);
    }

//    /**
//     * A latch allow all processes to wait for a "start-now" signal. This can be simulated with a CompletableFuture.
//     * Code up 10 threads that will wait for an external thread to open the latch, then all those 10 plus any others
//     * will be allowed to pass
//     */
//    @Test
//    public void testLatchFunctioning() {
//        logger.debug("Launching");
//        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
//        ExecutorService service = Executors.newCachedThreadPool();
//        // launch 10 runnables, each one performs a join on the CompletableFuture,
//        // thereby waiting for it to complete.
//        launchNRunnables(completableFuture, service, 10);
//
//        service.execute(() -> {
//            try {
//                Thread.sleep(2000);
//                logger.debug("Releasing");
//                // now we complete the future, thereby releasing all of the waiting threads
//                completableFuture.complete(null);
//                // just for the fun of it, launch a few more and make sure they do not block
//                Thread.sleep(100);
//                logger.debug("Launching 2 more");
//                // launch a few more, to show they run without waiting, as expected by a latch
//                launchNRunnables(completableFuture, service, 2);
////                latch.complete(null);
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    @Test
    public void whichThreadForThenRun() {
        logger.debug("Starting");
        CompletableFuture.runAsync(() ->
            logger.debug("In the runAsync()")).
            thenRun(() -> {
                logger.debug("In the thenRun()");
                latch.complete(null);
            });
// todo: do the same for an allOf CompletableFuture
    }

    @Test
    public void whichThreadForThenRunAsync() {
        logger.debug("Starting");
        CompletableFuture.runAsync(() ->
            logger.debug("In the runAsync()")).
            thenRun(() -> {
                logger.debug("In the thenRun()");
                latch.complete(null);
            });
// todo: do the same for an allOf CompletableFuture
    }

    @Test
    public void whichThreadForThenRunOnMultipleCompletableFutures() {
        logger.debug("Starting");
        CompletableFuture[] cfs = getNCompletableFutures(4);
        CompletableFuture allOf = CompletableFuture.allOf(cfs);
        allOf.thenRunAsync(() ->
            logger.debug("In the runAsync()")).
            thenRunAsync(() -> {
                logger.debug("In the thenRunAsync()");
            });

        logger.debug("Completing");

        CompletableFuture.runAsync(() -> Arrays.stream(cfs).forEach(cf -> cf.complete(null)),
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));

    }

    @Test
    public void testDelayedExecutor() {
        // todo: would be better if we tie this in with a CompletableFuture
        logger.debug("Launching delayed executor");
        Executor executor = CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS);
        executor.execute(() -> logger.debug("Delayed executor 1a done"));
        executor.execute(() -> logger.debug("Delayed executor 1b done"));
        executor.execute(() -> {
            logger.debug("Delayed executor 2 done");
            executor.execute(() -> logger.debug("Delayed executor 3 done"));
        });
    }

    @Test
    public void completeExceptionallyExample() {
        // wait a second then complete the future with upper case "MESSAGE"
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(String::toUpperCase,
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));

        // create a new CompletableFuture with the exception status, using an exception handler
        // If an exception is thrown,
        CompletableFuture<String> exceptionHandler = cf.handle((s, throwable) ->
            (throwable != null) ? "message upon cancel" : "");

        // force the original CF to complete with an exception
//        cf.completeExceptionally(new RuntimeException("completed exceptionally"));
        assertTrue("Was not completed exceptionally", cf.isCompletedExceptionally());
        try {
            cf.join();
            fail("Should have thrown an exception");
        } catch (CompletionException ex) { // just for testing
            assertEquals("completed exceptionally", ex.getCause().getMessage());
        }
        assertEquals("message upon cancel", exceptionHandler.join());
    }

    /**
     * Launches count runnables, that will block until the future completes
     *
     * @param completableFuture
     * @param executor
     * @param count
     */
    private void launchNRunnables(CompletableFuture<Void> completableFuture, ExecutorService executor, int count) {
        // 1. brute force
//        for (int i = 0; i < count; i++) {
//            CompletableFuture.runAsync(new Runnable() {
//                @Override
//                public void run() {
//                    completableFuture.join();
//                    logger.debug("Done!");
//                }
//            });
//               // you don't have to supply an executor, in which case you will be using the system ForkJoinPool common pool,
//               // which limits the concurrent threads according to the number of available CPUs
////            }, executor);
//        }

//        // 2. This time using lambda notation
//        for (int i = 0; i < count; i++) {
//            CompletableFuture.runAsync(() -> {
//                completableFuture.join();
//                logger.debug("Done!");
//            }, executor);
//        }
        // 3. replace explicit for-loop with a stream
        IntStream.range(0, count).forEach(i ->
            CompletableFuture.runAsync(() -> {
                completableFuture.join();
                logger.debug("Done!");
            }, executor));
    }

    @Test
    public void asynchronousMethodChaining() {
        Object[] cfs = IntStream.range(0, 10).mapToObj(i -> new CompletableFuture()).toArray();

        CompletableFuture<Void> allFutures = CompletableFuture.runAsync(
            () -> getRunnable(0, (CompletableFuture) cfs[0]))
            .thenRunAsync(() -> getRunnable(1, (CompletableFuture) cfs[1]))
            .thenRunAsync(() -> getRunnable(2, (CompletableFuture) cfs[2]));

        notify(0, (CompletableFuture) cfs[0], 0);
        notify(2, (CompletableFuture) cfs[2], 1);
        notify(1, (CompletableFuture) cfs[1], 2);

        allFutures.join();
        logger.debug("Done!!!");
    }

    private void getRunnable(int i, CompletableFuture cf) {
        logger.debug("starting runnable " + i);
        cf.join();
        logger.debug("exiting runnable " + i);
    }

    private void notify(int i, CompletableFuture cf, int delaySeconds) {
        CompletableFuture.runAsync(() -> {
            logger.debug("Notifying " + i);
            cf.complete("done " + i);
        }, CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS));
    }

    private CompletableFuture[] getNCompletableFutures(int count) {
        CompletableFuture[] cfs = IntStream.range(0, count)
            .mapToObj(i -> new CompletableFuture())
            .toArray(CompletableFuture[]::new);
        return cfs;
    }

    @Test
    public void thenApply() {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(1_000);
                logger.debug("Waking");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Done cf1";
        });
        CompletableFuture<String> cf = cf1.thenApply(x -> x + "s");
        String join = cf.join();
        logger.debug(join);
    }
    @Test
    public void thenCompose() {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(1_000);
                logger.debug("Waking");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Done cf1";
        });
        CompletableFuture<String> cf = cf1.thenCompose(x ->
            CompletableFuture.supplyAsync(
                ()-> x + "s"));
        String join = cf.join();
        logger.debug(join);
    }

    @Test
    public void geoCoordinatesTest() throws IOException {
        logger.debug("Starting geocoordinate test");

        CompletableFuture.supplyAsync(()-> {
            Dataset[] datasets = new Dataset[0];
            try {
                ObjectMapper mapper = new ObjectMapper();
                File file = new File("target/classes/us-zip-code-latitude-and-longitude.json");
                datasets = mapper.readValue(file, Dataset[].class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return datasets;
        }).thenApply(this::convertDataSetToZipcodeMap).thenAccept(map-> {
            Fields fields = map.get("11223").getFields();
            logger.info(String.format("11223 is in %s. Geocoords(Lat, long) = %s,%s", fields.getCity(), fields.getLatitude(), fields.getLongitude()));
        })
            .join();
        logger.debug("Done geocoordinate test");
    }

    private Map<GeoPoint, Dataset> convertDataSetToGeoCoordsMap(Dataset[] datasets) {
        return Arrays.stream(datasets).collect(Collectors.toMap(dataset ->
            new GeoPoint(dataset.getFields().getLatitude(), dataset.getFields().getLongitude())
            , dataset -> dataset));
    }

    private Map<String, Dataset> convertDataSetToZipcodeMap(Dataset[] datasets) {
        return Arrays.stream(datasets).collect(Collectors.toMap(dataset -> dataset.getFields().getZip(), dataset -> dataset));
    }

    public void keepAlive() {
        CompletableFuture<Void> future = latch.orTimeout(5, TimeUnit.SECONDS);
        try {
            future.join();
        } catch (Exception e) {

        }
    }
}
