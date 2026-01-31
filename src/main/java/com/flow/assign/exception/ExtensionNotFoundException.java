package com.flow.assign.exception;

public class ExtensionNotFoundException extends RuntimeException {

    public ExtensionNotFoundException(String message) {
        super(message);
    }

    public static ExtensionNotFoundException fixed(String extension) {
        return new ExtensionNotFoundException("고정 확장자를 찾을 수 없습니다: " + extension);
    }

    public static ExtensionNotFoundException custom(String extension) {
        return new ExtensionNotFoundException("커스텀 확장자를 찾을 수 없습니다: " + extension);
    }
}
