package com.sparc.wc.integration.exceptions;

public class SparcEntityNotFoundException extends SparcGenericException {

    public SparcEntityNotFoundException(final String message) {
        super(404, message);
    }

    public SparcEntityNotFoundException(final Throwable throwable) {
        super(404, throwable.getMessage());
    }

}
