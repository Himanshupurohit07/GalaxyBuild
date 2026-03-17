package com.sparc.wc.integration.lucky.domain;

public class LuckyCustomPluginException extends Exception {

    private static final long serialVersionUID = -1358543667977320452L;

    public LuckyCustomPluginException() {

    }

    public LuckyCustomPluginException(String message) {
        super(message);
    }

    public LuckyCustomPluginException(Throwable cause) {
        super(cause);
    }

    public LuckyCustomPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LuckyCustomPluginException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}