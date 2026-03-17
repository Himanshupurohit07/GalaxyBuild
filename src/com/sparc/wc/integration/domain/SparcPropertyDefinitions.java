package com.sparc.wc.integration.domain;

import com.sparc.wc.integration.util.SparcIntegrationUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SparcPropertyDefinitions {

    public enum Hierarchy {
        STYLE, FC, CAP, S4;
    }


    public static class Property {
        private String flexInternalName;
        private String aliasName;
        private Hierarchy hierarchy;
        private boolean allowBlank;
        private boolean lockAttr;


        @SuppressWarnings("all")
        public static class PropertyBuilder {
            @SuppressWarnings("all")
            private String flexInternalName;
            @SuppressWarnings("all")
            private String aliasName;
            @SuppressWarnings("all")
            private Hierarchy hierarchy;
            @SuppressWarnings("all")
            private boolean allowBlank;
            @SuppressWarnings("all")
            private boolean lockAttr;

            @SuppressWarnings("all")
            PropertyBuilder() {
            }

            @SuppressWarnings("all")
            public SparcPropertyDefinitions.Property.PropertyBuilder flexInternalName(final String flexInternalName) {
                this.flexInternalName = flexInternalName;
                return this;
            }

            @SuppressWarnings("all")
            public SparcPropertyDefinitions.Property.PropertyBuilder aliasName(final String aliasName) {
                this.aliasName = aliasName;
                return this;
            }

            @SuppressWarnings("all")
            public SparcPropertyDefinitions.Property.PropertyBuilder hierarchy(final Hierarchy hierarchy) {
                this.hierarchy = hierarchy;
                return this;
            }

            @SuppressWarnings("all")
            public SparcPropertyDefinitions.Property.PropertyBuilder allowBlank(final boolean allowBlank) {
                this.allowBlank = allowBlank;
                return this;
            }

            @SuppressWarnings("all")
            public SparcPropertyDefinitions.Property.PropertyBuilder lockAttr(final boolean lockAttr) {
                this.lockAttr = lockAttr;
                return this;
            }

            @SuppressWarnings("all")
            public SparcPropertyDefinitions.Property build() {
                return new SparcPropertyDefinitions.Property(this.flexInternalName, this.aliasName, this.hierarchy, this.allowBlank, this.lockAttr);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "SparcPropertyDefinitions.Property.PropertyBuilder(flexInternalName=" + this.flexInternalName + ", aliasName=" + this.aliasName + ", hierarchy=" + this.hierarchy + ", allowBlank=" + this.allowBlank + ", lockAttr=" + this.lockAttr + ")";
            }
        }

        @SuppressWarnings("all")
        public static SparcPropertyDefinitions.Property.PropertyBuilder builder() {
            return new SparcPropertyDefinitions.Property.PropertyBuilder();
        }

        @SuppressWarnings("all")
        public String getFlexInternalName() {
            return this.flexInternalName;
        }

        @SuppressWarnings("all")
        public String getAliasName() {
            return this.aliasName;
        }

        @SuppressWarnings("all")
        public Hierarchy getHierarchy() {
            return this.hierarchy;
        }

        @SuppressWarnings("all")
        public boolean isAllowBlank() {
            return this.allowBlank;
        }

        @SuppressWarnings("all")
        public boolean isLockAttr() {
            return this.lockAttr;
        }

        @SuppressWarnings("all")
        public void setFlexInternalName(final String flexInternalName) {
            this.flexInternalName = flexInternalName;
        }

        @SuppressWarnings("all")
        public void setAliasName(final String aliasName) {
            this.aliasName = aliasName;
        }

        @SuppressWarnings("all")
        public void setHierarchy(final Hierarchy hierarchy) {
            this.hierarchy = hierarchy;
        }

        @SuppressWarnings("all")
        public void setAllowBlank(final boolean allowBlank) {
            this.allowBlank = allowBlank;
        }

        @SuppressWarnings("all")
        public void setLockAttr(final boolean lockAttr) {
            this.lockAttr = lockAttr;
        }

        @SuppressWarnings("all")
        public Property() {
        }

        @SuppressWarnings("all")
        public Property(final String flexInternalName, final String aliasName, final Hierarchy hierarchy, final boolean allowBlank, final boolean lockAttr) {
            this.flexInternalName = flexInternalName;
            this.aliasName = aliasName;
            this.hierarchy = hierarchy;
            this.allowBlank = allowBlank;
            this.lockAttr = lockAttr;
        }
    }

    private Map<String, Property> properties = new HashMap<>();

    public static SparcPropertyDefinitions load(final String definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return new SparcPropertyDefinitions();
        }
        final List<Property> properties = Arrays.stream(definitions.split(",", -1)).map(propDef -> propDef.trim()).map(propDef -> {
            final String[] propertyDefinitions = propDef.split("\\|", -1);
            if (propertyDefinitions.length != 5) {
                return null;
            }
            final Hierarchy hierarchy = SparcIntegrationUtil.getEnumFromString(Hierarchy.class, propertyDefinitions[2], Hierarchy.STYLE);
            final boolean allowBlanks = getBoolean(propertyDefinitions[3]);
            final boolean lockAttr = getBoolean(propertyDefinitions[4]);
            return Property.builder().flexInternalName(propertyDefinitions[0].trim()).aliasName(propertyDefinitions[1].trim()).hierarchy(hierarchy).allowBlank(allowBlanks).lockAttr(lockAttr).build();
        }).filter(prop -> prop != null).collect(Collectors.toList());
        return load(properties);
    }

    private static boolean getBoolean(final String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.equalsIgnoreCase("true");
    }

    public static SparcPropertyDefinitions load(final List<Property> properties) {
        final SparcPropertyDefinitions definitions = new SparcPropertyDefinitions();
        if (properties != null) {
            properties.forEach(prop -> definitions.add(prop));
        }
        properties.forEach(prop -> definitions.add(prop));
        return definitions;
    }

    public void add(final Property property) {
        if (property.getFlexInternalName() == null || property.getFlexInternalName().isEmpty()) {
            return;
        }
        properties.put(property.getFlexInternalName(), property);
    }

    public Set<String> getNonBlankAttrs() {
        if (this.properties == null || this.properties.isEmpty()) {
            return new HashSet<>();
        }
        return this.properties.entrySet().stream().filter(entry -> entry.getValue() != null).filter(entry -> !entry.getValue().isAllowBlank()).map(entry -> entry.getValue().getFlexInternalName()).collect(Collectors.toSet());
    }

    public Set<String> getLockedAttrs() {
        if (this.properties == null || this.properties.isEmpty()) {
            return new HashSet<>();
        }
        return this.properties.entrySet().stream().filter(entry -> entry.getValue() != null).filter(entry -> entry.getValue().isLockAttr()).map(entry -> entry.getValue().getFlexInternalName()).collect(Collectors.toSet());
    }

    public Set<String> getAttrsFromHierarchy(final Hierarchy hierarchy) {
        if (this.properties == null || this.properties.isEmpty() || hierarchy == null) {
            return new HashSet<>();
        }
        return this.properties.entrySet().stream().filter(entry -> entry.getValue() != null).filter(entry -> entry.getValue().getHierarchy() == hierarchy).map(entry -> entry.getValue().getFlexInternalName()).collect(Collectors.toSet());
    }

    public Set<String> getAttrsFromHierarchy() {
        if (this.properties == null || this.properties.isEmpty() ) {
            return new HashSet<>();
        }
        return this.properties.entrySet().stream().filter(entry -> entry.getValue() != null).filter(entry -> entry.getValue() != null).map(entry -> entry.getValue().getFlexInternalName()).collect(Collectors.toSet());
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Map<String, Property> getProperties() {
        return this.properties;
    }

    @SuppressWarnings("all")
    public void setProperties(final Map<String, Property> properties) {
        this.properties = properties;
    }

    @SuppressWarnings("all")
    public SparcPropertyDefinitions() {
    }
    //</editor-fold>
}
