package com.flow.assign.controller;

import com.flow.assign.service.ExtensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/extension")
public class ExtensionController {

    private final ExtensionService extensionService;
}
