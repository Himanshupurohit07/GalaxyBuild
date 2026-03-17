package com.sparc.wc.integration.lucky.builders;

import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.sparc.wc.integration.aero.repository.LCSTableColumns;
import com.sparc.wc.integration.aero.repository.LCSTables;

import wt.util.WTException;

import static com.sparc.wc.integration.constants.SparcIntegrationConstants.*;


public class LuckyColorwaySeasonLinkQuery {

    private Long colorwayNumber;

    public LuckyColorwaySeasonLinkQuery(Long colorwayNumber) {
        this.colorwayNumber = colorwayNumber;
    }

    /**
     * Builds the SELECT portion of the query into the prepared statement.
     * @param pqs The prepared statement to be updated.
     * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
     */
    private void buildSelect(PreparedQueryStatement pqs) throws WTException {
        pqs.setDistinct(true);
        pqs.appendSelectColumn(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA2A2.name());
    }

    /**
     * Builds the FROM portion of the query into the prepared statement.
     * @param pqs The prepared statement to be updated.
     */
    private void buildFrom(PreparedQueryStatement pqs) {
        pqs.appendFromTable(LCSTables.LCSSEASON.name());
        pqs.appendFromTable(LCSTables.LCSSKU.name());
        pqs.appendFromTable(LCSTables.LCSSKUSEASONLINK.name());
    }

    /**
     * Builds the table JOINS portion of the query into the prepared statement.
     * @param pqs The prepared statement to be updated.
     */
    private void buildJoins(PreparedQueryStatement pqs) {
        //LCSSKUSEASONLINK.IDA3B5 <-> LCSSEASON.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.IDA3B5.name(),
                LCSTables.LCSSEASON.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
        //LCSSKUSEASONLINK.SKUMASTERID <-> LCSSKU.IDA3MASTERREFERENCE
        pqs.appendJoin(LCSTables.LCSSKUSEASONLINK.name(), LCSTableColumns.SKUMASTERID.name(),
                LCSTables.LCSSKU.name(), LCSTableColumns.IDA3MASTERREFERENCE.name());
    }

    /**
     * Builds the WHERE portion of the query into the prepared statement.
     * @param pqs The prepared statement to be updated.
     * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
     */
    private void buildWhere(PreparedQueryStatement pqs) throws WTException {
        buildWhereColorwayNumber(pqs);
        buildWhereLuckySeason(pqs);
        buildWhereColorwaySeasonLinkType(pqs);
        buildWhereActiveColorwaySeasonLinkType(pqs);
        buildWhereLatestFlexObjectIteration(pqs);
        buildWhereLatestFlexObjectVersion(pqs);
    }

    /**
     * Builds the WHERE criteria for the Colorway Number.
     * @param pqs The prepared statement to be updated.
     * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
     */
    private void buildWhereColorwayNumber(PreparedQueryStatement pqs) throws WTException {

        final FlexType colorwayFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_PRODUCT_FLEX_PATH);
        final String colorwayNumColName = colorwayFlexType.getAttribute(AERO_COLORWAY_NUMBER_ATTR).getColumnName();

        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(),
                colorwayNumColName,
                ((colorwayNumber != null) ? colorwayNumber.toString() : null),
                Criteria.EQUALS));

    }

    /**
     * Builds the WHERE criteria to include only valid Aero Seasons.<br>
     * Valid Aero seasons are: Fall, Holiday, Spring, Summer or BTS.
     * @param pqs The prepared statement to be updated.
     * @throws WTException If an error occurs while using the flex api to retrieve column metadata info from PLM.
     */
    private void buildWhereLuckySeason(PreparedQueryStatement pqs) throws WTException {

        final FlexType seasonFlexType = FlexTypeCache.getFlexTypeFromPath(LUCKY_SEASON_FLEX_PATH);

        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(),
                LCSTableColumns.FLEXTYPEIDPATH.name(),
                seasonFlexType.getIdPath() + "%",
                Criteria.LIKE));

        final String developmentSeasonColName = seasonFlexType.getAttribute(AERO_SEASON_TYPE_ATTR).getColumnName();

        if (!LUCKY_DEVELOPMENT_SEASONS.isEmpty()) {
            pqs.appendAndIfNeeded();
            pqs.appendInCriteria(LCSTables.LCSSEASON.name(), developmentSeasonColName, LUCKY_DEVELOPMENT_SEASONS);
        }

    }

    /**
     * Builds the WHERE criteria to include SKU type season links.<br>
     * @param pqs The prepared statement to be updated.
     */
    private void buildWhereColorwaySeasonLinkType(PreparedQueryStatement pqs) {

        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(),
                LCSTableColumns.SEASONLINKTYPE.name(),
                FLEX_SKU_SEASON_LINK_TYPE,
                Criteria.EQUALS));

    }

    /**
     * Builds the WHERE criteria to include active colorway season links.<br>
     * @param pqs The prepared statement to be updated.
     */
    private void buildWhereActiveColorwaySeasonLinkType(PreparedQueryStatement pqs) {

        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(),
                LCSTableColumns.SEASONREMOVED.name(),
                FLEX_SEASON_REMOVED_NO,
                Criteria.EQUALS));

    }

    /**
     * Builds the WHERE criteria to include latest flex object iteration entries for:
     * Sourcing Config, Season, Product, Sourcing to Season Link, Cost Sheet, Colorway, Colorway-Season Link, Product-Season Link and
     * Source-to-Season-Link-Master.
     * @param pqs The prepared statement to be updated.
     */
    private void buildWhereLatestFlexObjectIteration(PreparedQueryStatement pqs) {

        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(),
                LCSTableColumns.LATESTITERATIONINFO.name(),
                LATEST_FLEX_OBJECT_ITERATION,
                Criteria.EQUALS));
        pqs.appendAnd();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(),
                LCSTableColumns.LATESTITERATIONINFO.name(),
                LATEST_FLEX_OBJECT_ITERATION,
                Criteria.EQUALS));
        pqs.appendAnd();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKUSEASONLINK.name(),
                LCSTableColumns.EFFECTLATEST.name(),
                FLEX_OBJECT_EFFECT_LATEST_YES,
                Criteria.EQUALS));
    }

    /**
     * Builds the WHERE criteria to include latest flex object version entries.
     * @param pqs The prepared statement to be updated.
     */
    private void buildWhereLatestFlexObjectVersion(PreparedQueryStatement pqs) {

        pqs.appendAndIfNeeded();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSKU.name(),
                LCSTableColumns.VERSIONIDA2VERSIONINFO.name(),
                LATEST_FLEX_OBJECT_VERSION,
                Criteria.EQUALS));
        pqs.appendAnd();
        pqs.appendCriteria(new Criteria(LCSTables.LCSSEASON.name(),
                LCSTableColumns.VERSIONIDA2VERSIONINFO.name(),
                LATEST_FLEX_OBJECT_VERSION,
                Criteria.EQUALS));

    }

    /**
     * Builds the prepared query statement to retrieve a the colorway season link identifier for the given colorway number, season type and year.
     * @return The prepared query statament built.
     * @throws WTException If an error occurs while executing the flex api during the building of the various portions of the prepared query statement.
     */
    public PreparedQueryStatement build() throws WTException {

        PreparedQueryStatement pqs = new PreparedQueryStatement();

        buildSelect(pqs);
        buildFrom(pqs);
        buildJoins(pqs);
        buildWhere(pqs);

        return pqs;
    }
}
