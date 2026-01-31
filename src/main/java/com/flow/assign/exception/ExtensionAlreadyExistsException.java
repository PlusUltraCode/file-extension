package com.flow.assign.exception;

public class ExtensionAlreadyExistsException extends RuntimeException {

    public ExtensionAlreadyExistsException(String message) {
        super(message);
    }

    public static ExtensionAlreadyExistsException fixed(String extension) {
        return new ExtensionAlreadyExistsException("Fixed extension already exists: " + extension);
    }

    public static ExtensionAlreadyExistsException custom(String extension) {
        return new ExtensionAlreadyExistsException("Custom extension already exists: " + extension);
    }
}
