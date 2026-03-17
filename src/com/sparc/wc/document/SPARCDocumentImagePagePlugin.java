package com.sparc.wc.document;

import com.lcs.wc.document.LCSDocument;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;
import wt.util.WTException;
/**
 * 
 * @author vyerro25
 *
 */
public class SPARCDocumentImagePagePlugin {
private static String DOC_TYPE=LCSProperties.get("com.sparc.wc.document.SPARCDocumentImagePagePlugin.doc.type.att.key","scDocumentType");
private static String DOC_PAGE_TYPE=LCSProperties.get("com.sparc.wc.document.SPARCDocumentImagePagePlugin.doc.pageType.att.key","pageType");
/**
 * setPageType().
 * @param obj
 * @throws WTException 
 */
	public static void setPageType(WTObject obj) throws WTException {
		LCSDocument doc=null;
		
		if(obj instanceof LCSDocument) {
			
			doc=(LCSDocument)obj;
			String docType=(String)doc.getValue(DOC_TYPE);
			if(FormatHelper.hasContent(docType)) {
				
				doc.setValue(DOC_PAGE_TYPE, docType);
				
			}
			
		}
		
		
		
	}
}
