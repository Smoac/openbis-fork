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

package ch.ethz.bsse.cisd.dsu.tracking.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.email.EntityTrackingEmailData.SequencingSampleData;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Piotr Buczek
 */
public class EntityTrackingEmailGenerator implements IEntityTrackingEmailGenerator
{
    private static final String NOTIFICATION_EMAIL_FROM = "notification-email-from";

    private static final String NOTIFICATION_EMAIL_REPLY_TO = "notification-email-reply-to";

    private static final String NOTIFICATION_EMAIL_SUBJECT = "notification-email-subject";

    private static final String AFFILIATION_NOTIFICATION_EMAIL_CONTACT_SUFFIX =
            "-affiliation-notification-email-contact";

    private final String from;

    private final String replyTo;

    private final String subject;

    private final String template;

    public EntityTrackingEmailGenerator(Properties properties, String template)
    {
        this.from = PropertyUtils.getMandatoryProperty(properties, NOTIFICATION_EMAIL_FROM);
        this.replyTo = PropertyUtils.getMandatoryProperty(properties, NOTIFICATION_EMAIL_REPLY_TO);
        this.subject = PropertyUtils.getMandatoryProperty(properties, NOTIFICATION_EMAIL_SUBJECT);
        this.template = template;

        final Map<String, String> recipientsByAffiliation =
                retrieveRecipientsByAffiliation(properties);
        EntityTrackingEmailDataManager.initialize(recipientsByAffiliation);
    }

    // <affiliation, recipient email>
    private Map<String, String> retrieveRecipientsByAffiliation(Properties properties)
    {
        final Map<String, String> result = new HashMap<String, String>();

        for (Object key : properties.keySet())
        {
            final String propertyKey = (String) key;
            if (propertyKey.endsWith(AFFILIATION_NOTIFICATION_EMAIL_CONTACT_SUFFIX))
            {
                final String affiliation =
                        propertyKey.substring(0, propertyKey.length()
                                - AFFILIATION_NOTIFICATION_EMAIL_CONTACT_SUFFIX.length());
                final String affiliationRecipient =
                        PropertyUtils.getMandatoryProperty(properties, propertyKey);
                result.put(affiliation, affiliationRecipient);
            }
        }
        return result;
    }

    public List<EmailWithSummary> generateEmails(TrackedEntities trackedEntities)
    {
        final Collection<EntityTrackingEmailData> emailDataGroupedByRecipient =
                EntityTrackingEmailDataManager.groupByRecipient(trackedEntities);

        final List<EmailWithSummary> results = new ArrayList<EmailWithSummary>();
        for (EntityTrackingEmailData emailData : emailDataGroupedByRecipient)
        {
            results.add(createEmailWithSummary(emailData));
        }

        return results;
    }

    private EmailWithSummary createEmailWithSummary(EntityTrackingEmailData emailData)
    {
        return new EmailWithSummary(createEmail(emailData), emailData.getDescription());
    }

    private Email createEmail(EntityTrackingEmailData emailData)
    {
        String content = EmailContentGenerator.fillTemplateWithData(template, emailData);
        String recipient = emailData.getRecipient();
        return new Email(subject, content, replyTo, from, recipient);
    }

    /**
     * Helper class for generation of email content.
     * 
     * @author Piotr Buczek
     */
    private static final class EmailContentGenerator
    {
        private static final char NEW_LINE = '\n';

        private static final int SEPARATOR_LINE_WIDTH = 100;

        private static final char SECTION_SEPARATOR_CHAR = '#';

        private static final char SUBSECTION_SEPARATOR_CHAR = '-';

        private static final String SECTION_SEPARATOR_LINE =
                createSeparatorLine(SECTION_SEPARATOR_CHAR);

        private static final String SUBSECTION_SEPARATOR_LINE =
                createSeparatorLine(SUBSECTION_SEPARATOR_CHAR);

        private final static String EXTERNAL_SAMPLE_NAME_PROPERTY_CODE = "EXTERNAL_SAMPLE_NAME";

        private static final String PERMLINK_LABEL = "See details in openBIS";

        private static final String GENARATED_CONTENT_TARGET = "{generated-content}";

        public static String fillTemplateWithData(String template, EntityTrackingEmailData emailData)
        {
            return template.replace(GENARATED_CONTENT_TARGET, generateContent(emailData));
        }

        private static String generateContent(EntityTrackingEmailData emailData)
        {
            StringBuilder sb = new StringBuilder();
            appendSequencingSamplesData(sb, emailData.getSequencingSamplesData());
            appendDataSetsData(sb, emailData.getDataSets());
            return sb.toString();
        }

