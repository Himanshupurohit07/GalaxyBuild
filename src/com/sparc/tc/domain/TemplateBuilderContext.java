package com.sparc.tc.domain;

import com.sparc.tc.domain.InterpreterContext;
import com.sparc.tc.exceptions.TCExceptionRuntime;

import java.util.LinkedList;
import java.util.List;

public class TemplateBuilderContext {
    private List<String> copyErrors;
    private           List<String>                           copyWarnings;
    private           com.sparc.tc.domain.InterpreterContext interpreterContext;
    private transient short                                  currentSheet;

    public void addToCopyLog(final TCExceptionRuntime exceptionRuntime) {
        if (exceptionRuntime.getType() == TCExceptionRuntime.Type.ERROR) {
            if (copyErrors == null) {
                copyErrors = new LinkedList<>();
            }
            copyErrors.add(exceptionRuntime.getMessage());
        }
        if (exceptionRuntime.getType() == TCExceptionRuntime.Type.WARNING) {
            if (copyWarnings == null) {
                copyWarnings = new LinkedList<>();
            }
            copyWarnings.add(exceptionRuntime.getMessage());
        }
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public List<String> getCopyErrors() {
        return this.copyErrors;
    }

    @SuppressWarnings("all")
    public List<String> getCopyWarnings() {
        return this.copyWarnings;
    }

    @SuppressWarnings("all")
    public com.sparc.tc.domain.InterpreterContext getInterpreterContext() {
        return this.interpreterContext;
    }

    @SuppressWarnings("all")
    public short getCurrentSheet() {
        return this.currentSheet;
    }

    @SuppressWarnings("all")
    public void setCopyErrors(final List<String> copyErrors) {
        this.copyErrors = copyErrors;
    }

    @SuppressWarnings("all")
    public void setCopyWarnings(final List<String> copyWarnings) {
        this.copyWarnings = copyWarnings;
    }

    @SuppressWarnings("all")
    public void setInterpreterContext(final InterpreterContext interpreterContext) {
        this.interpreterContext = interpreterContext;
    }

    @SuppressWarnings("all")
    public void setCurrentSheet(final short currentSheet) {
        this.currentSheet = currentSheet;
    }

    @SuppressWarnings("all")
    public TemplateBuilderContext() {
    }
    //</editor-fold>
}
