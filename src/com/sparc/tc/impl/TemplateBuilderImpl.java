package com.sparc.tc.impl;

import com.sparc.tc.abstractions.Interpreter;
import com.sparc.tc.abstractions.TemplateBuilder;
import com.sparc.tc.abstractions.ValueSupplier;
import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.InterpreterContext;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.domain.TemplateBuilderContext;
import com.sparc.tc.exceptions.TCExceptionRuntime;
import com.sparc.tc.util.WorkbookUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateBuilderImpl implements TemplateBuilder {

    private Interpreter interpreter;

    public TemplateBuilderImpl(final Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public XSSFWorkbook build(final XSSFWorkbook template, final List<Coordinate> coordinates, final ValueSupplier valueSupplier, final TemplateBuilderContext context) {

        if (template == null || valueSupplier == null) {
            return null;
        }
        if (coordinates == null || coordinates.isEmpty()) {
            return template;
        }
        final XSSFFormulaEvaluator formulaEvaluator = template.getCreationHelper().createFormulaEvaluator();
        enableLocking(template, coordinates);
        coordinates.stream()
                .filter(coordinate -> coordinate.getPlaceHolder().getProcess() != PlaceHolder.Process.u || coordinate.getPlaceHolder().isLocked())
                .forEach(coordinate -> {
                    copyData(template, coordinate, valueSupplier, context);
                });
        final int                numberOfSheets      = template.getNumberOfSheets();
        final List<Short>        sheetIndices        = WorkbookUtils.getSheetIndices(numberOfSheets);
        final InterpreterContext interpreterContext  = new InterpreterContext();
        final List<Coordinate>   erasableCoordinates = interpreter.interpret(template, sheetIndices, interpreterContext);
        context.setInterpreterContext(interpreterContext);
        erasePlaceholders(template, erasableCoordinates);
        try {
            formulaEvaluator.evaluateAll();
        } catch (Exception e) {
            context.addToCopyLog(new TCExceptionRuntime(e.getMessage(), TCExceptionRuntime.Type.ERROR));
        }
        return template;
    }

    private void enableLocking(final XSSFWorkbook template, final List<Coordinate> coordinates) {
        coordinates.stream().map(Coordinate::getSheet).collect(Collectors.toSet()).forEach(index -> {
            final XSSFSheet sheet = template.getSheetAt(index);
            if (sheet != null) {
                sheet.enableLocking();
                unlockDefaultLockedCells(sheet);
            }
        });
    }

    private void unlockDefaultLockedCells(final XSSFSheet sheet) {
        sheet.rowIterator()
                .forEachRemaining(row -> {
                    if (row != null) {
                        row.cellIterator()
                                .forEachRemaining(cell -> {
                                    if (cell != null && cell.getCellStyle() != null) {
                                        cell.getCellStyle().setLocked(false);
                                    }
                                });
                    }
                });
    }

    private XSSFCell createCellIfRequired(final XSSFRow row, final Coordinate coordinate, final int clone) {
        if (coordinate.getPlaceHolder().getDir() == PlaceHolder.Direction.column) {
            final XSSFCell cell = row.getCell(coordinate.getColumn());
            if (cell == null) {
                return row.createCell(coordinate.getColumn());
            }
            return cell;
        } else if (coordinate.getPlaceHolder().getDir() == PlaceHolder.Direction.row) {
            final XSSFCell cell = row.getCell(coordinate.getColumn() + clone);
            if (cell == null) {
                return row.createCell(coordinate.getColumn() + clone);
            }
            return cell;
        }
        return null;
    }

    private XSSFRow createRowIfRequired(final XSSFSheet sheet, final Coordinate coordinate, final int clone) {
        if (coordinate.getPlaceHolder().getDir() == PlaceHolder.Direction.row) {
            final XSSFRow row = sheet.getRow(coordinate.getRow());
            if (row == null) {
                return sheet.createRow(coordinate.getRow());
            }
            return row;
        } else if (coordinate.getPlaceHolder().getDir() == PlaceHolder.Direction.column) {
            final XSSFRow row = sheet.getRow(coordinate.getRow() + clone);
            if (row == null) {
                return sheet.createRow(coordinate.getRow() + clone);
            }
            return row;
        }
        return null;
    }

    private void copyData(final XSSFWorkbook workbook, final Coordinate coordinate, final ValueSupplier valueSupplier, final TemplateBuilderContext context) {
        try {
            final AttributeValue attributeValue = getAttributeValue(valueSupplier, coordinate.getPlaceHolder());
            if (attributeValue == null) {
                WorkbookUtils.applyCellLock(workbook, WorkbookUtils.getCell(workbook, coordinate), coordinate.getPlaceHolder());
                if (coordinate.getPlaceHolder().directionEnabled()) {
                    applyLockingForNonVariableCells(0, coordinate, workbook);
                    hideReservedRows(0, coordinate, workbook);
                }
                return;
            }
            if (attributeValue.getData() instanceof List && coordinate.getPlaceHolder().directionEnabled()) {
                final List<Object> list = (List<Object>) attributeValue.getData();
                cloneCell(workbook, coordinate, list.size());
                setListData(workbook, attributeValue, coordinate);
                applyFormattingForLeftoverRows(list.size(), coordinate, attributeValue, workbook);
                hideReservedRows(list.size(), coordinate, workbook);
            } else {
                final XSSFCell cell = WorkbookUtils.getCell(workbook, coordinate);
                setCellData(workbook, cell, attributeValue.getData(), attributeValue.getType(), attributeValue.getParams(), coordinate);
            }
        } catch (TCExceptionRuntime e) {
            context.addToCopyLog(e);
        } catch (Exception e) {
            context.addToCopyLog(new TCExceptionRuntime(getErrorMessage(e, coordinate), TCExceptionRuntime.Type.ERROR));
        }
    }

    private AttributeValue getAttributeValue(final ValueSupplier valueSupplier, final PlaceHolder placeHolder) {
        if (!placeHolder.hasVariable() || placeHolder.getProcess() == PlaceHolder.Process.u) {
            return null;
        }
        return valueSupplier.getValue(placeHolder);
    }

    private void applyFormattingForLeftoverRows(final int actualSize, final Coordinate coordinate, final AttributeValue attributeValue, final XSSFWorkbook workbook) {
        if (!coordinate.getPlaceHolder().hasReservedRows()) {
            return;
        }
        final int currentRow   = coordinate.getRow();
        final int reservedRows = Integer.parseInt(coordinate.getPlaceHolder().getParams().getParam(PlaceHolder.RESERVED_ROWS));
        final int offset       = currentRow + actualSize;
        final int upperBound   = currentRow + reservedRows;
        for (int rowIterator = offset; rowIterator <= upperBound; rowIterator++) {
            final XSSFCell cell = WorkbookUtils.getCell(workbook, coordinate.getSheet(), rowIterator, coordinate.getColumn());
            if (cell != null) {
                WorkbookUtils.setCellFormat(workbook, cell, attributeValue.getType(), attributeValue.getParams());
            }
            if (cell != null && coordinate.getPlaceHolder().isLocked() && cell.getCellStyle() != null && !coordinate.getPlaceHolder().invertLockForLeftOverRows()) {
                cell.getCellStyle().setLocked(true);
            }
        }
    }

    private void hideReservedRows(final int actualSize, final Coordinate coordinate, XSSFWorkbook workbook) {
        if (!coordinate.getPlaceHolder().hideReservedRows()) {
            return;
        }
        final int           currentRow     = coordinate.getRow();
        final int           reservedRows   = Integer.parseInt(coordinate.getPlaceHolder().getParams().getParam(PlaceHolder.RESERVED_ROWS));
        final int           offset         = currentRow + actualSize;
        final int           upperBound     = currentRow + reservedRows;
        final XSSFCellStyle hiddenRowStyle = workbook.createCellStyle();
        hiddenRowStyle.setHidden(true);
        for (int rowIterator = offset; rowIterator <= upperBound; rowIterator++) {
            final XSSFRow row = WorkbookUtils.getRow(workbook, coordinate.getSheet(), rowIterator);
            if (row.getRowStyle() != null) {
                row.getRowStyle().setHidden(true);
                row.setZeroHeight(true);
            } else {
                row.setRowStyle(hiddenRowStyle);
                row.setZeroHeight(true);
            }
        }
    }

    private void applyLockingForNonVariableCells(final int actualSize, final Coordinate coordinate, XSSFWorkbook workbook) {
        final int currentRow   = coordinate.getRow();
        final int reservedRows = Integer.parseInt(coordinate.getPlaceHolder().getParams().getParam(PlaceHolder.RESERVED_ROWS));
        final int offset       = currentRow + actualSize;
        final int upperBound   = currentRow + reservedRows;
        for (int rowIterator = offset; rowIterator <= upperBound; rowIterator++) {
            final XSSFCell cell = WorkbookUtils.getCell(workbook, coordinate.getSheet(), rowIterator, coordinate.getColumn());
            WorkbookUtils.applyCellLock(workbook, cell, coordinate.getPlaceHolder());
        }
    }

    private void setListData(final XSSFWorkbook workbook, final AttributeValue attributeValue, final Coordinate coordinate) {
        final List<String> values = (List<String>) attributeValue.getData();
        for (int clone = 0; clone < values.size(); clone++) {
            final XSSFCell cell = WorkbookUtils.getCell(workbook, coordinate, clone);
            setCellData(workbook, cell, values.get(clone), attributeValue.getType(), attributeValue.getParams(), coordinate);
        }
        final XSSFCell eolCell = WorkbookUtils.getCell(workbook, coordinate, values.size());
        if (eolCell == null) {
            return;
        }
        eolCell.setCellType(CellType.BLANK);
        if (eolCell.getCellStyle() != null) {
            final CellStyle newStyle = workbook.createCellStyle();
            newStyle.cloneStyleFrom(eolCell.getCellStyle());
            newStyle.setLocked(false);
            eolCell.setCellStyle(newStyle);
        }
    }

    private void setCellData(final XSSFWorkbook workbook, final XSSFCell cell, final Object value, final AttributeValue.Type type, final Map<String, Object> params, final Coordinate coordinate) {
        if (cell == null) {
            return;
        }
        if (value != null) {
            WorkbookUtils.setCellValue(workbook, cell, value, type, params);
        }
        setLock(coordinate, cell);
    }

    private void setLock(final Coordinate coordinate, final XSSFCell cell) {
        if (cell == null) {
            return;
        }
        if (coordinate.getPlaceHolder().isLocked()) {
            cell.getCellStyle().setLocked(true);
        } else {
            cell.getCellStyle().setLocked(false);
        }
    }

    private void cloneCell(final XSSFWorkbook workbook, final Coordinate coordinate, final int clones) {
        if (clones < 1 && !coordinate.getPlaceHolder().hasReservedRows()) {
            return;
        }
        final int       cloneCount      = coordinate.getPlaceHolder().hasReservedRows() ? getReservedRows(coordinate) : clones;
        final XSSFCell  parentCell      = WorkbookUtils.getCell(workbook, coordinate);
        final CellStyle parentCellStyle = parentCell.getCellStyle();
        for (int clone = 0; clone <= cloneCount; clone++) {
            final XSSFSheet sheet = workbook.getSheetAt(coordinate.getSheet());
            final XSSFRow   row   = createRowIfRequired(sheet, coordinate, clone);
            if (row == null) {
                return;
            }
            row.setZeroHeight(false);
            if (row.getRowStyle() != null) {
                row.getRowStyle().setHidden(false);
            }
            final XSSFCell clonedCell = createCellIfRequired(row, coordinate, clone);
            if (clonedCell == null) {
                return;
            }
            if (parentCellStyle != null) {
                final CellStyle newStyle = workbook.createCellStyle();
                newStyle.cloneStyleFrom(parentCellStyle);
                clonedCell.setCellStyle(newStyle);
            }
            clonedCell.setCellStyle(parentCell.getCellStyle());
            clonedCell.setCellValue(parentCell.getStringCellValue());
            clonedCell.setCellType(parentCell.getCellType());
        }
    }

    private int getReservedRows(final Coordinate coordinate) {
        return Integer.parseInt(coordinate.getPlaceHolder().getParams().getParam(PlaceHolder.RESERVED_ROWS));
    }

    private void erasePlaceholders(final XSSFWorkbook workbook, final List<Coordinate> coordinates) {
        //TODO, add logic for eof list
        if (coordinates.isEmpty()) {
            return;
        }
        coordinates.forEach(coordinate -> {

            final XSSFCell cell = WorkbookUtils.getCell(workbook, coordinate);
            if (cell == null) {
                return;
            }
            if (coordinate.getPlaceHolder().isCommentInstruction()) {
                cell.removeCellComment();
                return;
            }
            if (coordinate.getPlaceHolder().directionEnabled()) {
                cell.setCellType(CellType.BLANK);
                return;
            }
            if (cell.getCellType() == CellType.NUMERIC) {
                cell.setCellValue(0);
            } else if (cell.getCellType() == CellType.STRING) {
                cell.setCellValue("");
            } else if (cell.getCellType() == CellType.BOOLEAN) {
                cell.setCellValue(false);
            }
        });
    }

    private String getErrorMessage(final Exception e, final Coordinate coordinate) {
        return e.getMessage() + ", caused at location - [ sheet:" + coordinate.getSheet() + ", row" + coordinate.getRow() + ", column:" + coordinate.getColumn() + ", var:" + coordinate.getPlaceHolder().getVar() + " ]";
    }

}
