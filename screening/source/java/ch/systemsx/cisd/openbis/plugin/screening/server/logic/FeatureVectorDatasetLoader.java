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
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
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
class FeatureVectorDatasetLoader extends ImageDatasetLoader
{
    // TODO 2010-05-27, CR : See PlateDatasetLoader todo comment

    // Parameter state
    private final String featureVectorDatasetTypeCode;

    // Running state
    private Collection<ExternalData> featureVectorDatasets;

    FeatureVectorDatasetLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String homeSpaceOrNull,
            Collection<? extends PlateIdentifier> plates)
    {
        super(session, businessObjectFactory, homeSpaceOrNull, plates,
                ScreeningConstants.IMAGE_DATASET_TYPE,
                ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        featureVectorDatasetTypeCode = ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE;
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
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);

        List<ExternalData> imageDatasets = new ArrayList<ExternalData>();
        for (ExternalData dataset : getDatasets())
        {
            if (ScreeningUtils.isTypeEqual(dataset, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE))
            {
                featureVectorDatasetSet.put(dataset.getId(), dataset);
            } else if (ScreeningUtils.isTypeEqual(dataset, ScreeningConstants.IMAGE_DATASET_TYPE))
            {
                imageDatasets.add(dataset);
            }
        }

        List<ExternalData> connectedFeatureVectorDatasets =
                listChildrenFeatureVectorDatasets(datasetLister, extractIds(imageDatasets));
        Map<Long, Set<Long>> featureVectorToImageDatasetIdsMap =
                datasetLister.listParentIds(extractIds(connectedFeatureVectorDatasets));
        Map<Long, List<ExternalData>> featureVectorToImageDatasetsMap =
                createFeatureVectorToImageDatasetsMap(featureVectorToImageDatasetIdsMap,
                        imageDatasets);
        // Implementation note: some data sets in this loop may overwrite data from the first loop.
        // This is intended as we want to keep the parent relationship of the feature vector data
        // sets, if they exist.
        for (ExternalData fv : connectedFeatureVectorDatasets)
        {
            List<ExternalData> parentImageDatasets =
                    featureVectorToImageDatasetsMap.get(fv.getId());
            if (parentImageDatasets != null)
            {
                fv.setParents(parentImageDatasets);
            }
            featureVectorDatasetSet.put(fv.getId(), fv);
        }
        featureVectorDatasets = featureVectorDatasetSet.values();
    }

    private List<ExternalData> listChildrenFeatureVectorDatasets(IDatasetLister datasetLister,
            Collection<Long> imageDatasetIds)
    {
        List<ExternalData> datasets = datasetLister.listByParentTechIds(imageDatasetIds);
        return ScreeningUtils.filterExternalDataByType(datasets, featureVectorDatasetTypeCode);
    }

    private static Map<Long/* feature vector dataset id */, List<ExternalData/* image dataset */>> createFeatureVectorToImageDatasetsMap(
            Map<Long, Set<Long>> featureVectorToImageDatasetsMap, List<ExternalData> imageDatasets)
    {
        Map<Long, List<ExternalData>> featureVectorToImageDatasetMap =
                new HashMap<Long, List<ExternalData>>();
        for (Entry<Long, Set<Long>> entry : featureVectorToImageDatasetsMap.entrySet())
        {
            List<ExternalData> parentImageDatasets =
                    findDatasetsWithIds(entry.getValue(), imageDatasets);
            // NOTE: if a feature vector dataset has more than one image dataset parent, all the
            // parents will be ignored.
            if (parentImageDatasets.size() == 1)
            {
                Long featureVectorDatasetId = entry.getKey();
                featureVectorToImageDatasetMap.put(featureVectorDatasetId, parentImageDatasets);
            }
        }
        return featureVectorToImageDatasetMap;
    }

    // returns all dataset which have an id contained in the specified id set
    private static List<ExternalData> findDatasetsWithIds(Set<Long> datasetIds,
            List<ExternalData> datasets)
    {
        List<ExternalData> found = new ArrayList<ExternalData>();
        for (ExternalData dataset : datasets)
        {
            if (datasetIds.contains(dataset.getId()))
            {
                found.add(dataset);
            }
        }
        return found;
    }

    private static Collection<Long> extractIds(List<ExternalData> datasets)
    {
        List<Long> ids = new ArrayList<Long>();
        for (ExternalData dataset : datasets)
        {
            ids.add(dataset.getId());
        }
        return ids;
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
        if (parentDataset == null)
        {
            return new FeatureVectorDatasetReference(externalData.getCode(),
                    getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(externalData),
                    createExperimentIdentifier(externalData), extractPlateGeometry(externalData),
                    externalData.getRegistrationDate(), null);
        } else
        {
            // Note: this only works reliably because this class sets the parents of the feature
            // vector data sets itself and sets it to a list with exactly one entry!
            // (see loadFeatureVectorDatasets() above)
            return new FeatureVectorDatasetReference(externalData.getCode(),
                    getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(parentDataset),
                    createExperimentIdentifier(externalData), extractPlateGeometry(parentDataset),
                    externalData.getRegistrationDate(), asImageDataset(parentDataset));
        }
    }
}
