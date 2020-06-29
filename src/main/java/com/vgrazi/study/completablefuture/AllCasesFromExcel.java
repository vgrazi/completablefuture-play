package com.vgrazi.study.completablefuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgrazi.study.completablefuture.parser.currency.Currencies;
import com.vgrazi.study.completablefuture.parser.geo.Dataset;
import com.vgrazi.study.completablefuture.parser.geo.GeoPoint;
import lombok.SneakyThrows;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AllCasesFromExcel {
    private final Logger logger = LoggerFactory.getLogger("");
    private CompletableFuture<Void> latch = new CompletableFuture();


    // 1. Create a latch
    @Test
    public void latch() {
        CompletableFuture<String> latch = new CompletableFuture();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                logger.debug("waiting for latch");
                String join = latch.join();
                logger.debug("Done:" + join);
            }).start();
        }
        new Thread(() -> {
            try {
                Thread.sleep(3_000);
                logger.debug("Opening latch");
                latch.complete("Opened latch");
                logger.debug("Latch open");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        String join = latch.join();
        logger.debug("Latch open:" + join);

    }

    // 2. replace the wait's with CompletableFuture
    @Test
    public void latchWithDelayedExecutor() {
        CompletableFuture<String> latch = new CompletableFuture();
        for (int i = 0; i < 3; i++) {
            CompletableFuture.runAsync(() -> {
                logger.debug("waiting for latch");
                String join = latch.join();
                logger.debug("Done:" + join);
            });
        }
        CompletableFuture.runAsync(() -> {
            logger.debug("Opening latch");
            latch.complete("Opened latch");
            logger.debug("Latch open");
        }, CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS));

        String join = latch.join();
        logger.debug("Latch open:" + join);
    }

    // 3. runAsync
    @Test
    public void runAsync() {
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            logger.debug("Sleeping...");
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.debug("Exiting");
        });
        Void join = cf.join();
        logger.debug(String.valueOf(join));
    }

    // 3A. supplyAsync
    @Test
    public void supplyAsync() {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            logger.debug("Sleeping...");
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.debug("Exiting");
            return "All done";
        });
        String join = cf.join();
        logger.debug(join);
    }

    // 3B. supplyAsync
    @Test
    public void supplyAsyncWithExecutor() {
        logger.debug("Starting");
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            logger.debug("Exiting");
            return "All done";
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        String join = cf.join();
        logger.debug(join);
    }

    // 4A. test join
    @Test
    public void join() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Completed value");
        String join = cf.join();
        logger.debug(join);
    }

    // 4B. test join with exception
    @Test
    public void joinWithException() {
        CompletableFuture<String> cf = CompletableFuture.failedFuture(new IllegalArgumentException("Some exception"));
        try
        {
            String join = cf.join();
            logger.debug(join);
        }
        catch (CompletionException e) {
            logger.debug(String.valueOf(e));
            logger.debug(String.valueOf(e.getCause()));
        }
    }

    // 5A. test get
    @Test
    public void get() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Completed value");
        try {
            String join = cf.get();
            logger.debug(join);
        } catch (InterruptedException | ExecutionException e) {
            logger.debug(String.valueOf(e));
        }
    }

    // 5B. test getWithException
    @Test
    public void getWithException() {
        CompletableFuture<String> cf = CompletableFuture.failedFuture(new IllegalArgumentException("Some exception"));
        String join = null;
        try {
            join = cf.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            logger.debug(String.valueOf(e));
            logger.debug(String.valueOf(e.getCause()));
        }
        logger.debug(join);
    }

    // 6A. test getNow
    @Test
    public void getNow() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Completed value");
