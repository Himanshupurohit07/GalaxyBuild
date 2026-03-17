package com.sparc.wc.plugins;

import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.util.LCSException;
import com.sparc.wc.services.SparcSKUSeasonAttributeTransitionGuard;
import org.apache.logging.log4j.Logger;
import wt.fc.WTObject;
import wt.log4j.LogR;

public final class SparcAttributesTransitionGuard {

    private static final Logger LOGGER = LogR.getLogger(SparcAttributesTransitionGuard.class.getName());

    public static void handleTransition(final WTObject wtObject) throws LCSException {
        if (wtObject == null) {
            return;
        }
        LOGGER.debug("Handling attribute transition");
        if (wtObject instanceof LCSSKUSeasonLink) {
            final LCSSKUSeasonLink link = (LCSSKUSeasonLink) wtObject;
            if (!link.isEffectLatest()) {
                return;
            }
            SparcSKUSeasonAttributeTransitionGuard.getInstance().validateTransition(link);
        }
    }

}
