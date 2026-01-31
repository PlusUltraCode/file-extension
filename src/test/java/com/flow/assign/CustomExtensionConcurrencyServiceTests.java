package com.flow.assign;

import com.flow.assign.domain.CustomExtensionBlock;
import com.flow.assign.exception.CustomExtensionLimitExceededException;
import com.flow.assign.repository.CustomExtensionBlockRepository;
import com.flow.assign.service.ExtensionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomExtensionConcurrencyServiceTests {

    private static final String LOCK = "__lock__";

    @Autowired
    ExtensionService extensionService;

    @Autowired
    CustomExtensionBlockRepository customExtensionBlockRepository;

    @BeforeEach
    void setUp() {
        customExtensionBlockRepository.deleteAll();
        customExtensionBlockRepository.save(CustomExtensionBlock.of(LOCK, LocalDateTime.now()));
    }

    @Test
    void 동시에_많이_요청해도_커스텀_확장자는_최대_200개까지만_등록된다() throws Exception {
        int totalRequests = 250;
        int threads = 32;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(totalRequests);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger limit = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();

        List<Throwable> others = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            final String ext = String.format("ext%03d", i);
            pool.submit(() -> {
                try {
                    start.await();
                    extensionService.createCustomExtension(ext);
                    success.incrementAndGet();
                } catch (CustomExtensionLimitExceededException e) {
                    limit.incrementAndGet();
                } catch (Throwable t) {
                    other.incrementAndGet();
                    synchronized (others) {
                        others.add(t);
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();

        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(finished).isTrue();

        long persisted = customExtensionBlockRepository.countByExtensionNot(LOCK);

        assertThat(persisted).isEqualTo(200);
        assertThat(success.get()).isEqualTo(200);
        assertThat(limit.get()).isEqualTo(50);
        assertThat(other.get()).isZero();
        assertThat(others).isEmpty();
    }
}
