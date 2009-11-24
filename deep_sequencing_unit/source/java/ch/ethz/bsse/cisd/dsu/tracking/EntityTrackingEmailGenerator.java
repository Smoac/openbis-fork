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

package ch.ethz.bsse.cisd.dsu.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author Piotr Buczek
 */
public class EntityTrackingEmailGenerator implements IEntityTrackingEmailGenerator
{
    private static final String NOTIFICATION_EMAIL_SUBJECT = "notification-email-subject";

    private static final String NOTIFICATION_EMAIL_FROM = "notification-email-from";

    private static final String NOTIFICATION_EMAIL_REPLY_TO = "notification-email-reply-to";

    private final String subject;

    private final String from;

    private final String replyTo;

    public EntityTrackingEmailGenerator(Properties properties)
    {
        subject = PropertyUtils.getMandatoryProperty(properties, NOTIFICATION_EMAIL_SUBJECT);
        from = PropertyUtils.getMandatoryProperty(properties, NOTIFICATION_EMAIL_FROM);
        replyTo = PropertyUtils.getMandatoryProperty(properties, NOTIFICATION_EMAIL_REPLY_TO);
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
        String content = EmailContentGenerator.generate(emailData);
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

        private static String createSeparatorLine(char separatorChar)
        {
            char[] line = new char[SEPARATOR_LINE_WIDTH];
            Arrays.fill(line, separatorChar);
            return new String(line);
        }

        // TODO 2009-11-23, Piotr Buczek: implement
        public static String generate(EntityTrackingEmailData emailData)
        {
            StringBuilder sb = new StringBuilder();
            appendDataSetsInfo(sb, emailData.getDataSets());
            return null;
        }

        private static void appendDataSetsInfo(StringBuilder sb, List<ExternalData> dataSets)
        {
            appendln(sb, SECTION_SEPARATOR_LINE);
            appendln(sb, SUBSECTION_SEPARATOR_LINE);
            appendln(sb, String.format(
                    "Tracked creation of %s data set(s) connected with Flow Lane samples.",
                    dataSets.size()));
            appendln(sb, SUBSECTION_SEPARATOR_LINE);
            for (ExternalData dataSet : dataSets)
            {
                appendDataSetInfo(sb, dataSet);
                appendln(sb, SUBSECTION_SEPARATOR_LINE);
            }
        }

        private static void appendDataSetInfo(StringBuilder sb, ExternalData dataSet)
        {
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

            // no information about assigned properties - cannot put information about empty
            // List<DataSetTypePropertyType> assignedProperties =
            // dataSet.getDataSetType().getAssignedPropertyTypes();
            for (IEntityProperty property : dataSet.getProperties())
            {
                appendProperty(sb, property);
            }
        }

        private static void appendProperty(StringBuilder sb, IEntityProperty property)
        {
            final String label = property.getPropertyType().getLabel();
            final String value = property.getValue();
            appendAttribute(sb, label, value);
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

    }

}
