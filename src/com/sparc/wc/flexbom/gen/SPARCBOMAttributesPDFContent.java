/*
 * BOMAttributesPDFContent.java
 *
 * Created on January 14, 2007, 4:04 PM
 */

package com.sparc.wc.flexbom.gen;

import com.lcs.wc.client.web.pdf.*;
import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.flexbom.*;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.util.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.util.*;
import wt.util.*;

import com.lcs.wc.flexbom.gen.*;
/**
 *
 * @author cbrown
 */
public class SPARCBOMAttributesPDFContent extends SPARCBOMPDFContentGenerator {

    // Collection pageTitles = new ArrayList();

    /** Creates a new instance of BOMAttributesPDFContent */
    public SPARCBOMAttributesPDFContent() {
    }

    /**
     * gets an Element for insertion into a PDF Document
     * 
     * @param params
     *            A Map of parameters to pass to the Object. This provides the means for the calling class to have some
     *            "fore" knowledge of what implementations are being used and pass appropriate parameters.
     * @param document
     *            The PDF Document which the content is going to be added to. The document is passed in order to provide
     *            additional information related to the Document itself incase it is not provided in the params
     * @throws WTException
     *             For any error
     * @return an Element for insertion into a Document
     */

    public Collection getPDFContentCollection(Map params, Document document) throws WTException {
        ArrayList content = new ArrayList();

        PDFFlexTypeGenerator pftg = new PDFFlexTypeGenerator();
        pftg.setScope(FlexBOMFlexTypeScopeDefinition.BOM_SCOPE);
        pftg.setDoc(document);
        content.add(pftg.generatePDFDetails((FlexTyped) params.get("FLEXTYPED")));

        this.pageTitles.add(LCSMessage.getLocalizedMessage(RB.FLEXBOM, "BOMFlexAttributes_PG_TLE"));

        return content;
    }
}
