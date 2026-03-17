package com.sparc.wc.tc.domain;

import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.util.StringUtils;
import com.sparc.wc.tc.abstractions.MonoAppendable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NonFlexObjectsCluster {

    public static class GroupedNode implements MonoAppendable<Map<String, Object>> {
        private List<Map<String, Object>> list = new LinkedList<>();

        public AttributeValue getValue(final PlaceHolder placeHolder) {
            if (placeHolder == null || !StringUtils.hasContent(placeHolder.getVar()) || !placeHolder.isGrouped()) {
                return null;
            }
            if (list.isEmpty()) {
                return null;
            }
            final boolean valueExists = list.stream().anyMatch(params -> {
                return params != null && params.containsKey(placeHolder.getVar());
            });
            if (!valueExists) {
                return null;
            }
            final AttributeValue attributeValue = AttributeValue.builder().type(AttributeValue.Type.USE_CELL_TYPE).build();
            final List<Object> valueList = list.stream().map(params -> {
                if (params == null) {
                    return null;
                }
                return params.get(placeHolder.getVar());
            }).collect(Collectors.toList());
            attributeValue.setData(valueList);
            if (placeHolder.hasParams() && placeHolder.getParams().getParameters() != null) {
                if (attributeValue.getParams() == null) {
                    attributeValue.setParams(new HashMap<>());
                }
                attributeValue.getParams().putAll(placeHolder.getParams().getParameters());
            }
            return attributeValue;
        }

        public void add(final Map<String, Object> params) {
            this.list.add(params);
        }

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class GroupedNodeBuilder {
            @SuppressWarnings("all")
            private List<Map<String, Object>> list;

            @SuppressWarnings("all")
            GroupedNodeBuilder() {
            }

            @SuppressWarnings("all")
            public NonFlexObjectsCluster.GroupedNode.GroupedNodeBuilder list(final List<Map<String, Object>> list) {
                this.list = list;
                return this;
            }

            @SuppressWarnings("all")
            public NonFlexObjectsCluster.GroupedNode build() {
                return new NonFlexObjectsCluster.GroupedNode(this.list);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "NonFlexObjectsCluster.GroupedNode.GroupedNodeBuilder(list=" + this.list + ")";
            }
        }

        @SuppressWarnings("all")
        public static NonFlexObjectsCluster.GroupedNode.GroupedNodeBuilder builder() {
            return new NonFlexObjectsCluster.GroupedNode.GroupedNodeBuilder();
        }

        @SuppressWarnings("all")
        public List<Map<String, Object>> getList() {
            return this.list;
        }

        @SuppressWarnings("all")
        public void setList(final List<Map<String, Object>> list) {
            this.list = list;
        }

        @SuppressWarnings("all")
        public GroupedNode() {
        }

        @SuppressWarnings("all")
        public GroupedNode(final List<Map<String, Object>> list) {
            this.list = list;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "NonFlexObjectsCluster.GroupedNode(list=" + this.getList() + ")";
        }
        //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    public static class NonGroupedNode implements MonoAppendable<Map<String, Object>> {
        //</editor-fold>
        private Map<String, Object> params;

        public AttributeValue getValue(final PlaceHolder placeHolder) {
            if (placeHolder == null || placeHolder.getVar() == null || placeHolder.getVar().isEmpty()) {
                return null;
            }
            final Object value = params.get(placeHolder.getVar());
            if (value == null) {
                return null;
            }
            final AttributeValue attributeValue = AttributeValue.builder().data(value).type(AttributeValue.Type.USE_CELL_TYPE).build();
            if (attributeValue != null && placeHolder.hasParams() && placeHolder.getParams().getParameters() != null) {
                if (attributeValue.getParams() == null) {
                    attributeValue.setParams(new HashMap<>());
                }
                attributeValue.getParams().putAll(placeHolder.getParams().getParameters());
            }
            return attributeValue;
        }

        public void add(final Map<String, Object> params) {
            if (params == null) {
                return;
            }
            this.params = params;
        }

        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class NonGroupedNodeBuilder {
            @SuppressWarnings("all")
            private Map<String, Object> params;

            @SuppressWarnings("all")
            NonGroupedNodeBuilder() {
            }

            @SuppressWarnings("all")
            public NonFlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder params(final Map<String, Object> params) {
                this.params = params;
                return this;
            }

            @SuppressWarnings("all")
            public NonFlexObjectsCluster.NonGroupedNode build() {
                return new NonFlexObjectsCluster.NonGroupedNode(this.params);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "NonFlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder(params=" + this.params + ")";
            }
        }

        @SuppressWarnings("all")
        public static NonFlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder builder() {
            return new NonFlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder();
        }

        @SuppressWarnings("all")
        public Map<String, Object> getParams() {
            return this.params;
        }

        @SuppressWarnings("all")
        public void setParams(final Map<String, Object> params) {
            this.params = params;
        }

        @SuppressWarnings("all")
        public NonGroupedNode() {
        }

        @SuppressWarnings("all")
        public NonGroupedNode(final Map<String, Object> params) {
            this.params = params;
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "NonFlexObjectsCluster.NonGroupedNode(params=" + this.getParams() + ")";
        }
        //</editor-fold>
    }

    private Map<String, GroupedNode> groupedNodeMap  = new HashMap<>();
    private List<NonGroupedNode>     nonGroupedNodes = new ArrayList<>();

    public void addGroupedNodeSingleKeyValue(final String group, final String key, final List<Object> values) {
        if (key == null || key.isEmpty() || group == null || group.isEmpty()) {
            return;
        }
        values.forEach(value -> {
            final Map<String, Object> params = new HashMap<>();
            params.put(key, value);
            addGroupedNode(group, params);
        });
    }

    public void addGroupedNode(final String group, final Map<String, Object> params) {
        if (params == null || group == null || group.isEmpty()) {
            return;
        }
        if (groupedNodeMap.get(group) == null) {
            groupedNodeMap.put(group, new GroupedNode());
        }
        final GroupedNode groupedNode = groupedNodeMap.get(group);
        groupedNode.add(params);
    }

    public void addNonGroupedNode(final String key, final Object value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        final Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        addNonGroupedNode(params);
    }

    public void addNonGroupedNode(final Map<String, Object> params) {
        if (params == null) {
            return;
        }
        final NonGroupedNode nonGroupedNode = new NonGroupedNode();
        nonGroupedNode.add(params);
        nonGroupedNodes.add(nonGroupedNode);
    }

    public AttributeValue getValue(final PlaceHolder placeHolder) {
        if (placeHolder == null || placeHolder.getVar() == null || placeHolder.getVar().isEmpty()) {
            return null;
        }
        if (placeHolder.isGrouped()) {
            final GroupedNode gn = groupedNodeMap.values().stream().filter(groupedNode -> {
                return groupedNode.getValue(placeHolder) != null;
            }).findFirst().orElse(null);
            if (gn == null) {
                return null;
            }
            return gn.getValue(placeHolder);
        } else {
            final NonGroupedNode nonGn = nonGroupedNodes.stream().filter(nonGroupedNode -> {
                return nonGroupedNode.getValue(placeHolder) != null;
            }).findFirst().orElse(null);
            if (nonGn == null) {
                return null;
            }
            return nonGn.getValue(placeHolder);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Map<String, GroupedNode> getGroupedNodeMap() {
        return this.groupedNodeMap;
    }

    @SuppressWarnings("all")
    public List<NonGroupedNode> getNonGroupedNodes() {
        return this.nonGroupedNodes;
    }

    @SuppressWarnings("all")
    public void setGroupedNodeMap(final Map<String, GroupedNode> groupedNodeMap) {
        this.groupedNodeMap = groupedNodeMap;
    }

    @SuppressWarnings("all")
    public void setNonGroupedNodes(final List<NonGroupedNode> nonGroupedNodes) {
        this.nonGroupedNodes = nonGroupedNodes;
    }

    @SuppressWarnings("all")
    public NonFlexObjectsCluster() {
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "NonFlexObjectsCluster(groupedNodeMap=" + this.getGroupedNodeMap() + ", nonGroupedNodes=" + this.getNonGroupedNodes() + ")";
    }
    //</editor-fold>
}
