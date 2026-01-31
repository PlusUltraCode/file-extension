package com.flow.assign.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FixedExtensionResponse {
    private String extension;
    private String message;


    public static FixedExtensionResponse postOf(String extension){
        return FixedExtensionResponse.builder()
                .extension(extension)
                .message("정상적으로 생성 되었습니다.")
                .build();
    }

}
