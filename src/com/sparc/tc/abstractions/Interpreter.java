package com.sparc.tc.abstractions;

import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.InterpreterContext;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public interface Interpreter {

    List<Coordinate> interpret(final XSSFWorkbook workbook, final List<Short> sheetIndices, final InterpreterContext interpreterContext);

    List<Coordinate> interpret(final XSSFWorkbook workbook, final InterpreterContext interpreterContext);

}
