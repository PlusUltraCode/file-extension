package com.flow.assign.service;

import com.flow.assign.domain.CustomExtensionBlock;
import com.flow.assign.exception.CustomExtensionLimitExceededException;
import com.flow.assign.repository.CustomExtensionBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExtensionServiceConcurrencyTest {

    private static final String LOCK_EXTENSION = "__lock__";
    private static final int MAX = 200;

    @Autowired
    ExtensionService extensionService;

    @Autowired
    CustomExtensionBlockRepository customExtensionBlockRepository;

    @BeforeEach
    void setUp() {
        // Keep the lock row, clear user rows.
        List<CustomExtensionBlock> rows = customExtensionBlockRepository.findAllByExtensionNot(LOCK_EXTENSION);
        customExtensionBlockRepository.deleteAll(rows);

        customExtensionBlockRepository.findByExtension(LOCK_EXTENSION)
                .orElseGet(() -> customExtensionBlockRepository.save(CustomExtensionBlock.of(LOCK_EXTENSION, LocalDateTime.now())));
    }

    @Test
    void concurrentCreate_doesNotExceedMax200() throws Exception {
        int attempts = 250;

        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(attempts);

        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            final String ext = String.format("ext%03d", i);
            tasks.add(() -> {
                try {
                    start.await(10, TimeUnit.SECONDS);
                    extensionService.createCustomExtension(ext);
                    return true;
                } catch (CustomExtensionLimitExceededException e) {
                    return false;
                } finally {
                    done.countDown();
                }
            });
        }

        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(pool.submit(task));
        }

        start.countDown();

        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();

        int success = 0;
        int failedLimit = 0;
        for (Future<Boolean> f : futures) {
            try {
                boolean ok = f.get(20, TimeUnit.SECONDS);
                if (ok) {
                    success++;
                } else {
                    failedLimit++;
                }
            } catch (ExecutionException e) {
                throw e;
            }
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        long count = customExtensionBlockRepository.countByExtensionNot(LOCK_EXTENSION);

        assertThat(success).isEqualTo(MAX);
        assertThat(failedLimit).isEqualTo(attempts - MAX);
        assertThat(count).isEqualTo(MAX);
    }

    @Test
    void concurrentCreate_at199_allowsOnlyOneMore() throws Exception {
        // Pre-fill 199
        for (int i = 0; i < 199; i++) {
            extensionService.createCustomExtension(String.format("pre%03d", i));
        }
        assertThat(customExtensionBlockRepository.countByExtensionNot(LOCK_EXTENSION)).isEqualTo(199);

        int attempts = 10;
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(attempts);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            final String ext = String.format("race%03d", i);
            futures.add(pool.submit(() -> {
                try {
                    start.await(10, TimeUnit.SECONDS);
                    extensionService.createCustomExtension(ext);
                    return true;
                } catch (CustomExtensionLimitExceededException e) {
                    return false;
                } finally {
                    done.countDown();
                }
            }));
        }

        start.countDown();

        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();

        int success = 0;
        int failedLimit = 0;
        for (Future<Boolean> f : futures) {
            boolean ok = f.get(20, TimeUnit.SECONDS);
            if (ok) {
                success++;
            } else {
                failedLimit++;
            }
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        long count = customExtensionBlockRepository.countByExtensionNot(LOCK_EXTENSION);

        assertThat(success).isEqualTo(1);
        assertThat(failedLimit).isEqualTo(attempts - 1);
        assertThat(count).isEqualTo(MAX);
    }
}
