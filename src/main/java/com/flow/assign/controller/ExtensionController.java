package com.flow.assign.controller;

import com.flow.assign.controller.dto.request.CustomCreateRequest;
import com.flow.assign.controller.dto.request.CustomUpdateRequest;
import com.flow.assign.controller.dto.request.FixedCreateRequest;
import com.flow.assign.controller.dto.request.FixedUpdateRequest;
import com.flow.assign.controller.dto.response.CustomExtensionResponse;
import com.flow.assign.controller.dto.response.CustomExtensionPageResponse;
import com.flow.assign.controller.dto.response.FixedExtensionPolicyResponse;
import com.flow.assign.controller.dto.response.FixedExtensionResponse;
import com.flow.assign.service.ExtensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/extension")
public class ExtensionController {

    private final ExtensionService extensionService;

    @PostMapping("/fixed")
    public ResponseEntity<FixedExtensionResponse> fixedExtensionCreate(
            @Valid @RequestBody FixedCreateRequest request
    ) {
        FixedExtensionResponse response = extensionService.createFixedExtension(request.getExtension());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/fixed/{extension}")
    public ResponseEntity<FixedExtensionPolicyResponse> fixedExtensionGet(@PathVariable String extension) {
        return ResponseEntity.ok(extensionService.getFixedExtension(extension));
    }

    @GetMapping("/fixed")
    public ResponseEntity<List<FixedExtensionPolicyResponse>> fixedExtensionList() {
        return ResponseEntity.ok(extensionService.listFixedExtensions());
    }

    @PutMapping("/fixed/{extension}")
    public ResponseEntity<FixedExtensionPolicyResponse> fixedExtensionUpdate(
            @PathVariable String extension,
            @Valid @RequestBody FixedUpdateRequest request
    ) {
        return ResponseEntity.ok(extensionService.updateFixedExtension(extension, request.getBlocked()));
    }

    @DeleteMapping("/fixed/{extension}")
    public ResponseEntity<Void> fixedExtensionDelete(@PathVariable String extension) {
        extensionService.deleteFixedExtension(extension);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/custom")
    public ResponseEntity<CustomExtensionResponse> customExtensionCreate(
            @Valid @RequestBody CustomCreateRequest request
    ) {
        CustomExtensionResponse response = extensionService.createCustomExtension(request.getExtension());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/custom/{extension}")
    public ResponseEntity<CustomExtensionResponse> customExtensionGet(@PathVariable String extension) {
        return ResponseEntity.ok(extensionService.getCustomExtension(extension));
    }

    @GetMapping("/custom")
    public ResponseEntity<List<CustomExtensionResponse>> customExtensionList() {
        return ResponseEntity.ok(extensionService.listCustomExtensions());
    }

    @GetMapping("/custom/page")
    public ResponseEntity<CustomExtensionPageResponse> customExtensionPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(extensionService.listCustomExtensionsPage(page, size));
    }

    @PutMapping("/custom/{extension}")
    public ResponseEntity<CustomExtensionResponse> customExtensionUpdate(
            @PathVariable String extension,
            @Valid @RequestBody CustomUpdateRequest request
    ) {
        return ResponseEntity.ok(extensionService.updateCustomExtension(extension, request.getNewExtension()));
    }

    @DeleteMapping("/custom/{extension}")
    public ResponseEntity<Void> customExtensionDelete(@PathVariable String extension) {
        extensionService.deleteCustomExtension(extension);
        return ResponseEntity.noContent().build();
    }

}
