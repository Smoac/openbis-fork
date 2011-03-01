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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IFeatureValues;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;

/**
 * Allows to define feature vectors of one image analysis dataset.
 * 
 * @author Tomasz Pylak
 */
public class FeaturesBuilder implements IFeaturesBuilder
{
    private final List<FeatureDefinitionValues> featureDefinitionValuesList;

    public FeaturesBuilder()
    {
        this.featureDefinitionValuesList = new ArrayList<FeatureDefinitionValues>();
    }

    /** Defines a container to which values of the feature for each well can be added. */
    public IFeatureValues defineFeature(FeatureDefinition featureDefinition)
    {
        featureDefinition.ensureValid();
        FeatureDefinitionValues featureDefinitionValues =
                new FeatureDefinitionValues(convert(featureDefinition));
        featureDefinitionValuesList.add(featureDefinitionValues);
        return featureDefinitionValues;
    }

    private static ImgFeatureDefDTO convert(FeatureDefinition featureDefinition)
    {
        ImgFeatureDefDTO dto = new ImgFeatureDefDTO();
        dto.setCode(featureDefinition.getCode());
        dto.setLabel(featureDefinition.getLabel());
        dto.setDescription(featureDefinition.getDescription());
        return dto;
    }

    public List<FeatureDefinitionValues> getFeatureDefinitionValuesList()
    {
        return featureDefinitionValuesList;
    }
}
