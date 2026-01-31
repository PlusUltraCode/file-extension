package com.flow.assign.service;

import com.flow.assign.repository.ExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtensionService {

    private ExtensionRepository extensionRepository;
}
