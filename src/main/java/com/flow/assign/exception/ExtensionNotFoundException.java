package com.flow.assign.exception;

public class ExtensionNotFoundException extends RuntimeException {

    public ExtensionNotFoundException(String message) {
        super(message);
    }

    public static ExtensionNotFoundException fixed(String extension) {
        return new ExtensionNotFoundException("Fixed extension not found: " + extension);
    }

    public static ExtensionNotFoundException custom(String extension) {
        return new ExtensionNotFoundException("Custom extension not found: " + extension);
    }
}
