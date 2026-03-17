package com.sparc.tc.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sparc.tc.domain.Coordinate;
import com.sparc.tc.domain.CorrespondenceMakerContext;
import com.sparc.tc.domain.Variable;
import com.sparc.tc.exceptions.TCExceptionRuntime;
import com.sparc.tc.util.WorkbookUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Page {

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
            public Page.Element.ElementBuilder data(final String data) {
                this.data = data;
                return this;
            }

            @SuppressWarnings("all")
            public Page.Element.ElementBuilder coordinate(final Coordinate coordinate) {
                this.coordinate = coordinate;
                return this;
            }

            @SuppressWarnings("all")
            public Page.Element build() {
                return new Element(this.data, this.coordinate);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "Page.Element.ElementBuilder(data=" + this.data + ", coordinate=" + this.coordinate + ")";
            }
        }

        @SuppressWarnings("all")
        public static Page.Element.ElementBuilder builder() {
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


    public static class ListElement {
        private List<String> data;
        private Coordinate coordinate;


        @SuppressWarnings("all")
        public static class ListElementBuilder {
            @SuppressWarnings("all")
            private List<String> data;
            @SuppressWarnings("all")
            private Coordinate coordinate;

            @SuppressWarnings("all")
            ListElementBuilder() {
            }

            @SuppressWarnings("all")
            public Page.ListElement.ListElementBuilder data(final List<String> data) {
                this.data = data;
                return this;
            }

            @SuppressWarnings("all")
            public Page.ListElement.ListElementBuilder coordinate(final Coordinate coordinate) {
                this.coordinate = coordinate;
                return this;
            }

            @SuppressWarnings("all")
            public Page.ListElement build() {
                return new ListElement(this.data, this.coordinate);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "Page.ListElement.ListElementBuilder(data=" + this.data + ", coordinate=" + this.coordinate + ")";
            }
        }

        @SuppressWarnings("all")
        public static Page.ListElement.ListElementBuilder builder() {
            return new ListElementBuilder();
        }

        @SuppressWarnings("all")
        public List<String> getData() {
            return this.data;
        }

        @SuppressWarnings("all")
        public Coordinate getCoordinate() {
            return this.coordinate;
        }

        @SuppressWarnings("all")
        public void setData(final List<String> data) {
            this.data = data;
        }

        @SuppressWarnings("all")
        public void setCoordinate(final Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        @SuppressWarnings("all")
        public ListElement() {
        }

        @SuppressWarnings("all")
        public ListElement(final List<String> data, final Coordinate coordinate) {
            this.data = data;
            this.coordinate = coordinate;
        }
    }

    private Map<Short, Map<String, Element>> variableMap;
    private Map<Short, Map<String, ListElement>> listVariableMap;
    @JsonIgnore
    private transient Set<Short> availableSheets;

    public List<Variable> getVariables(final Predicate<Variable> variableFilter, final short sheet) {
        final List<Variable> variables = new LinkedList<>();
        if (sheet < 0) {
            return variables;
        }
        fillElements(variables, sheet);
        if (variableFilter == null) {
            return variables;
        }
        return variables.stream().filter(variableFilter).collect(Collectors.toList());
    }

    private void fillElements(final List<Variable> variables, final short sheet) {
        if (this.variableMap != null) {
            final Map<String, Element> sheetElements = this.variableMap.get(sheet);
            if (sheetElements != null) {
                sheetElements.forEach((key, value) -> {
                    variables.add(Variable.builder().type(Variable.Type.SINGLE).coordinate(value.getCoordinate()).data(value.getData()).build());
                });
            }
        }
        if (this.listVariableMap != null) {
            final Map<String, ListElement> sheetListElements = this.listVariableMap.get(sheet);
            if (sheetListElements != null) {
                sheetListElements.forEach((key, value) -> {
                    variables.add(Variable.builder().type(Variable.Type.LIST).coordinate(value.getCoordinate()).data(value.getData()).build());
                });
            }
        }
    }

    public Variable getVariable(final String variable, final short sheet) {
        if (variable == null || variable.isEmpty() || sheet < 0) {
            return null;
        }
        final Variable varMapElement = getVariableFromVariableMap(variable, sheet);
        if (varMapElement != null) {
            return varMapElement;
        }
        return getVariableFromListVariableMap(variable, sheet);
    }

    private Variable getVariableFromVariableMap(final String variable, final short sheet) {
        if (this.variableMap == null) {
            return null;
        }
        final Map<String, Element> sheetVariableMap = this.variableMap.get(sheet);
        if (sheetVariableMap == null) {
            return null;
        }
        final Element element = sheetVariableMap.get(variable);
        if (element == null) {
            return null;
        }
        return Variable.builder().type(WorkbookUtils.getVariableDataType(element.getCoordinate())).coordinate(element.getCoordinate()).data(element.getData()).build();
    }

    private Variable getVariableFromListVariableMap(final String variable, final short sheet) {
        if (this.listVariableMap == null) {
            return null;
        }
        final Map<String, ListElement> sheetListVariableMap = this.listVariableMap.get(sheet);
        if (sheetListVariableMap == null) {
            return null;
        }
        final ListElement element = sheetListVariableMap.get(variable);
        if (element == null) {
            return null;
        }
        return Variable.builder().type(Variable.Type.LIST).coordinate(element.getCoordinate()).data(element.getData()).build();
    }

    public Set<Short> computeAvailableSheets() {
        if (this.availableSheets != null) {
            return this.availableSheets;
        }
        final Set<Short> sheets = new HashSet<>();
        if (this.variableMap == null && this.listVariableMap == null) {
            this.availableSheets = sheets;
            return sheets;
        }
        if (this.variableMap != null) {
            sheets.addAll(this.variableMap.keySet());
        }
        if (this.listVariableMap != null) {
            sheets.addAll(this.listVariableMap.keySet());
        }
        this.availableSheets = sheets;
        return sheets;
    }

    public void addVariable(final Element element, final com.sparc.tc.domain.CorrespondenceMakerContext context) {
        if (element == null || context == null) {
            return;
        }
        if (this.variableMap == null) {
            this.variableMap = new LinkedHashMap<>();
        }
        if (this.variableMap.get(element.getCoordinate().getSheet()) == null) {
            this.variableMap.put(element.getCoordinate().getSheet(), new LinkedHashMap<>());
        }
        final Map<String, Element> perSheetVariableMap = this.variableMap.get(element.getCoordinate().getSheet());
        if (perSheetVariableMap.get(element.getCoordinate().getPlaceHolder().getVar()) != null) {
            context.addToCorrespondenceLog(new TCExceptionRuntime("Found duplicate variable with name:" + element.getCoordinate().getPlaceHolder().getVar() + ", with value:" + perSheetVariableMap.get(element.getCoordinate().getPlaceHolder().getVar()) + ", at coordinates:" + element.getCoordinate() + ", replacing the value..", TCExceptionRuntime.Type.WARNING));
        }
        perSheetVariableMap.put(element.getCoordinate().getPlaceHolder().getVar(), element);
        this.availableSheets = null;
    }

    public void addVariable(final ListElement variable, final CorrespondenceMakerContext context) {
        if (variable == null || context == null) {
            return;
        }
        if (this.listVariableMap == null) {
            this.listVariableMap = new LinkedHashMap<>();
        }
        if (this.listVariableMap.get(variable.getCoordinate().getSheet()) == null) {
            this.listVariableMap.put(variable.getCoordinate().getSheet(), new LinkedHashMap<>());
        }
        final Map<String, ListElement> perSheetVariableMap = this.listVariableMap.get(variable.getCoordinate().getSheet());
        if (perSheetVariableMap.get(variable.getCoordinate().getPlaceHolder().getVar()) != null) {
            context.addToCorrespondenceLog(new TCExceptionRuntime("Found duplicate variable with name:" + variable.getCoordinate().getPlaceHolder().getVar() + ", with value:" + perSheetVariableMap.get(variable.getCoordinate().getPlaceHolder().getVar()) + ", at coordinates:" + variable.getCoordinate() + ", replacing the value..", TCExceptionRuntime.Type.WARNING));
        }
        perSheetVariableMap.put(variable.getCoordinate().getPlaceHolder().getVar(), variable);
        this.availableSheets = null;
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Map<Short, Map<String, Element>> getVariableMap() {
        return this.variableMap;
    }

    @SuppressWarnings("all")
    public Map<Short, Map<String, ListElement>> getListVariableMap() {
        return this.listVariableMap;
    }

    @SuppressWarnings("all")
    public Set<Short> getAvailableSheets() {
        return this.availableSheets;
    }

    @SuppressWarnings("all")
    public void setVariableMap(final Map<Short, Map<String, Element>> variableMap) {
        this.variableMap = variableMap;
    }

    @SuppressWarnings("all")
    public void setListVariableMap(final Map<Short, Map<String, ListElement>> listVariableMap) {
        this.listVariableMap = listVariableMap;
    }

    @JsonIgnore
    @SuppressWarnings("all")
    public void setAvailableSheets(final Set<Short> availableSheets) {
        this.availableSheets = availableSheets;
    }

    @SuppressWarnings("all")
    public Page() {
    }
    //</editor-fold>
}
