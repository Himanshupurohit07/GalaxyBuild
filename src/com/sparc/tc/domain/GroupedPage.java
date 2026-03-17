package com.sparc.tc.domain;

import com.sparc.tc.domain.CorrespondenceMakerContext;
import com.sparc.tc.domain.Table;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GroupedPage {
    private Map<Short, com.sparc.tc.domain.Table> tableMap;

    public com.sparc.tc.domain.Table getTable(final short sheet) {
        if (this.tableMap == null) {
            return null;
        }
        return this.tableMap.get(sheet);
    }

    public Set<Short> availableSheets() {
        if (this.tableMap == null) {
            return new HashSet<>();
        }
        return this.tableMap.keySet();
    }

    public void addElement(final com.sparc.tc.domain.Table.Record.Element element, final CorrespondenceMakerContext context) {
        if (element == null) {
            return;
        }
        if (this.tableMap == null) {
            this.tableMap = new LinkedHashMap<>();
        }
        if (this.tableMap.get(element.getCoordinate().getSheet()) == null) {
            this.tableMap.put(element.getCoordinate().getSheet(), new com.sparc.tc.domain.Table());
        }
        final com.sparc.tc.domain.Table table = this.tableMap.get(element.getCoordinate().getSheet());
        table.addElement(element, context);
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Map<Short, com.sparc.tc.domain.Table> getTableMap() {
        return this.tableMap;
    }

    @SuppressWarnings("all")
    public void setTableMap(final Map<Short, Table> tableMap) {
        this.tableMap = tableMap;
    }

    @SuppressWarnings("all")
    public GroupedPage() {
    }
    //</editor-fold>
}
