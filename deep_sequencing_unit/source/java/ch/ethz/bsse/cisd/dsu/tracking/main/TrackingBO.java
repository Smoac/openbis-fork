/*
 * Copyright 2015 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.bsse.cisd.dsu.tracking.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.email.Email;
import ch.ethz.bsse.cisd.dsu.tracking.email.EmailWithSummary;
import ch.ethz.bsse.cisd.dsu.tracking.email.IEntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.common.exceptions.Status;

/**
 * @author Tomasz Pylak
 * @author Manuel Kohler
 */

public class TrackingBO
{
    private static final String PROPERTY_RUN_NAME_FOLDER = "RUN_NAME_FOLDER";

	private static final String SEQUENCING_SAMPLE_TYPE = "ILLUMINA_SEQUENCING";

    private static final String FLOW_LANE_SAMPLE_TYPE = "ILLUMINA_FLOW_LANE";

    private static final String PROCESSING_POSSIBLE_PROPERTY_CODE = "LIBRARY_PROCESSING_POSSIBLE";

    private static final String PROCESSING_SUCCESSFUL_PROPERTY_CODE =
            "LIBRARY_PROCESSING_SUCCESSFUL";

    private static final String TRUE = "true";
    
    private final ITrackingServer trackingServer;

    private final IEntityTrackingEmailGenerator emailGenerator;

    private final IMailClient mailClient;

    public TrackingBO(ITrackingServer trackingServer, IEntityTrackingEmailGenerator emailGenerator,
            IMailClient mailClient)
    {
        this.trackingServer = trackingServer;
        this.emailGenerator = emailGenerator;
        this.mailClient = mailClient;
    }

    public void trackAndNotify(ITrackingDAO trackingDAO, final HashMap<String, String[]> commandLineMap,
            Parameters params, SessionContextDTO session)
    {
        Boolean sendEmails = true;
        TrackingStateDTO prevTrackingState = trackingDAO.getTrackingState();
        
        extractCommandLineFlags(commandLineMap);
        LogUtils.debug("prevTrackingState: " + prevTrackingState.getLastSeenDataSetIdMap().toString());

        TrackedEntities changedEntities = null;
        List<EmailWithSummary> emailsWithSummary = null;

        
        if (commandLineMap.get(TrackingClient.CL_PARAMETER_LANES) != null)
        {
        	String[] laneCodeList = commandLineMap.get(TrackingClient.CL_PARAMETER_LANES);
            changedEntities = fetchChangedDataSets(prevTrackingState, trackingServer, params,
                    commandLineMap, laneCodeList, session);
        }
        
        else if (commandLineMap.get(TrackingClient.CL_PARAMETER_REMOVE_LANES) != null)
        {
            sendEmails = false;
        	String[] laneCodeList = commandLineMap.get(TrackingClient.CL_PARAMETER_REMOVE_LANES);
        	changedEntities = fetchChangedDataSets(prevTrackingState, trackingServer, params,
                    commandLineMap, laneCodeList, session);
        } 

        
        else if (commandLineMap.containsKey(TrackingClient.CL_PARAMETER_ALL))
        {
            // changedEntities = fetchChangedEntities(prevTrackingState, trackingServer, commandLineMap, session);
            System.out.println("This function is deactivated");
        }
        
        // just list the potential changed lanes
        else if (commandLineMap.containsKey(TrackingClient.CL_PARAMETER_CHANGED_LANES))
        {
            Map<String, String> changed_lanes = fetchChangedLanes(prevTrackingState, trackingServer, params, session);
            sendEmails = false;

        }
        else
        {
            LogUtils.debug("Should never be reached.");
        }

        
        if (sendEmails)
        {
            emailsWithSummary = emailGenerator.generateDataSetsEmails(changedEntities);
            sendEmails(emailsWithSummary, mailClient);
            
        }
        else {
        	LogUtils.info("Not sending out any emails.");
        }

        if (!params.getDebug() && changedEntities != null) {
        	LogUtils.info("Saving new state to tracking database.");
        	saveTrackingState(prevTrackingState, changedEntities, trackingDAO);
        }
        else {
        	LogUtils.info("Debug mode activated! Won't save anything to the tracking database.");
        }

    }

