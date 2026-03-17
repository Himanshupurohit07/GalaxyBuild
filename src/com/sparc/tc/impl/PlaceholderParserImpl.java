package com.sparc.tc.impl;

import com.sparc.tc.abstractions.PlaceholderParser;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.exceptions.TCExceptionRuntime;
import com.sparc.tc.util.StringUtils;

import java.util.List;

public class PlaceholderParserImpl implements PlaceholderParser {

    private static final String INSTRUCTION_SET_DELIMITER = "\\|";
    private static final String VALUE_DELIMITER           = ":";
    private static final String KEY_VALUE_DELIMITER       = "~";
    public static final  String INSTRUCTION_LEFT_BRACE    = "{";
    public static final  String INSTRUCTION_RIGHT_BRACE   = "}";

    @Override
    public PlaceHolder parse(final String instruction) throws TCExceptionRuntime {

        if (!StringUtils.hasContent(instruction)) {
            return null;
        }
        if (!recognizeInstruction(instruction)) {
            return null;
        }
        final String       trimmedInstruction = trimBraces(instruction);
        final List<String> instructionTokens  = StringUtils.tokenize(trimmedInstruction, INSTRUCTION_SET_DELIMITER);
        if (instructionTokens.isEmpty()) {
            return null;
        }
        final PlaceHolder placeHolder = new PlaceHolder();
        instructionTokens.forEach(ins -> addInstructionToPlaceholder(ins, placeHolder));
        if (placeHolder.getProcess() == null && !placeHolder.isLocked()) {
            throw new TCExceptionRuntime("The process is a required parameter for a given instruction, please check the instruction to add process:" + instruction, TCExceptionRuntime.Type.WARNING);
        }
        return placeHolder;
    }

    private String trimBraces(final String instruction) {
        final String substring = instruction.substring(1);
        return substring.substring(0, substring.length() - 1);
    }

    private boolean recognizeInstruction(final String instruction) {
        return instruction.startsWith(INSTRUCTION_LEFT_BRACE) && instruction.endsWith(INSTRUCTION_RIGHT_BRACE);
    }

    private void addInstructionToPlaceholder(final String instructionToken, final PlaceHolder placeHolder) {

        if (!StringUtils.hasContent(instructionToken)) {
            return;
        }
        final String[] instructionValue = StringUtils.tokenizeToArray(instructionToken, VALUE_DELIMITER);
        if (instructionValue.length == 0) {
            return;
        }
        if (instructionValue.length == 1) {
            addFlagsToPlaceholder(instructionValue[0], placeHolder);
            return;
        }
        if (instructionValue.length == 2) {
            addOptionsToPlaceholder(instructionValue, placeHolder);
            return;
        }

    }

    private void addOptionsToPlaceholder(final String[] instructionValue, final PlaceHolder placeHolder) {
        final String option = instructionValue[0].toLowerCase();
        switch (option) {
            case "var":
                placeHolder.setVar(instructionValue[1]);
                break;
            case "dir":
                final PlaceHolder.Direction direction = StringUtils.stringToEnum(instructionValue[1].toLowerCase(), PlaceHolder.Direction.class);
                if (direction == null) {
                    throw new TCExceptionRuntime("Expecting a valid direction, but found:" + instructionValue[1], TCExceptionRuntime.Type.WARNING);
                }
                placeHolder.setDir(direction);
                break;
            case "param":
                final String[] keyValuePair = StringUtils.tokenizeToArray(instructionValue[1], KEY_VALUE_DELIMITER);
                if (keyValuePair.length != 2) {
                    break;
                }
                placeHolder.addParam(keyValuePair[0], keyValuePair[1]);
                break;
            case "ft":
                placeHolder.setFlexType(instructionValue[1]);
            default:
                break;
        }
    }

    private void addFlagsToPlaceholder(final String flagInstruction, final PlaceHolder placeholder) {
        switch (flagInstruction.toLowerCase()) {
            case "locked":
                placeholder.setLocked(true);
                break;
            case "grouped":
                placeholder.setGrouped(true);
                break;
            case "*du":
                placeholder.setProcess(PlaceHolder.Process.du);
                break;
            case "*ud":
                placeholder.setProcess(PlaceHolder.Process.du);
                break;
            case "*d":
                placeholder.setProcess(PlaceHolder.Process.d);
                break;
            case "*u":
                placeholder.setProcess(PlaceHolder.Process.u);
                break;
            default:
                break;
        }
    }

}
