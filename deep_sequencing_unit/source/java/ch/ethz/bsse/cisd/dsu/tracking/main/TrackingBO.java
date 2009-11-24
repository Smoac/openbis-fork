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

import java.util.List;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.email.Email;
import ch.ethz.bsse.cisd.dsu.tracking.email.IEntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Tomasz Pylak
 */
public class TrackingBO
{
    private static final String SEQUENCING_SAMPLE_TYPE = "ILLUMINA_SEQUENCING";

    private static final String FLOW_LANE_SAMPLE_TYPE = "ILLUMINA_FLOW_LANE";

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
        List<Email> emails = emailGenerator.generateEmails(changedEntities);
        sendEmails(emails, mailClient);
        saveTrackingState(changedEntities, trackingDAO);
    }

    private static void sendEmails(List<Email> emails, IMailClient mailClient)
    {
        for (Email email : emails)
        {
            try
            {
                From from = tryGetFromField(email);
                String[] recipients = email.getRecipients();
                String content = email.getContent();
                String subject = email.getSubject();
                String replyToOrNull = email.getReplyToOrNull();
                sendMessage(mailClient, subject, content, replyToOrNull, from, recipients);
            } catch (Exception ex)
            {
                sendErrorReport(mailClient, ex, email);
            }
        }
    }

    private static From tryGetFromField(Email email)
    {
        return (email.getFromOrNull() == null) ? null : new From(email.getFromOrNull());
    }

    // This email could not be sent, most probably the recipient addresses were
    // incorrect.
    // We send the email to the administrator "replyTo' address, the admin should
    // forward it to the right recipient.
    private static void sendErrorReport(IMailClient mailClient, Exception exception, Email email)
    {
        StringBuffer errorReportContent = new StringBuffer();
        appendLine(errorReportContent, "Dear openBIS Admin,");
        appendLine(errorReportContent,
                "This email has been generated automatically from the openBIS Changes Tracking system.");
        appendLine(errorReportContent, "There was a failure while trying to send the email:");
        appendLine(errorReportContent, exception.getMessage());
        appendLine(errorReportContent,
                "The possible reason is that the recipient address is not valid.");
        appendLine(errorReportContent,
                "If you know the address of the recipient please correct it and forward this email to him.");
        appendLine(errorReportContent,
                "!!! Note that the Tracking System will not try to send this email again !!!");
        appendLine(errorReportContent,
                "Please correct the recipient email address in openBIS to avoid similar problems in future.");
        appendLine(errorReportContent, "");
        appendLine(errorReportContent, "Subject:    " + email.getSubject());
        appendLine(errorReportContent, "Recipients: "
                + CollectionUtils.abbreviate(email.getRecipients(), -1));
        appendLine(errorReportContent, "");
        appendLine(errorReportContent, "Original content:");

        sendMessage(mailClient, "[Tracking] Sending an email failed",
                errorReportContent.toString(), null, tryGetFromField(email), email
                        .getReplyToOrNull());
    }

    private static void sendMessage(IMailClient mailClient, String subject, String content,
            String replyToOrNull, From fromOrNull, String... recipients)
    {
        mailClient.sendMessage(subject, content, replyToOrNull, fromOrNull, recipients);
        LogUtils.debug("Sending an email [" + subject + "]\n" + content);
    }

    private static void appendLine(StringBuffer sb, String msg)
    {
        sb.append(msg);
        sb.append("\n");
    }

    private static void saveTrackingState(TrackedEntities changedEntities, ITrackingDAO trackingDAO)
    {
        TrackingStateDTO state = new TrackingStateDTO();
        state.setLastSeenSequencingSampleId(calcMaxId(changedEntities.getSequencingSamples()));
        state.setLastSeenFlowLaneSampleId(calcMaxId(changedEntities.getFlowLaneSamples()));
        state.setLastSeenDatasetId(calcMaxId(changedEntities.getDataSets()));
        trackingDAO.saveTrackingState(state);
    }

    private static int calcMaxId(List<? extends IIdentifiable> entities)
    {
        long max = 0;
        for (IIdentifiable entity : entities)
        {
            max = Math.max(max, entity.getId());
        }
        // FIXME 2009--, Tomasz Pylak: refactor ids to long everywhere
        return (int) max;
    }

    private static TrackedEntities fetchChangedEntities(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, SessionContextDTO session)
    {
        List<Sample> sequencingSamples =
                listSamples(SEQUENCING_SAMPLE_TYPE, trackingState.getLastSeenSequencingSampleId(),
                        trackingServer, session);

        List<Sample> flowLaneSamples =
                listSamples(FLOW_LANE_SAMPLE_TYPE, trackingState.getLastSeenFlowLaneSampleId(),
                        trackingServer, session);

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(FLOW_LANE_SAMPLE_TYPE, trackingState
                        .getLastSeenDatasetId());
        List<ExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        return new TrackedEntities(sequencingSamples, flowLaneSamples, dataSets);
    }

    private static List<Sample> listSamples(String sampleType, int lastSeenSampleId,
            ITrackingServer trackingServer, SessionContextDTO session)
    {
        TrackingSampleCriteria criteria = new TrackingSampleCriteria(sampleType, lastSeenSampleId);
        return trackingServer.listSamples(session.getSessionToken(), criteria);
    }
}
