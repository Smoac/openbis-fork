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

import ch.systemsx.cisd.openbis.generic.shared.api.json.GenericJsonClassValueToClassObjectsMap;

/**
 * @author pkupczyk
 */
public class ScreeningJsonClassValueToClassObjectsMap extends
        GenericJsonClassValueToClassObjectsMap
{

    public ScreeningJsonClassValueToClassObjectsMap()
    {
        put(".FeatureVector",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector.class);
        put(".FeatureVectorDataset",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset.class);
        put(".FeatureVectorWithDescription",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription.class);
        put(".MaterialIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier.class);
        put(".PermanentIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PermanentIdentifier.class);
        put(".ExperimentIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier.class);
        put(".PlateIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier.class);
        put(".Plate", ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate.class);
        put(".PlateMetadata",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata.class);
        put(".WellIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier.class);
        put(".WellMetadata",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellMetadata.class);
        put(".DatasetIdentifier",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier.class);
        put(".DatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetReference.class);
        put(".FeatureVectorDatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference.class);
        put(".FeatureVectorDatasetWellReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference.class);
        put(".ImageDatasetReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference.class);
        put(".MicroscopyImageReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MicroscopyImageReference.class);
        put(".PlateImageReference",
                ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference.class);

    }
}
