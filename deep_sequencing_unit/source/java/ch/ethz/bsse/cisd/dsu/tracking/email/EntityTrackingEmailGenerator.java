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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.ethz.bsse.cisd.dsu.tracking.email.EntityTrackingEmailData.SequencingSampleData;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
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
                        propertyKey.substring(0, AFFILIATION_NOTIFICATION_EMAIL_CONTACT_SUFFIX
                                .length() - 1);
                final String affiliationRecipient =
                        PropertyUtils.getMandatoryProperty(properties, propertyKey);
                result.put(affiliation, affiliationRecipient);
            }
        }
        return result;
    }

    public List<Email> generateEmails(TrackedEntities trackedEntities)
    {
        final Collection<EntityTrackingEmailData> emailDataGroupedByRecipient =
                EntityTrackingEmailDataManager.groupByRecipient(trackedEntities);

        final List<Email> results = new ArrayList<Email>();
        for (EntityTrackingEmailData emailData : emailDataGroupedByRecipient)
        {
            results.add(createEmail(emailData));
        }

        return results;
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

        private static final String PERMLINK_LABEL = "Permlink";

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
                final int flowLaneSamplesSize = sequencingSampleData.getFlowLaneSamples().size();
                if (sequencingSampleData.isNewlyTracked())
                {
                    String headingSuffix =
                            flowLaneSamplesSize == 0 ? "" : String.format(
                                    " and %d connected Flow Lane sample(s).", flowLaneSamplesSize);
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
                appendSampleDetails(sb, "Sequencing", sequencingSampleData.getSequencingSample());
                appendln(sb, SUBSECTION_SEPARATOR_LINE);
                for (Sample flowLaneSample : sequencingSampleData.getFlowLaneSamples())
                {
                    appendSampleDetails(sb, "Flow Lane", flowLaneSample);
                    appendln(sb, SUBSECTION_SEPARATOR_LINE);
                }
            }
        }

        private static void appendSampleDetails(StringBuilder sb, String sampleTypeLabel,
                Sample sample)
        {
            appendln(sb, String.format("%s sample '%s' details", sampleTypeLabel, sample.getCode()));

            // basic sample info
            appendAttribute(sb, PERMLINK_LABEL, sample.getPermlink());
            appendAttribute(sb, "Identifier", sample.getIdentifier());
            appendNewline(sb);

            // sample properties
            appendln(sb, "Filled sample properties:");
            appendProperties(sb, sample.getProperties());
        }

        private static void appendDataSetsData(StringBuilder sb, List<ExternalData> dataSets)
        {
            appendln(sb, SECTION_SEPARATOR_LINE);
            appendln(sb, SUBSECTION_SEPARATOR_LINE);
            appendln(sb, String.format(
                    "Tracked creation of %d data set(s) connected with Flow Lane samples.",
                    dataSets.size()));
            appendln(sb, SUBSECTION_SEPARATOR_LINE);
            for (ExternalData dataSet : dataSets)
            {
                appendDataSetDetails(sb, dataSet);
                appendln(sb, SUBSECTION_SEPARATOR_LINE);
            }
        }

        private static void appendDataSetDetails(StringBuilder sb, ExternalData dataSet)
        {
            appendln(sb, String.format("Data set '%s' details", dataSet.getCode()));

            // basic data set info
            appendAttribute(sb, PERMLINK_LABEL, dataSet.getPermlink());
            appendNewline(sb);

            // basic flow lane and sequencing sample info
            Sample flowLaneSample = dataSet.getSample();
            assert flowLaneSample != null;
            Sample sequencingSample = flowLaneSample.getGeneratedFrom();
            assert sequencingSample != null;
            appendAttribute(sb, "Flow Lane sample", String.format("'%s'\n  %s", flowLaneSample
                    .getCode(), flowLaneSample.getPermlink()));
            appendAttribute(sb, "Sequencing sample", String.format("'%s'\n  %s", sequencingSample
                    .getCode(), sequencingSample.getPermlink()));

            // data set properties
            appendln(sb, "Filled data set properties:");
            appendProperties(sb, dataSet.getProperties());
        }

        // NOTE: Information about properties assigned to entity type are not loaded.
        // If it would be available we could append information about all properties assigned
        // to entity type, not only about properties filled for specific entity. Additionally
        // we could group entities by in sections.
        private static void appendProperties(StringBuilder sb, List<IEntityProperty> properties)
        {
            for (IEntityProperty property : properties)
            {
                final String label = property.getPropertyType().getLabel();
                final String value = property.getValue();
                appendAttribute(sb, label, value);
            }
        }

        private static void appendAttribute(StringBuilder sb, String name, String value)
        {
            appendln(sb, String.format("- %s:\n  %s", name, value == null ? "(empty)" : value));
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
