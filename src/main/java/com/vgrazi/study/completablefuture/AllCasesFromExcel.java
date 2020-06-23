package com.vgrazi.study.completablefuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgrazi.study.completablefuture.parser.currency.Currencies;
import com.vgrazi.study.completablefuture.parser.geo.Dataset;
import com.vgrazi.study.completablefuture.parser.geo.Fields;
import com.vgrazi.study.completablefuture.parser.geo.GeoPoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class AllCasesFromExcel {
    private final Logger logger = LoggerFactory.getLogger(AllCasesFromExcel.class);
    private CompletableFuture<Void> latch = new CompletableFuture();

    @Test
    public void acceptEither() throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        // accept a consumer
        CompletableFuture<Void> either = cf1.acceptEitherAsync(cf2, logger::debug,
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
//        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        either.get(2, TimeUnit.SECONDS);
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
    public void geoCoordinatesTest() throws IOException {
        logger.debug("Starting geocoordinate test");

        CompletableFuture.supplyAsync(() -> {
            return parseGeoDatasets();
        }).thenApply(this::convertDataSetToZipcodeMap).thenAccept(map -> {
            Fields fields = map.get("11223").getFields();
            logger.info(String.format("11223 is in %s. Geocoords(Lat, long) = %s,%s", fields.getCity(), fields.getLatitude(), fields.getLongitude()));
        })
            .join();
        logger.debug("Done geo-coordinate test");
    }

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
