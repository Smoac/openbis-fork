/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * Helper class for finding the root entities of entities belonging to the same deletion event.
 * It is assumed that experiments are added before samples which are added before data sets.
 *
 * @author Franz-Josef Elmer
 */
final class RootEntitiesFinder
{
    private List<ExperimentPE> experiments = new ArrayList<ExperimentPE>();
    private Set<Long> experimentIDs = new HashSet<Long>();
    private List<SamplePE> samples = new ArrayList<SamplePE>();
    private Set<Long> sampleIDs = new HashSet<Long>();
    private List<DataPE> dataSets = new ArrayList<DataPE>();
    
    void addEntity(IDeletablePE entity)
    {
        if (entity instanceof ExperimentPE)
        {
            addExperiment((ExperimentPE) entity);
        }
        if (entity instanceof SamplePE)
        {
            addSample((SamplePE) entity);
        }
        if (entity instanceof DataPE)
        {
            addDataSet((DataPE) entity);
        }
    }
    
    private void addExperiment(ExperimentPE experiment)
    {
        experiments.add(experiment);
        experimentIDs.add(experiment.getId());
    }
    
    private void addSample(SamplePE sample)
    {
        samples.add(sample);
        sampleIDs.add(sample.getId());
    }
    
    private void addDataSet(DataPE dataSet)
    {
        ExperimentPE experiment = dataSet.getExperiment();
        if (experiment != null && experimentIDs.contains(experiment.getId()))
        {
            return;
        }
        SamplePE sample = dataSet.tryGetSample();
        if (sample != null && sampleIDs.contains(sample.getId()))
        {
            return;
        }
        dataSets.add(dataSet);
    }
    
    void addRootEntitiesTo(Deletion deletion)
    {
        Collections.sort(experiments, new Comparator<ExperimentPE>()
            {
                public int compare(ExperimentPE e1, ExperimentPE e2)
                {
                    return e1.getIdentifier().compareTo(e2.getIdentifier());
                }
            });
        for (ExperimentPE experiment : experiments)
        {
            deletion.addDeletedEntity(ExperimentTranslator.translate(experiment, ""));
        }
        List<SamplePE> rootSamples = findRootSamples();
        Collections.sort(rootSamples, new Comparator<SamplePE>()
                {
                    public int compare(SamplePE s1, SamplePE s2)
                    {
                        return s1.getIdentifier().compareTo(s2.getIdentifier());
                    }
                });
        for (SamplePE sample : rootSamples)
        {
            deletion.addDeletedEntity(SampleTranslator.translate(sample, ""));
        }
        Collections.sort(dataSets, new Comparator<DataPE>()
                {
                    public int compare(DataPE d1, DataPE d2)
                    {
                        return d1.getIdentifier().compareTo(d2.getIdentifier());
                    }
                });
        for (DataPE dataSet : dataSets)
        {
            deletion.addDeletedEntity(DataSetTranslator.translate(dataSet, ""));
        }
    }
    
    private List<SamplePE> findRootSamples()
    {
        List<SamplePE> rootSamples = new ArrayList<SamplePE>();
        for (SamplePE sample : samples)
        {
            ExperimentPE experiment = sample.getExperiment();
            if (experiment != null && experimentIDs.contains(experiment.getId()))
            {
                continue;
            }
            SamplePE container = sample.getContainer();
            if (container != null && sampleIDs.contains(container.getId()))
            {
                continue;
            }
            List<SamplePE> parents = sample.getParents();
            if (parents != null && atLeastOneDeleted(parents))
            {
                continue;
            }
            rootSamples.add(sample);
        }
        return rootSamples;
    }
    
    private boolean atLeastOneDeleted(List<SamplePE> parents)
    {
        for (SamplePE sample : parents)
        {
            if (sampleIDs.contains(sample))
            {
                return true;
            }
        }
        return false;
    }
    
}