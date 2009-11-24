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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static class SequencingSampleData
    {
        private final boolean newlyTracked;

        private final Sample sequencingSample;

        private final List<Sample> flowLaneSamples = new ArrayList<Sample>(0);

        public SequencingSampleData(Sample sequencingSample, boolean newlyTracked)
        {
            super();
            this.sequencingSample = sequencingSample;
            this.newlyTracked = newlyTracked;
        }

        public void addFlowLaneSample(Sample flowLaneSample)
        {
            flowLaneSamples.add(flowLaneSample);
        }

        public Sample getSequencingSample()
        {
            return sequencingSample;
        }

        public List<Sample> getFlowLaneSamples()
        {
            return flowLaneSamples;
        }

        public boolean isNewlyTracked()
        {
            return newlyTracked;
        }
    }

    // data grouped by sequencing sample: <sequencing sample id, data>
    private final Map<Long, SequencingSampleData> sequencingSampleData =
            new HashMap<Long, SequencingSampleData>(0);

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

    public Collection<SequencingSampleData> getSequencingSamplesData()
    {
        return sequencingSampleData.values();
    }

    public List<ExternalData> getDataSets()
    {
        return dataSets;
    }

    /** adds info about newly tracked sequencing */
    public void addSequencingSample(Sample sequencingSample)
    {
        createSequencingSampleData(sequencingSample, true);
    }

    /** adds info about newly tracked flow lane sample */
    public void addFlowLaneSample(Sample flowLaneSample)
    {
        // if data about sequencing sample is not yet added add it too
        final Sample sequencingSample = flowLaneSample.getGeneratedFrom();
        assert sequencingSample != null;

        SequencingSampleData infoOrNull = sequencingSampleData.get(sequencingSample.getId());
        if (infoOrNull == null)
        {
            // because sequencing samples are processed before flow lane samples
            // the sequencing sample we have here must have existed before
            infoOrNull = createSequencingSampleData(sequencingSample, false);
        }

        infoOrNull.addFlowLaneSample(flowLaneSample);
    }

    private SequencingSampleData createSequencingSampleData(Sample sequencingSample,
            boolean newlyTracked)
    {
        final SequencingSampleData data = new SequencingSampleData(sequencingSample, newlyTracked);
        sequencingSampleData.put(sequencingSample.getId(), data);
        return data;
    }

    /** adds info about newly tracked data set */
    public void addDataSet(ExternalData dataSet)
    {
        dataSets.add(dataSet);
    }

}
