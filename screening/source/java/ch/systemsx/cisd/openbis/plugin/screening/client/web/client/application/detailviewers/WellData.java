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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Stores information about well metadata, may be extended in future.
 * 
 * @author Tomasz Pylak
 */
public class WellData
{
    private final WellLocation wellLocation;

    private final Experiment experiment;

    private WellMetadata wellMetadataOrNull;

    private float[] featureValuesOrNull;

    public WellData(WellLocation wellLocation, Experiment experiment)
    {
        this.wellLocation = wellLocation;
        this.experiment = experiment;
    }

    public void setFeatureValues(float[] featureValues)
    {
        this.featureValuesOrNull = featureValues;
    }

    public Float tryGetFeatureValue(int featureVectorIndex)
    {
        if (featureValuesOrNull == null)
        {
            return null;
        }
        return featureValuesOrNull[featureVectorIndex];
    }

    public void setMetadata(WellMetadata well)
    {
        this.wellMetadataOrNull = well;
    }

    public WellMetadata tryGetMetadata()
    {
        return wellMetadataOrNull;
    }

    public WellLocation getWellLocation()
    {
        return wellLocation;
    }

    public Experiment getExperiment()
    {
        return experiment;
    }
}