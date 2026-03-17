package com.sparc.tc.abstractions;

import com.sparc.tc.domain.CorrespondenceData;
import com.sparc.tc.domain.CorrespondenceMakerContext;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface CorrespondenceMaker {

    CorrespondenceData correspond(XSSFWorkbook template, XSSFWorkbook dataSheet, CorrespondenceMakerContext context);
	
	 CorrespondenceData correspond(XSSFWorkbook template, XSSFWorkbook dataSheet, CorrespondenceMakerContext context,Boolean isCheck);

}
