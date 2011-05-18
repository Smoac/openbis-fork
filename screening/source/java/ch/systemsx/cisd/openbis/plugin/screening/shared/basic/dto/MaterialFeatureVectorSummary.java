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

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Aggregated feature vector with its ranking in one experiment for one material.
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeatureVectorSummary implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Material material;

    private float[] featureVectorSummary;

    private float[] featureVectorDeviations;

    private int[] featureVectorRanks;

    private int numberOfMaterialsInExperiment;

    // GTW
    @SuppressWarnings("unused")
    private MaterialFeatureVectorSummary()
    {
    }

    public MaterialFeatureVectorSummary(Material material, float[] featureVectorSummary,
            float[] featureVectorDeviations, int[] featureVectorRanks,
            int numberOfMaterialsInExperiment)
    {
        this.material = material;
        this.featureVectorSummary = featureVectorSummary;
        this.featureVectorDeviations = featureVectorDeviations;
        this.featureVectorRanks = featureVectorRanks;
        this.numberOfMaterialsInExperiment = numberOfMaterialsInExperiment;
    }

    public Material getMaterial()
    {
        return material;
    }

    public float[] getFeatureVectorSummary()
    {
        return featureVectorSummary;
    }

    public float[] getFeatureVectorDeviations()
    {
        return featureVectorDeviations;
    }

    public int[] getFeatureVectorRanks()
    {
        return featureVectorRanks;
    }

    public int getNumberOfMaterialsInExperiment()
    {
        return numberOfMaterialsInExperiment;
    }
}