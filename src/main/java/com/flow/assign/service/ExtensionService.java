package com.flow.assign.service;

import com.flow.assign.controller.dto.response.FixedExtensionResponse;
import com.flow.assign.controller.dto.response.CustomExtensionResponse;
import com.flow.assign.controller.dto.response.FixedExtensionPolicyResponse;
import com.flow.assign.domain.CustomExtensionBlock;
import com.flow.assign.domain.FixedExtensionPolicy;
import com.flow.assign.exception.CustomExtensionLimitExceededException;
import com.flow.assign.exception.ExtensionAlreadyExistsException;
import com.flow.assign.exception.ExtensionNotFoundException;
import com.flow.assign.repository.CustomExtensionBlockRepository;
import com.flow.assign.repository.FixedExtensionPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExtensionService {

    private static final int CUSTOM_EXTENSION_MAX = 200;
    private static final String CUSTOM_LOCK_EXTENSION = "__lock__";

    private final FixedExtensionPolicyRepository fixedExtensionPolicyRepository;
    private final CustomExtensionBlockRepository customExtensionBlockRepository;

    @Transactional
    public FixedExtensionResponse createFixedExtension(String extension) {
        String normalized = normalizeExtension(extension);
        if (fixedExtensionPolicyRepository.existsById(normalized)) {
            throw ExtensionAlreadyExistsException.fixed(normalized);
        }
        FixedExtensionPolicy saved = fixedExtensionPolicyRepository.save(FixedExtensionPolicy.of(normalized, LocalDateTime.now()));
        return FixedExtensionResponse.postOf(saved.getExtension());
    }

    @Transactional(readOnly = true)
    public FixedExtensionPolicyResponse getFixedExtension(String extension) {
        String normalized = normalizeExtension(extension);
        FixedExtensionPolicy policy = fixedExtensionPolicyRepository.findById(normalized)
                .orElseThrow(() -> ExtensionNotFoundException.fixed(normalized));
        return FixedExtensionPolicyResponse.from(policy);
    }

    @Transactional(readOnly = true)
    public List<FixedExtensionPolicyResponse> listFixedExtensions() {
        return fixedExtensionPolicyRepository.findAll().stream()
                .map(FixedExtensionPolicyResponse::from)
                .toList();
    }

    @Transactional
    public FixedExtensionPolicyResponse updateFixedExtension(String extension, boolean blocked) {
        String normalized = normalizeExtension(extension);
        FixedExtensionPolicy policy = fixedExtensionPolicyRepository.findById(normalized)
                .orElseThrow(() -> ExtensionNotFoundException.fixed(normalized));
        policy.updateBlocked(blocked, LocalDateTime.now());
        return FixedExtensionPolicyResponse.from(policy);
    }

    @Transactional
    public void deleteFixedExtension(String extension) {
        String normalized = normalizeExtension(extension);
        if (!fixedExtensionPolicyRepository.existsById(normalized)) {
            throw ExtensionNotFoundException.fixed(normalized);
        }
        fixedExtensionPolicyRepository.deleteById(normalized);
    }

    @Transactional
    public CustomExtensionResponse createCustomExtension(String extension) {
        String normalized = normalizeExtension(extension);

        LocalDateTime now = LocalDateTime.now();
        lockCustomExtensionsTable(now);

        long currentCount = customExtensionBlockRepository.countByExtensionNot(CUSTOM_LOCK_EXTENSION);
        if (currentCount >= CUSTOM_EXTENSION_MAX) {
            throw CustomExtensionLimitExceededException.max(CUSTOM_EXTENSION_MAX);
        }

        if (customExtensionBlockRepository.existsByExtension(normalized)) {
            throw ExtensionAlreadyExistsException.custom(normalized);
        }

        CustomExtensionBlock saved = customExtensionBlockRepository.save(CustomExtensionBlock.of(normalized, now));
        return CustomExtensionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CustomExtensionResponse getCustomExtension(String extension) {
        String normalized = normalizeExtension(extension);
        CustomExtensionBlock block = customExtensionBlockRepository.findByExtension(normalized)
                .orElseThrow(() -> ExtensionNotFoundException.custom(normalized));
        return CustomExtensionResponse.from(block);
    }

    @Transactional(readOnly = true)
    public List<CustomExtensionResponse> listCustomExtensions() {
        return customExtensionBlockRepository.findAllByExtensionNot(CUSTOM_LOCK_EXTENSION).stream()
                .map(CustomExtensionResponse::from)
                .toList();
    }

    @Transactional
    public CustomExtensionResponse updateCustomExtension(String currentExtension, String newExtension) {
        String current = normalizeExtension(currentExtension);
        String next = normalizeExtension(newExtension);

        CustomExtensionBlock block = customExtensionBlockRepository.findByExtension(current)
                .orElseThrow(() -> ExtensionNotFoundException.custom(current));

        if (!current.equals(next) && customExtensionBlockRepository.existsByExtension(next)) {
            throw ExtensionAlreadyExistsException.custom(next);
        }

        block.changeExtension(next);
        return CustomExtensionResponse.from(block);
    }

    @Transactional
    public void deleteCustomExtension(String extension) {
        String normalized = normalizeExtension(extension);
        Optional<CustomExtensionBlock> existing = customExtensionBlockRepository.findByExtension(normalized);
        if (existing.isEmpty()) {
            throw ExtensionNotFoundException.custom(normalized);
        }
        customExtensionBlockRepository.delete(existing.get());
    }

    private void lockCustomExtensionsTable(LocalDateTime now) {
        try {
            customExtensionBlockRepository.findByExtensionForUpdate(CUSTOM_LOCK_EXTENSION)
                    .orElseGet(() -> {
                        try {
                            customExtensionBlockRepository.save(CustomExtensionBlock.of(CUSTOM_LOCK_EXTENSION, now));
                        } catch (DataIntegrityViolationException ignored) {

                        }
                        return customExtensionBlockRepository.findByExtensionForUpdate(CUSTOM_LOCK_EXTENSION)
                                .orElseThrow(() -> new IllegalStateException("Custom extension lock row missing"));
                    });
        } catch (DataIntegrityViolationException ignored) {

            customExtensionBlockRepository.findByExtensionForUpdate(CUSTOM_LOCK_EXTENSION)
                    .orElseThrow(() -> new IllegalStateException("Custom extension lock row missing"));
        }
    }

    private String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        String trimmed = extension.trim();
        if (trimmed.startsWith(".")) {
            trimmed = trimmed.substring(1);
        }

        String normalized = trimmed.toLowerCase();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("extension must not be blank");
        }
        if (CUSTOM_LOCK_EXTENSION.equals(normalized)) {
            throw new IllegalArgumentException("reserved extension");
        }
        return normalized;
    }


}
