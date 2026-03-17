package com.sparc.tc.impl;

import com.sparc.tc.abstractions.Interpreter;
import com.sparc.tc.abstractions.PlaceholderParser;
import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.InterpreterContext;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.exceptions.TCExceptionRuntime;
import com.sparc.tc.util.WorkbookUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class InterpreterImpl implements Interpreter {

    private PlaceholderParser placeholderParser;

    public InterpreterImpl(final PlaceholderParser placeholderParser) {
        this.placeholderParser = placeholderParser;
    }

    @Override
    public List<Coordinate> interpret(final XSSFWorkbook workbook, final List<Short> sheetIndices, final InterpreterContext interpreterContext) {

        if (workbook == null || sheetIndices == null || sheetIndices.isEmpty() || interpreterContext == null) {
            return new ArrayList<>();
        }
        return sheetIndices.stream().flatMap(sheetIndex -> {
            interpreterContext.setCurrentSheet(sheetIndex);
            return iterateSheets(workbook, sheetIndex, interpreterContext).stream();
        }).collect(Collectors.toList());

    }

    @Override
    public List<Coordinate> interpret(XSSFWorkbook workbook, InterpreterContext interpreterContext) {

        if (workbook == null || interpreterContext == null) {
            return new ArrayList<>();
        }
        final int         numberOfSheets = workbook.getNumberOfSheets();
        final List<Short> sheetIndices   = WorkbookUtils.getSheetIndices(numberOfSheets);
        return interpret(workbook, sheetIndices, interpreterContext);
    }

    private List<Coordinate> iterateSheets(final XSSFWorkbook workbook, final short sheetIndex, final InterpreterContext interpreterContext) {
        final XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        if (sheet == null) {
            return new ArrayList<>();
        }
        return iterateRows(sheet, interpreterContext);
    }

    private List<Coordinate> iterateRows(final XSSFSheet sheet, final InterpreterContext interpreterContext) {
        final List<Coordinate> coordinates = new LinkedList<>();
        sheet.rowIterator().forEachRemaining(row -> {
            if (row == null) {
                return;
            }
            coordinates.addAll(iterateColumns(row, interpreterContext));
        });
        return coordinates;
    }

    private List<Coordinate> iterateColumns(final Row row, final InterpreterContext interpreterContext) {
        final List<Coordinate> coordinates = new LinkedList<>();
        row.cellIterator().forEachRemaining(cell -> {
            if (cell == null) {
                return;
            }
            try {
                final String      instruction = cell.toString();
                final PlaceHolder placeHolder = placeholderParser.parse(instruction);
                if (placeHolder != null) {
                    coordinates.add(Coordinate.builder()
                            .placeHolder(placeHolder)
                            .column(cell.getColumnIndex())
                            .row(cell.getRowIndex())
                            .sheet(interpreterContext.getCurrentSheet())
                            .build()
                    );
                } else {
                    final String cellComment = getCellComment(cell);
                    if (cellComment != null && !cellComment.isEmpty()) {
                        final PlaceHolder commentPlaceholder = placeholderParser.parse(cellComment);
                        if (commentPlaceholder != null) {
                            commentPlaceholder.setCommentInstruction(true);
                            coordinates.add(Coordinate.builder()
                                    .placeHolder(commentPlaceholder)
                                    .column(cell.getColumnIndex())
                                    .row(cell.getRowIndex())
                                    .sheet(interpreterContext.getCurrentSheet())
                                    .build());
                        }
                    }
                }
            } catch (TCExceptionRuntime tce) {
                interpreterContext.addToInstructionLog(tce);
            } catch (Exception e) {
                interpreterContext.addToInstructionLog(new TCExceptionRuntime(e.getMessage(), TCExceptionRuntime.Type.ERROR));

            }
        });
        return coordinates;
    }

    private String getCellComment(final Cell cell) {
        if (cell == null || cell.getCellComment() == null) {
            return null;
        }
        final String author = cell.getCellComment().getAuthor();
        if (author != null && !author.isEmpty()) {
            final String authorStr = author + ":";
            if (cell.getCellComment().getString().getString().startsWith(authorStr)) {
                return cell.getCellComment().getString().getString().substring(authorStr.length()).trim();
            }
        }
        return cell.getCellComment().getString().getString().trim();
    }

}
