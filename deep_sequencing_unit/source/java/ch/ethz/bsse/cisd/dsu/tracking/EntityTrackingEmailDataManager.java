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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Manager that groups data about tracked entities into {@link EntityTrackingEmailData} objects.
 * 
 * @author Piotr Buczek
 */
class EntityTrackingEmailDataManager
{
    private final static String AFFILIATION = "AFFILIATION";

    private final static String CONTACT_PERSON_EMAIL = "CONTACT_PERSON_EMAIL";

    private final static String PRINCIPAL_INVESTIGATOR_EMAIL = "PRINCIPAL_INVESTIGATOR_EMAIL";

    private static Map<String, String> recipientsByAffiliation;

    public static void initialize(final Map<String, String> recipients)
    {
        recipientsByAffiliation = recipients;
    }

    public static Collection<EntityTrackingEmailData> groupByRecipient(
            TrackedEntities trackedEntities)
    {
        assert recipientsByAffiliation != null : "recipientsByAffiliation not initialized";
        // <recipients email, email data>
        final Map<String, EntityTrackingEmailData> dataByRecipient =
                new HashMap<String, EntityTrackingEmailData>();
        groupSequencingSamples(dataByRecipient, trackedEntities);
        groupFlowLaneSamples(dataByRecipient, trackedEntities);
        groupDataSetSamples(dataByRecipient, trackedEntities);
        return dataByRecipient.values();
    }

    /** Puts tracked sequencing samples grouped by recipient into <var>result</var>. */
    private static void groupSequencingSamples(Map<String, EntityTrackingEmailData> result,
            TrackedEntities trackedEntities)
    {
        for (Sample sequencingSample : trackedEntities.getSequencingSamples())
        {
            for (String recipient : getFlowLaneSampleTrackingRecipients(sequencingSample))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addSequencingSample(sequencingSample);
            }
        }
    }

    /** Puts tracked flow lane samples grouped by recipient into <var>result</var>. */
    private static void groupFlowLaneSamples(Map<String, EntityTrackingEmailData> result,
            TrackedEntities trackedEntities)
    {
        for (Sample flowLaneSample : trackedEntities.getFlowLaneSamples())
        {
            for (String recipient : getFlowLaneSampleTrackingRecipients(flowLaneSample))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addFlowLaneSample(flowLaneSample);
            }
        }
    }

    /** Puts tracked data sets grouped by recipient into <var>result</var>. */
    private static void groupDataSetSamples(Map<String, EntityTrackingEmailData> result,
            TrackedEntities trackedEntities)
    {
        for (ExternalData dataSet : trackedEntities.getDataSets())
        {
            for (String recipient : getDataSetTrackingRecipients(dataSet))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addDataSet(dataSet);
            }
        }
    }

    private static EntityTrackingEmailData getOrCreateRecipientEmailData(
            Map<String, EntityTrackingEmailData> dataByRecipient, String recipient)
    {
        EntityTrackingEmailData emailDataOrNull = dataByRecipient.get(recipient);
        if (emailDataOrNull == null)
        {
            emailDataOrNull = new EntityTrackingEmailData(recipient);
        }
        return emailDataOrNull;
    }

    /**
     * Returns an array of emails of recipient that should get a tracking information about given
     * <var>sequencingSample</var>.
     */
    private static String[] getSequencingSampleTrackingRecipients(Sample sequencingSample)
    {
        // Recipients are taken from properties of the sequencing sample.
        assert sequencingSample != null;

        final Set<String> recipientPropertyTypeCodes = new HashSet<String>();
        recipientPropertyTypeCodes.add(CONTACT_PERSON_EMAIL);
        recipientPropertyTypeCodes.add(PRINCIPAL_INVESTIGATOR_EMAIL);

        final List<String> recipients = new ArrayList<String>();
        for (IEntityProperty property : sequencingSample.getProperties())
        {
            final String propertyCode = property.getPropertyType().getCode();
            final String propertyValue = property.getValue();
            if (recipientPropertyTypeCodes.contains(propertyCode))
            {
                recipients.add(propertyValue);
            } else
            {
                // add recipient for affiliation if his email was specified in properties
                if (propertyCode.equals(AFFILIATION))
                {
                    String affiliationRecipientOrNull = recipientsByAffiliation.get(propertyValue);
                    if (affiliationRecipientOrNull != null)
                    {
                        recipients.add(affiliationRecipientOrNull);
                    }
                }
            }
        }

        return recipients.toArray(new String[0]);
    }

    /**
     * Returns an array of emails of recipient that should get a tracking information about given
     * <var>flowLaneSample</var>.
     */
    private static String[] getFlowLaneSampleTrackingRecipients(Sample flowLaneSample)
    {
        // Recipients are taken from properties of sequencing sample
        // that is a parent of the flow lane sample.
        assert flowLaneSample != null;
        return getSequencingSampleTrackingRecipients(flowLaneSample.getGeneratedFrom());
    }

    /**
     * Returns an array of emails of recipient that should get a tracking information about given
     * <var>dataSet</var>.
     */
    private static String[] getDataSetTrackingRecipients(ExternalData dataSet)
    {
        // Recipients are taken from properties of sequencing sample
        // that is a parent of a flow lane sample connected directly with the data set.
        assert dataSet != null;
        return getFlowLaneSampleTrackingRecipients(dataSet.getSample());
    }

}