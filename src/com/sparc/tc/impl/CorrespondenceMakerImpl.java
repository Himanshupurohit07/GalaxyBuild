package com.sparc.tc.impl;

import com.sparc.tc.abstractions.CorrespondenceMaker;
import com.sparc.tc.abstractions.Interpreter;
import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.CorrespondenceData;
import com.sparc.tc.domain.CorrespondenceMakerContext;
import com.sparc.tc.domain.Page;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.domain.Table;
import com.sparc.tc.exceptions.TCExceptionRuntime;
import com.sparc.tc.util.WorkbookUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.LinkedList;
import java.util.List;

public class CorrespondenceMakerImpl implements CorrespondenceMaker {

    public static final String ALLOW_BLANKS = "allowBlanks";

    private Interpreter interpreter;

    public CorrespondenceMakerImpl(final Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public CorrespondenceData correspond(XSSFWorkbook template, XSSFWorkbook dataSheet, CorrespondenceMakerContext context) {

        if (template == null || dataSheet == null || context == null) {
            return null;
        }
        final List<Coordinate> coordinates = interpreter.interpret(template, context.getInterpreterContext());
        if (coordinates.isEmpty()) {
            return null;
        }
        final XSSFFormulaEvaluator formulaEvaluator = dataSheet.getCreationHelper().createFormulaEvaluator();
        formulaEvaluator.evaluateAll();
        final CorrespondenceData correspondenceData = new CorrespondenceData();
        coordinates.stream()
                .filter(coordinate -> coordinate.getPlaceHolder().getProcess() != PlaceHolder.Process.d)
                .forEach(coordinate -> {
                    addData(correspondenceData, coordinate, dataSheet, context);
                });
        return correspondenceData;
    }

    public CorrespondenceData correspond(XSSFWorkbook template, XSSFWorkbook dataSheet, CorrespondenceMakerContext context, Boolean isCheck) {

        if (template == null || dataSheet == null || context == null) {
            return null;
        }
        final List<Coordinate> coordinates = interpreter.interpret(template, context.getInterpreterContext());
        if (coordinates.isEmpty()) {
            return null;
        }
        final XSSFFormulaEvaluator formulaEvaluator = dataSheet.getCreationHelper().createFormulaEvaluator();
        formulaEvaluator.evaluateAll();
        final CorrespondenceData correspondenceData = new CorrespondenceData();
        coordinates.stream()
                .filter(coordinate -> coordinate.getPlaceHolder().getProcess() != null && coordinate.getPlaceHolder().getProcess() != PlaceHolder.Process.d)
                .forEach(coordinate -> {
                    addData(correspondenceData, coordinate, dataSheet, context, formulaEvaluator);
                });
        return correspondenceData;
    }

    private void addData(final CorrespondenceData correspondenceData, final Coordinate coordinate, final XSSFWorkbook dataSheet, final CorrespondenceMakerContext context, XSSFFormulaEvaluator formulaEvaluator) {
        if (coordinate.getPlaceHolder().directionEnabled() && coordinate.getPlaceHolder().isGrouped()) {
            addGroupedData(correspondenceData, coordinate, dataSheet, context, formulaEvaluator);
        } else if (coordinate.getPlaceHolder().directionEnabled()) {
            addListData(correspondenceData, coordinate, dataSheet, context);
        } else {
            addSingleData(correspondenceData, coordinate, dataSheet, context);
        }
    }

    private void addData(final CorrespondenceData correspondenceData, final Coordinate coordinate, final XSSFWorkbook dataSheet,
                         final CorrespondenceMakerContext context) {
        if (coordinate.getPlaceHolder().directionEnabled() && coordinate.getPlaceHolder().isGrouped()) {
            addGroupedData(correspondenceData, coordinate, dataSheet, context);
        } else if (coordinate.getPlaceHolder().directionEnabled()) {
            addListData(correspondenceData, coordinate, dataSheet, context);
        } else {
            addSingleData(correspondenceData, coordinate, dataSheet, context);
        }
    }

    private void addGroupedData(final CorrespondenceData correspondenceData, final Coordinate coordinate,
                                final XSSFWorkbook dataSheet, final CorrespondenceMakerContext context,
                                final XSSFFormulaEvaluator formulaEvaluator) {
        int clone = 0;
        do {
            try {
                final XSSFCell   cell             = WorkbookUtils.getCell(dataSheet, coordinate, clone);
                final Coordinate clonedCoordinate = WorkbookUtils.cloneCoordinates(coordinate, clone);
                if (!hasMoreElements(coordinate, clone) && (cell == null || cell.getCellType() == CellType.BLANK)) {
                    break;
                }
                final String value = cell == null ? null : cell.toString();
                if (value == null || value.isEmpty()) {
                    if (isRequiredField(coordinate)) {
                        addRequiredFieldMissing(clonedCoordinate, context);
                    }
                }
                if (cell != null && CellType.FORMULA == cell.getCellType()) {
                    final XSSFCell formulaCell     = formulaEvaluator.evaluateInCell(cell);
                    final String   calculatedValue = formulaCell.toString();
                    correspondenceData.getGroupedPage().addElement(Table.Record.Element.builder().data(calculatedValue).coordinate(clonedCoordinate).build(), context);
                } else {
                    correspondenceData.getGroupedPage().addElement(Table.Record.Element.builder().data(value).coordinate(clonedCoordinate).build(), context);
                }
            } catch (Exception e) {
                context.addToCorrespondenceLog(new TCExceptionRuntime("Encountered an error while reading value of field:" + coordinate.getPlaceHolder().getVar() + ", error:" + e.getMessage(), TCExceptionRuntime.Type.WARNING));
            }
            clone++;
        } while (true);
    }

    private void addGroupedData(final CorrespondenceData correspondenceData, final Coordinate coordinate,
                                final XSSFWorkbook dataSheet, final CorrespondenceMakerContext context) {
        int clone = 0;
        do {
            try {
                final XSSFCell   cell             = WorkbookUtils.getCell(dataSheet, coordinate, clone);
                final Coordinate clonedCoordinate = WorkbookUtils.cloneCoordinates(coordinate, clone);
                if (!hasMoreElements(coordinate, clone) && (cell == null || cell.getCellType() == CellType.BLANK)) {
                    break;
                }
                final String value = cell == null ? null : cell.toString();
                if (value == null || value.isEmpty()) {
                    if (isRequiredField(coordinate)) {
                        addRequiredFieldMissing(clonedCoordinate, context);
                    }
                }

                correspondenceData.getGroupedPage().addElement(Table.Record.Element.builder().data(value).coordinate(clonedCoordinate).build(), context);
            } catch (Exception e) {
                context.addToCorrespondenceLog(new TCExceptionRuntime("Encountered an error while reading value of field:" + coordinate.getPlaceHolder().getVar() + ", error:" + e.getMessage(), TCExceptionRuntime.Type.WARNING));
            }
            clone++;
        } while (true);
    }

    private void addListData(final CorrespondenceData correspondenceData, final Coordinate coordinate, final XSSFWorkbook dataSheet, final CorrespondenceMakerContext context) {

        final List<String> values = new LinkedList<>();
        int                clone  = 0;
        do {
            try {
                final XSSFCell cell = WorkbookUtils.getCell(dataSheet, coordinate, clone);
                if (!hasMoreElements(coordinate, clone) && (cell == null || cell.getCellType() == CellType.BLANK)) {
                    break;
                }
                final String value = cell == null ? null : cell.toString();
                values.add(value);
            } catch (Exception e) {
                context.addToCorrespondenceLog(new TCExceptionRuntime("Encountered an error while reading value of field:" + coordinate.getPlaceHolder().getVar() + ", error:" + e.getMessage(), TCExceptionRuntime.Type.WARNING));
            }
            clone++;
        } while (true);
        if (isRequiredField(coordinate)) {
            if (values.isEmpty() || values.stream().allMatch(value -> value.isEmpty())) {
                addRequiredFieldMissing(coordinate, context);
            }
        }
        correspondenceData.getPage().addVariable(Page.ListElement.builder().data(values).coordinate(coordinate).build(), context);
    }

    private boolean hasMoreElements(final Coordinate coordinate, final int clone) {
        if (!coordinate.getPlaceHolder().hasReservedRows()) {
            return false;
        }
        final int reservedRows = Integer.parseInt(coordinate.getPlaceHolder().getParams().getParam(PlaceHolder.RESERVED_ROWS));
        return clone < reservedRows;
    }

    private void addSingleData(final CorrespondenceData correspondenceData, final Coordinate coordinate, final XSSFWorkbook dataSheet, final CorrespondenceMakerContext context) {
        final XSSFCell cell = WorkbookUtils.getCell(dataSheet, coordinate);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            if (isRequiredField(coordinate)) {
                addRequiredFieldMissing(coordinate, context);
            }
        }
        try {
            final String value = WorkbookUtils.getCellValue(cell);
            if (value == null || value.isEmpty()) {
                if (isRequiredField(coordinate)) {
                    addRequiredFieldMissing(coordinate, context);
                }
            }
            correspondenceData.getPage().addVariable(Page.Element.builder().data(value).coordinate(coordinate).build(), context);
        } catch (Exception e) {
            context.addToCorrespondenceLog(new TCExceptionRuntime("Encountered an error while reading value of field:" + coordinate.getPlaceHolder().getVar() + ", error:" + e.getMessage(), isRequiredField(coordinate) ? TCExceptionRuntime.Type.ERROR : TCExceptionRuntime.Type.WARNING));
        }
    }

    private void addRequiredFieldMissing(final Coordinate coordinate, final CorrespondenceMakerContext context) {
        context.addToCorrespondenceLog(new TCExceptionRuntime("The field:" + coordinate.getPlaceHolder().getVar() + " is marked as non blank, but found no data, coordinates:" + coordinate.toString(), TCExceptionRuntime.Type.ERROR));
    }

    private boolean isRequiredField(final Coordinate coordinate) {
        if (!coordinate.getPlaceHolder().hasParams()) {
            return false;
        }
        final PlaceHolder.Params params = coordinate.getPlaceHolder().getParams();
        final String             param  = params.getParam(ALLOW_BLANKS);
        return param != null && param.equalsIgnoreCase("true");
    }

}
