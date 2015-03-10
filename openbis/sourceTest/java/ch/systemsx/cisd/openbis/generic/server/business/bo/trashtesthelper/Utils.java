/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Utils
{

    public static ExternalDataPE createData(DataSetNode dataSetNode)
    {
        ExternalDataPE data = new ExternalDataPE();
        Long id = dataSetNode.getId();
        data.setId(id);
        data.setCode(dataSetNode.getCode());
        ExperimentNode experiment;
        SampleNode sample = dataSetNode.getSample();
        if (sample != null)
        {
            SamplePE samplePE = new SamplePE();
            samplePE.setId(sample.getId());
            samplePE.setCode(sample.getCode());
            data.setSample(samplePE);
            experiment = sample.getExperiment();
        } else
        {
            experiment = dataSetNode.getExperiment();
        }
        if (experiment != null)
        {
            ExperimentPE experimentPE = new ExperimentPE();
            experimentPE.setId(experiment.getId());
            experimentPE.setCode(experiment.getCode());
            data.setExperiment(experimentPE);
        }
        return data;
    }

    static final void appendTo(StringBuilder builder, String label, List<? extends EntityNode> entityNodes)
    {
        if (entityNodes.isEmpty())
        {
            return;
        }
        builder.append(", ").append(label).append(": ");
        String delim = "[";
        for (EntityNode entityNode : entityNodes)
        {
            builder.append(delim).append(entityNode.getCode());
            delim = ", ";
        }
        builder.append("]");
    }

    static final void appendTo(StringBuilder builder, String label, EntityNode entityNodeOrNull)
    {
        if (entityNodeOrNull == null)
        {
            return;
        }
        builder.append(", ").append(label).append(": ").append(entityNodeOrNull.getCode());
    }

}
