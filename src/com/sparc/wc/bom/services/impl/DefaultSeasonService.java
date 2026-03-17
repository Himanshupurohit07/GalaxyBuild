package com.sparc.wc.bom.services.impl;

import com.lcs.wc.season.LCSSeason;
import com.sparc.wc.bom.services.SeasonService;

import com.sparc.wc.bom.helper.SparcBomHelper;

import java.util.Set;

public class DefaultSeasonService implements SeasonService {

    @Override
    public LCSSeason getSeason(String path, String name) {
        return SparcBomHelper.getSeason(path, name);
    }

    @Override
    public Set<LCSSeason> getReebokSeasons(String typePath) {
        return SparcBomHelper.getReebokSeasons(typePath);
    }
}
