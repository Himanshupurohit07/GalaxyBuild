package com.sparc.wc.integration.exceptions;

import com.lcs.wc.util.LCSAccessException;

public class SparcGenericException extends Exception {
    private int    code = 500;
    private String message;

    public SparcGenericException(final String message) {
        super(message);
    }

    public SparcGenericException(final String error, final int statusCode) {
        super(error);
        code = statusCode;
        message = error;
    }

    public SparcGenericException(final Throwable throwable) {
        super(throwable);
        if (throwable instanceof LCSAccessException) {
            this.code = 403;
        } else if (throwable instanceof SparcEntityNotFoundException) {
            this.code = 404;
        }
        this.message = throwable.getMessage();
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public int getCode() {
        return this.code;
    }

    @SuppressWarnings("all")
    public String getMessage() {
        return this.message;
    }

    @SuppressWarnings("all")
    public void setCode(final int code) {
        this.code = code;
    }

    @SuppressWarnings("all")
    public void setMessage(final String message) {
        this.message = message;
    }

    @SuppressWarnings("all")
    public SparcGenericException() {
    }

    @SuppressWarnings("all")
    public SparcGenericException(final int code, final String message) {
        this.code = code;
        this.message = message;
    }
    //</editor-fold>
}
