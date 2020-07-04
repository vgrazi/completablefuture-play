package com.vgrazi.study.completablefuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vgrazi.study.completablefuture.license.License;
import com.vgrazi.study.completablefuture.parser.account.Account;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WorkSheet {
    private final Logger logger = LoggerFactory.getLogger("");

    @Test
    public void parseCoordinatesAndCities() {
        // Summary - we are tasked with preparing incoming customer data for indexing in Elasticsearch
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

        List<Account> accountsList = new ArrayList<>(); // target/classes/accounts.json
        Map<GeoPoint, GeoDataset> geoDatasetMap = new HashMap<>(); // target/classes/us-zip-code-latitude-and-longitude.json
        Map<String, Double> currencyMap = new HashMap<>(); // target/classes/currency-conversions.json
        Map<String, License> licenseMap = new HashMap<>(); // target/classes/licenses.json

        CompletableFuture<List<Account>> accountCf = CompletableFuture.supplyAsync(() -> {
            File file = new File("target/classes/accounts.json");
            Account[] accounts = readFile(file, Account[].class);
            return accounts;
        }).thenApply(accounts -> {
            List<Account> list = Arrays.asList(accounts);
            accountsList.addAll(list);
            return list;
        }).exceptionally(e -> accountsList);

        // remember to join on something, or the method will exit before the threads complete
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

    private Map<String, License> convertLicensesToLicenseMap(License[] licenses) {
        Map<String, License> licenseMap = Arrays.stream(licenses).collect(Collectors.toMap(License::getLicense, license -> license));
        return licenseMap;
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
