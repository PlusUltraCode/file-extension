package com.flow.assign.controller.dto.response;

import com.flow.assign.domain.CustomExtensionBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomExtensionResponse {

    private Long id;
    private String extension;
    private LocalDateTime createdAt;

    public static CustomExtensionResponse from(CustomExtensionBlock block) {
        return CustomExtensionResponse.builder()
                .id(block.getId())
                .extension(block.getExtension())
                .createdAt(block.getCreatedAt())
                .build();
    }
}
