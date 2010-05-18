/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * Class managing indirect relation from Data Sets to Samples. 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetManager
{
    private final Map<Long, MsInjectionSample> samples = new LinkedHashMap<Long, MsInjectionSample>();
    
    public void addSample(Sample plainSample)
    {
        MsInjectionSample sample = new MsInjectionSample(plainSample);
        samples.put(plainSample.getId(), sample);
        Experiment experiment = plainSample.getExperiment();
        if (experiment == null)
        {
            throw new UserFailureException("Sample '" + plainSample.getIdentifier() + "' of type '"
                    + plainSample.getSampleType() + "' does not belong to an experiment.");
        }
    }

    public List<MsInjectionSample> getSamples()
    {
        return new ArrayList<MsInjectionSample>(samples.values());
    }
    
    public void gatherDataSets(IDatasetLister datasetLister)
    {
        List<ExternalData> dataSets = getDataSets(datasetLister);
        Map<Long, Long> dataSetSampleMap = mapDataSetsOnSamples(dataSets, datasetLister);
        for (ExternalData dataSet : dataSets)
        {
            Long sampleID = dataSetSampleMap.get(dataSet.getId());
            if (sampleID != null)
            {
                MsInjectionSample sample = samples.get(sampleID);
                if (sample != null)
                {
                    sample.addLatestDataSet(dataSet);
                }
            }
        }
    }

    private List<ExternalData> getDataSets(IDatasetLister datasetLister)
    {
        Set<TechId> experimentIds = new LinkedHashSet<TechId>();
        for (MsInjectionSample sample : samples.values())
        {
            experimentIds.add(new TechId(sample.getSample().getExperiment().getId()));
        }
        List<ExternalData> result = new ArrayList<ExternalData>();
        List<ExternalData> dataSets = datasetLister.listByExperimentTechIds(experimentIds);
        for (ExternalData dataSet : dataSets)
        {
            result.add(dataSet);
        }
        return result;
    }
    
    private Map<Long, Long> mapDataSetsOnSamples(List<ExternalData> dataSets,
            IDatasetLister datasetLister)
    {
        List<ExternalData> descendentDataSets = new ArrayList<ExternalData>();
        Map<Long, Long> dataSetSampleMap = new HashMap<Long, Long>();
        Set<Long> ids = new LinkedHashSet<Long>();
        for (ExternalData dataSet : dataSets)
        {
            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                dataSetSampleMap.put(dataSet.getId(), sample.getId());
            } else
            {
                descendentDataSets.add(dataSet);
                ids.add(dataSet.getId());
            }
        }
        Map<Long, Set<Long>> parentIds = datasetLister.listParentIds(ids);
        while (descendentDataSets.isEmpty() == false)
        {
            boolean nothingRemoved = true;
            for (Iterator<ExternalData> iterator = descendentDataSets.iterator(); iterator
                    .hasNext();)
            {
                ExternalData dataSet = iterator.next();
                Set<Long> parent = parentIds.get(dataSet.getId());
                if (parent == null)
                {
                    iterator.remove();
                    nothingRemoved = false;
                    continue;
                }
                if (parent.size() != 1)
                {
                    throw new UserFailureException("Data set '" + dataSet.getCode() + "' has "
                            + parent.size() + " instead of one parent data set.");

                }
                Long sampleID = dataSetSampleMap.get(parent.iterator().next());
                if (sampleID != null)
                {
                    dataSetSampleMap.put(dataSet.getId(), sampleID);
                    iterator.remove();
                    nothingRemoved = false;
                }
            }
            assertSomeDataSetsAreRemoved(nothingRemoved, descendentDataSets);
        }
        return dataSetSampleMap;
    }

    private void assertSomeDataSetsAreRemoved(boolean nothingRemoved,
            List<ExternalData> descendentDataSets)
    {
        if (nothingRemoved)
        {
            StringBuilder builder = new StringBuilder();
            for (ExternalData externalData : descendentDataSets)
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(externalData.getCode());
            }
            throw new UserFailureException("Following data sets have wrong parents: "
                    + builder.toString().trim());
        }
    }

}
