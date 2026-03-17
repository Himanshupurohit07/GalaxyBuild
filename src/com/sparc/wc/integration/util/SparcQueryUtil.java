package com.sparc.wc.integration.util;

import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.VersionHelper;
import org.apache.logging.log4j.Logger;
import wt.enterprise.Master;
import wt.log4j.LogR;
import wt.vc.Mastered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SparcQueryUtil {

    private static final Logger LOGGER = LogR.getLogger(SparcQueryUtil.class.getName());

    private SparcQueryUtil() {

    }

    public static <T> T findObjectById(final String oid) {
        try {
            return (T) LCSQuery.findObjectById(oid, false);
        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

    public static <T> T findObjectById(final String oid, final boolean cache) {
        try {
            return (T) LCSQuery.findObjectById(oid, cache);
        } catch (Exception e) {
            //do nothing
        }
        return null;
    }

    public static <T> List<T> findLatestIterationsOf(final Collection<?> masters) {
        if (masters == null) {
            return new ArrayList<T>();
        }
        return masters.stream().map(master -> {
            try {
                return (T) VersionHelper.latestIterationOf((Mastered) master);
            } catch (Exception e) {
                // do nothing
            }
            return null;
        }).filter(version -> version != null).collect(Collectors.toList());
    }

    public static <T> T findLatestIterationOf(final Master master) {
        if (master == null) {
            return null;
        }

        try {
            return (T) VersionHelper.latestIterationOf(master);
        } catch (Exception e) {
            // do nothing
        }
        return null;

    }

    public static SearchResults runQuery(final PreparedQueryStatement query) {
        if (query == null) {
            return null;
        }
        try {
            return LCSQuery.runDirectQuery(query);
        } catch (Exception e) {
            LOGGER.error("Encountered an error while fetching query results, error:" + e.getMessage());
            return null;
        }
    }
}
