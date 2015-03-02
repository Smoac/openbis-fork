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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Tomasz Pylak
 * @author Manuel Kohler
 */

public class TrackingBO
{
    private static final List<String> SEQUENCING_SAMPLE_TYPES = Arrays.asList("ILLUMINA_SEQUENCING", "ILLUMINA_SEQUENCING_NEUROSTEMX");

    private static final String EXTERNAL_SAMPLE_NAME = "EXTERNAL_SAMPLE_NAME";

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

    public void trackAndNotify(ITrackingDAO trackingDAO, final HashMap<String, String[]> clMap, SessionContextDTO session)
    {
        TrackingStateDTO prevTrackingState = trackingDAO.getTrackingState();
        LogUtils.info(prevTrackingState.getLastSeenDataSetIdMap().toString());

        String CL_PARAMETER_LANES = "lanes";
        String CL_PARAMETER_ALL = "all";
        TrackedEntities changedEntities = null;
        List<EmailWithSummary> emailsWithSummary = null;
        HashMap<String, ArrayList<Long>> changedTrackingMap = null;

        if (clMap.get(CL_PARAMETER_LANES) != null)
        {
            Object[] result = fetchChangedDataSets(prevTrackingState, trackingServer, clMap.get(CL_PARAMETER_LANES), session);
            changedEntities = (TrackedEntities) result[0];
            changedTrackingMap = (HashMap<String, ArrayList<Long>>) result[1];
            emailsWithSummary = emailGenerator.generateDataSetsEmails(changedEntities);
        }
        else if (clMap.containsKey(CL_PARAMETER_ALL))
        {
            Object[] result = fetchChangedEntities(prevTrackingState, trackingServer, clMap, session);
            changedEntities = (TrackedEntities) result[0];
            changedTrackingMap = (HashMap<String, ArrayList<Long>>) result[1];
            emailsWithSummary = emailGenerator.generateEmails(changedEntities);
        }
        else
        {
            LogUtils.debug("Should never be reached.");
        }

        sendEmails(emailsWithSummary, mailClient);
        saveTrackingState(prevTrackingState, changedTrackingMap, changedEntities, trackingDAO);
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
            HashMap<String, ArrayList<Long>> changedTrackingMap,
            TrackedEntities changedEntities, ITrackingDAO trackingDAO)
    {
        TrackingStateDTO state =
                TrackingStateUpdateHelper.calcNewTrackingState(prevTrackingState, changedEntities, changedTrackingMap);

        trackingDAO.saveTrackingState(state);
    }

    private static Object[] fetchChangedEntities(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, HashMap<String, String[]> clMap, SessionContextDTO session)
    {
        List<Sample> sequencingSamplesToBeProcessed =
                listSequencingSamples(PROCESSING_POSSIBLE_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsToBeProcessed(), trackingServer, session);
        List<Sample> sequencingSamplesSuccessfullyProcessed =
                listSequencingSamples(PROCESSING_SUCCESSFUL_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsProcessed(), trackingServer, session);

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, trackingState
                        .getLastSeenDatasetId());
        List<AbstractExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        HashMap<String, ArrayList<Long>> changedTrackingMap = new HashMap<String, ArrayList<Long>>();

        // Loop over all new data sets
        for (AbstractExternalData d : dataSets)
        {
            Sample currentLane = d.getSample();
            String lanePermId = currentLane.getPermId();

            LogUtils.info("Found lane with permId: " + lanePermId + " with new DS techId " + d.getId() + " and DS permId " + d.getPermId());
            if (changedTrackingMap.get(lanePermId) != null)
            {
                ArrayList<Long> existingList = changedTrackingMap.get(lanePermId);
                existingList.add(d.getId());
                changedTrackingMap.put(lanePermId, existingList);
            }
            else
            {
                ArrayList<Long> newList = new ArrayList<Long>();
                newList.add(d.getId());
                changedTrackingMap.put(lanePermId, newList);
            }
        }

        return new Object[] { new TrackedEntities(sequencingSamplesToBeProcessed, sequencingSamplesSuccessfullyProcessed, dataSets),
                changedTrackingMap };
    }

    private static Object[] fetchChangedDataSets(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, String[] laneCodeList, SessionContextDTO session)
    {

        List<Sample> sequencingSamplesToBeProcessed =
                listSequencingSamples(PROCESSING_POSSIBLE_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsToBeProcessed(), trackingServer, session);
        List<Sample> sequencingSamplesSuccessfullyProcessed =
                listSequencingSamples(PROCESSING_SUCCESSFUL_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsProcessed(), trackingServer, session);

        ArrayList<Long> allDataSetIds = new ArrayList<Long>();
        for (Map.Entry<String, Long> entry : trackingState.getLastSeenDataSetIdMap().entrySet())
        {
            allDataSetIds.add(entry.getValue());
        }
        Long maxDataSetId = Collections.max(allDataSetIds);
        LogUtils.info("Using maximum DS techId " + maxDataSetId + " for search of changed data sets");

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, maxDataSetId);
        List<AbstractExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        ArrayList<SampleIdentifier> filterList = new ArrayList<SampleIdentifier>();
        ArrayList<AbstractExternalData> filteredDataSets = new ArrayList<AbstractExternalData>();

        HashMap<String, ArrayList<Long>> changedTrackingMap = new HashMap<String, ArrayList<Long>>();

        for (String lane : laneCodeList)
        {
            LogUtils.info("Searching for new data sets which belong to " + lane);
            filterList.add(new SampleIdentifier(lane));
        }
        // Loop over all new data sets
        for (AbstractExternalData d : dataSets)
        {
            // Check if the given lanes/samples have data sets which are new
            SampleIdentifier currentLaneId = new SampleIdentifier(d.getSampleCode());
            if (filterList.contains(currentLaneId))
            {
                filteredDataSets.add(d);
                Sample currentLane = d.getSample();
                String lanePermId = currentLane.getPermId();

                LogUtils.info("Found lane with permId: " + lanePermId + " with new DS techId " + d.getId() + " and DS permId " + d.getPermId());
                if (changedTrackingMap.get(lanePermId) != null)
                {
                    ArrayList<Long> existingList = changedTrackingMap.get(lanePermId);
                    existingList.add(d.getId());
                    changedTrackingMap.put(lanePermId, existingList);
                }
                else
                {
                    ArrayList<Long> newList = new ArrayList<Long>();
                    newList.add(d.getId());
                    changedTrackingMap.put(lanePermId, newList);
                }
            }
        }

        LogUtils.info(changedTrackingMap.toString());
        LogUtils.info("Found " + filteredDataSets.size() + " data sets which are connected to samples in " + filterList.toString());
        return new Object[] { new TrackedEntities(sequencingSamplesToBeProcessed, sequencingSamplesSuccessfullyProcessed, filteredDataSets),
                changedTrackingMap };
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
                TrackedEntities changedEntities, HashMap<String, ArrayList<Long>> changedTrackingMap)
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

            System.out.println(changedTrackingMap.toString());

            for (Map.Entry<String, ArrayList<Long>> entry : changedTrackingMap.entrySet())
            {
                newTrackingState.put(entry.getKey(), Collections.max(entry.getValue()));
            }
            System.out.println(newTrackingState.entrySet().toString());

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
