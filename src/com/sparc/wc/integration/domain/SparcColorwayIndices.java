package com.sparc.wc.integration.domain;

import com.sparc.wc.integration.util.SparcIntegrationUtil;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SparcColorwayIndices {
    private final Set<String> colorways = new HashSet<>();

    public void addColorways(final Collection<String> colorwayList) {
        if (colorwayList != null) {
            colorwayList.forEach(this::add);
        }
    }

    public void add(final String colorwayId) {
        if (colorwayId != null && !colorwayId.isEmpty()) {
            final String paddedNum = SparcIntegrationUtil.addPadding(colorwayId);
            this.colorways.add(paddedNum);
        }
    }

    public SparcGenericResponse genericResponse() {
        return SparcGenericResponse.builder().data(this).build();
    }

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public Set<String> getColorways() {
        return this.colorways;
    }

    @SuppressWarnings("all")
    public SparcColorwayIndices() {
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "SparcColorwayIndices(colorways=" + this.getColorways() + ")";
    }
    //</editor-fold>
}
