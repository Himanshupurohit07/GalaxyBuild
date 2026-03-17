package com.galaxy.wc.migration.loader.util;

public class BomProcessingResult {

    public BomPartProcessingStatus status;
    public boolean bomCreated;
    public boolean specLinkFailed;
    public boolean bomAlreadyExists;


    public BomProcessingResult(BomPartProcessingStatus status, boolean bomCreated, boolean specLinkFailed,boolean bomAlreadyExists) {
        this.status = status;
        this.bomCreated = bomCreated;
        this.specLinkFailed = specLinkFailed;
        this.bomAlreadyExists = bomAlreadyExists;
    }
}
