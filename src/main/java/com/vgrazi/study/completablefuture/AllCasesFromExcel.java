package com.vgrazi.study.completablefuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgrazi.study.completablefuture.license.License;
import com.vgrazi.study.completablefuture.parser.account.Account;
import com.vgrazi.study.completablefuture.parser.currency.Currencies;
import com.vgrazi.study.completablefuture.parser.geo.GeoDataset;
import com.vgrazi.study.completablefuture.parser.geo.GeoPoint;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class AllCasesFromExcel {
    private final Logger logger = LoggerFactory.getLogger("");

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
        logger.debug("Starting");

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

    /**
     * 3C. supplyAsync exception handling
     */
    @Test
    public void supplyAsyncExceptionHandling() {
        CompletableFuture<List<Account>> cf = CompletableFuture.supplyAsync(() -> {
            File jsonFile = new File("target/classes/accounts.json");
            // must throw RuntimeException. Else, catch and rethrow RuntimeException
            Account[] accounts = readFile(jsonFile, Account[].class);
            return accounts;
        }).thenApply(
            // skipped if there is an exception
            a -> Arrays.asList(a)
        ).exceptionally(
            e -> {
                logger.debug("Exception " + e);
                return new ArrayList<>();
            });
        List<Account> accounts = cf.join();
        logger.debug("Accounts size: " + accounts.size());
    }


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
        try {
            String join = cf.join();
            logger.debug(join);
        } catch (CompletionException e) {
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

    // 7B. obtrudeException
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

    // 9A. then accept
    @Test
    public void thenAccept() {
        logger.debug("Starting thenAccept");
        CompletableFuture<String> cf1 = new CompletableFuture<>();
        CompletableFuture<Void> cf2 = cf1.thenAccept(logger::debug);
        cf1.complete("Done cf1");
        cf2.join();
        logger.debug("Finishing");
//    }
//    @Test
//    public void thenAcceptAsync() {
        logger.debug("Starting thenAcceptAsync");
        CompletableFuture<String> cf3 = new CompletableFuture<>();
        CompletableFuture<Void> cf4 = cf3.thenAcceptAsync(logger::debug);
        cf3.complete("Done cf1");
        cf4.join();
        logger.debug("Finishing");
    }

    // 9B. then accept both
    @Test
    public void thenAcceptBoth() throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Starting thenAcceptBoth");
        CompletableFuture<String> cf1 = new CompletableFuture<>();
        CompletableFuture<String> cf2 = new CompletableFuture<>();
        CompletableFuture<Void> both = cf1.thenAcceptBoth(cf2, (x, y) -> logger.debug(x + "," + y));
        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        both.get(1, TimeUnit.SECONDS);
        logger.debug("Finishing");
//    }
//
//    @Test
//    public void thenAcceptBothAsync() {
        logger.debug("Starting thenAcceptBothAsync");
        CompletableFuture<String> cf3 = new CompletableFuture<>();
        CompletableFuture<String> cf4 = new CompletableFuture<>();
        CompletableFuture<Void> both1 = cf3.thenAcceptBothAsync(cf4, (x, y) -> logger.debug(x + "," + y),
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        cf3.complete("cf3 is done");
        cf4.complete("cf4 is done");
        Object result = both1.join();
        logger.debug(String.valueOf(result));
        logger.debug("Finishing");
    }

    @Test
    public void applyToEither() throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Starting");
        CompletableFuture<String> cf3 = new CompletableFuture<>();
        CompletableFuture<String> cf4 = new CompletableFuture<>();
        CompletableFuture<String> either = cf3.applyToEither(cf4, x -> x);
        cf3.complete("cf3 is done");
        cf4.complete("cf4 is done");
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
        // 1. The main file accounts.csv consists of:
        // Date, License plate #, geo coords of toll booth, pmnt currency
        // there are some fields that need to be enriched:
        // 2. Geo coords - convertDataSetToGeoCoordsMap(geoDatasets)
        // 3. Currency rate - parseCurrencyConversions, then map usdcad, usdmxn, and usdusd to their exchange rate
        // 4. License plates - convertLicensesToLicenseMap
        // Goal: Launch supplyAsync processes:
        // 1. Read main file
        // 2. Read Geo file
        // 3. Read Currency conversions file
        // 4. Read license plates file
        // strategy
        // 1. Create async jobs to read the 4 files into external maps
        // 2. When all are done, enrich the accounts records
        // 3. Write back the revised accounts file - writeAccountFile

        List<Account> accountsList = new ArrayList<>();
        Map<GeoPoint, GeoDataset> geoDatasetMap = new HashMap<>();
        Map<String, Double> currencyMap = new HashMap<>();
        Map<String, License> licenseMap = new HashMap<>();

        CompletableFuture<List<Account>> accountCf = CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/accounts.json");
            Account[] accounts = readFile(file, Account[].class);
            return accounts;
        }).thenApply(accounts -> {
            List<Account> list = Arrays.asList(accounts);
            accountsList.addAll(list);
            return list;
        }).exceptionally(e -> accountsList);

        CompletableFuture<Map<GeoPoint, GeoDataset>> geoCF = CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/us-zip-code-latitude-and-longitude.json");
            GeoDataset[] geoDatasets = readFile(file, GeoDataset[].class);
            logger.debug("geoDatasets length:" + geoDatasets.length);
            return geoDatasets;
        }).thenApply(geoDatasets -> {
            Map<GeoPoint, GeoDataset> geoPointGeoDatasetMap = convertDataSetToGeoCoordsMap(geoDatasets);
            geoDatasetMap.putAll(geoPointGeoDatasetMap);
            logger.debug("geoPointGeoDatasetMap size:" + geoDatasetMap.size());
            return geoPointGeoDatasetMap;
        }).exceptionally(e -> {
            logger.debug("Exception " + e);
            return geoDatasetMap;
        });

        CompletableFuture<Map<String, Double>> currencyCF = CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/currency-conversions.json");
            Currencies currencies = readFile(file, Currencies.class);
            return currencies;
        }).thenApply(currencies -> {
            currencyMap.put("usdcad", currencies.getUsdcad());
            currencyMap.put("usdmxn", currencies.getUsdmxn());
            currencyMap.put("usdusd", currencies.getUsdusd());
            return currencyMap;
        });

        CompletableFuture<Map<String, License>> licensesCF = CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/licenses.json");
            License[] licenses = readFile(file, License[].class);
            return licenses;
        }).thenApply(licenses -> {
            Map<String, License> licensesMap = convertLicensesToLicenseMap(licenses);
            licenseMap.putAll(licensesMap);
            return licensesMap;
        }).exceptionally(ex -> licenseMap);

        CompletableFuture.allOf(geoCF, accountCf, currencyCF, licensesCF)
            .thenRun(() -> {
                logger.debug("In allOf");
                try {
                    logger.debug("accountCf :isDone():" + accountCf.isDone() + ": size:" + accountCf.get().size());
                    logger.debug("geoCF :isDone():" + geoCF.isDone() + ": size:" + geoCF.get().size());
                    logger.debug("currencyCF :isDone():" + currencyCF.isDone() + ": size:" + currencyCF.get().size());
                    logger.debug("licensesCF :isDone():" + licensesCF.isDone() + ": size:" + licensesCF.get().size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                accountsList.forEach(account -> {
                        GeoPoint geoPoint = account.getGeoPoint();
                        GeoDataset geoDataset = geoDatasetMap.get(geoPoint);
                        account.setCity(geoDataset.getFields().getCity());
                        account.setState(geoDataset.getFields().getState());
                        account.setZip(geoDataset.getFields().getZip());
                    }
                );
            })
            .thenRun(() ->
                accountsList.forEach(account -> {
                        String licenseId = account.getLicense();
                        License license = licenseMap.get(licenseId);
                        if (license != null) {
                            String name = license.getName();
                            account.setName(name);
                        }
                        else {
                            logger.debug("No license id:" + licenseId);
                        }
                    }
                ))
            .thenRun(() ->
                accountsList.forEach(account -> {
                        String currency = account.getCurrency();
                        account.setExchangeRate(currencyMap.get(currency));
                    }
                ))
            .thenRun(() -> writeAccountFile(accountsList))
            .join();
        ;

    }

    private Map<String, License> convertLicensesToLicenseMap(License[] licenses) {
        Map<String, License> licenseMap = Arrays.stream(licenses).collect(Collectors.toMap(License::getLicense, license -> license));
        return licenseMap;
    }

    @Test
    public void geoCoordinatesTest() throws IOException {
        logger.debug("Starting geocoordinate test");

        Map<String, GeoDataset> map = CompletableFuture.supplyAsync(this::readGeoDatasets)
            .thenApply(this::convertDataSetToZipcodeMap)
//            .thenAccept(map -> {
//                Fields fields = map.get("11223").getFields();
//                logger.info(String.format("11223 is in %s. Geocoords(Lat, long) = %s,%s", fields.getCity(), fields.getLatitude(), fields.getLongitude()));
//            })
            .join();
        assertTrue(map.size() > 40_000);
        logger.debug("Done geo-coordinate test. Map size:" + map.size());
    }

    /**
     * Input file contains 43,191 entries of usa coordinates
     *
     * @return
     */
    private GeoDataset[] readGeoDatasets() {
        GeoDataset[] geoDatasets = new GeoDataset[0];
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("target/classes/us-zip-code-latitude-and-longitude.json");
            geoDatasets = mapper.readValue(file, GeoDataset[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return geoDatasets;
    }

    /**
     * Input file contains 43,191 entries of usa coordinates
     *
     * @return
     */
    private GeoDataset[] parseAccount() {
        GeoDataset[] geoDatasets = new GeoDataset[0];
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File("target/classes/us-zip-code-latitude-and-longitude.json");
            geoDatasets = mapper.readValue(file, GeoDataset[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return geoDatasets;
    }

    @Test
    public void exceptionally() throws IOException {
        logger.debug("Starting");
        CompletableFuture<List<Account>> cf = readAccountsFile();

        List<Account> accounts = cf.join();
        logger.debug("Is completed exceptionally:" + cf.isCompletedExceptionally());
        logger.debug("Accounts length:" + accounts.size());
    }

    private CompletableFuture<List<Account>> readAccountsFile() {
        CompletableFuture<List<Account>> completableFuture = CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/accounts.json");
            Account[] accounts = readFile(file, Account[].class);
            return accounts;
        }).thenApply(x -> {
            logger.debug("Running inner apply");
            return x;
        }).exceptionally(
            e -> {
                logger.debug("Exception " + e);
                return new Account[0];
            }).thenApply(
            a -> Arrays.asList(a)
        );
        return completableFuture;
    }

    /**
     * Reads the supplied Json file, and parses it to the supplied class. For an array, use Type[].class,
     * for example Account[].class
     *
     * @throws UncheckedIOException if IOException on read
     */
    private <T> T readFile(File jsonFile, Class<T> clazz) throws UncheckedIOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            T records = mapper.readValue(jsonFile, clazz);
            return records;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeAccountFile(List<Account> accountsList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(Paths.get("updatedAccounts.json").toFile(), accountsList);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
     * @param geoDatasets
     */
    private void writeAccountsFile(GeoDataset[] geoDatasets) {
        Path path = Paths.get("accounts.csv");
        try {
            Files.write(path, "".getBytes());
            for (int i = 0; i < geoDatasets.length; i += 40) {
                GeoDataset geoDataset = geoDatasets[i];
                String str = geoDataset.toString() + "\n";

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
        GeoDataset[] geoDatasets = readGeoDatasets();
        Path path = Paths.get("license.csv");
        try {
            Files.write(path, "".getBytes());
            for (int i = 0; i < geoDatasets.length / 40; i++) {
                int index = random.nextInt(geoDatasets.length);
                String state = geoDatasets[index].getFields().getState();
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

    private Map<GeoPoint, GeoDataset> convertDataSetToGeoCoordsMap(GeoDataset[] geoDatasets) {
        return Arrays.stream(geoDatasets).collect(Collectors.toMap(geoDataset ->
                new GeoPoint(geoDataset.getFields().getLatitude(), geoDataset.getFields().getLongitude())
            , geoDataset -> geoDataset, (key1, key2) -> key1));
    }

    /**
     * Parses the Returns a Map&lt;Zip-code, Dataset>
     */
    private Map<String, GeoDataset> convertDataSetToZipcodeMap(GeoDataset[] geoDatasets) {
        return Arrays.stream(geoDatasets).collect(Collectors.toMap(geoDataset -> geoDataset.getFields().getZip(), geoDataset -> geoDataset));
    }
}
