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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CodingProject {
    private final Logger log = LoggerFactory.getLogger("");

    @Test
    public void parseCoordinatesAndCities() {
        log.debug("Starting");
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

        // all of the steps above have corresponding calls below

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
