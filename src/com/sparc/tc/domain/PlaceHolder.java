package com.sparc.tc.domain;

import com.sparc.tc.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class PlaceHolder {
    public static final String HIDE_RESERVED_ROWS             = "HRR";
    public static final String RESERVED_ROWS                  = "RR";
    public static final String INVERT_LOCKS_FOR_LEFTOVER_ROWS = "IL";

    //<editor-fold defaultstate="collapsed" desc="delombok">
//</editor-fold>
    public enum Process {
        d, u, du, ud;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
//</editor-fold>
    public enum Direction {
        row, column;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    public static class Params {
        //</editor-fold>
        private Map<String, String> parameters;

        public boolean hasParam(final String key) {
            return key != null && !key.isEmpty() && parameters != null && parameters.containsKey(key);
        }

        public String getParam(final String key) {
            if (key == null || key.isEmpty() || parameters == null) {
                return null;
            }
            return parameters.get(key);
        }

        protected void addParam(final String key, final String value) {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            parameters.put(key, value);
        }

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public Map<String, String> getParameters() {
            return this.parameters;
        }

        @SuppressWarnings("all")
        public void setParameters(final Map<String, String> parameters) {
            this.parameters = parameters;
        }

        @SuppressWarnings("all")
        public Params() {
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "PlaceHolder.Params(parameters=" + this.getParameters() + ")";
        }
        //</editor-fold>
    }

    private Process   process;
    private String    var;
    private boolean   locked;
    private Direction dir;
    private boolean   grouped;
    private Params    params;
    private String    flexType;
    private boolean   commentInstruction;

    public boolean hasVariable() {
        return var != null;
    }

    public boolean hasParams() {
        return params != null;
    }

    public boolean directionEnabled() {
        return dir != null;
    }

    public void addParam(final String key, final String value) {
        if (key == null || key.isEmpty() || value == null) {
            return;
        }
        if (params == null) {
            params = new Params();
        }
        params.addParam(key, value);
    }

    public boolean hideReservedRows() {
        if (params == null || params.getParameters() == null || params.getParam(HIDE_RESERVED_ROWS) == null || params.getParam(RESERVED_ROWS) == null) {
            return false;
        }
        try {
            int reservedRows = Integer.parseInt(params.getParam(RESERVED_ROWS));
            if (reservedRows < 1) {
                return false;
            }
        } catch (Exception e) {
        }
        //<editor-fold defaultstate="collapsed" desc="delombok">
        //ignore
        //</editor-fold>
        return StringUtils.stringToEnum(params.getParam(HIDE_RESERVED_ROWS).toUpperCase(), AlwaysTrue.class) != null;
    }

    public boolean hasReservedRows() {
        if (!hasParam(RESERVED_ROWS)) {
            return false;
        }
        try {
            int reservedRows = Integer.parseInt(params.getParam(RESERVED_ROWS));
            if (reservedRows < 1) {
                return false;
            }
        } catch (Exception e) {
        }
        return true;
    }

    public boolean invertLockForLeftOverRows() {
        return hasParam(INVERT_LOCKS_FOR_LEFTOVER_ROWS) && StringUtils.stringToEnum(params.getParam(INVERT_LOCKS_FOR_LEFTOVER_ROWS).toUpperCase(), AlwaysTrue.class) != null;
    }

    private boolean hasParam(final String paramKey) {
        return !(params == null || params.getParameters() == null || params.getParam(paramKey) == null);
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Process getProcess() {
        return this.process;
    }

    @SuppressWarnings("all")
    public String getVar() {
        return this.var;
    }

    @SuppressWarnings("all")
    public boolean isLocked() {
        return this.locked;
    }

    @SuppressWarnings("all")
    public Direction getDir() {
        return this.dir;
    }

    @SuppressWarnings("all")
    public boolean isGrouped() {
        return this.grouped;
    }

    @SuppressWarnings("all")
    public Params getParams() {
        return this.params;
    }

    @SuppressWarnings("all")
    public String getFlexType() {
        return this.flexType;
    }

    @SuppressWarnings("all")
    public boolean isCommentInstruction() {
        return this.commentInstruction;
    }

    @SuppressWarnings("all")
    public void setProcess(final Process process) {
        this.process = process;
    }

    @SuppressWarnings("all")
    public void setVar(final String var) {
        this.var = var;
    }

    @SuppressWarnings("all")
    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    @SuppressWarnings("all")
    public void setDir(final Direction dir) {
        this.dir = dir;
    }

    @SuppressWarnings("all")
    public void setGrouped(final boolean grouped) {
        this.grouped = grouped;
    }

    @SuppressWarnings("all")
    public void setParams(final Params params) {
        this.params = params;
    }

    @SuppressWarnings("all")
    public void setFlexType(final String flexType) {
        this.flexType = flexType;
    }

    @SuppressWarnings("all")
    public void setCommentInstruction(final boolean commentInstruction) {
        this.commentInstruction = commentInstruction;
    }

    @SuppressWarnings("all")
    public PlaceHolder() {
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "PlaceHolder(process=" + this.getProcess() + ", var=" + this.getVar() + ", locked=" + this.isLocked() + ", dir=" + this.getDir() + ", grouped=" + this.isGrouped() + ", params=" + this.getParams() + ", flexType=" + this.getFlexType() + ", commentInstruction=" + this.isCommentInstruction() + ")";
    }
    //</editor-fold>
}
