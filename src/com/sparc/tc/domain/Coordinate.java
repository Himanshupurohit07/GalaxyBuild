package com.sparc.tc.domain;

import com.sparc.tc.domain.PlaceHolder;

public class Coordinate implements Cloneable {
    private com.sparc.tc.domain.PlaceHolder placeHolder;
    private int                             row;
    private int                             column;
    private short                           sheet;

    public Coordinate clone() {
        return Coordinate.builder().sheet(this.sheet).row(this.row).column(this.column).placeHolder(this.placeHolder).build();
    }


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class CoordinateBuilder {
        @SuppressWarnings("all")
        private com.sparc.tc.domain.PlaceHolder placeHolder;
        @SuppressWarnings("all")
        private int                             row;
        @SuppressWarnings("all")
        private int column;
        @SuppressWarnings("all")
        private short sheet;

        @SuppressWarnings("all")
        CoordinateBuilder() {
        }

        @SuppressWarnings("all")
        public Coordinate.CoordinateBuilder placeHolder(final com.sparc.tc.domain.PlaceHolder placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        @SuppressWarnings("all")
        public Coordinate.CoordinateBuilder row(final int row) {
            this.row = row;
            return this;
        }

        @SuppressWarnings("all")
        public Coordinate.CoordinateBuilder column(final int column) {
            this.column = column;
            return this;
        }

        @SuppressWarnings("all")
        public Coordinate.CoordinateBuilder sheet(final short sheet) {
            this.sheet = sheet;
            return this;
        }

        @SuppressWarnings("all")
        public Coordinate build() {
            return new Coordinate(this.placeHolder, this.row, this.column, this.sheet);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "Coordinate.CoordinateBuilder(placeHolder=" + this.placeHolder + ", row=" + this.row + ", column=" + this.column + ", sheet=" + this.sheet + ")";
        }
    }

    @SuppressWarnings("all")
    public static Coordinate.CoordinateBuilder builder() {
        return new CoordinateBuilder();
    }

    @SuppressWarnings("all")
    public com.sparc.tc.domain.PlaceHolder getPlaceHolder() {
        return this.placeHolder;
    }

    @SuppressWarnings("all")
    public int getRow() {
        return this.row;
    }

    @SuppressWarnings("all")
    public int getColumn() {
        return this.column;
    }

    @SuppressWarnings("all")
    public short getSheet() {
        return this.sheet;
    }

    @SuppressWarnings("all")
    public void setPlaceHolder(final com.sparc.tc.domain.PlaceHolder placeHolder) {
        this.placeHolder = placeHolder;
    }

    @SuppressWarnings("all")
    public void setRow(final int row) {
        this.row = row;
    }

    @SuppressWarnings("all")
    public void setColumn(final int column) {
        this.column = column;
    }

    @SuppressWarnings("all")
    public void setSheet(final short sheet) {
        this.sheet = sheet;
    }

    @SuppressWarnings("all")
    public Coordinate() {
    }

    @SuppressWarnings("all")
    public Coordinate(final PlaceHolder placeHolder, final int row, final int column, final short sheet) {
        this.placeHolder = placeHolder;
        this.row = row;
        this.column = column;
        this.sheet = sheet;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "Coordinate(placeHolder=" + this.getPlaceHolder() + ", row=" + this.getRow() + ", column=" + this.getColumn() + ", sheet=" + this.getSheet() + ")";
    }
    //</editor-fold>
}
