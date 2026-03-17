package com.sparc.wc.exports.services;

import com.sparc.tc.domain.WorkbookFileSystem;
import com.sparc.tc.exceptions.TCException;

public interface SparcLineSheetExporter {

    WorkbookFileSystem export() throws TCException;

}
