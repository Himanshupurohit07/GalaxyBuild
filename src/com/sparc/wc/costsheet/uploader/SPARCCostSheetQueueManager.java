package com.sparc.wc.costsheet.uploader;

import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.RequestHelper;
import org.apache.logging.log4j.Logger;
import wt.fc.PersistenceServerHelper;
import wt.log4j.LogR;
import wt.org.WTPrincipal;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.session.SessionHelper;
import wt.util.WTException;

/**
 * This class is used to get the Processing Queue object and add the entry to the Processing Queue.
 */
public class SPARCCostSheetQueueManager {

    private static final Logger LOGGER = LogR.getLogger(SPARCCostSheetQueueManager.class.getName());

    private static final String costingUploaderQueueName = LCSProperties.get("com.sparc.wc.costsheet.uploaderQueueName");

    /**
     * This method gets the processing queue object and add the entry to the processing queue.
     * @param fileName
     * @param strDivision
     */
    public void addQueueEntryForDetailedUpload(final String fileName,final String strDivision,String seasonId){
        LOGGER.info(SPARCCostSheetQueueManager.class.getName()+ " Start of the method addQueueEntryForDetailedUpload----fileName"+fileName);
        Class<?> argTypes[];
        Object args[];
        ProcessingQueue costingUploaderProcessingQueue =  null;
        LOGGER.debug("Queue Entry for adding the fileName");
        try {
            WTPrincipal user = SessionHelper.manager.getPrincipal();
            costingUploaderProcessingQueue = getProcessingQueue(costingUploaderQueueName);
            argTypes = (new Class[] {String.class,String.class,String.class});
            args = (new Object[]{fileName,strDivision,seasonId});
            costingUploaderProcessingQueue.addEntry(user,"processFile","com.sparc.wc.costsheet.uploader.SPARCCostSheetUploader",argTypes,args);

        } catch(WTException ex){
            ex.printStackTrace();
        }
        LOGGER.info(SPARCCostSheetQueueManager.class.getName()+ " End of the method addQueueEntryForDetailedUpload----fileName");

    }
    /**
     * This method gets the processing queue object and add the entry to the processing queue for High Level Upload.
     * @param fileName
     * @param seasonId
     */
    public void addQueueEntryForHighLevelUpload(String fileName,String strDivision, String seasonId){
        LOGGER.info(SPARCCostSheetQueueManager.class.getName()+ " Start of the method addQueueEntryForHighLevelUpload----fileName"+fileName);
        Class<?> argTypes[];
        Object args[];
        ProcessingQueue costingUploaderProcessingQueue =  null;
        LOGGER.debug("Queue Entry for adding the fileName");
        try {
            WTPrincipal user = SessionHelper.manager.getPrincipal();
            costingUploaderProcessingQueue = getProcessingQueue(costingUploaderQueueName);
            argTypes = (new Class[] {String.class,String.class,String.class});
            args = (new Object[]{fileName,strDivision,seasonId});
            costingUploaderProcessingQueue.addEntry(user,"processFile","com.sparc.wc.costsheet.uploader.SPARCHighLevelCostSheetUploader",argTypes,args);

        } catch(WTException ex){
            ex.printStackTrace();
        }
        LOGGER.info(SPARCCostSheetQueueManager.class.getName()+ " End of the method addQueueEntryForSummaryUpload----fileName");

    }
    

    /**
     * This method gets the processing Queue object if available for the queuename passed.
     * otherwise creates a processing queue object with that queuename
     * @param strQueueName
     * @return
     */
    public static ProcessingQueue getProcessingQueue(final String strQueueName){
        LOGGER.info("Start of the getProcessingQueue");
        ProcessingQueue costingProcessingQueue = null;
        try{
            costingProcessingQueue = QueueHelper.manager.getQueue(costingUploaderQueueName);
            if(costingProcessingQueue == null){
                costingProcessingQueue =  QueueHelper.manager.createQueue(strQueueName,true);
                QueueHelper.manager.startQueue(costingProcessingQueue);
            }
            if(!costingProcessingQueue.isEnabled()){
                costingProcessingQueue.setEnabled(true);
            }

            PersistenceServerHelper.manager.update(costingProcessingQueue);

        }catch(Exception ex){
            ex.printStackTrace();
        }
        LOGGER.info("End of the getProcessingQueue");
        return costingProcessingQueue;
    }
}
