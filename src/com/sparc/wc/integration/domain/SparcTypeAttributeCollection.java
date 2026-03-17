package com.sparc.wc.integration.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SparcTypeAttributeCollection {

    public static class Attribute {
        private String alias;
        private String internalName;
        private String scope;
        private String level;
        private Map<String, String> params = new HashMap<>();


        //<editor-fold defaultstate="collapsed" desc="delombok">
        @SuppressWarnings("all")
        public static class AttributeBuilder {
            @SuppressWarnings("all")
            private String alias;
            @SuppressWarnings("all")
            private String internalName;
            @SuppressWarnings("all")
            private String scope;
            @SuppressWarnings("all")
            private String level;
            @SuppressWarnings("all")
            private Map<String, String> params;

            @SuppressWarnings("all")
            AttributeBuilder() {
            }

            @SuppressWarnings("all")
            public SparcTypeAttributeCollection.Attribute.AttributeBuilder alias(final String alias) {
                this.alias = alias;
                return this;
            }

            @SuppressWarnings("all")
            public SparcTypeAttributeCollection.Attribute.AttributeBuilder internalName(final String internalName) {
                this.internalName = internalName;
                return this;
            }

            @SuppressWarnings("all")
            public SparcTypeAttributeCollection.Attribute.AttributeBuilder scope(final String scope) {
                this.scope = scope;
                return this;
            }

            @SuppressWarnings("all")
            public SparcTypeAttributeCollection.Attribute.AttributeBuilder level(final String level) {
                this.level = level;
                return this;
            }

            @SuppressWarnings("all")
            public SparcTypeAttributeCollection.Attribute.AttributeBuilder params(final Map<String, String> params) {
                this.params = params;
                return this;
            }

            @SuppressWarnings("all")
            public SparcTypeAttributeCollection.Attribute build() {
                return new SparcTypeAttributeCollection.Attribute(this.alias, this.internalName, this.scope, this.level, this.params);
            }

            @Override
            @SuppressWarnings("all")
            public String toString() {
                return "SparcTypeAttributeCollection.Attribute.AttributeBuilder(alias=" + this.alias + ", internalName=" + this.internalName + ", scope=" + this.scope + ", level=" + this.level + ", params=" + this.params + ")";
            }
        }

        @SuppressWarnings("all")
        public static SparcTypeAttributeCollection.Attribute.AttributeBuilder builder() {
            return new SparcTypeAttributeCollection.Attribute.AttributeBuilder();
        }

        @SuppressWarnings("all")
        public String getAlias() {
            return this.alias;
        }

        @SuppressWarnings("all")
        public String getInternalName() {
            return this.internalName;
        }

        @SuppressWarnings("all")
        public String getScope() {
            return this.scope;
        }

        @SuppressWarnings("all")
        public String getLevel() {
            return this.level;
        }

        @SuppressWarnings("all")
        public Map<String, String> getParams() {
            return this.params;
        }

        @SuppressWarnings("all")
        public void setAlias(final String alias) {
            this.alias = alias;
        }

        @SuppressWarnings("all")
        public void setInternalName(final String internalName) {
            this.internalName = internalName;
        }

        @SuppressWarnings("all")
        public void setScope(final String scope) {
            this.scope = scope;
        }

        @SuppressWarnings("all")
        public void setLevel(final String level) {
            this.level = level;
        }

        @SuppressWarnings("all")
        public void setParams(final Map<String, String> params) {
            this.params = params;
        }

        @SuppressWarnings("all")
        public Attribute() {
        }

        @SuppressWarnings("all")
        public Attribute(final String alias, final String internalName, final String scope, final String level, final Map<String, String> params) {
            this.alias = alias;
            this.internalName = internalName;
            this.scope = scope;
            this.level = level;
            this.params = params;
        }
        //</editor-fold>
    }

    private List<Attribute> attributes = new ArrayList<>();
    private Map<String, Attribute> attributeCache = new HashMap<>();

    public Attribute getAttributeByAlias(final String alias) {
        if (alias == null || alias.isEmpty()) {
            return null;
        }
        return this.attributeCache.get(alias);
    }

    public List<Attribute> getAttributesByScopeByLevel(final String scope, final String level) {
        if (this.attributes.isEmpty()) {
            return new ArrayList<>();
        }
        return this.attributes.stream().filter(attr -> {
            final boolean scopeMatch = scope != null && !scope.isEmpty() ? scope.equals(attr.getScope()) : false;
            final boolean levelMatch = level != null && !level.isEmpty() ? level.equals(attr.getLevel()) : false;
            if (scope != null && level != null) {
                return scopeMatch && levelMatch;
            }
            return scopeMatch || levelMatch;
        }).collect(Collectors.toList());
    }

    public void buildAttributeCache() {
        if (attributes.isEmpty()) {
            return;
        }
        if (attributeCache == null) {
            attributeCache = new HashMap<>();
        }
        attributes.forEach(attr -> {
            attributeCache.put(attr.getAlias(), attr);
        });
    }

    public static SparcTypeAttributeCollection load(final String rawString) {
        if (rawString == null || rawString.isEmpty()) {
            return new SparcTypeAttributeCollection();
        }
        final List<Attribute> attributes = Arrays.stream(rawString.split(",", -1)).filter(attrDef -> attrDef != null).map(attrDef -> attrDef.trim()).filter(attrDef -> !attrDef.isEmpty()).map(attrDef -> {
            final String[] tokens = attrDef.split("\\|", -1);
            if (tokens == null || tokens.length < 4) {
                return null;
            }
            final Attribute attr = Attribute.builder().alias(tokens[0].trim()).internalName(tokens[1].trim()).scope(tokens[2].trim()).level(tokens[3].trim()).build();
            if (tokens.length > 4) {
                attr.setParams(resolveParams(tokens));
            }
            return attr;
        }).filter(attr -> attr != null).collect(Collectors.toList());
        final SparcTypeAttributeCollection attributeCollection = SparcTypeAttributeCollection.builder().attributes(attributes).build();
        attributeCollection.buildAttributeCache();
        return attributeCollection;
    }

    private static Map<String, String> resolveParams(final String[] tokens) {
        final Map<String, String> params = new HashMap<>();
        for (int iterator = 4; iterator < tokens.length; iterator++) {
            final String[] keyValuePair = tokens[iterator].split("=", -1);
            if (keyValuePair.length == 2) {
                params.put(keyValuePair[0].trim(), keyValuePair[1].trim());
            }
        }
        return params;
    }


    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public static class SparcTypeAttributeCollectionBuilder {
        @SuppressWarnings("all")
        private List<Attribute> attributes;
        @SuppressWarnings("all")
        private Map<String, Attribute> attributeCache;

        @SuppressWarnings("all")
        SparcTypeAttributeCollectionBuilder() {
        }

        @SuppressWarnings("all")
        public SparcTypeAttributeCollection.SparcTypeAttributeCollectionBuilder attributes(final List<Attribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        @SuppressWarnings("all")
        public SparcTypeAttributeCollection.SparcTypeAttributeCollectionBuilder attributeCache(final Map<String, Attribute> attributeCache) {
            this.attributeCache = attributeCache;
            return this;
        }

        @SuppressWarnings("all")
        public SparcTypeAttributeCollection build() {
            return new SparcTypeAttributeCollection(this.attributes, this.attributeCache);
        }

        @Override
        @SuppressWarnings("all")
        public String toString() {
            return "SparcTypeAttributeCollection.SparcTypeAttributeCollectionBuilder(attributes=" + this.attributes + ", attributeCache=" + this.attributeCache + ")";
        }
    }

    @SuppressWarnings("all")
    public static SparcTypeAttributeCollection.SparcTypeAttributeCollectionBuilder builder() {
        return new SparcTypeAttributeCollection.SparcTypeAttributeCollectionBuilder();
    }

    @SuppressWarnings("all")
    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    @SuppressWarnings("all")
    public Map<String, Attribute> getAttributeCache() {
        return this.attributeCache;
    }

    @SuppressWarnings("all")
    public void setAttributes(final List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @SuppressWarnings("all")
    public void setAttributeCache(final Map<String, Attribute> attributeCache) {
        this.attributeCache = attributeCache;
    }

    @SuppressWarnings("all")
    public SparcTypeAttributeCollection() {
    }

    @SuppressWarnings("all")
    public SparcTypeAttributeCollection(final List<Attribute> attributes, final Map<String, Attribute> attributeCache) {
        this.attributes = attributes;
        this.attributeCache = attributeCache;
    }
    //</editor-fold>
}
