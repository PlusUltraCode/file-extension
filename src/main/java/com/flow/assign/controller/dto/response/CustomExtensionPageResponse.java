package com.flow.assign.controller.dto.response;

import com.flow.assign.domain.CustomExtensionBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomExtensionPageResponse {

    private List<CustomExtensionResponse> items;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;

    public static CustomExtensionPageResponse from(Page<CustomExtensionBlock> result){
        return CustomExtensionPageResponse.builder()
                .items(result.getContent().stream().map(CustomExtensionResponse::from).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .build();
    }
}

