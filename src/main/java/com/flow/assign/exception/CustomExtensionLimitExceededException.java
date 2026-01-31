package com.flow.assign.exception;

public class CustomExtensionLimitExceededException extends RuntimeException {

    public CustomExtensionLimitExceededException(String message) {
        super(message);
    }

    public static CustomExtensionLimitExceededException max(int max) {
        return new CustomExtensionLimitExceededException("커스텀 확장자는 최대 " + max + "개까지 등록할 수 있습니다");
    }
}
