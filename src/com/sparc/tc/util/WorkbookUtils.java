package com.sparc.tc.util;

import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.domain.Variable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class WorkbookUtils {

    private WorkbookUtils() {

    }

    public static XSSFCell getCell(final XSSFWorkbook workbook, final Coordinate coordinate) {
        final XSSFSheet sheet = workbook.getSheetAt(coordinate.getSheet());
        if (sheet == null) {
            return null;
        }
        if (coordinate.getPlaceHolder().isLocked()) {
            //TODO, add logic specific locking
        }
        final XSSFRow row = sheet.getRow(coordinate.getRow());
        if (row == null) {
            return null;
        }
        return row.getCell(coordinate.getColumn());
    }

    public static XSSFRow getRow(final XSSFWorkbook workbook, final short sheetIndex, final int rowIndex) {
        if (workbook == null || sheetIndex < 0 || rowIndex < 0) {
            return null;
        }
        final XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            return null;
        }
        final XSSFRow row = sheet.getRow(rowIndex);
        if (row != null) {
            return row;
        }
        return sheet.createRow(rowIndex);
    }

    public static XSSFCell getCell(final XSSFWorkbook workbook, final Coordinate coordinate, final int clone) {
        final XSSFSheet sheet = workbook.getSheetAt(coordinate.getSheet());
        if (sheet == null || clone < 0 || !coordinate.getPlaceHolder().directionEnabled()) {
            return null;
        }
        if (coordinate.getPlaceHolder().isLocked()) {
            //TODO, add logic specific locking
        }
        final int     rowIndex = (coordinate.getPlaceHolder().getDir() != PlaceHolder.Direction.column) ? coordinate.getRow() : coordinate.getRow() + clone;
        final XSSFRow row      = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        if (row.getRowStyle() != null && row.getRowStyle().getHidden()) {
            row.getRowStyle().setHidden(false);
        }
        final int columnIndex = (coordinate.getPlaceHolder().getDir() != PlaceHolder.Direction.row) ? coordinate.getColumn() : coordinate.getColumn() + clone;
        return row.getCell(columnIndex);
    }

    public static XSSFCell getCell(final XSSFWorkbook workbook, final int sheetIndex, final int rowIndex, final int columnIndex) {
        final XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null || rowIndex < 0 || columnIndex < 0) {
            return null;
        }
        final XSSFRow row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        final XSSFCell cell = row.getCell(columnIndex);
        if (cell == null) {
            return row.createCell(columnIndex);
        }
        return row.getCell(columnIndex);
    }

    public static Coordinate cloneCoordinates(final Coordinate coordinate, final int clone) {
        if (clone < 0) {
            return null;
        }
        final int        rowIndex         = (coordinate.getPlaceHolder().getDir() != PlaceHolder.Direction.column) ? coordinate.getRow() : coordinate.getRow() + clone;
        final int        columnIndex      = (coordinate.getPlaceHolder().getDir() != PlaceHolder.Direction.row) ? coordinate.getColumn() : coordinate.getColumn() + clone;
        final Coordinate clonedCoordinate = coordinate.clone();
        clonedCoordinate.setRow(rowIndex);
        clonedCoordinate.setColumn(columnIndex);
        return clonedCoordinate;
    }

    public static List<Short> getSheetIndices(final int number) {
        final List<Short> indices = new LinkedList<>();
        for (short index = 0; index < number; index++) {
            indices.add(index);
        }
        return indices;
    }

    public static Variable.Type getVariableDataType(final Coordinate coordinate) {
        if (coordinate == null || coordinate.getPlaceHolder() == null) {
            return null;
        }
        if (coordinate.getPlaceHolder().directionEnabled()) {
            return Variable.Type.LIST;
        }
        if (coordinate.getPlaceHolder().isGrouped()) {
            return Variable.Type.GROUPED;
        }
        return Variable.Type.SINGLE;
    }

    public static String getCellValue(final Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return ((XSSFCell) cell).getRawValue();
        }
        return cell.toString();
    }

    public static void setCellValue(final XSSFWorkbook workbook, final Cell cell, final Object value, final AttributeValue.Type type, final Map<String, Object> params) {
        if (cell == null) {
            return;
        }
        if (type == null) {
            cell.setCellValue(value.toString());
            return;
        }
        final Object    castedValue = StringUtils.cast(value, type);
        final CellStyle cellStyle   = cell.getCellStyle();
        final CellStyle newStyle    = workbook.createCellStyle();
        newStyle.cloneStyleFrom(cellStyle);
        cell.setCellStyle(newStyle);
        switch (type) {
            case STRING:
            case USE_CELL_TYPE:
                cell.setCellValue((String) StringUtils.convertListToString(castedValue, params));
                break;
            case DATE:
                final String dateFormat = StringUtils.getDateFormat(params);
                cell.setCellValue((Date) castedValue);
                if (dateFormat != null) {
                    newStyle.setDataFormat(workbook.createDataFormat().getFormat(dateFormat));
                }
                break;
            case BOOLEAN:
                cell.setCellValue((Boolean) castedValue);
                break;
            case INTEGER:
            case DECIMAL:
                final String numberFormat = StringUtils.getNumberFormat(params);
                cell.setCellValue((double) castedValue);
                if (numberFormat != null) {
                    newStyle.setDataFormat(workbook.createDataFormat().getFormat(numberFormat));
                }
                break;
            case CURRENCY:
                final String currencyFormat = StringUtils.getCurrencyFormat(params);
                cell.setCellValue((double) castedValue);
                if (currencyFormat != null) {
                    newStyle.setDataFormat(workbook.createDataFormat().getFormat(currencyFormat));
                }
                break;
            default:
                cell.setCellValue((String) value);
        }
    }

    public static void setCellFormat(final XSSFWorkbook workbook, final Cell cell, final AttributeValue.Type type, final Map<String, Object> params) {
        if (cell == null) {
            return;
        }
        if (type == null) {
            return;
        }
        final CellStyle cellStyle = cell.getCellStyle();
        final CellStyle newStyle  = workbook.createCellStyle();
        newStyle.cloneStyleFrom(cellStyle);
        cell.setCellStyle(newStyle);
        switch (type) {
            case DATE:
                final String dateFormat = StringUtils.getDateFormat(params);
                if (dateFormat != null) {
                    newStyle.setDataFormat(workbook.createDataFormat().getFormat(dateFormat));
                }
                break;
            case INTEGER:
            case DECIMAL:
                final String numberFormat = StringUtils.getNumberFormat(params);
                if (numberFormat != null) {
                    newStyle.setDataFormat(workbook.createDataFormat().getFormat(numberFormat));
                }
                break;
            case CURRENCY:
                final String currencyFormat = StringUtils.getCurrencyFormat(params);
                if (currencyFormat != null) {
                    newStyle.setDataFormat(workbook.createDataFormat().getFormat(currencyFormat));
                }
                break;
            default:
                break;
        }
    }

    public static void applyLockToCellStyle(final CellStyle cellStyle, final PlaceHolder placeHolder) {
        if (cellStyle == null || placeHolder == null) {
            return;
        }
        if (placeHolder.isLocked()) {
            cellStyle.setLocked(true);
        } else {
            cellStyle.setLocked(false);
        }
    }

    public static void applyCellLock(final XSSFWorkbook workbook, final XSSFCell cell, final PlaceHolder placeHolder) {
        final XSSFCellStyle newLockStyle     = workbook.createCellStyle();
        final XSSFCellStyle currentCellStyle = cell.getCellStyle();
        if (currentCellStyle != null) {
            newLockStyle.cloneStyleFrom(currentCellStyle);
        }
        WorkbookUtils.applyLockToCellStyle(newLockStyle, placeHolder);
        cell.setCellStyle(newLockStyle);
    }

}


