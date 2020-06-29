package com.vgrazi.study.completablefuture;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
class CompletableFutureApplicationTests {

    Logger logger = LoggerFactory.getLogger(CompletableFutureApplicationTests.class);
    @Test
    public void validateEnvironment() {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> logger.debug("You're good to go!"));
        completableFuture.join();
    }
}