	private void extractCommandLineFlags(final HashMap<String, String[]> commandLineMap) {
		StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : commandLineMap.entrySet()) {
        	sb.append(entry.getKey() + " ");
        	if  (entry.getValue() != null) {
            	for (String value : entry.getValue()) {
            		sb.append(value);
            	}        		
        	}
        }
        LogUtils.info("Got these command line flags: '" + sb + "'");
	}

    private static void sendEmails(List<EmailWithSummary> emailsWithSummary, IMailClient mailClient)
    {
        for (EmailWithSummary emailWithSummary : emailsWithSummary)
        {
            Email email = emailWithSummary.getEmail();
            try
            {
                logEmailSummary(emailWithSummary);
                sendMessage(mailClient, email);
            } catch (Exception ex)
            {
                sendErrorReport(mailClient, ex, email);
            }
        }
    }

    private static void logEmailSummary(EmailWithSummary emailWithSummary)
    {
        LogUtils.info("Sending an email [" + emailWithSummary.getEmail().getSubject()
                + "]. Summary:\n" + emailWithSummary.getSummary());
    }

    // This email could not be sent, most probably the recipient addresses were
    // incorrect.
    // We send the email to the administrator "replyTo' address, the admin should
    // forward it to the right recipient.
    private static void sendErrorReport(IMailClient mailClient, Exception exception, Email email)
    {
        StringBuilder errorReportContentBuilder = new StringBuilder();
        appendLine(errorReportContentBuilder, "Dear openBIS Admin,");
        appendLine(errorReportContentBuilder,
                "This email has been generated automatically from the openBIS Changes Tracking system.");
        appendLine(errorReportContentBuilder, "There was a failure while trying to send the email:");
        appendLine(errorReportContentBuilder, exception.getMessage() == null ? "<no details>"
                : exception.getMessage());
        appendLine(errorReportContentBuilder,
                "The possible reason is that the recipient address is not valid.");
        appendLine(errorReportContentBuilder,
                "If you know the address of the recipient please correct it and forward this email to him.");
        appendLine(errorReportContentBuilder,
                "!!! Note that the Tracking System will not try to send this email again !!!");
        appendLine(errorReportContentBuilder,
                "Please correct the recipient email address in openBIS to avoid similar problems in future.");
        appendLine(errorReportContentBuilder, "");
        appendLine(errorReportContentBuilder, "Subject:    " + email.getSubject());
        appendLine(errorReportContentBuilder, "Recipients: "
                + CollectionUtils.abbreviate(email.getRecipients(), -1));
        appendLine(errorReportContentBuilder, "");

        appendLine(errorReportContentBuilder, "Original content: ");
        appendLine(errorReportContentBuilder, email.getContent());
        String errorReportContent = errorReportContentBuilder.toString();

        Email errorReportEmail =
                new Email("[Tracking] Sending an email failed", errorReportContent, null, email
                        .getFromOrNull(), email.getReplyToOrNull());
        sendMessage(mailClient, errorReportEmail);
    }

    private static void sendMessage(IMailClient mailClient, Email email)
    {
        String subject = email.getSubject();
        String content = email.getContent();
        EMailAddress replyToOrNull = email.getReplyToOrNull();
        EMailAddress fromOrNull = email.getFromOrNull();
        EMailAddress[] recipients = email.getRecipients();

        mailClient.sendEmailMessage(subject, content, replyToOrNull, fromOrNull, recipients);

        // sendEmailMessage(String subject, String content, EMailAddress replyToOrNull,
        // EMailAddress fromOrNull, EMailAddress... recipients)

    }

    private static void appendLine(StringBuilder sb, String msg)
    {
        sb.append(msg);
        sb.append("\n");
    }

    private static void saveTrackingState(TrackingStateDTO prevTrackingState,
            TrackedEntities changedEntities, ITrackingDAO trackingDAO)
    {
        TrackingStateDTO state = TrackingStateUpdateHelper.calcNewTrackingState(prevTrackingState, changedEntities);

        trackingDAO.saveTrackingState(state);
    }

    // gets *all* data sets
    private static TrackedEntities fetchChangedEntities(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, HashMap<String, String[]> clMap, SessionContextDTO session)
    {

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, trackingState
                        .getLastSeenDatasetId());
        List<AbstractExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        HashMap<String, ArrayList<Long>> changedTrackingMap = new HashMap<String, ArrayList<Long>>();

        // Loop over all new data sets
        for (AbstractExternalData d : dataSets)
        {
            addDataSetTo(changedTrackingMap, d);
        }

        return gatherTrackedEntities(trackingState, trackingServer, session, dataSets, changedTrackingMap);
    }

    private static Map<String, String> fetchChangedLanes(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, Parameters params, SessionContextDTO session)
    {
    	long usableDataSetId = getUsableDataSetId(trackingState, params);
        LogUtils.info("Using maximum DS techId " + usableDataSetId + " for search of changed data sets");

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, usableDataSetId);
        List<AbstractExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        Map<String, String> changedLanesMap = new HashMap<String, String>();
        
        // Loop over all new data sets
        for (AbstractExternalData d : dataSets)
        {       	       	
            Long newDataSetID = d.getId();
            Sample lane = d.getSample();
            String lanePermId = lane.getPermId();
            String laneSpace = lane.getSpace().getCode();
            Long maxDatasetIdForSample = getMaxDataSetIdForSample(trackingState, lanePermId);

            // Check if the given lanes/samples have data sets which are newer than the last seen one (= maxDatasetIdForSample)
            if (newDataSetID > maxDatasetIdForSample)
            {
                SampleIdentifier currentLaneId = new SampleIdentifier(d.getSampleCode());
                Sample flowcell = lane.getContainer();
                String runNameFolder = "";
                List<IEntityProperty> flowcellProperties = flowcell.getProperties();
                for (IEntityProperty property : flowcellProperties)
                {
                    if (property.getPropertyType().getCode().equals(PROPERTY_RUN_NAME_FOLDER))
                    {
                        runNameFolder = property.getValue();
                        break;
                    }
                }
                String laneString = currentLaneId.toString().split(":")[1];
                changedLanesMap.put(runNameFolder + ":" + laneString, laneSpace + " " + lane.getCode());
                LogUtils.info("DataSetID: " + newDataSetID + " of NEW data Sets > MAX DataSet id for this sample: " + maxDatasetIdForSample);                
            }
        }
        
        Set<Map.Entry<String, String>> entrySet = changedLanesMap.entrySet();
        for (Entry<String, String> entry : entrySet)
        {
            // needed for the integration of the openBIS webapp
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        LogUtils.debug("changedLanesMap: " + changedLanesMap.toString());
        return changedLanesMap;
    }

    private static TrackedEntities fetchChangedDataSets(TrackingStateDTO trackingState,
            ITrackingServer trackingServer,  Parameters params, final HashMap<String, String[]> commandLineMap,
            String[] laneCodeList, SessionContextDTO session)
    {
    	long usableDataSetId = getUsableDataSetId(trackingState, params);
    	
        List<String> spaceWhiteList = Arrays.asList(params.getSpaceWhitelist().split("\\s*,\\s*"));
        List<String> datasetTypeList = Arrays.asList(params.getdataSetTypeList().split("\\s*,\\s*"));
       
        LogUtils.info("Using maximum DS techId " + usableDataSetId + " for search of changed data sets");

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, usableDataSetId);
        List<AbstractExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        ArrayList<SampleIdentifier> filterList = new ArrayList<SampleIdentifier>();
        ArrayList<AbstractExternalData> filteredDataSets = new ArrayList<AbstractExternalData>();
        ArrayList<AbstractExternalData> toTransferDataSets = new ArrayList<AbstractExternalData>();
        HashMap<String, ArrayList<Long>> changedTrackingMap = new HashMap<String, ArrayList<Long>>();

        // Loop over all lanes and create a list of relevant lanes
        for (String lane : laneCodeList)
        {
            LogUtils.info("Searching for new data sets which belong to " + lane);
            filterList.add(new SampleIdentifier(lane));
        }

        // Loop over all new data sets
        for (AbstractExternalData d : dataSets)
        {
            Long newDataSetID = d.getId();
            SampleIdentifier currentLaneId = new SampleIdentifier(d.getSampleCode());
            String lanePermId = d.getSample().getPermId();
            Long maxDatasetIdForSample = getMaxDataSetIdForSample(trackingState, lanePermId);

            // Check if the given lanes/samples have data sets which are newer than the last seen one (= maxDatasetIdForSample)
            if (filterList.contains(currentLaneId) && newDataSetID > maxDatasetIdForSample)
            {
                LogUtils.debug("DataSetID: " + newDataSetID + " of NEW data Sets > MAX DataSet id for this sample: " + maxDatasetIdForSample);
                filteredDataSets.add(d);
                
                if (spaceWhiteList.contains(d.getSpace().getCode()) || d.getSpace().getCode().startsWith(params.getDbmSpacePrefix())) {
                	if (datasetTypeList.contains(d.getDataSetType().getCode())) {
                		if (d.tryGetAsDataSet().getStatus().equals(DataSetArchivingStatus.AVAILABLE) || d.tryGetAsDataSet().getStatus().equals(DataSetArchivingStatus.LOCKED))  {                			
                			toTransferDataSets.add(d);
                		}
                		else {
                            LogUtils.error("Data set " + d.getCode() + " eventually archived!");
                			}
                		}
                	}
                }
//                System.out.println("Sending Email for Data Set with permID " + d.getPermId() + " which is part of " + d.getSampleCode());
                addDataSetTo(changedTrackingMap, d);
        }
        
        LogUtils.info("TO_TRANSFER: Found " + toTransferDataSets.size() + " data sets which are in the list of 'space-whitelist' and could be transferred to an extra folder");
       
        
        if (commandLineMap.containsKey(TrackingClient.CL_PARAMETER_COPY_DATA_SETS))
        {
            extraDataSetCopy(params, toTransferDataSets);
        }

        LogUtils.info("Found " + filteredDataSets.size() + " data sets which are connected to samples in " + filterList.toString());
        return gatherTrackedEntities(trackingState, trackingServer, session, filteredDataSets, changedTrackingMap);
    }
    
    
	private static void extraDataSetCopy(Parameters params, List<AbstractExternalData> dataSets) {
		File rsyncBinary = new File(params.getRsyncBinary());
		File destination = new File(params.getDestinationFolder());
		String base_path_string = params.getDssRoot();
		List<String> cmdLineOptions = new ArrayList<String>(params.getRsyncFlags().length());
		Collections.addAll(cmdLineOptions, params.getRsyncFlags());
		RsyncCopier copier = new RsyncCopier(rsyncBinary, null, cmdLineOptions.toArray(new String[cmdLineOptions.size()]));
				
		for (AbstractExternalData ds : dataSets) {
			File source = new File(base_path_string, ds.tryGetAsDataSet().getFullLocation());
			LogUtils.info("Start rsyncing " + ds.getCode() + " from " + source + " to " + params.getDestinationFolder());
			Status status = copier.copy(source, destination, null, null);
			LogUtils.info("Got status: " + status + " for " + ds.getCode());
		}
	}

    private static long getMaxDataSetId(TrackingStateDTO trackingState)
    {
        long maxDataSetId = 0;
        for (Long id : trackingState.getLastSeenDataSetIdMap().values())
        {
            maxDataSetId = Math.max(maxDataSetId, id);
        }
//        return 0;
        return maxDataSetId;
    }
    
    /** Little helper function which reduces the number of data sets we are looking at. This can be configured
     * by value 'old-data-set-backlog-number' in the service.properties. Without this value
     * the calls get slower and slower with the growing data set amount. But we can assume that older data sets
     * already got triggered earlier to send out an email. If not, the sample is so old that we do not want
     * to send an email. 
     */
    private static long getUsableDataSetId (TrackingStateDTO trackingState, Parameters params) {
        long maxDataSetId = getMaxDataSetId(trackingState);
        long oldDataSetBacklogNumber = params.getoldDataSetBacklogNumber();       
        long usableDataSetId = Math.max(maxDataSetId - oldDataSetBacklogNumber, 0);
        return usableDataSetId;    	 
    }
    

    private static long getMaxDataSetIdForSample(TrackingStateDTO trackingState, String lanePermId)
    {
        long maxDataSetId = 0;
        Long maxDatasetIdForSample = trackingState.getLastSeenDataSetIdMap().get(lanePermId);
        if (maxDatasetIdForSample != null)
        {
            return maxDatasetIdForSample;
        } else
            return maxDataSetId;
    }

    private static TrackedEntities gatherTrackedEntities(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, SessionContextDTO session,
            List<AbstractExternalData> dataSets, HashMap<String, ArrayList<Long>> changedTrackingMap)
    {
        List<Sample> sequencingSamplesToBeProcessed =
                listSequencingSamples(PROCESSING_POSSIBLE_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsToBeProcessed(), trackingServer, session);
        List<Sample> sequencingSamplesSuccessfullyProcessed =
                listSequencingSamples(PROCESSING_SUCCESSFUL_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsProcessed(), trackingServer, session);
        return new TrackedEntities(sequencingSamplesToBeProcessed, sequencingSamplesSuccessfullyProcessed, dataSets,
                changedTrackingMap);
    }

    private static void addDataSetTo(HashMap<String, ArrayList<Long>> changedTrackingMap, AbstractExternalData dataSet)
    {
        Sample currentLane = dataSet.getSample();
        String lanePermId = currentLane.getPermId();

        LogUtils.debug("Found lane " + currentLane.getCode() + " with permId: " + lanePermId + " with new DS techId " + dataSet.getId() + " and DS permId " + dataSet.getPermId());
        ArrayList<Long> existingList = changedTrackingMap.get(lanePermId);
        if (existingList == null)
        {
            existingList = new ArrayList<Long>();
            changedTrackingMap.put(lanePermId, existingList);
        }
        existingList.add(dataSet.getId());
    }

    private static List<Sample> listSequencingSamples(String propertyTypeCode,
            Set<Long> alreadyTrackedSampleIds, ITrackingServer trackingServer,
            SessionContextDTO session)
    {
        return listSamples(SEQUENCING_SAMPLE_TYPE, propertyTypeCode, TRUE, alreadyTrackedSampleIds,
                trackingServer, session);
    }

    private static List<Sample> listSamples(String sampleType, String propertyTypeCode,
            String propertyValue, Set<Long> alreadyTrackedSampleIds,
            ITrackingServer trackingServer, SessionContextDTO session)
    {
        TrackingSampleCriteria criteria =
                new TrackingSampleCriteria(sampleType, propertyTypeCode, propertyValue,
                        alreadyTrackedSampleIds);
        return trackingServer.listSamples(session.getSessionToken(), criteria);
    }

    static class TrackingStateUpdateHelper
    {

        static TrackingStateDTO calcNewTrackingState(TrackingStateDTO prevState,
                TrackedEntities changedEntities)
        {
            TrackingStateDTO state = new TrackingStateDTO();
            Set<Long> sequencingSamplesToBeProcessed =
                    new TreeSet<Long>(prevState.getAlreadyTrackedSampleIdsToBeProcessed());
            addNewSampleIds(sequencingSamplesToBeProcessed, changedEntities
                    .getSequencingSamplesToBeProcessed());
            state.setAlreadyTrackedSampleIdsToBeProcessed(sequencingSamplesToBeProcessed);

            Set<Long> sequencingSamplesProcessed =
                    new TreeSet<Long>(prevState.getAlreadyTrackedSampleIdsProcessed());
            addNewSampleIds(sequencingSamplesProcessed, changedEntities
                    .getSequencingSamplesProcessed());
            state.setAlreadyTrackedSampleIdsProcessed(sequencingSamplesProcessed);

            TreeMap<String, Long> newTrackingState = new TreeMap<String, Long>();

            HashMap<String, ArrayList<Long>> changedTrackingMap = changedEntities.getChangedTrackingMap();
            // System.out.println(changedTrackingMap.toString());

            for (Map.Entry<String, ArrayList<Long>> entry : changedTrackingMap.entrySet())
            {
                newTrackingState.put(entry.getKey(), Collections.max(entry.getValue()));
            }
            // System.out.println(newTrackingState.entrySet().toString());

            for (Map.Entry<String, Long> entry : prevState.getLastSeenDataSetIdMap().entrySet())
            {
                if (!newTrackingState.containsKey(entry.getKey()))
                {
                    newTrackingState.put(entry.getKey(), entry.getValue());
                }
            }

            state.setLastSeenDataSetIdMap(newTrackingState);
            return state;
        }

        static TrackingStateDTO calcNewTrackingStateDataSets(HashMap<String, ArrayList<Long>> changedTrackingMap,
                TrackedEntities changedEntities)
        {
            TrackingStateDTO state = new TrackingStateDTO();

            TreeMap<String, Long> newTrackingState = new TreeMap<String, Long>();
            for (Map.Entry<String, ArrayList<Long>> entry : changedTrackingMap.entrySet())
            {
                newTrackingState.put(entry.getKey(), Collections.max(entry.getValue()));
            }
            LogUtils.info(newTrackingState.toString());

            state.setLastSeenDataSetIdMap(newTrackingState);
            return state;
        }

        private static void addNewSampleIds(Set<Long> alreadyTrackedSampleIdsProcessed,
                List<Sample> sequencingSamplesProcessed)
        {
            for (Sample sample : sequencingSamplesProcessed)
            {
                alreadyTrackedSampleIdsProcessed.add(sample.getId());
            }
        }
    }
}
