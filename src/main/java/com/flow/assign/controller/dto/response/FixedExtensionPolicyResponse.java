package com.flow.assign.controller.dto.response;

import com.flow.assign.domain.FixedExtensionPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FixedExtensionPolicyResponse {

    private String extension;
    private boolean blocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FixedExtensionPolicyResponse from(FixedExtensionPolicy policy) {
        return FixedExtensionPolicyResponse.builder()
                .extension(policy.getExtension())
                .blocked(policy.isBlocked())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
