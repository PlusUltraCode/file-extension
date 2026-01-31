package com.flow.assign.exception;

public class ExtensionAlreadyExistsException extends RuntimeException {

    public ExtensionAlreadyExistsException(String message) {
        super(message);
    }

    public static ExtensionAlreadyExistsException fixed(String extension) {
        return new ExtensionAlreadyExistsException("이미 등록된 고정 확장자입니다: " + extension);
    }

    public static ExtensionAlreadyExistsException custom(String extension) {
        return new ExtensionAlreadyExistsException("이미 등록된 커스텀 확장자입니다: " + extension);
    }
}
