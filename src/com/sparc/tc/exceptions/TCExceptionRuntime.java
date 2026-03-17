package com.sparc.tc.exceptions;

public class TCExceptionRuntime extends RuntimeException {

    public enum Type {
        ERROR, WARNING;
    }

    private Type type;

    public TCExceptionRuntime(final String message, final Type type) {
        super(message);
        this.type = type;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Type getType() {
        return this.type;
    }
    //</editor-fold>
}
