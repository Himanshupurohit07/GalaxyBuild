package com.sparc.tc.abstractions;

import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.TemplateBuilderContext;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public interface TemplateBuilder {

    XSSFWorkbook build(XSSFWorkbook template, List<Coordinate> coordinates, ValueSupplier valueSupplier, TemplateBuilderContext context);

}