//        CompletableFuture<String> cf = new CompletableFuture<>();
        String join = cf.getNow("Default value");
        logger.debug(join);
    }

    // 6B. test getNow with Exception
    @Test
    public void getNowWithException() {
        CompletableFuture<String> cf = CompletableFuture.failedFuture(new IllegalArgumentException("Some exception"));
        String join = null;
        try {
            join = cf.getNow("Default value");
        } catch (CompletionException e) {
            logger.debug(String.valueOf(e));
        }
        logger.debug(join);
    }

    // 7A. obtrude
    @Test
    public void obtrude() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("completed");
        logger.debug("1." + cf.join());
        cf.complete("some completed value");
        logger.debug("2." + cf.join());
        cf.obtrudeValue("some obtruded value");
        logger.debug("3." + cf.join());
    }

    // 7A. obtrudeException
    @Test
    public void obtrudeException() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("completed");
        logger.debug("1." + cf.join());
        cf.obtrudeException(new RuntimeException("You got the exception"));
        cf.complete("some completed value");
//        logger.debug("2." + cf.join());
        cf.obtrudeValue("some obtruded value");
        logger.debug("3." + cf.join());
        String join = null;
        try {
            join = cf.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug(join);
    }

    // 8. accept either
    @Test
    public void acceptEither() throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture<>();
        CompletableFuture<String> cf2 = new CompletableFuture<>();
        // accept a consumer
        CompletableFuture<Void> cfEither = cf1.acceptEitherAsync(cf2, logger::debug,
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
//        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        cfEither.get(2, TimeUnit.SECONDS);
        logger.debug("Finishing");
    }

    @Test
    public void thenAccept() {
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<Void> then = cf1.thenAccept(x -> logger.debug(x));
        CompletableFuture.supplyAsync(() -> {
            logger.debug("completing");
            cf1.complete("completed result");
            return null;
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        logger.debug("Joining");
        then.join();
        logger.debug("Finishing");
    }

    @Test
    public void thenAcceptAsync() {
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<Void> then = cf1.thenAcceptAsync(x -> logger.debug(x),
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        cf1.complete("cf1 is done");
        then.join();
        logger.debug("Finishing");
    }

    @Test
    public void thenAcceptBoth() throws InterruptedException, ExecutionException, TimeoutException {
        latch = CompletableFuture.completedFuture(null);
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        CompletableFuture<Void> both = cf1.thenAcceptBoth(cf2, (x, y) -> logger.debug(x + "," + y));
        cf1.complete("cf1 is done");
//        cf2.complete("cf2 is done");
        both.get(1, TimeUnit.SECONDS);
        logger.debug("Finishing");
    }

    @Test
    public void thenAcceptBothAsync() {
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        CompletableFuture<Void> both = cf1.thenAcceptBothAsync(cf2, (x, y) -> logger.debug(x + "," + y),
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        Object result = both.join();
        logger.debug(String.valueOf(result));
        logger.debug("Finishing");
    }

    @Test
    public void applyToEither() throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        CompletableFuture<String> either = cf1.applyToEither(cf2, x -> x);
        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        String join = either.get(2, TimeUnit.SECONDS);
        logger.debug("Finishing:" + join);
    }

    @Test
    public void thenApply() throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        CompletableFuture<String> either = cf1.thenApply(x -> x);
        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        String join = either.get(2, TimeUnit.SECONDS);
        logger.debug("Finishing:" + join);
    }

    @Test
    public void thenCompose() {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
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
                () -> x + "s"));
        String join = cf.join();
        logger.debug(join);
    }

    @Test
    public void allOf() {
        keepAlive();
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        CompletableFuture<String> cf3 = new CompletableFuture();
        CompletableFuture<Void> allOf = CompletableFuture.allOf(cf1, cf2, cf3);
        allOf.thenRunAsync(() -> logger.debug("thenRunAsync"));
        allOf.thenRun(() -> {
            try {
                logger.debug("thenRun starting");
                Thread.sleep(1_000);
                logger.debug("thenRun exiting");
            } catch (InterruptedException e) {
                logger.debug("Interrupted", e);
            }
        });
        cf1.complete("cf1 is done");
        logger.debug("cf1 is done!");
        cf2.complete("cf2 is done");
        logger.debug("cf2 is done!");
        CompletableFuture.runAsync(() -> {
            cf3.complete("cf3 is done");
            logger.debug("cf3 is done!");
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        logger.debug("Joining");
        allOf.join();
        logger.debug("Finishing");
    }

    @Test
    public void parseCoordinatesAndCities() {
        // Summary - we are tasked with preparing incoming EZ Pass data for indexing in Elasticsearch
        // The main file consists of Date, License plate #, geo coords of toll booth, pmnt currency
        // there are some fields that need to be enriched:
        // 1. Geo coords
        // 2. Currency rate
        // 3. License plates
        // Goal: Launch supplyAsync processes:
        // 1. Read main file
        // 2. Read Geo file
        // 3. Read Currency conversions file
        // strategy

    }

    @Test
    public void geoCoordinatesTest() throws IOException {
        logger.debug("Starting geocoordinate test");

        Map<String, Dataset> join = CompletableFuture.supplyAsync(this::parseGeoDatasets)
            .thenApply(this::convertDataSetToZipcodeMap)
//            .thenAccept(map -> {
//                Fields fields = map.get("11223").getFields();
//                logger.info(String.format("11223 is in %s. Geocoords(Lat, long) = %s,%s", fields.getCity(), fields.getLatitude(), fields.getLongitude()));
//            })
            .join();
        logger.debug("Done geo-coordinate test");
    }

    /**
     * Input file contains 43,191 entries of usa coordinates
     *
     * @return
     */
    private Dataset[] parseGeoDatasets() {
        Dataset[] datasets = new Dataset[0];
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("target/classes/us-zip-code-latitude-and-longitude.json");
            datasets = mapper.readValue(file, Dataset[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datasets;
    }

    private String generateLicenseData() {
        int count = random.nextInt(2) + 2;// first 2 to 3 characters are letters
        StringBuilder license = new StringBuilder();
        for (int i = 0; i < count; i++) {
            license.append(generateRandomCharacter());
        }
        license.append("-");
        count = random.nextInt(2) + 3;// final 3 to 4 characters are digits
        for (int i = 0; i < count; i++) {
            license.append(random.nextInt(10));
        }
        return license.toString();
    }

    private final static Random random = new Random();

    private char generateRandomCharacter() {
        char ch = (char) (random.nextInt(26) + 'A');
        return ch;
    }

    /**
     * Creates an account file consisting of over 1000 records
     *
     * @param datasets
     */
    private void writeAccountsFile(Dataset[] datasets) {
        Path path = Paths.get("accounts.csv");
        try {
            Files.write(path, "".getBytes());
            for (int i = 0; i < datasets.length; i += 40) {
                Dataset dataset = datasets[i];
                String str = dataset.toString() + "\n";

                byte[] strToBytes = str.getBytes();

                Files.write(path, strToBytes, StandardOpenOption.APPEND);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a license file consisting of over 1000 records
     */
    @Test
    public void writeLicenseFile() {
        Dataset[] datasets = parseGeoDatasets();
        Path path = Paths.get("license.csv");
        try {
            Files.write(path, "".getBytes());
            for (int i = 0; i < datasets.length / 40; i++) {
                int index = random.nextInt(datasets.length);
                String state = datasets[index].getFields().getState();
                String license = state + ", " + generateLicenseData() + "\n";
                Files.write(path, license.getBytes(), StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parseCurrencies() {
        Currencies currencies = parseCurrencyConversions();
        logger.debug(String.valueOf(currencies));
    }

    private Currencies parseCurrencyConversions() {
        Currencies currencies = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("target/classes/currency-conversions.json");
            currencies = mapper.readValue(file, Currencies.class);
        } catch (IOException e) {
            logger.debug("Exception parsing currency conversions", e);
        }
        return currencies;
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
