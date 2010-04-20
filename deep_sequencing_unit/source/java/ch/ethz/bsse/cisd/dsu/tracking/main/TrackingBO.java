/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.email.Email;
import ch.ethz.bsse.cisd.dsu.tracking.email.EmailWithSummary;
import ch.ethz.bsse.cisd.dsu.tracking.email.IEntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewTrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Tomasz Pylak
 */
public class TrackingBO
{
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

    public void trackAndNotify(ITrackingDAO trackingDAO, SessionContextDTO session)
    {
        TrackingStateDTO prevTrackingState = trackingDAO.getTrackingState();

        TrackedEntities changedEntities =
                fetchChangedEntities(prevTrackingState, trackingServer, session);
        List<EmailWithSummary> emailsWithSummary = emailGenerator.generateEmails(changedEntities);
        sendEmails(emailsWithSummary, mailClient);
        saveTrackingState(prevTrackingState, changedEntities, trackingDAO);
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
        String replyToOrNull = email.getReplyToOrNull();
        From fromOrNull = email.getFromOrNull();
        String[] recipients = email.getRecipients();

        mailClient.sendMessage(subject, content, replyToOrNull, fromOrNull, recipients);
    }

    private static void appendLine(StringBuilder sb, String msg)
    {
        sb.append(msg);
        sb.append("\n");
    }

    private static void saveTrackingState(TrackingStateDTO prevState,
            TrackedEntities changedEntities, ITrackingDAO trackingDAO)
    {
        TrackingStateDTO state = calcNewTrackingState(prevState, changedEntities);
        trackingDAO.saveTrackingState(state);
    }

    private static TrackingStateDTO calcNewTrackingState(TrackingStateDTO prevState,
            TrackedEntities changedEntities)
    {
        TrackingStateDTO state = new TrackingStateDTO();
        Set<Long> sequencingSamplesProcessed =
                new HashSet<Long>(prevState.getAlreadyTrackedSampleIdsProcessed());
        addNewSampleIds(sequencingSamplesProcessed, changedEntities.getSequencingSamplesProcessed());
        state.setAlreadyTrackedSampleIdsProcessed(sequencingSamplesProcessed);

        int lastSeenDatasetId =
                calcMaxId(changedEntities.getDataSets(), prevState.getLastSeenDatasetId());
        state.setLastSeenDatasetId(lastSeenDatasetId);
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

    private static int calcMaxId(List<? extends IIdentifiable> entities, int initialValue)
    {
        long max = initialValue;
        for (IIdentifiable entity : entities)
        {
            max = Math.max(max, entity.getId());
        }
        // TODO 2009-12-01, Tomasz Pylak: refactor ids to long everywhere
        return (int) max;
    }

    private static TrackedEntities fetchChangedEntities(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, SessionContextDTO session)
    {
        List<Sample> sequencingSamplesToBeProcessed =
                listSamples(SEQUENCING_SAMPLE_TYPE, PROCESSING_POSSIBLE_PROPERTY_CODE, TRUE,
                        trackingServer, session);
        List<Sample> sequencingSamplesSuccessfullyProcessed =
                listSamples(SEQUENCING_SAMPLE_TYPE, PROCESSING_SUCCESSFUL_PROPERTY_CODE, TRUE,
                        trackingServer, session);

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, trackingState
                        .getLastSeenDatasetId());
        List<ExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        return new TrackedEntities(sequencingSamplesToBeProcessed,
                sequencingSamplesSuccessfullyProcessed, dataSets);
    }

    @SuppressWarnings("unchecked")
    private static List<Sample> listSamples(String sampleType, String propertyTypeCode,
            String propertyValue, ITrackingServer trackingServer, SessionContextDTO session)
    {
        NewTrackingSampleCriteria criteria =
                new NewTrackingSampleCriteria(sampleType, propertyTypeCode, propertyValue,
                        Collections.EMPTY_LIST);
        return trackingServer.listSamples(session.getSessionToken(), criteria);
    }
}
