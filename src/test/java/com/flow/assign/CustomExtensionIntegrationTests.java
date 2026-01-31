package com.flow.assign;

import com.flow.assign.domain.CustomExtensionBlock;
import com.flow.assign.repository.CustomExtensionBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomExtensionIntegrationTests {

    private static final String LOCK = "__lock__";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CustomExtensionBlockRepository customExtensionBlockRepository;

    @BeforeEach
    void setUp() {
        customExtensionBlockRepository.deleteAll();
        customExtensionBlockRepository.save(CustomExtensionBlock.of(LOCK, LocalDateTime.now()));
    }

    @Test
    void 커스텀_확장자_200개_초과시_409_응답() throws Exception {
        for (int i = 0; i < 200; i++) {
            mockMvc.perform(post("/api/extension/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CustomCreate("ext" + i))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/extension/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomCreate("ext_over"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("커스텀 확장자는 최대 200개까지 등록할 수 있습니다"));
    }

    @Test
    void 커스텀_페이지_API는_10개씩_조회된다() throws Exception {
        for (int i = 0; i < 25; i++) {
            mockMvc.perform(post("/api/extension/custom")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CustomCreate("ext" + i))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/extension/custom/page")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.items.length()").value(10));
    }

    private record CustomCreate(String extension) {
    }
}
