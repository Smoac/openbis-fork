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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Stores feature vector summaries for one material and experiment.
 * 
 * @author Tomasz Pylak
 */
public class MaterialSimpleFeatureVectorSummary implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Experiment experiment;

    // has the same length as feature vectors summary
    private List<CodeAndLabel> featureDescriptions;

    private float[] featureVectorSummary;

    // GTW
    // TODO KE: create a separate DTO for the UI layer if there is enough time
    @SuppressWarnings("unused")
    private MaterialSimpleFeatureVectorSummary()
    {
    }

    public MaterialSimpleFeatureVectorSummary(Experiment experiment,
            List<CodeAndLabel> featureDescriptions, float[] featureVectorSummary)
    {
        this.experiment = experiment;
        this.featureDescriptions = featureDescriptions;
        this.featureVectorSummary = featureVectorSummary;
    }

    public Experiment getExperiment()
    {
        return experiment;
    }

    public List<CodeAndLabel> getFeatureDescriptions()
    {
        return featureDescriptions;
    }

    public float[] getFeatureVectorSummary()
    {
        return featureVectorSummary;
    }

}
