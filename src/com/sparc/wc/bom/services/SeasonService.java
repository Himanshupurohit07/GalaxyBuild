package com.sparc.wc.bom.services;

import com.lcs.wc.season.LCSSeason;

import java.util.Set;

public interface SeasonService {

    LCSSeason getSeason(String path, String name);

    Set<LCSSeason> getReebokSeasons(String flexTypePath);


}
