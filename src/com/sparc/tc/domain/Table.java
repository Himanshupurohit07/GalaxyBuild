package com.sparc.tc.domain;

import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.CorrespondenceMakerContext;
import com.sparc.tc.domain.Variable;
import com.sparc.tc.exceptions.TCExceptionRuntime;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Table {

    public static class Record {

        public static class Element {
            private String     data;
            private Coordinate coordinate;


            //<editor-fold defaultstate="collapsed" desc="delombok">
            @SuppressWarnings("all")
            public static class ElementBuilder {
                @SuppressWarnings("all")
                private String data;
                @SuppressWarnings("all")
                private Coordinate coordinate;

                @SuppressWarnings("all")
                ElementBuilder() {
                }

                @SuppressWarnings("all")
                public Table.Record.Element.ElementBuilder data(final String data) {
                    this.data = data;
                    return this;
                }

                @SuppressWarnings("all")
                public Table.Record.Element.ElementBuilder coordinate(final Coordinate coordinate) {
                    this.coordinate = coordinate;
                    return this;
                }

                @SuppressWarnings("all")
                public Table.Record.Element build() {
                    return new Element(this.data, this.coordinate);
                }

                @Override
                @SuppressWarnings("all")
                public String toString() {
                    return "Table.Record.Element.ElementBuilder(data=" + this.data + ", coordinate=" + this.coordinate + ")";
                }
            }

            @SuppressWarnings("all")
            public static Table.Record.Element.ElementBuilder builder() {
                return new ElementBuilder();
            }

            @SuppressWarnings("all")
            public String getData() {
                return this.data;
            }

            @SuppressWarnings("all")
            public Coordinate getCoordinate() {
                return this.coordinate;
            }

            @SuppressWarnings("all")
            public void setData(final String data) {
                this.data = data;
            }

            @SuppressWarnings("all")
            public void setCoordinate(final Coordinate coordinate) {
                this.coordinate = coordinate;
            }

            @SuppressWarnings("all")
            public Element() {
            }

            @SuppressWarnings("all")
            public Element(final String data, final Coordinate coordinate) {
                this.data = data;
                this.coordinate = coordinate;
            }
            //</editor-fold>
        }

        private Map<String, Element> elements;

        public Variable getVariable(final String variableName) {
            if (variableName == null || variableName.isEmpty() || this.elements == null) {
                return null;
            }
            final Element element = this.elements.get(variableName);
            if (element == null) {
                return null;
            }
            return Variable.builder().type(Variable.Type.GROUPED).data(element.data).coordinate(element.getCoordinate()).build();
        }

        public List<Variable> getVariables(final Predicate<Variable> variableFilter) {
            final List<Variable> variables = new LinkedList<>();
            if (this.elements == null) {
                return variables;
            }
            this.elements.forEach((key, value) -> variables.add(Variable.builder().type(Variable.Type.GROUPED).data(value.data).coordinate(value.getCoordinate()).build()));
            if (variableFilter == null) {
                return variables;
            } else {
                return variables.stream().filter(variableFilter).collect(Collectors.toList());
            }
        }

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public Map<String, Element> getElements() {
            return this.elements;
        }

        @SuppressWarnings("all")
        public void setElements(final Map<String, Element> elements) {
            this.elements = elements;
        }

        @SuppressWarnings("all")
        public Record() {
        }
        //</editor-fold>
    }

    private Map<Integer, Record> recordMap;

    public List<Record> getRecords() {
        if (this.recordMap == null) {
            return new LinkedList<>();
        }
        final List<Record> records = new LinkedList<>();
        this.recordMap.forEach((key, value) -> records.add(value));
        return records;
    }

    public void addElement(final Record.Element element, final CorrespondenceMakerContext context) {
        if (element == null) {
            return;
        }
        if (this.recordMap == null) {
            this.recordMap = new LinkedHashMap<>();
        }
        if (this.recordMap.get(element.getCoordinate().getRow()) == null) {
            this.recordMap.put(element.getCoordinate().getRow(), new Record());
        }
        final Record record = this.recordMap.get(element.getCoordinate().getRow());
        if (record.getElements() == null) {
            record.setElements(new LinkedHashMap<>());
        }
        if (record.getElements().get(element.getCoordinate().getPlaceHolder().getVar()) != null) {
            context.addToCorrespondenceLog(new TCExceptionRuntime("Found duplicate variable with name:" + element.getCoordinate().getPlaceHolder().getVar() + ", with value:" + record.getElements().get(element.getCoordinate().getPlaceHolder().getVar()).getData() + ", at coordinates:" + element.getCoordinate() + ", replacing the value..", TCExceptionRuntime.Type.WARNING));
        }
        record.getElements().put(element.getCoordinate().getPlaceHolder().getVar(), element);
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Map<Integer, Record> getRecordMap() {
        return this.recordMap;
    }

    @SuppressWarnings("all")
    public void setRecordMap(final Map<Integer, Record> recordMap) {
        this.recordMap = recordMap;
    }

    @SuppressWarnings("all")
    public Table() {
    }
    //</editor-fold>
}
