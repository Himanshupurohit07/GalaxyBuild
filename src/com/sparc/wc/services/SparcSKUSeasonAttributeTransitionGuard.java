package com.sparc.wc.services;

import com.lcs.wc.client.ClientContext;
import com.lcs.wc.season.LCSSKUSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.sparc.tc.util.StringUtils;
import com.sparc.wc.integration.util.SparcIntegrationUtil;
import com.sparc.wc.integration.util.SparcQueryUtil;
import org.apache.logging.log4j.Logger;
import wt.log4j.LogR;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SparcSKUSeasonAttributeTransitionGuard {

    private static final Logger LOGGER      = LogR.getLogger(SparcSKUSeasonAttributeTransitionGuard.class.getName());
    private static final String PATH_PREFIX = "transition.guard";

    private static final String                                 SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME                = LCSProperties.get(PATH_PREFIX + ".sku.season.plm.colorway.status.internal.name", "scPLMPolorwayStatus");
    private static final Set<String>                            SKU_SEASON_PLM_COLORWAY_STATUS_TERMINAL_STATE_VALUES        = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".sku.season.plm.colorway.status.terminal.state.values", "scProduction, scDropped").split(",", -1)).map(token -> token.trim()).collect(Collectors.toSet());
    private static final Set<String>                            SKU_SEASON_PLM_COLORWAY_STATUS_RESTRICTED_TRANSITION_VALUES = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".sku.season.plm.colorway.status.restricted.transition.values", "scInitial, scDevelopment").split(",", -1)).map(token -> token.trim()).collect(Collectors.toSet());
    private static final String                                 SEASON_BRAND_INTERNAL_NAME                                  = LCSProperties.get(PATH_PREFIX + ".season.brand.internal.name", "brand");
    private static final String                                 LUCKY_BRAND_VALUE                                           = LCSProperties.get(PATH_PREFIX + ".lucky.brand.value", "Lucky");
    private static final Set<String>                            SKU_SEASON_PLM_COLORWAY_STATUS_TRANSITION_ALLOWED_GROUPS    = Arrays.stream(LCSProperties.get(PATH_PREFIX + ".sku.season.plm.colorway.status.transition.allowed.groups", "ADMINISTRATORS, LUCKY_SPARC_OPERATIONS").split(",", -1)).map(token -> token.trim()).collect(Collectors.toSet());
    private static       SparcSKUSeasonAttributeTransitionGuard INSTANCE;

    private SparcSKUSeasonAttributeTransitionGuard() {

    }

    public static SparcSKUSeasonAttributeTransitionGuard getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SparcSKUSeasonAttributeTransitionGuard();
        }
        return INSTANCE;
    }

    public void validateTransition(final LCSSKUSeasonLink link) throws LCSException {
        LOGGER.debug("Validating transition");
        final Set<String> restrictedTransitions = getRestrictedColorwayStatusTransitions(link);
        if (restrictedTransitions.isEmpty()) {
            LOGGER.debug("No restricted transitions found");
            return;
        }
        final LCSSKUSeasonLink currentLink = getCurrentLink(link);
        if (currentLink == null) {
            LOGGER.warn("Current link is found null, bypassing transition check");
            return;
        }
        final String storedValue  = (String) SparcIntegrationUtil.getValueFrom(currentLink, SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME);
        final String currentValue = (String) SparcIntegrationUtil.getValueFrom(link, SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME);
        LOGGER.debug("Stored status:" + storedValue + ", current value:" + currentValue);
		if((link.getCopiedFrom() != null || link.getCarriedOverFrom() != null) && link.getEffectSequence() < 2){
            return;
        }
		
        if (storedValue != null && SKU_SEASON_PLM_COLORWAY_STATUS_TERMINAL_STATE_VALUES.contains(storedValue)) {
            if (currentValue != null && restrictedTransitions.contains(currentValue)) {
                throw new LCSException("Cannot change PLM Colorway Status with current login");
            }
        }
    }

    public String getCurrentColorwayStatus(final LCSSKUSeasonLink link) {
        if (link == null) {
            return "";
        }
        return (String) SparcIntegrationUtil.getValueFrom(link, SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME, true);
    }

    public Set<String> getRestrictedColorwayStatusTransitions(final LCSSKUSeasonLink link) {
        if (link == null) {
            LOGGER.debug("Cannot extract transitions, as the link is found as null");
            return new HashSet<>();
        }
        if (!isLuckySeason(link)) {
            LOGGER.debug("Bypassing colorway status transitions, as the current season is not lucky");
            return new HashSet<>();
        }
        final LCSSKUSeasonLink currentLink = getCurrentLink(link);
        if (currentLink == null) {
            LOGGER.error("Cannot get current colorway-season link");
            return new HashSet<>();
        }
        final String storedValue = (String) SparcIntegrationUtil.getValueFrom(currentLink, SKU_SEASON_PLM_COLORWAY_STATUS_INTERNAL_NAME);
        if (!SKU_SEASON_PLM_COLORWAY_STATUS_TERMINAL_STATE_VALUES.contains(storedValue)) {
            return new HashSet<>();
        }
        final Set<String> clientGroups = getClientGroups();
        LOGGER.debug("Client groups:" + clientGroups + ", configured groups:" + SKU_SEASON_PLM_COLORWAY_STATUS_TRANSITION_ALLOWED_GROUPS);
        clientGroups.retainAll(SKU_SEASON_PLM_COLORWAY_STATUS_TRANSITION_ALLOWED_GROUPS);
        LOGGER.debug("Allowed client groups:" + clientGroups);
        if (clientGroups.isEmpty() && !SKU_SEASON_PLM_COLORWAY_STATUS_TRANSITION_ALLOWED_GROUPS.isEmpty()) {
            return SKU_SEASON_PLM_COLORWAY_STATUS_RESTRICTED_TRANSITION_VALUES;
        }
        return new HashSet<>();
    }

    private Set<String> getClientGroups() {
        try {
            LOGGER.debug("Current logged user:" + ClientContext.getContext().getUserName());
            return ClientContext.getContext().getGroups().stream().collect(Collectors.toSet());
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching client groups, error:" + e.getMessage());
            return new HashSet<>();
        }

    }

    private boolean isLuckySeason(final LCSSKUSeasonLink link) {
        final LCSSeasonMaster seasonMaster = link.getSeasonMaster();
        final LCSSeason       season       = SparcQueryUtil.findLatestIterationOf(seasonMaster);
        if (season == null) {
            return false;
        }
        final String brand = (String) SparcIntegrationUtil.getValueFrom(season, SEASON_BRAND_INTERNAL_NAME);
        LOGGER.debug("Brand:" + brand);
        return StringUtils.hasContent(brand) && brand.equals(LUCKY_BRAND_VALUE);
    }

    private LCSSKUSeasonLink getCurrentLink(final LCSSKUSeasonLink link) {
        try {
            if (link.getPersistInfo().getObjectIdentifier() != null) {
                return SparcQueryUtil.findObjectById(FormatHelper.getObjectId(link));
            } else {
                return (LCSSKUSeasonLink) LCSSeasonQuery.getPriorSeasonProductLink(link);
            }
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching current version of SKU-Season Link, error:" + e.getMessage());
        }
        return null;
    }
}
