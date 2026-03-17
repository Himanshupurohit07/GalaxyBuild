package com.sparc.wc.specification;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lcs.wc.document.LCSDocumentClientModel;
import com.lcs.wc.specification.TechPackGenerationLogEntry;
import com.lcs.wc.specification.TechPackGenerationLogEntryUtility;
import com.ptc.core.common.util.Pair;
import com.sparc.wc.product.SPARCProductLogic;

import wt.pom.Transaction;


public class SPARCAsyncTPGenService {
  private static final String CLASSNAME = SPARCAsyncTPGenService.class.getName();
  
  private static final Logger LOGGER = LogManager.getLogger(CLASSNAME);
  
  public static void generateTPAsynchronous(String timeToLive, String specId, String productId, String specPages, Map criteria, String availDocs, String logEntryOID) {
		TechPackGenerationLogEntry logEntry = TechPackGenerationLogEntryUtility.find(logEntryOID);
		if (logEntry != null)
			try {
				logEntry = TechPackGenerationLogEntryUtility.toWorkInProgress(logEntry);
			} catch (Exception e) {
				LOGGER.error("Could not update log entry", e);
			}
		Transaction tr = null;
		Exception tpGenException = null;
		try {
			tr = new Transaction();
			tr.start();
			SPARCFlexSpecLogic logic = new SPARCFlexSpecLogic();
			Pair<String, LCSDocumentClientModel> tpResult = logic.asyncGenerateTechPackImpl(timeToLive, specId,
					productId, specPages, criteria, availDocs);
			tr.commit();
			tr = null;
			if (logEntry != null)
				TechPackGenerationLogEntryUtility.toCompleteSuccess(logEntry, tpResult.second);
		} catch (Exception e) {
			LOGGER.error("generateTPAsynchronous Error:  see error in MethodServer.log:  ", e);
			tpGenException = e;
		} finally {
			if (tr != null)
				tr.rollback();
		}
		if (tpGenException != null && logEntry != null)
			try {
				TechPackGenerationLogEntryUtility.toCompleteFailure(logEntry);
			} catch (Exception e) {
				LOGGER.error("Could not update log entry", e);
			}
	}
  
  public static void asyncCreatePDFSpecifications(Collection specIds, Map params) {
    Transaction tr = null;
    try {
      tr = new Transaction();
      tr.start();
      SPARCProductLogic logic = new SPARCProductLogic();
      logic.createPDFSpecifications(specIds, params, true);
      tr.commit();
      tr = null;
    } catch (Throwable t) {
      t.printStackTrace();
      System.out
        .println("asyncCreatePDFSpecifications Error:  see error in MethodServer.log:  " + t.getMessage());
    } finally {
      if (tr != null)
        tr.rollback(); 
    } 
  }
}