        private static void appendSequencingSamplesData(StringBuilder sb,
                Collection<SequencingSampleData> sequencingSamplesData)
        {
            for (SequencingSampleData sequencingSampleData : sequencingSamplesData)
            {
                appendln(sb, SECTION_SEPARATOR_LINE);
                appendln(sb, SUBSECTION_SEPARATOR_LINE);
                // heading of section depends on whether sequencing sample already existed
                String sectionHeading;
                final String sequencingSampleCode =
                        sequencingSampleData.getSequencingSample().getCode();
                final String externalSampleName =
                        getExternalSampleName(sequencingSampleData.getSequencingSample());

                final int flowLaneSamplesSize = sequencingSampleData.getFlowLaneSamples().size();

                if (sequencingSampleData.isNewlyTracked())
                {
                    String headingSuffix =
                            flowLaneSamplesSize == 0 ? "" : String.format(
                                    " and %d connected Flow Lane sample(s)", flowLaneSamplesSize);
                    sectionHeading =
                            String.format("Tracked creation of Sequencing sample '%s'%s.",
                                    sequencingSampleCode, headingSuffix);
                } else
                {
                    sectionHeading =
                            String.format("Tracked creation of %d Flow Lane sample(s) "
                                    + "connected with Sequencing sample '%s'.",
                                    flowLaneSamplesSize, sequencingSampleCode);
                }
                appendln(sb, sectionHeading);
                appendln(sb, SUBSECTION_SEPARATOR_LINE);

                // append Sequencing sample details and then Flow Lane samples in subsections
                appendSampleDetails(sb, String.format(
                        "Your order for sample '%s' is now queued for sequencing.",
                        externalSampleName), sequencingSampleData.getSequencingSample());
                appendln(sb, SUBSECTION_SEPARATOR_LINE);
                for (Sample flowLaneSample : sequencingSampleData.getFlowLaneSamples())
                {
                    appendSampleDetails(sb, String.format("Sample '%s' is now being sequenced.",
                            externalSampleName), flowLaneSample);
                    appendln(sb, SUBSECTION_SEPARATOR_LINE);
                }
            }
        }

        private static void appendSampleDetails(StringBuilder sb, String heading, Sample sample)
        {
            appendln(sb, heading);

            // basic sample info
            appendAttribute(sb, PERMLINK_LABEL, sample.getPermlink());
            appendAttribute(sb, "Sample identifier", sample.getIdentifier());
            appendNewline(sb);

            // sample properties
            appendProperties(sb, sample.getProperties());
        }

        private static void appendDataSetsData(StringBuilder sb, List<ExternalData> dataSets)
        {
            if (dataSets.isEmpty())
            {
                return;
            }
            appendln(sb, SECTION_SEPARATOR_LINE);
            appendln(sb, SUBSECTION_SEPARATOR_LINE);
            appendln(sb, "There are new sequencing results available to you.");
            appendln(sb, SUBSECTION_SEPARATOR_LINE);
            for (ExternalData dataSet : dataSets)
            {
                appendDataSetDetails(sb, dataSet);
                appendln(sb, SUBSECTION_SEPARATOR_LINE);
            }
        }

        private static void appendDataSetDetails(StringBuilder sb, ExternalData dataSet)
        {
            Sample flowLaneSample = dataSet.getSample();
            assert flowLaneSample != null;
            Sample sequencingSample = flowLaneSample.getGeneratedFrom();
            assert sequencingSample != null;
            String externalSampleName = getExternalSampleName(sequencingSample);

            // link to openbis
            appendAttribute(sb, String.format(
                    "You can dowload results for extrernal sample named '%s' at",
                    externalSampleName), dataSet.getPermlink());

            // data set properties
            appendProperties(sb, dataSet.getProperties());

            // sequencing sample info
            appendAttribute(sb, String.format(
                    "Meta data of Sequencing sample '%s' are available here", externalSampleName),
                    sequencingSample.getPermlink());

        }

        private static String getExternalSampleName(Sample sequencingSample)
        {
            String externalSampleName =
                    tryGetSamplePropertyValue(sequencingSample, EXTERNAL_SAMPLE_NAME_PROPERTY_CODE);
            assert externalSampleName != null;
            return externalSampleName;
        }

        private static String tryGetSamplePropertyValue(Sample sequencingSample, String propertyCode)
        {
            String result = null;
            for (IEntityProperty property : sequencingSample.getProperties())
            {
                if (property.getPropertyType().getCode().equals(propertyCode))
                {
                    result = StringEscapeUtils.unescapeHtml(property.getValue());
                    break;
                }
            }
            return result;
        }

        // NOTE: Information about properties assigned to entity type are not loaded.
        // If it would be available we could append information about all properties assigned
        // to entity type, not only about properties filled for specific entity. Additionally
        // we could group entities by in sections.
        private static void appendProperties(StringBuilder sb, List<IEntityProperty> properties)
        {
            Collections.sort(properties); // sorting by property label or code if there is no label
            for (IEntityProperty property : properties)
            {
                final String label = property.getPropertyType().getLabel();
                final String valueOrNull = property.tryGetAsString();
                appendAttribute(sb, label, valueOrNull);
            }
        }

        private static void appendAttribute(StringBuilder sb, String name, String valueOrNull)
        {
            appendln(sb, String.format("- %s:\n\t\t%s", StringEscapeUtils.unescapeHtml(name),
                    valueOrNull == null ? "(empty)" : StringEscapeUtils.unescapeHtml(valueOrNull)));
        }

        private static void appendln(StringBuilder sb, String string)
        {
            sb.append(string);
            appendNewline(sb);
        }

        private static void appendNewline(StringBuilder sb)
        {
            sb.append(NEW_LINE);
        }

        private static String createSeparatorLine(char separatorChar)
        {
            char[] line = new char[SEPARATOR_LINE_WIDTH];
            Arrays.fill(line, separatorChar);
            return new String(line);
        }

    }

}
