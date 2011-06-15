/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class FeatureVectorDatasetLoader extends HCSImageDatasetLoader
{
    // TODO 2010-05-27, CR : See PlateDatasetLoader todo comment

    // Parameter state
    private final String featureVectorDatasetTypeCode;

    // Running state
    private Collection<ExternalData> featureVectorDatasets;

    FeatureVectorDatasetLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String homeSpaceOrNull,
            Set<? extends PlateIdentifier> plates)
    {
        super(session, businessObjectFactory, homeSpaceOrNull, plates,
                ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN,
                ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN);
        featureVectorDatasetTypeCode = ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN;
    }

    public static class FeatureVectorExternalData
    {
        private final ExternalData featureVectorDataset;

        private final ExternalData imageDatasetOrNull;

        public FeatureVectorExternalData(ExternalData featureVectorDataset,
                ExternalData imageDatasetOrNull)
        {
            this.featureVectorDataset = featureVectorDataset;
            this.imageDatasetOrNull = imageDatasetOrNull;
        }

        public ExternalData getFeatureVectorDataset()
        {
            return featureVectorDataset;
        }

        public ExternalData tryGetImageDataset()
        {
            return imageDatasetOrNull;
        }
    }

    /** enriched with image dataset parents */
    public Collection<ExternalData> getFeatureVectorDatasets()
    {
        loadAll();
        return featureVectorDatasets;
    }

    public List<FeatureVectorDatasetReference> getFeatureVectorDatasetReferences()
    {
        loadAll();
        return asFeatureVectorDatasetReferences();
    }

    private void loadAll()
    {
        // Load the image data sets
        load();
        loadFeatureVectorDatasets();
    }

    private void loadFeatureVectorDatasets()
    {
        final Map<Long, ExternalData> featureVectorDatasetSet = new HashMap<Long, ExternalData>();

        List<ExternalData> imageDatasets = new ArrayList<ExternalData>();
        for (ExternalData dataset : getDatasets())
        {
            if (ScreeningUtils.isTypeMatching(dataset,
                    ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN))
            {
                featureVectorDatasetSet.put(dataset.getId(), dataset);
            } else if (ScreeningUtils.isTypeMatching(dataset,
                    ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN))
            {
                imageDatasets.add(dataset);
            }
        }

        gatherChildrenDataSets(featureVectorDatasetSet, imageDatasets, featureVectorDatasetTypeCode);
        featureVectorDatasets = featureVectorDatasetSet.values();
    }

    private List<FeatureVectorDatasetReference> asFeatureVectorDatasetReferences()
    {
        List<FeatureVectorDatasetReference> result = new ArrayList<FeatureVectorDatasetReference>();
        for (ExternalData externalData : featureVectorDatasets)
        {
            result.add(asFeatureVectorDataset(externalData));
        }
        return result;
    }

    private static ExternalData tryGetOneParent(ExternalData externalData)
    {
        Collection<ExternalData> parents = externalData.getParents();
        if (parents != null && parents.size() == 1)
        {
            return parents.iterator().next();
        } else
        {
            return null;
        }
    }

    protected FeatureVectorDatasetReference asFeatureVectorDataset(ExternalData externalData)
    {
        DataStore dataStore = externalData.getDataStore();
        // there should be no more parents than one, we ensure about that earlier
        ExternalData parentDataset = tryGetOneParent(externalData);
        DataSetType dataSetType = externalData.getDataSetType();
        String dataSetTypeCodeOrNull = dataSetType == null ? null : dataSetType.getCode();
        if (parentDataset == null)
        {
            return new FeatureVectorDatasetReference(externalData.getCode(), dataSetTypeCodeOrNull, 
                    getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(externalData),
                    createExperimentIdentifier(externalData), extractPlateGeometry(externalData),
                    externalData.getRegistrationDate(), null, extractProperties(externalData));
        } else
        {
            // Note: this only works reliably because this class sets the parents of the feature
            // vector data sets itself and sets it to a list with exactly one entry!
            // (see loadFeatureVectorDatasets() above)
            return new FeatureVectorDatasetReference(externalData.getCode(), dataSetTypeCodeOrNull,
                    getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(parentDataset),
                    createExperimentIdentifier(externalData), extractPlateGeometry(parentDataset),
                    externalData.getRegistrationDate(), tryAsImageDataset(parentDataset),
                    extractProperties(externalData));
        }
    }
}
