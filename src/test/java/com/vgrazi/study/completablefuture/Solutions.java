package com.vgrazi.study.completablefuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgrazi.study.completablefuture.license.LicensePlate;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Solutions {
    private final Logger log = LoggerFactory.getLogger("");

    // 4A. test join
    @Test
    public void join() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Completed value");
        String join = cf.join();
        log.debug(join);
    }

    // 4B. test join with exception
    @Test
    public void joinWithException() {
        CompletableFuture<String> cf = CompletableFuture.failedFuture(new IllegalArgumentException("Some exception"));
        try {
            String join = cf.join();
            log.debug(join);
        } catch (CompletionException e) {
            log.debug(String.valueOf(e));
            log.debug(String.valueOf(e.getCause()));
        }
    }

    // 5A. test get
    @Test
    public void get() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Completed value");
        try {
            String join = cf.get();
            log.debug(join);
        } catch (InterruptedException | ExecutionException e) {
            log.debug(String.valueOf(e));
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
            log.debug(String.valueOf(e));
            log.debug(String.valueOf(e.getCause()));
        }
        log.debug(join);
    }

    // 6A. test getNow
    @Test
    public void getNow() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Completed value");
//        CompletableFuture<String> cf = new CompletableFuture<>();
        String join = cf.getNow("Default value");
        log.debug(join);
    }

    // 6B. test getNow with Exception
    @Test
    public void getNowWithException() {
        CompletableFuture<String> cf = CompletableFuture.failedFuture(new IllegalArgumentException("Some exception"));
        String join = null;
        try {
            join = cf.getNow("Default value");
        } catch (CompletionException e) {
            log.debug(String.valueOf(e));
        }
        log.debug(join);
    }

    // 7A. obtrude
    @Test
    public void obtrude() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("completed");
        log.debug("1." + cf.join());
        cf.complete("some completed value");
        log.debug("2." + cf.join());
        cf.obtrudeValue("some obtruded value");
        log.debug("3." + cf.join());
    }

    // 7B. obtrudeException
    @Test
    public void obtrudeException() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("completed");
        log.debug("1." + cf.join());
        cf.obtrudeException(new RuntimeException("You got the exception"));
        cf.complete("some completed value");
