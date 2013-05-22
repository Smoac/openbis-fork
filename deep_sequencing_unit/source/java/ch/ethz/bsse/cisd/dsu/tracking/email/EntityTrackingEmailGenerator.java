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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Piotr Buczek
 * @author Manuel Kohler
 */
public class EntityTrackingEmailGenerator implements IEntityTrackingEmailGenerator
{
    private static final String NOTIFICATION_EMAIL_FROM = "mail.from";

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

    @Override
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
        return new Email(subject, content, replyTo, from, filterBlanks(recipient.split(",|;| ")));
    }

    private static String[] filterBlanks(String[] arr)
    {
        ArrayList<String> result = new ArrayList<String>();

        for (String s : arr)
        {
            if (false == StringUtils.isBlank(s))
            {
                result.add(s.trim());
            }
        }

        return result.toArray(new String[0]);
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

        private final static String INDEX1_PROPERTY_CODE = "BARCODE";

        private final static String INDEX2_PROPERTY_CODE = "INDEX2";

        private static final String PERMLINK_LABEL = "See details in openBIS";

        private static final String GENERATED_CONTENT_TARGET = "{generated-content}";

        public static String fillTemplateWithData(String template, EntityTrackingEmailData emailData)
        {
            return template.replace(GENERATED_CONTENT_TARGET, generateContent(emailData));
        }

        private static String generateContent(EntityTrackingEmailData emailData)
        {
            StringBuilder sb = new StringBuilder();
            appendDataSetsData(sb, emailData.getDataSets());
            appendSequencingSamplesData(sb, emailData.getSequencingSamplesToBeProcessed(), false);
            appendSequencingSamplesData(sb, emailData.getSequencingSamplesProcessed(), true);
            return sb.toString();
        }

        private static String getName(Sample sequencingSample)
        {
            /**
             * We try to get the external sample name recursively in the sample tree. This means we take the first name in the tree. This results in
             * the following precedence: LIBARY -> RAW
             */

            String externalSampleName = getExternalSampleName(sequencingSample);
            if (externalSampleName == null || externalSampleName.isEmpty())
            {
                Set<Sample> parents = sequencingSample.getParents();
                for (Sample parent : parents)
                {
                    externalSampleName = getName(parent);
                }
            }

            return externalSampleName;

        }

        private static void appendSequencingSamplesData(StringBuilder sb,
                Collection<Sample> sequencingSamples, boolean processed)
        {
            for (Sample sequencingSample : sequencingSamples)
            {
                String externalSampleName = getName(sequencingSample);

                appendln(sb, SECTION_SEPARATOR_LINE);
                appendln(sb, SUBSECTION_SEPARATOR_LINE);

                // append Sequencing sample details
                if (processed)
                {
                    appendSampleDetails(sb,
                            String.format("Library processing of sample '%s' was successful.",
                                    externalSampleName), sequencingSample);
                    appendln(sb, SUBSECTION_SEPARATOR_LINE);
                } else
                {
                    appendSampleDetails(sb, String.format(
                            "Library processing of sample '%s' is possible.", externalSampleName),
                            sequencingSample);
                    appendln(sb, SUBSECTION_SEPARATOR_LINE);
                }
            }
        }

        private static void appendSampleDetails(StringBuilder sb, String heading, Sample sample)
        {
            appendln(sb, heading);

            // basic sample info
            appendAttribute(sb, PERMLINK_LABEL, sample.getSearchlink());
            appendAttribute(sb, "Sample identifier", sample.getIdentifier());
            appendNewline(sb);

            // sample properties
            appendProperties(sb, sample.getProperties());
        }

        private static void appendDataSetsData(StringBuilder sb, List<AbstractExternalData> dataSets)
        {
            if (dataSets.isEmpty())
            {
                return;
            }
            appendln(sb, SECTION_SEPARATOR_LINE);
            appendln(sb, SUBSECTION_SEPARATOR_LINE);
            appendln(sb, "There are new sequencing results available to you.");
            appendln(sb, SUBSECTION_SEPARATOR_LINE);

            // Using a TreeMap, so the keys are sorted
            TreeMap<String, List<AbstractExternalData>> sampleMap = new TreeMap<String, List<AbstractExternalData>>();
            List<AbstractExternalData> dsList = new ArrayList<AbstractExternalData>();

            // we just loop over the data sets and write the connected samples as keys
            // and the data sets as values in a map, so that we can group together as
            // data sets per lane
            for (AbstractExternalData dataSet : dataSets)
            {
                Sample s = dataSet.getSample();
                if (sampleMap.containsKey(s.getIdentifier()))
                {
                    dsList = sampleMap.get(s.getIdentifier());
                }
                dsList.add(dataSet);
                sampleMap.put(s.getIdentifier(), dsList);
                dsList = new ArrayList<AbstractExternalData>();
            }

            // now we can write out this per sample
            Iterator<Entry<String, List<AbstractExternalData>>> it = sampleMap.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pairs = (Map.Entry) it.next();
                appendln(sb, String.format("Results for %s", pairs.getKey()));
                dsList = (List<AbstractExternalData>) pairs.getValue();
                for (AbstractExternalData ed : dsList)
                {
                    appendDataSetDetails(sb, ed);
                }
                it.remove(); // avoids a ConcurrentModificationException
                appendln(sb, SUBSECTION_SEPARATOR_LINE);
            }
        }

        private static void appendDataSetDetails(StringBuilder sb, AbstractExternalData dataSet)
        {
            Collection<Sample> sequencingSamples;

            Sample flowLaneSample = dataSet.getSample();
            assert flowLaneSample != null;
            sequencingSamples = flowLaneSample.getParents();
            assert sequencingSamples != null;

            String Index1 = getIndex1(dataSet);
            String Index2 = getIndex2(dataSet);
            String Index = null;

            if (Index1 != null)
            {
                Index = Index1;
            }
            if (Index2 != null)
            {
                Index = Index + "-" + Index2;
            }

            if (Index != null)
            {
                appendln(sb, "Data Set Type: " + dataSet.getDataSetType().toString() +
                        " Index: " + Index);
            }
            else
            {
                appendln(sb, "Data Set Type: " + dataSet.getDataSetType().toString());
            }
            appendln(sb, dataSet.getPermlink());
        }

        private static String getExternalSampleName(Sample sequencingSample)
        {
            String externalSampleName =
                    tryGetSamplePropertyValue(sequencingSample, EXTERNAL_SAMPLE_NAME_PROPERTY_CODE);
            // assert externalSampleName != null;
            if (externalSampleName == null)
            {
                externalSampleName = "";
            }
            return externalSampleName;
        }

        private static String getIndex1(AbstractExternalData dataSet)
        {
            List<IEntityProperty> properties = dataSet.getProperties();

            String Index = null;
            for (IEntityProperty p : properties)
            {
                if (p.getPropertyType().getCode().equals(INDEX1_PROPERTY_CODE))
                {
                    Index = p.getVocabularyTerm().getCode();
                    if (!Index.equals("NOINDEX"))
                    {
                        return Index;
                    }
                }
            }
            return null;
        }

        private static String getIndex2(AbstractExternalData dataSet)
        {
            List<IEntityProperty> properties = dataSet.getProperties();

            String Index = null;
            for (IEntityProperty p : properties)
            {
                if (p.getPropertyType().getCode().equals(INDEX2_PROPERTY_CODE))
                {
                    Index = p.getVocabularyTerm().getCode();
                    if (!Index.equals("NOINDEX"))
                    {
                        return Index;
                    }
                }
            }
            return null;
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

    public static void findSampleWithExternalSampleName(Sample sequencingSample)
    {
        // TODO Auto-generated method stub

    }

}
