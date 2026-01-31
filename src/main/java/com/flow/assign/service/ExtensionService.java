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

    private static final String CUSTOM_LOCK_EXTENSION = "__lock__";

    private final FixedExtensionPolicyRepository fixedExtensionPolicyRepository;

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
