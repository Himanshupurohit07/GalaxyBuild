package com.sparc.wc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import wt.org.WTPrincipal;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.session.SessionHelper;
import wt.util.WTException;

public class SPARCQueueHelper {
	private static final Logger logger = LogManager.getLogger(SPARCQueueHelper.class.getName()); 

	public void addQueueEntryForPDFExport(Class<?>[] argTypes, Object[] args, String queueName, String className, String methodName) {
		logger.debug(" Start of the method addQueueEntryForPDFExport----");
		
	
		ProcessingQueue pdfExportProcessingQueue =  null;
		try {
			//WTPrincipal user = SessionHelper.manager.getPrincipal(); 
			WTPrincipal admin = SessionHelper.manager.getAdministrator();
			pdfExportProcessingQueue = getProcessingQueue(queueName);
			pdfExportProcessingQueue.addEntry(admin,methodName,className,argTypes,args);

		}catch(WTException ex){
			logger.debug("Error adding queue entry : {}" , (Object)ex.getStackTrace());
			ex.printStackTrace();
		}
		logger.debug(" End of the method addQueueEntryForPDFExport----");
	}
	
	public static ProcessingQueue getProcessingQueue(final String strQueueName) throws WTException{
		ProcessingQueue pdfExportProcessingQueue = null;
		
			pdfExportProcessingQueue = QueueHelper.manager.getQueue(strQueueName);
			if(pdfExportProcessingQueue == null){
				pdfExportProcessingQueue =  QueueHelper.manager.createQueue(strQueueName,true);
				pdfExportProcessingQueue.setEnabled(true); 
			}

			return pdfExportProcessingQueue;

	}
}
