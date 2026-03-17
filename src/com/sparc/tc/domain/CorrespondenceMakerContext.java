package com.sparc.tc.domain;

import com.sparc.tc.domain.InterpreterContext;
import com.sparc.tc.exceptions.TCExceptionRuntime;

import java.util.LinkedList;
import java.util.List;

public class CorrespondenceMakerContext {
    private List<String> correspondenceErrors;
    private           List<String>       correspondenceWarnings;
    private           InterpreterContext interpreterContext = new InterpreterContext();
    private transient short              currentSheet;

    public void addToCorrespondenceLog(final TCExceptionRuntime exceptionRuntime) {
        if (exceptionRuntime.getType() == TCExceptionRuntime.Type.ERROR) {
            if (correspondenceErrors == null) {
                correspondenceErrors = new LinkedList<>();
            }
            correspondenceErrors.add(exceptionRuntime.getMessage());
        }
        if (exceptionRuntime.getType() == TCExceptionRuntime.Type.WARNING) {
            if (correspondenceWarnings == null) {
                correspondenceWarnings = new LinkedList<>();
            }
            correspondenceWarnings.add(exceptionRuntime.getMessage());
        }
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public List<String> getCorrespondenceErrors() {
        return this.correspondenceErrors;
    }

    @SuppressWarnings("all")
    public List<String> getCorrespondenceWarnings() {
        return this.correspondenceWarnings;
    }

    @SuppressWarnings("all")
    public InterpreterContext getInterpreterContext() {
        return this.interpreterContext;
    }

    @SuppressWarnings("all")
    public short getCurrentSheet() {
        return this.currentSheet;
    }

    @SuppressWarnings("all")
    public void setCorrespondenceErrors(final List<String> correspondenceErrors) {
        this.correspondenceErrors = correspondenceErrors;
    }

    @SuppressWarnings("all")
    public void setCorrespondenceWarnings(final List<String> correspondenceWarnings) {
        this.correspondenceWarnings = correspondenceWarnings;
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
    public CorrespondenceMakerContext() {
    }
    //</editor-fold>
}
