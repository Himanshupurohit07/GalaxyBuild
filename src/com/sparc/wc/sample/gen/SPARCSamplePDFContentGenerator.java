package com.sparc.wc.sample.gen;

import com.lcs.wc.client.web.pdf.*;
import com.lowagie.text.*;

import wt.util.WTException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Chuck
 */
public abstract class SPARCSamplePDFContentGenerator implements PDFContentCollection {

    public SPARCSamplePDFContentGenerator() {
    }

    protected Collection pageTitles = new ArrayList();

    public abstract Collection getPDFContentCollection(Map params, Document document) throws WTException;

    public Collection getPageTitles() {
        return pageTitles;
    }

    public void init() throws WTException {
        try {
            pageTitles = new ArrayList();
        } catch (Exception e) {
            throw new WTException(e);
        }
    }

}