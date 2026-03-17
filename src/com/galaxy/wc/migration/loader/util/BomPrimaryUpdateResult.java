package com.galaxy.wc.migration.loader.util;

public class BomPrimaryUpdateResult {
    private BomPartProcessingStatus status;
    private boolean bomUpdated;


    public BomPrimaryUpdateResult(BomPartProcessingStatus status, boolean bomUpdated) {
        this.status = status;
        this.bomUpdated = bomUpdated;
    }


    public BomPartProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(BomPartProcessingStatus status) {
        this.status = status;
    }

    public boolean isBomUpdated() {
        return bomUpdated;
    }

    public void setBomUpdated(boolean bomUpdated) {
        this.bomUpdated = bomUpdated;
    }
}