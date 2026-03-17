package com.sparc.wc.tc.domain;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTyped;
import com.sparc.tc.domain.AttributeValue;
import com.sparc.tc.domain.PlaceHolder;
import com.sparc.tc.exceptions.TCException;
import com.sparc.tc.util.StringUtils;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.tc.abstractions.BiAppendable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlexObjectsCluster {

    public static class GroupedNode implements BiAppendable<FlexTyped, String> {
        private List<FlexTyped> list = new LinkedList<>();
        private FlexType flexType;
        private String className;

        public AttributeValue getValue(final PlaceHolder placeHolder) {
            if (placeHolder == null || !StringUtils.hasContent(placeHolder.getVar()) || !placeHolder.isGrouped()) {
                return null;
            }
            if (list.isEmpty()) {
                return null;
            }
            final List<AttributeValue> attributeValues = list.stream().map(flexTyped -> SparcIntegrationUtil.getAttributeValueFrom(placeHolder, flexTyped)).collect(Collectors.toList());
            if (attributeValues.isEmpty()) {
                return null;
            }
            final List<Object> dataValues = attributeValues.stream().map(attr -> {
                if (attr == null) {
                    return null;
                }
                return attr.getData();
            }).collect(Collectors.toList());
            final AttributeValue attributeValue = attributeValues.stream().filter(attr -> attr != null).findFirst().orElse(null);
            if (attributeValue == null) {
                return null;
            }
            attributeValue.setData(dataValues);
            if (placeHolder.hasParams() && placeHolder.getParams().getParameters() != null) {
                if (attributeValue.getParams() == null) {
                    attributeValue.setParams(new HashMap<>());
                }
                attributeValue.getParams().putAll(placeHolder.getParams().getParameters());
            }
            return attributeValue;
        }

        @Override
        public void add(final FlexTyped flexTyped, final String className) throws TCException {
            if (flexTyped == null || !StringUtils.hasContent(className)) {
                return;
            }
            this.list.add(flexTyped);
            this.className = className;
            if (this.flexType == null) {
                try {
                    this.flexType = flexTyped.getFlexType();
                } catch (Exception e) {
                    throw new TCException(e);
                }
            }
        }


        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class GroupedNodeBuilder {
            @SuppressWarnings("all")
            private List<FlexTyped> list;
            @SuppressWarnings("all")
            private FlexType flexType;
            @SuppressWarnings("all")
            private String className;

            @SuppressWarnings("all")
            GroupedNodeBuilder() {
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.GroupedNode.GroupedNodeBuilder list(final List<FlexTyped> list) {
                this.list = list;
                return this;
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.GroupedNode.GroupedNodeBuilder flexType(final FlexType flexType) {
                this.flexType = flexType;
                return this;
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.GroupedNode.GroupedNodeBuilder className(final String className) {
                this.className = className;
                return this;
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.GroupedNode build() {
                return new FlexObjectsCluster.GroupedNode(this.list, this.flexType, this.className);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "FlexObjectsCluster.GroupedNode.GroupedNodeBuilder(list=" + this.list + ", flexType=" + this.flexType + ", className=" + this.className + ")";
            }
        }

        @SuppressWarnings("all")
        public static FlexObjectsCluster.GroupedNode.GroupedNodeBuilder builder() {
            return new FlexObjectsCluster.GroupedNode.GroupedNodeBuilder();
        }

        @SuppressWarnings("all")
        public List<FlexTyped> getList() {
            return this.list;
        }

        @SuppressWarnings("all")
        public FlexType getFlexType() {
            return this.flexType;
        }

        @SuppressWarnings("all")
        public String getClassName() {
            return this.className;
        }

        @SuppressWarnings("all")
        public void setList(final List<FlexTyped> list) {
            this.list = list;
        }

        @SuppressWarnings("all")
        public void setFlexType(final FlexType flexType) {
            this.flexType = flexType;
        }

        @SuppressWarnings("all")
        public void setClassName(final String className) {
            this.className = className;
        }

        @SuppressWarnings("all")
        public GroupedNode() {
        }

        @SuppressWarnings("all")
        public GroupedNode(final List<FlexTyped> list, final FlexType flexType, final String className) {
            this.list = list;
            this.flexType = flexType;
            this.className = className;
        }
        //</editor-fold>
    }


    //<editor-fold defaultstate="collapsed" desc="delombok">
    public static class NonGroupedNode implements BiAppendable<FlexTyped, String> {
    //</editor-fold>
        private FlexTyped flexTyped;
        private FlexType flexType;
        private String className;

        public AttributeValue getValue(final PlaceHolder placeHolder) {
            if (placeHolder == null || placeHolder.getVar() == null || placeHolder.getVar().isEmpty()) {
                return null;
            }
            final AttributeValue attributeValue = SparcIntegrationUtil.getAttributeValueFrom(placeHolder, flexTyped);
            if (attributeValue != null && placeHolder.hasParams() && placeHolder.getParams().getParameters() != null) {
                if (attributeValue.getParams() == null) {
                    attributeValue.setParams(new HashMap<>());
                }
                attributeValue.getParams().putAll(placeHolder.getParams().getParameters());
            }
            return attributeValue;
        }

        public void add(final FlexTyped flexTyped, final String className) throws TCException {
            if (flexTyped == null || !StringUtils.hasContent(className)) {
                return;
            }
            this.flexTyped = flexTyped;
            this.className = className;
            try {
                this.flexType = flexTyped.getFlexType();
            } catch (Exception e) {
                throw new TCException(e);
            }
        }


        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class NonGroupedNodeBuilder {
            @SuppressWarnings("all")
            private FlexTyped flexTyped;
            @SuppressWarnings("all")
            private FlexType flexType;
            @SuppressWarnings("all")
            private String className;

            @SuppressWarnings("all")
            NonGroupedNodeBuilder() {
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder flexTyped(final FlexTyped flexTyped) {
                this.flexTyped = flexTyped;
                return this;
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder flexType(final FlexType flexType) {
                this.flexType = flexType;
                return this;
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder className(final String className) {
                this.className = className;
                return this;
            }

            @SuppressWarnings("all")
            public FlexObjectsCluster.NonGroupedNode build() {
                return new FlexObjectsCluster.NonGroupedNode(this.flexTyped, this.flexType, this.className);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "FlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder(flexTyped=" + this.flexTyped + ", flexType=" + this.flexType + ", className=" + this.className + ")";
            }
        }

        @SuppressWarnings("all")
        public static FlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder builder() {
            return new FlexObjectsCluster.NonGroupedNode.NonGroupedNodeBuilder();
        }

        @SuppressWarnings("all")
        public FlexTyped getFlexTyped() {
            return this.flexTyped;
        }

        @SuppressWarnings("all")
        public FlexType getFlexType() {
            return this.flexType;
        }

        @SuppressWarnings("all")
        public String getClassName() {
            return this.className;
        }

        @SuppressWarnings("all")
        public void setFlexTyped(final FlexTyped flexTyped) {
            this.flexTyped = flexTyped;
        }

        @SuppressWarnings("all")
        public void setFlexType(final FlexType flexType) {
            this.flexType = flexType;
        }

        @SuppressWarnings("all")
        public void setClassName(final String className) {
            this.className = className;
        }

        @SuppressWarnings("all")
        public NonGroupedNode() {
        }

        @SuppressWarnings("all")
        public NonGroupedNode(final FlexTyped flexTyped, final FlexType flexType, final String className) {
            this.flexTyped = flexTyped;
            this.flexType = flexType;
            this.className = className;
        }
        //</editor-fold>
    }

    private Map<String, GroupedNode> groupedNodeMap = new HashMap<>();
    private List<NonGroupedNode> nonGroupedNodes = new ArrayList<>();

    public void addGroupedNode(final String group, final List<FlexTyped> flexTypedList, final String className) throws TCException {
        if (group == null || group.isEmpty() || flexTypedList == null) {
            return;
        }
        for (FlexTyped flexTyped : flexTypedList) {
            addGroupedNode(group, flexTyped, className);
        }
    }

    public void addGroupedNode(final String group, final FlexTyped flexTyped, final String className) throws TCException {
        if (!StringUtils.hasContent(group) || flexTyped == null || !StringUtils.hasContent(className)) {
            return;
        }
        if (groupedNodeMap.get(group) == null) {
            groupedNodeMap.put(group, new GroupedNode());
        }
        final GroupedNode gn = groupedNodeMap.get(group);
        gn.add(flexTyped, className);
    }

    public void addNonGroupedNode(final FlexTyped flexTyped, final String className) throws TCException {
        if (flexTyped == null) {
            return;
        }
        final NonGroupedNode nonGroupedNode = new NonGroupedNode();
        nonGroupedNode.add(flexTyped, className);
        nonGroupedNodes.add(nonGroupedNode);
    }

    public AttributeValue getValue(final PlaceHolder placeHolder) {
        if (placeHolder == null || placeHolder.getVar() == null || placeHolder.getFlexType() == null) {
            return null;
        }
        if (placeHolder.isGrouped()) {
            final GroupedNode gpNode = groupedNodeMap.values().stream().filter(groupedNode -> {
                return SparcIntegrationUtil.hasAttribute(placeHolder, groupedNode.getFlexType(), groupedNode.getClassName());
            }).findFirst().orElse(null);
            if (gpNode == null) {
                return null;
            }
            return gpNode.getValue(placeHolder);
        } else {
            final NonGroupedNode nonGpNode = nonGroupedNodes.stream().filter(nonGroupedNode -> {
                return SparcIntegrationUtil.hasAttribute(placeHolder, nonGroupedNode.getFlexType(), nonGroupedNode.getClassName());
            }).findFirst().orElse(null);
            if (nonGpNode == null) {
                return null;
            }
            return nonGpNode.getValue(placeHolder);
        }
    //<editor-fold defaultstate="collapsed" desc="delombok">
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class FlexObjectsClusterBuilder {
        @SuppressWarnings("all")
        private Map<String, GroupedNode> groupedNodeMap;
        @SuppressWarnings("all")
        private List<NonGroupedNode> nonGroupedNodes;

        @SuppressWarnings("all")
        FlexObjectsClusterBuilder() {
        }

        @SuppressWarnings("all")
        public FlexObjectsCluster.FlexObjectsClusterBuilder groupedNodeMap(final Map<String, GroupedNode> groupedNodeMap) {
            this.groupedNodeMap = groupedNodeMap;
            return this;
        }

        @SuppressWarnings("all")
        public FlexObjectsCluster.FlexObjectsClusterBuilder nonGroupedNodes(final List<NonGroupedNode> nonGroupedNodes) {
            this.nonGroupedNodes = nonGroupedNodes;
            return this;
        }

        @SuppressWarnings("all")
        public FlexObjectsCluster build() {
            return new FlexObjectsCluster(this.groupedNodeMap, this.nonGroupedNodes);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "FlexObjectsCluster.FlexObjectsClusterBuilder(groupedNodeMap=" + this.groupedNodeMap + ", nonGroupedNodes=" + this.nonGroupedNodes + ")";
        }
    //</editor-fold>
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static FlexObjectsCluster.FlexObjectsClusterBuilder builder() {
        return new FlexObjectsCluster.FlexObjectsClusterBuilder();
    }

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
    public FlexObjectsCluster() {
    }

    @SuppressWarnings("all")
    public FlexObjectsCluster(final Map<String, GroupedNode> groupedNodeMap, final List<NonGroupedNode> nonGroupedNodes) {
        this.groupedNodeMap = groupedNodeMap;
        this.nonGroupedNodes = nonGroupedNodes;
    }
    //</editor-fold>
}
