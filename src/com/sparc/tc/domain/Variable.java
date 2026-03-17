package com.sparc.tc.domain;

import com.sparc.tc.domain.Coordinate;

import java.util.List;

public class Variable {

    public enum Type {
        SINGLE, LIST, GROUPED;
    }

    private Coordinate coordinate;
    private Type       type;
    private Object data;

    public String getSingleData() {
        if (type == null || type != Type.SINGLE) {
            return null;
        }
        return (String) data;
    }

    public List<String> getListData() {
        if (type == null || type != Type.LIST) {
            return null;
        }
        return (List<String>) data;
    }

    public String getGroupedDate() {
        if (type == null || type != Type.GROUPED) {
            return null;
        }
        return (String) data;
    }


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class VariableBuilder {
        @SuppressWarnings("all")
        private Coordinate coordinate;
        @SuppressWarnings("all")
        private Type type;
        @SuppressWarnings("all")
        private Object data;

        @SuppressWarnings("all")
        VariableBuilder() {
        }

        @SuppressWarnings("all")
        public Variable.VariableBuilder coordinate(final Coordinate coordinate) {
            this.coordinate = coordinate;
            return this;
        }

        @SuppressWarnings("all")
        public Variable.VariableBuilder type(final Type type) {
            this.type = type;
            return this;
        }

        @SuppressWarnings("all")
        public Variable.VariableBuilder data(final Object data) {
            this.data = data;
            return this;
        }

        @SuppressWarnings("all")
        public Variable build() {
            return new Variable(this.coordinate, this.type, this.data);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "Variable.VariableBuilder(coordinate=" + this.coordinate + ", type=" + this.type + ", data=" + this.data + ")";
        }
    }

    @SuppressWarnings("all")
    public static Variable.VariableBuilder builder() {
        return new VariableBuilder();
    }

    @SuppressWarnings("all")
    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    @SuppressWarnings("all")
    public Type getType() {
        return this.type;
    }

    @SuppressWarnings("all")
    public Object getData() {
        return this.data;
    }

    @SuppressWarnings("all")
    public void setCoordinate(final Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @SuppressWarnings("all")
    public void setType(final Type type) {
        this.type = type;
    }

    @SuppressWarnings("all")
    public void setData(final Object data) {
        this.data = data;
    }

    @SuppressWarnings("all")
    public Variable() {
    }

    @SuppressWarnings("all")
    public Variable(final Coordinate coordinate, final Type type, final Object data) {
        this.coordinate = coordinate;
        this.type = type;
        this.data = data;
    }
    //</editor-fold>
}
