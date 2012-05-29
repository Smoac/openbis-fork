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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.json;

import ch.systemsx.cisd.openbis.generic.shared.api.json.GenericJsonClassValueToClassObjectsMapping;

/**
 * @author pkupczyk
 */
public class ScreeningJsonClassValueToClassObjectsMapping extends
        GenericJsonClassValueToClassObjectsMapping
{

    public ScreeningJsonClassValueToClassObjectsMapping()
    {
        add(".FeatureVector",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector.class);
        add(".FeatureVectorDataset",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset.class);
        add(".FeatureVectorWithDescription",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription.class);
        add(".MaterialIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier.class);
        add(".PermanentIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PermanentIdentifier.class);
        add(".ExperimentIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier.class);
        add(".PlateIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier.class);
        add(".Plate", ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate.class);
        add(".PlateMetadata",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata.class);
        add(".WellIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier.class);
        add(".WellMetadata",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellMetadata.class);
        add(".DatasetIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier.class);
        add(".DatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetReference.class);
        add(".FeatureVectorDatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference.class);
        add(".FeatureVectorDatasetWellReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference.class);
        add(".ImageDatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference.class);
        add(".MicroscopyImageReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MicroscopyImageReference.class);
        add(".PlateImageReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference.class);

    }
}
