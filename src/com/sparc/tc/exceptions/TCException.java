package com.sparc.tc.exceptions;

public class TCException extends Exception {

    public TCException(final String message) {
        super(message);
    }

    public TCException(final Throwable throwable) {
        super(throwable);
    }

}
