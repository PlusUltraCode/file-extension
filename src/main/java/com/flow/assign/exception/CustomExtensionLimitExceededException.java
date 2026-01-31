package com.flow.assign.exception;

public class CustomExtensionLimitExceededException extends RuntimeException {

    public CustomExtensionLimitExceededException(String message) {
        super(message);
    }

    public static CustomExtensionLimitExceededException max(int max) {
        return new CustomExtensionLimitExceededException("Custom extensions limit exceeded (max=" + max + ")");
    }
}
