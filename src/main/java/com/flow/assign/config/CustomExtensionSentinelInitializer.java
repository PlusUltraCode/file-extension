package com.flow.assign.config;

import com.flow.assign.domain.CustomExtensionBlock;
import com.flow.assign.repository.CustomExtensionBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomExtensionSentinelInitializer implements ApplicationRunner {

    private static final String CUSTOM_LOCK_EXTENSION = "__lock__";

    private final CustomExtensionBlockRepository customExtensionBlockRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        customExtensionBlockRepository.findByExtension(CUSTOM_LOCK_EXTENSION)
                .orElseGet(() -> customExtensionBlockRepository.save(CustomExtensionBlock.of(CUSTOM_LOCK_EXTENSION, LocalDateTime.now())));
    }
}
