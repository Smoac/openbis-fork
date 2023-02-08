/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author pkupczyk
 */
@JsonObject("MetaprojectAssignmentsIds")
public class MetaprojectAssignmentsIds implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<IExperimentId> experiments = new ArrayList<IExperimentId>();

    private List<ISampleId> samples = new ArrayList<ISampleId>();

    private List<IDataSetId> dataSets = new ArrayList<IDataSetId>();

    private List<IMaterialId> materials = new ArrayList<IMaterialId>();

    public void addExperiment(IExperimentId experimentId)
    {
        if (experimentId == null)
        {
            throw new IllegalArgumentException("Experiment id cannot be null");
        }
        experiments.add(experimentId);
    }

    public void addSample(ISampleId sampleId)
    {
        if (sampleId == null)
        {
            throw new IllegalArgumentException("Sample id cannot be null");
        }
        samples.add(sampleId);
    }

    public void addDataSet(IDataSetId dataSetId)
    {
        if (dataSetId == null)
        {
            throw new IllegalArgumentException("Data set id cannot be null");
        }
        dataSets.add(dataSetId);
    }

    public void addMaterial(IMaterialId materialId)
    {
        if (materialId == null)
        {
            throw new IllegalArgumentException("Material id cannot be null");
        }
        materials.add(materialId);
    }

    public List<IExperimentId> getExperiments()
    {
        return experiments;
    }

    public List<ISampleId> getSamples()
    {
        return samples;
    }

    public List<IDataSetId> getDataSets()
    {
        return dataSets;
    }

    public List<IMaterialId> getMaterials()
    {
        return materials;
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private void setExperiments(List<IExperimentId> experiments)
    {
        if (experiments == null)
        {
            this.experiments = Collections.emptyList();
        } else
        {
            this.experiments = experiments;
        }
    }

    @SuppressWarnings("unused")
    private void setSamples(List<ISampleId> samples)
    {
        if (samples == null)
        {
            this.samples = Collections.emptyList();
        } else
        {
            this.samples = samples;
        }
    }

    @SuppressWarnings("unused")
    private void setDataSets(List<IDataSetId> dataSets)
    {
        if (dataSets == null)
        {
            this.dataSets = Collections.emptyList();
        } else
        {
            this.dataSets = dataSets;
        }
    }

    @SuppressWarnings("unused")
    private void setMaterials(List<IMaterialId> materials)
    {
        if (materials == null)
        {
            this.materials = Collections.emptyList();
        } else
        {
            this.materials = materials;
        }
    }

}
