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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;

/**
 * @author Jakub Straszewski
 */
public class MaterialGroupingDAG extends EntityGroupingDAG<NewMaterialWithType>
{
    private MaterialGroupingDAG(Collection<NewMaterialWithType> samples)
    {
        super(samples);
    }

    /**
     * Return the new samples in the list of groups, where the earlier groups are independent to the latter ones.
     * 
     * @param samples The list of samples to create.
     */
    public static List<List<NewMaterialWithType>> groupByDepencies(Collection<NewMaterialWithType> samples)
    {
        if (samples.size() == 0)
        {
            return Collections.emptyList();
        }

        MaterialGroupingDAG dag = new MaterialGroupingDAG(samples);
        return dag.getDependencyGroups();
    }

    @Override
    public String getCode(NewMaterialWithType material)
    {
        return MaterialIdentifier.toString(material.getCode(), material.getType());
    }

    @Override
    public Collection<String> getDependent(NewMaterialWithType sample)
    {
        return null;
    }

    @Override
    public Collection<String> getDependencies(NewMaterialWithType newMaterial)
    {
        // get potential dependencies to other materials from properties

        LinkedList<String> relatedMaterials = null;
        IEntityProperty[] properties = newMaterial.getProperties();

        for (IEntityProperty iEntityProperty : properties)
        {
            if (relatedMaterials == null)
            {
                relatedMaterials = new LinkedList<String>();
            }
            // TODO: it would be better to translate property into the real material -
            // or at least know here that the given property is indeed a material link
            // this way there is a risk that someone will add property value to be the name
            // of other existing property and cause false loop dependency error
            relatedMaterials.add(iEntityProperty.getValue());
        }
        return relatedMaterials;
    }
}
