package com.sparc.wc.tc.domain;

public class ObjectsCluster {
    private FlexObjectsCluster flexObjectsCluster = new FlexObjectsCluster();
    private NonFlexObjectsCluster nonFlexObjectsCluster = new NonFlexObjectsCluster();

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public FlexObjectsCluster getFlexObjectsCluster() {
        return this.flexObjectsCluster;
    }

    @SuppressWarnings("all")
    public NonFlexObjectsCluster getNonFlexObjectsCluster() {
        return this.nonFlexObjectsCluster;
    }

    @SuppressWarnings("all")
    public void setFlexObjectsCluster(final FlexObjectsCluster flexObjectsCluster) {
        this.flexObjectsCluster = flexObjectsCluster;
    }

    @SuppressWarnings("all")
    public void setNonFlexObjectsCluster(final NonFlexObjectsCluster nonFlexObjectsCluster) {
        this.nonFlexObjectsCluster = nonFlexObjectsCluster;
    }

    @SuppressWarnings("all")
    public ObjectsCluster() {
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "ObjectsCluster(flexObjectsCluster=" + this.getFlexObjectsCluster() + ", nonFlexObjectsCluster=" + this.getNonFlexObjectsCluster() + ")";
    }
    //</editor-fold>
}
