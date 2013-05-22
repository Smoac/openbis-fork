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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackingStateDTO;
import ch.ethz.bsse.cisd.dsu.tracking.email.Email;
import ch.ethz.bsse.cisd.dsu.tracking.email.EmailWithSummary;
import ch.ethz.bsse.cisd.dsu.tracking.email.IEntityTrackingEmailGenerator;
import ch.ethz.bsse.cisd.dsu.tracking.utils.LogUtils;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.SearchOperator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Tomasz Pylak
 */
public class TrackingBO
{
    private static final String EXTERNAL_SAMPLE_NAME = "EXTERNAL_SAMPLE_NAME";

    private static final String LIBRARY_SAMPLE_TYPE = "LIBRARY";

    private static final String RAW_SAMPLE_TYPE = "RAW_SAMPLE";

    private static final String DATASET_ATTACHED_TO_SAMPLE_TYPE = "LIBRARY";

    private static final String PROCESSING_POSSIBLE_PROPERTY_CODE = "LIBRARY_PROCESSING_POSSIBLE_YES_NO";

    private static final String PROCESSING_SUCCESSFUL_PROPERTY_CODE =
            "LIBRARY_PROCESSING_SUCCESSFUL";

    private final static String CONTACT_PERSON_EMAIL = "CONTACT_PERSON_EMAIL";

    private final static String PRINCIPAL_INVESTIGATOR_EMAIL = "PRINCIPAL_INVESTIGATOR_EMAIL";

    private static final String TRUE = "true";

    private static final String YES = "YES";

    private final ITrackingServer trackingServer;

    private final IGeneralInformationService gis;

    private final IEntityTrackingEmailGenerator emailGenerator;

    private final IMailClient mailClient;

    public TrackingBO(ITrackingServer trackingServer, IGeneralInformationService gis, IEntityTrackingEmailGenerator emailGenerator,
            IMailClient mailClient)
    {
        this.trackingServer = trackingServer;
        this.gis = gis;
        this.emailGenerator = emailGenerator;
        this.mailClient = mailClient;
    }

    public void trackAndNotify(ITrackingDAO trackingDAO, SessionContextDTO session)
    {
        TrackingStateDTO prevTrackingState = trackingDAO.getTrackingState();

        TrackedEntities changedEntities =
                fetchChangedEntities(prevTrackingState, trackingServer, gis, session);
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
        TrackingStateDTO state =
                TrackingStateUpdateHelper.calcNewTrackingState(prevState, changedEntities);
        trackingDAO.saveTrackingState(state);
    }

    private static TrackedEntities fetchChangedEntities(TrackingStateDTO trackingState,
            ITrackingServer trackingServer, IGeneralInformationService gis, SessionContextDTO session)
    {
        List<Sample> sequencingSamplesToBeProcessed =
                listSequencingSamples(RAW_SAMPLE_TYPE, YES, PROCESSING_POSSIBLE_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsToBeProcessed(), trackingServer, gis, session);

        List<Sample> sequencingSamplesSuccessfullyProcessed =
                listSequencingSamples(LIBRARY_SAMPLE_TYPE, TRUE, PROCESSING_SUCCESSFUL_PROPERTY_CODE, trackingState
                        .getAlreadyTrackedSampleIdsProcessed(), trackingServer, gis, session);

        TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(DATASET_ATTACHED_TO_SAMPLE_TYPE, trackingState
                        .getLastSeenDatasetId());
        List<AbstractExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);

        return new TrackedEntities(sequencingSamplesToBeProcessed,
                sequencingSamplesSuccessfullyProcessed, dataSets);
    }

    private static List<Sample> listSequencingSamples(String SampleType, String propertyValue, String propertyTypeCode,
            Set<Long> alreadyTrackedSampleIds, ITrackingServer trackingServer, IGeneralInformationService gis,
            SessionContextDTO session)
    {
        return listSamples(SampleType, propertyTypeCode, propertyValue, alreadyTrackedSampleIds,
                trackingServer, gis, session);
    }

    private static List<Sample> listSamples(String sampleType, String propertyTypeCode,
            String propertyValue, Set<Long> alreadyTrackedSampleIds,
            ITrackingServer trackingServer, IGeneralInformationService gis, SessionContextDTO session)
    {
        TrackingSampleCriteria criteria =
                new TrackingSampleCriteria(sampleType, propertyTypeCode, propertyValue,
                        alreadyTrackedSampleIds);
        List<Sample> samples = trackingServer.listSamples(session.getSessionToken(), criteria);

        for (Sample sample : samples)
        {
            SearchCriteria parentCriteria = new SearchCriteria();
            parentCriteria.setOperator(SearchOperator.MATCH_ANY_CLAUSES);

            for (Sample parent : sample.getParents())
            {
                parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, parent.getCode()));
            }

            List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample> apiParents =
                    gis.searchForSamples(session.getSessionToken(), parentCriteria,
                            EnumSet.of(SampleFetchOption.PARENTS, SampleFetchOption.PROPERTIES));
            Set<Sample> dtoParents = new HashSet<Sample>();

            for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample apiParent : apiParents)
            {
                dtoParents.add(convertSample(apiParent));
            }
            sample.setParents(dtoParents);
        }

        return samples;
    }

    private static Sample convertSample(ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample apiSample)
    {
        Sample dtoSample = new Sample();

        EntityProperty person = new EntityProperty();
        EntityProperty pi = new EntityProperty();
        EntityProperty externalSampleName = new EntityProperty();

        List<IEntityProperty> propertyList = new ArrayList<IEntityProperty>();

        dtoSample.setCode(apiSample.getCode());
        String sampleTypeCode = apiSample.getSampleTypeCode();

        SampleType stc = new SampleType();
        stc.setCode(sampleTypeCode);

        dtoSample.setSampleType(stc);

        Map<String, String> properties = apiSample.getProperties();

        PropertyType personType = new PropertyType();
        PropertyType piType = new PropertyType();
        PropertyType externalSampleNameType = new PropertyType();

        personType.setCode(CONTACT_PERSON_EMAIL);
        piType.setCode(PRINCIPAL_INVESTIGATOR_EMAIL);
        externalSampleNameType.setCode(EXTERNAL_SAMPLE_NAME);

        person.setPropertyType(personType);
        person.setValue(properties.get(CONTACT_PERSON_EMAIL));
        propertyList.add(person);

        pi.setPropertyType(piType);
        pi.setValue(properties.get(PRINCIPAL_INVESTIGATOR_EMAIL));
        propertyList.add(pi);

        externalSampleName.setPropertyType(externalSampleNameType);
        externalSampleName.setValue(properties.get(EXTERNAL_SAMPLE_NAME));
        propertyList.add(externalSampleName);

        dtoSample.setProperties(propertyList);

        if (apiSample.getRetrievedFetchOptions().contains(SampleFetchOption.PARENTS))
        {
            Set<Sample> dtoParents = new HashSet<Sample>();
            for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample apiParent : apiSample.getParents())
            {
                dtoParents.add(convertSample(apiParent));
            }
            dtoSample.setParents(dtoParents);
        }
        return dtoSample;
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

            long lastSeenDatasetId =
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

        private static long calcMaxId(List<? extends IIdAndCodeHolder> entities, long initialValue)
        {
            long max = initialValue;
            for (IIdAndCodeHolder entity : entities)
            {
                max = Math.max(max, entity.getId());
            }
            return max;
        }
    }
}
