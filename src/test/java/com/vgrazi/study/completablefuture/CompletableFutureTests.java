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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CompletableFutureTests {
    private final Logger log = LoggerFactory.getLogger("");









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
}
