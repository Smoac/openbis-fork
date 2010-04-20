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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Structure containing all data about tracked entities that will be used in a single email to an
 * recipient. Its purpose is to group data that will be used to generate content of an email
 * containing merged information about all events that the recipient should be notified about.
 * 
 * @author Piotr Buczek
 */
public class EntityTrackingEmailData
{
    private List<Sample> sequencingSamplesToBeProcessed;

    private List<Sample> sequencingSamplesProcessed;

    private final List<ExternalData> dataSets = new ArrayList<ExternalData>(0);

    private final String recipient;

    /** creates email data for given <var>recipient</var> */
    public EntityTrackingEmailData(String recipient)
    {
        this.recipient = recipient;
    }

    public String getRecipient()
    {
        return recipient;
    }

    public List<Sample> getSequencingSamplesToBeProcessed()
    {
        return sequencingSamplesToBeProcessed;
    }

    public List<Sample> getSequencingSamplesProcessed()
    {
        return sequencingSamplesToBeProcessed;
    }

    public List<ExternalData> getDataSets()
    {
        return dataSets;
    }

    /** adds info about newly tracked sequencing sample to be processed */
    public void addSequencingSampleToBeProcessed(Sample sequencingSample)
    {
        sequencingSamplesToBeProcessed.add(sequencingSample);
    }

    /** adds info about newly tracked sequencing sample successfully processed */
    public void addSequencingSample(Sample sequencingSample)
    {
        sequencingSamplesToBeProcessed.add(sequencingSample);
    }

    /** adds info about newly tracked data set */
    public void addDataSet(ExternalData dataSet)
    {
        dataSets.add(dataSet);
    }

    /** short description of data kept in the structure */
    public String getDescription()
    {
        final StringBuilder sb = new StringBuilder();
        appendSamplesInfo(sb, sequencingSamplesToBeProcessed, "possible for processing");
        appendSamplesInfo(sb, sequencingSamplesProcessed, "successfully processed");
        appendDataSetsInfo(sb);
        return sb.toString();
    }

    private void appendSamplesInfo(final StringBuilder sb, final List<Sample> samples,
            final String actionDescription)
    {
        if (sequencingSamplesToBeProcessed.isEmpty())
        {
            sb.append(String.format("no new samples are %s\n", actionDescription));
        } else
        {
            for (Sample seqencingSample : sequencingSamplesToBeProcessed)
            {
                final String sequencingSampleIdentifier = seqencingSample.getIdentifier();
                sb.append(String.format("Sequencing sample: '%s' is %s",
                        sequencingSampleIdentifier, actionDescription));
                sb.append("\n");
            }
        }
    }

    private void appendDataSetsInfo(final StringBuilder sb)
    {
        if (getDataSets().isEmpty())
        {
            sb.append("no new data sets tracked");
        } else
        {
            sb.append(getDataSets().size() + " new data set(s) tracked: ");
            for (ExternalData dataSet : getDataSets())
            {
                sb.append(dataSet.getIdentifier() + ", ");
            }
        }
        sb.append("\n");
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return getDescription();
    }

}