//        logger.debug("2." + cf.join());
        cf.obtrudeValue("some obtruded value");
        log.debug("3." + cf.join());
        String join = null;
        try {
            join = cf.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug(join);
    }

    // 3. runAsync
    @Test
    public void runAsync() {
        CompletableFuture<Void> cf = CompletableFuture.runAsync(
            () -> {
                log.debug("Sleeping...");
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("Exiting");
            }).thenRunAsync(
            () -> {
                log.debug("Sleeping...");
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("Exiting");
            });
        Void join = cf.join();
        log.debug(String.valueOf(join));
    }

    // 3A. supplyAsync
    @Test
    public void supplyAsync() {
        log.debug("Starting");

        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            log.debug("Sleeping...");
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.debug("Exiting");
            return "All done";
        });
        String join = cf.join();
        log.debug(join);
    }

    // 3B. supplyAsync
    @Test
    public void supplyAsyncWithExecutor() {
        log.debug("Starting");
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            log.debug("Exiting");
            return "All done";
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        String join = cf.join();
        log.debug(join);
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
                log.debug("Exception " + e);
                return new ArrayList<>();
            });
        List<Account> accounts = cf.join();
        log.debug("Accounts size: " + accounts.size());
    }

    @Test
    public void completedThenRun() {
        log.debug("Starting");
        CompletableFuture<Void> cf = CompletableFuture.completedFuture("start")
            .thenRunAsync(() -> {
                log.debug("Sleeping...");
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("Exiting");
            })
            .thenRunAsync(() -> {
                log.debug("Sleeping...");
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("Exiting");
            })

        ;

        Void join = cf.join();
        log.debug(String.valueOf(join));
    }


    // 1. Create a latch
    @Test
    public void latch() {
        CompletableFuture<String> latch = new CompletableFuture();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                log.debug("waiting for latch");
                String join = latch.join();
                log.debug("Done:" + join);
            }).start();
        }
        new Thread(() -> {
            try {
                Thread.sleep(3_000);
                log.debug("Opening latch");
                latch.complete("Opened latch");
                log.debug("Latch open");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        String join = latch.join();
        log.debug("Latch open:" + join);

    }

    // 2. replace the wait's with CompletableFuture
    @Test
    public void latchWithDelayedExecutor() {
        CompletableFuture<String> latch = new CompletableFuture();
        for (int i = 0; i < 3; i++) {
            CompletableFuture.runAsync(() -> {
                log.debug("waiting for latch");
                String join = latch.join();
                log.debug("Done:" + join);
            });
        }
        CompletableFuture.runAsync(() -> {
            log.debug("Opening latch");
            latch.complete("Opened latch");
            log.debug("Latch open");
        }, CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS));

        String join = latch.join();
        log.debug("Latch open:" + join);
    }



    // 8. accept either
    @Test
    public void acceptEither() throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture<>();
        CompletableFuture<String> cf2 = new CompletableFuture<>();
        // accept a consumer
        CompletableFuture<Void> cfEither = cf1.acceptEitherAsync(cf2, log::debug,
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
//        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        cfEither.get(2, TimeUnit.SECONDS);
        log.debug("Finishing");
    }

    // 9A. then accept
    @Test
    public void thenAccept() {
        log.debug("Starting thenAccept");
        CompletableFuture<String> cf1 = new CompletableFuture<>();
        CompletableFuture<Void> cf2 = cf1.thenAccept(s -> log.debug(s));
        cf1.complete("Done cf1");
        cf2.join();
        log.debug("Finishing");
//    }
//    @Test
//    public void thenAcceptAsync() {
        log.debug("Starting thenAcceptAsync");
        CompletableFuture<String> cf3 = new CompletableFuture<>();
        CompletableFuture<Void> cf4 = cf3.thenAcceptAsync(log::debug);
        cf3.complete("Done cf1");
        cf4.join();
        log.debug("Finishing");
    }

    // 9B. then accept both
    @Test
    public void thenAcceptBoth() throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Starting thenAcceptBoth");
        CompletableFuture<String> cf1 = new CompletableFuture<>();
        CompletableFuture<String> cf2 = new CompletableFuture<>();
        CompletableFuture<Void> both = cf1.thenAcceptBoth(cf2, (x, y) -> log.debug(x + "," + y));
        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        both.get(1, TimeUnit.SECONDS);
        log.debug("Finishing");
//    }
//
//    @Test
//    public void thenAcceptBothAsync() {
        log.debug("Starting thenAcceptBothAsync");
        CompletableFuture<String> cf3 = new CompletableFuture<>();
        CompletableFuture<String> cf4 = new CompletableFuture<>();
        CompletableFuture<Void> both1 = cf3.thenAcceptBothAsync(cf4, (x, y) -> log.debug(x + "," + y),
            CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        cf3.complete("cf3 is done");
        cf4.complete("cf4 is done");
        Object result = both1.join();
        log.debug(String.valueOf(result));
        log.debug("Finishing");
    }

    @Test
    public void applyToEither() throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Starting");
        CompletableFuture<String> cf3 = new CompletableFuture<>();
        CompletableFuture<String> cf4 = new CompletableFuture<>();
        CompletableFuture<String> either = cf3.applyToEither(cf4, x -> x);
        cf3.complete("cf3 is done");
        cf4.complete("cf4 is done");
        String join = either.get(2, TimeUnit.SECONDS);
        log.debug("Finishing:" + join);
    }

    @Test
    public void thenApply() throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        CompletableFuture<String> either = cf1.thenApply(x -> x);
        cf1.complete("cf1 is done");
        cf2.complete("cf2 is done");
        String join = either.get(2, TimeUnit.SECONDS);
        log.debug("Finishing:" + join);
    }

    @Test
    public void thenCombine() {
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            log.debug("cf1 is complete");
            return "Done cf1";
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));

        CompletableFuture<String> cf2 = CompletableFuture.supplyAsync(() -> {
            log.debug("cf2 is complete");
            return "Done cf2";
        }, CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS));

        CompletableFuture<String> cf = cf1.thenCombine(cf2, (cf1_value, cf2_value)-> cf1_value+"+"+cf2_value);
        String join = cf.join();
        log.debug("Completed: " + join);
    }

    @Test
    public void thenCompose() {
        CompletableFuture cf1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1_000);
                log.debug("Waking");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Done cf1";
        });
        CompletableFuture<String> cf = cf1.thenCompose(x ->
            CompletableFuture.supplyAsync(
                () -> x + "s"));
        String join = cf.join();
        log.debug(join);
    }

    @Test
    public void allOf() {
        log.debug("Starting");
        CompletableFuture<String> cf1 = new CompletableFuture();
        CompletableFuture<String> cf2 = new CompletableFuture();
        CompletableFuture<String> cf3 = new CompletableFuture();
        CompletableFuture<Void> allOf = CompletableFuture.allOf(cf1, cf2, cf3);
        allOf.thenRunAsync(() -> log.debug("thenRunAsync"));
        allOf.thenRun(() -> {
            try {
                log.debug("thenRun starting");
                Thread.sleep(1_000);
                log.debug("thenRun exiting");
            } catch (InterruptedException e) {
                log.debug("Interrupted", e);
            }
        });
        cf1.complete("cf1 is done");
        log.debug("cf1 is done!");
        cf2.complete("cf2 is done");
        log.debug("cf2 is done!");
        CompletableFuture.runAsync(() -> {
            cf3.complete("cf3 is done");
            log.debug("cf3 is done!");
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        log.debug("Joining");
        allOf.join();
        log.debug("Finishing");
    }

    @Test
    public void whenComplete() {
        log.debug("Starting");
        CompletableFuture<String> cf = CompletableFuture.completedFuture("Done");
// can add whenComplete in between completion stages
        cf.whenComplete((value, exception)->{
            if(exception != null) {
                log.debug("Exception:" + exception);
            }
            else log.debug("Value:" + value);
        })
    .thenAccept((x)-> log.debug(x));


        String join = cf.join();
        log.debug(join);
    }

    @Test
    public void handle() throws ExecutionException, InterruptedException {
        log.debug("Starting");
        String name = null;
        CompletableFuture<String> cf =  CompletableFuture.supplyAsync(() -> {
            if (name == null) {
                throw new RuntimeException("Problems!");
            }
            else {
                return "Hello," + name;
            }
        }).handle((value, t) -> value != null ? value : "Hello, You!");

        String join = cf.join();
        log.debug(join);
    }


    @Test
    public void parseCoordinatesAndCities() {
        // Summary - we are tasked with preparing incoming customer data for indexing in Elasticsearch
        // 1. First step is read the accounts.json file - readAccountsFile()
        // The main file accounts.json consists of:
        // Date, License plate #, geo coords of toll booth, pmnt currency
        // there are some Account fields that need to be enriched from the following:
        // 2. Next, read the Geo coords file us-zip-code-latitude-and-longitude.json
        // (Don't try to open that file in the IDE, it's very big). There is a small sample if you want to see that
        // - readGeoFile(), then grab the fields:
        // GeoDataset.fields.city, GeoDataset.fields.state, GeoDataset.fields.zip, and store in the corresponding
//        Account fields
        // 3. Currency rate read currency-conversions.json - readCurrencyFile(), then map usdcad, usdmxn, and usdusd to their exchange rate in Account instance
        // 4. License plates - licenses.json readLicenseFile(), then grab name and store in Account instance

        // after launching the read for each of these, combine them in sequence (but concurrently) with the account file

        // finally, when all the maps are done and applied to the accounts list, write the accounts file using writeAccountsFile(accountsList)

        // be sure to join at the end of the method, so that the test method does not exit before the process completes.

        CompletableFuture<List<Account>> accountCf = readAccountsFile();
        CompletableFuture<Map<GeoPoint, GeoDataset>> geoCF = readGeoFile();
        CompletableFuture<Map<String, Double>> currencyCF = readCurrencyFile();
        CompletableFuture<Map<String, LicensePlate>> licensesCF = readLicenseFile();

        combineGeoFile(accountCf, geoCF);
        combineCurrencyMap(accountCf, currencyCF);
        combineLicenseMap(accountCf, licensesCF);

        CompletableFuture.allOf(geoCF, accountCf, currencyCF, licensesCF)
            .thenRun(() -> {
                List<Account> accountsList = accountCf.getNow(null);
                writeAccountFile(accountsList);
            })
            .join();
    }

    private void combineCurrencyMap(CompletableFuture<List<Account>> accountCf, CompletableFuture<Map<String, Double>> currencyCF) {
        accountCf.thenCombineAsync(currencyCF, (accountList, currencyMap) -> {
            log.debug("Combining currency map");
            accountList.forEach(account -> {
                String currency = account.getCurrency();
                Double exchangeRate = currencyMap.get(currency);
                if (exchangeRate != null) {
                    account.setExchangeRate(exchangeRate);
                }
            });

            return accountList;
        });
    }

    private void combineGeoFile(CompletableFuture<List<Account>> accountCf, CompletableFuture<Map<GeoPoint, GeoDataset>> geoCF) {
        accountCf.thenCombineAsync(geoCF, (accountList, geoPointGeoDatasetMap) -> {
            log.debug("Combining geo file");
            accountList.forEach(account -> {
                GeoPoint geoPoint = account.getGeoPoint();
                GeoDataset geoDataset = geoPointGeoDatasetMap.get(geoPoint);
                if (geoDataset != null) {
                    String city = geoDataset.getFields().getCity();
                    String state = geoDataset.getFields().getState();
                    String zip = geoDataset.getFields().getZip();
                    account.setCity(city);
                    account.setState(state);
                    account.setZip(zip);
                } else {
                    log.debug("No geoDataSet:" + geoDataset);
                }
            });

            return accountList;
        });
    }

    private CompletableFuture<List<Account>> readAccountsFile() {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/accounts.json");
            Account[] accounts = readFile(file, Account[].class);
            return accounts;
        }).thenApply(accounts -> {
            log.debug("Mapping accounts");
            List<Account> list = Arrays.asList(accounts);
            return list;
        });
    }

    private CompletableFuture<Map<GeoPoint, GeoDataset>> readGeoFile() {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/us-zip-code-latitude-and-longitude.json");
            GeoDataset[] geoDatasets = readFile(file, GeoDataset[].class);
            return geoDatasets;
        }).thenApply(geoDatasets -> {
            log.debug("Mapping geo file");
            Map<GeoPoint, GeoDataset> geoPointGeoDatasetMap = convertDataSetToGeoCoordsMap(geoDatasets);
            return geoPointGeoDatasetMap;
        }).exceptionally(e -> {
            log.debug("Exception " + e);
            return new HashMap<>();
        });
    }

    private CompletableFuture<Map<String, Double>> readCurrencyFile() {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/currency-conversions.json");
            Currencies currencies = readFile(file, Currencies.class);
            return currencies;
        }).thenApply(currencies -> {
            log.debug("Mapping currency file");
            Map<String, Double> currencyMap = new HashMap<>();
            currencyMap.put("usdcad", currencies.getUsdcad());
            currencyMap.put("usdmxn", currencies.getUsdmxn());
            currencyMap.put("usdusd", currencies.getUsdusd());
            return currencyMap;
        });
    }

    private CompletableFuture<Map<String, LicensePlate>> readLicenseFile() {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/licenses.json");
            LicensePlate[] licenses = readFile(file, LicensePlate[].class);
            return licenses;
        }).thenApply(licenses -> {
            log.debug("Mapping license file");
            Map<String, LicensePlate> licensesMap = convertLicensesToLicenseMap(licenses);
            return licensesMap;
        }).exceptionally(ex -> new HashMap<>());
    }

    private void combineLicenseMap(CompletableFuture<List<Account>> accountCf, CompletableFuture<Map<String, LicensePlate>> licensesCF) {
        accountCf.thenCombineAsync(licensesCF, (accountList, licenseMap)->{
            log.debug("Combining license map");
            accountList.forEach(account -> {
                String licenseId = account.getLicense();
                LicensePlate license = licenseMap.get(licenseId);
                if (license != null) {
                    String name = license.getName();
                    account.setName(name);
                } else {
                    log.debug("No license id:" + licenseId);
                }
            });

            return accountList;
        });
    }

    /**
     * Reads the supplied Json file, and parses it to the supplied class. For an array, use Type[].class,
     * for example Account[].class
     *
     * @throws UncheckedIOException if IOException on read
     */
    private <T> T readFile(File jsonFile, Class<T> clazz) throws UncheckedIOException {
        try {
            log.debug("Reading file " + jsonFile);
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

    private Map<String, LicensePlate> convertLicensesToLicenseMap(LicensePlate[] licenses) {
        Map<String, LicensePlate> licenseMap = Arrays.stream(licenses).collect(Collectors.toMap(LicensePlate::getLicense, license -> license));
        return licenseMap;
    }

    private Map<GeoPoint, GeoDataset> convertDataSetToGeoCoordsMap(GeoDataset[] geoDatasets) {
        return Arrays.stream(geoDatasets).collect(Collectors.toMap(geoDataset ->
                new GeoPoint(geoDataset.getFields().getLatitude(), geoDataset.getFields().getLongitude())
            , geoDataset -> geoDataset, (key1, key2) -> key1));
    }
}
