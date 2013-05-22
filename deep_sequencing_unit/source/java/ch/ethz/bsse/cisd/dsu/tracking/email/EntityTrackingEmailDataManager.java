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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

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

    private final static String MASTER_SAMPLE_TYPE = "MASTER_SAMPLE";

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
        groupSequencingSamplesToBeProcessed(dataByRecipient, trackedEntities);
        groupSequencingSamplesProcessed(dataByRecipient, trackedEntities);
        groupDataSetSamples(dataByRecipient, trackedEntities);
        return dataByRecipient.values();
    }

    /** Puts tracked sequencing samples to be processed grouped by recipient into <var>result</var>. */
    private static void groupSequencingSamplesToBeProcessed(
            Map<String, EntityTrackingEmailData> result, TrackedEntities trackedEntities)
    {
        for (Sample sequencingSample : trackedEntities.getSequencingSamplesToBeProcessed())
        {
            for (String recipient : getSequencingSampleTrackingRecipients(Collections
                    .singleton(sequencingSample)))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addSequencingSampleToBeProcessed(sequencingSample);
            }
        }
    }

    /** Puts tracked processed sequencing samples grouped by recipient into <var>result</var>. */
    private static void groupSequencingSamplesProcessed(
            Map<String, EntityTrackingEmailData> result, TrackedEntities trackedEntities)
    {
        for (Sample sequencingSample : trackedEntities.getSequencingSamplesProcessed())
        {
            for (String recipient : getSequencingSampleTrackingRecipients(Collections
                    .singleton(sequencingSample)))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addSequencingSampleProcessed(sequencingSample);
            }
        }
    }

    /** Puts tracked data sets grouped by recipient into <var>result</var>. */
    private static void groupDataSetSamples(Map<String, EntityTrackingEmailData> result,
            TrackedEntities trackedEntities)
    {
        for (AbstractExternalData dataSet : trackedEntities.getDataSets())
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
            dataByRecipient.put(recipient, emailDataOrNull);
        }
        return emailDataOrNull;
    }

    /**
     * Returns a set of emails of recipients that should get a tracking information about given <var>sequencingSample</var>.<br>
     */
    // NOTE: Set is needed because one recipient can occur in many roles for one sample
    private static Set<String> getSequencingSampleTrackingRecipients(
            Collection<Sample> sequencingSamples)
    {
        assert sequencingSamples != null;

        final Set<String> recipients = new HashSet<String>();

        // Recipients are taken from properties of the sequencing sample.
        final Set<String> recipientPropertyTypeCodes = new HashSet<String>();
        recipientPropertyTypeCodes.add(CONTACT_PERSON_EMAIL);
        recipientPropertyTypeCodes.add(PRINCIPAL_INVESTIGATOR_EMAIL);

        SampleType masterSampleType = new SampleType();
        masterSampleType.setCode(MASTER_SAMPLE_TYPE);

        for (Sample sequencingSample : sequencingSamples)
        {

            findMasterSample(sequencingSample, recipients);

            // SampleType seqSampleSampleType = sequencingSample.getSampleType();
            // if (seqSampleSampleType.equals(masterSampleType))
            // {
            // Set<String> r = extractProperties(recipientPropertyTypeCodes, sequencingSample);
            // recipients.addAll(r);
            // }
            // else
            // {
            // Set<Sample> parents = sequencingSample.getParents();
            // assert parents != null;
            // for (Sample parent : parents)
            // {
            // SampleType sampleType = parent.getSampleType();
            //
            // if (false == sampleType.equals(masterSampleType))
            // {
            // Set<Sample> grandParents = parent.getParents();
            // for (Sample grandparent : grandParents)
            // {
            // Set<String> r = extractProperties(recipientPropertyTypeCodes, grandparent);
            // recipients.addAll(r);
            // }
            // }
            // else
            // {
            // {
            // Set<String> r = extractProperties(recipientPropertyTypeCodes, sequencingSample);
            // recipients.addAll(r);
            // }
            // }
            //
            // }
            //
            // }
        }
        return recipients;
    }

    private static void findMasterSample(Sample s, Set<String> recipients)
    {
        SampleType masterSampleType = new SampleType();
        masterSampleType.setCode(MASTER_SAMPLE_TYPE);
        // Recipients are taken from properties of the sequencing sample.
        final Set<String> recipientPropertyTypeCodes = new HashSet<String>();
        recipientPropertyTypeCodes.add(CONTACT_PERSON_EMAIL);
        recipientPropertyTypeCodes.add(PRINCIPAL_INVESTIGATOR_EMAIL);

        SampleType seqSampleSampleType = s.getSampleType();
        if (seqSampleSampleType.equals(masterSampleType))
        {
            Set<String> r = extractProperties(recipientPropertyTypeCodes, s);
            recipients.addAll(r);
        }
        else
        {
            for (Sample parent : s.getParents())
            {
                findMasterSample(parent, recipients);

            }
        }

    }

    private static Set<String> extractProperties(final Set<String> recipientPropertyTypeCodes, Sample sequencingSample)
    {
        Set<String> recipients = new HashSet<String>();
        for (IEntityProperty property : sequencingSample.getProperties())
        {
            final String propertyCode = property.getPropertyType().getCode();
            final String propertyValue = property.tryGetAsString();
            if (recipientPropertyTypeCodes.contains(propertyCode))
            {
                recipients.add(propertyValue);
            } else
            {
                // add recipient for affiliation if his email was specified in properties
                if (propertyCode.equals(AFFILIATION))
                {
                    String affiliationRecipientOrNull =
                            recipientsByAffiliation.get(propertyValue);
                    if (affiliationRecipientOrNull != null)
                    {
                        recipients.add(affiliationRecipientOrNull);
                    }
                }
            }
        }
        return recipients;
    }

    /**
     * Returns a set of emails of recipients that should get a tracking information about given <var>flowLaneSample</var>.
     */
    private static Set<String> getSampleTrackingRecipients(Sample sample)
    {
        // Recipients are taken from properties of master sample
        // that is a parent/grandparent of a library/raw sample
        assert sample != null;
        return getSequencingSampleTrackingRecipients(sample.getParents());
    }

    /**
     * Returns a set of emails of recipients that should get a tracking information about given <var>dataSet</var>.
     */
    private static Set<String> getDataSetTrackingRecipients(AbstractExternalData dataSet)
    {
        // Recipients are taken from properties of master sample
        // that is a parent/grandparent of a library/raw sample connected with the data set.
        assert dataSet != null;
        return getSampleTrackingRecipients(dataSet.getSample());
    }

}