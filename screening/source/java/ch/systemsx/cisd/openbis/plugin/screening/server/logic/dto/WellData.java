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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * The simplest implementation of {@Link IWellExtendedData}.
 * 
 * @author Tomasz Pylak
 */
public class WellData implements IWellExtendedData
{
    private final long replicaId;

    private Sample well;

    private final float[] featureVector;

    private final Material material;

    public WellData(long replicaId, float[] featureVector, Sample well, Material material)
    {
        this.well = well;
        this.featureVector = featureVector;
        this.replicaId = replicaId;
        this.material = material;
    }

    public long getReplicaMaterialId()
    {
        return replicaId;
    }

    public Sample getWell()
    {
        return well;
    }

    public float[] getFeatureVector()
    {
        return featureVector;
    }

    public Material getMaterial()
    {
        return material;
    }

    @Override
    public String toString()
    {
        return "repl " + replicaId + ": " + Arrays.toString(featureVector);
    }

    public List<IEntityProperty> getProperties()
    {
        return well.getProperties();
    }

    public Long getId()
    {
        return well.getId();
    }

}
