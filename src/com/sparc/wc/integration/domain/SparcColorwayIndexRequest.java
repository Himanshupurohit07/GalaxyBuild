package com.sparc.wc.integration.domain;

import javax.validation.constraints.NotNull;

public class SparcColorwayIndexRequest {
    private long from = -1;
    private long to = -1;
    @NotNull
    private SparcColorwayProcesses process;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public long getFrom() {
        return this.from;
    }

    @SuppressWarnings("all")
    public long getTo() {
        return this.to;
    }

    @SuppressWarnings("all")
    public SparcColorwayProcesses getProcess() {
        return this.process;
    }

    @SuppressWarnings("all")
    public void setFrom(final long from) {
        this.from = from;
    }

    @SuppressWarnings("all")
    public void setTo(final long to) {
        this.to = to;
    }

    @SuppressWarnings("all")
    public void setProcess(final SparcColorwayProcesses process) {
        this.process = process;
    }

    @SuppressWarnings("all")
    public SparcColorwayIndexRequest() {
    }
    //</editor-fold>
}
