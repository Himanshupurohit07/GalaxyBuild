package com.sparc.tc.domain;

import com.sparc.tc.exceptions.TCExceptionRuntime;

import java.util.LinkedList;
import java.util.List;

public class InterpreterContext {
    private List<String> instructionErrors;
    private List<String> instructionWarnings;
    private transient short currentSheet;

    public void addToInstructionLog(final TCExceptionRuntime exceptionRuntime) {
        if (exceptionRuntime.getType() == TCExceptionRuntime.Type.ERROR) {
            if (instructionErrors == null) {
                instructionErrors = new LinkedList<>();
            }
            instructionErrors.add(exceptionRuntime.getMessage());
        }
        if (exceptionRuntime.getType() == TCExceptionRuntime.Type.WARNING) {
            if (instructionWarnings == null) {
                instructionWarnings = new LinkedList<>();
            }
            instructionWarnings.add(exceptionRuntime.getMessage());
        }
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public List<String> getInstructionErrors() {
        return this.instructionErrors;
    }

    @SuppressWarnings("all")
    public List<String> getInstructionWarnings() {
        return this.instructionWarnings;
    }

    @SuppressWarnings("all")
    public short getCurrentSheet() {
        return this.currentSheet;
    }

    @SuppressWarnings("all")
    public void setInstructionErrors(final List<String> instructionErrors) {
        this.instructionErrors = instructionErrors;
    }

    @SuppressWarnings("all")
    public void setInstructionWarnings(final List<String> instructionWarnings) {
        this.instructionWarnings = instructionWarnings;
    }

    @SuppressWarnings("all")
    public void setCurrentSheet(final short currentSheet) {
        this.currentSheet = currentSheet;
    }

    @SuppressWarnings("all")
    public InterpreterContext() {
    }
    //</editor-fold>
}
