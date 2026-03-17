package com.sparc.tc.domain;

import com.sparc.tc.domain.GroupedPage;
import com.sparc.tc.domain.Page;

public class CorrespondenceData {
    private GroupedPage groupedPage = new GroupedPage();
    private Page        page        = new Page();

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public GroupedPage getGroupedPage() {
        return this.groupedPage;
    }

    @SuppressWarnings("all")
    public Page getPage() {
        return this.page;
    }

    @SuppressWarnings("all")
    public void setGroupedPage(final GroupedPage groupedPage) {
        this.groupedPage = groupedPage;
    }

    @SuppressWarnings("all")
    public void setPage(final Page page) {
        this.page = page;
    }

    @SuppressWarnings("all")
    public CorrespondenceData() {
    }
    //</editor-fold>
}
