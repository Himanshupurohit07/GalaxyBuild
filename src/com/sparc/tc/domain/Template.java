package com.sparc.tc.domain;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Template {
    private XSSFWorkbook workbook;

    public XSSFWorkbook getTemplate() {
        if (this.workbook == null) {
            return null;
        }
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            workbook.write(byteArrayOutputStream);
            return new XSSFWorkbook(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
        }
        //<editor-fold defaultstate="collapsed" desc="delombok">
        //ignore as writing to byte array stream
        //</editor-fold>
        return null;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Template(final XSSFWorkbook workbook) {
        this.workbook = workbook;
    }
    //</editor-fold>
}
