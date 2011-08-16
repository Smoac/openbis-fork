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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Utility class for loading HCS image datasets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class HCSImageDatasetLoader extends PlateDatasetLoader
{
    // TODO 2010-05-27, CR : See PlateDatasetLoader todo comment

    protected static Collection<Long> extractIds(List<ExternalData> datasets)
    {
        List<Long> ids = new ArrayList<Long>();
        for (ExternalData dataset : datasets)
        {
            ids.add(dataset.getId());
        }
        return ids;
    }

    private static Map<Long/* child data set id */, List<ExternalData/* parent data sets */>> createChildDataSetToParentDataSetsMap(
            Map<Long, Set<Long>> childIdToParentIdsMap, List<ExternalData> parentDatasets)
    {
        Map<Long, List<ExternalData>> childDataSetToParentDataSetsMap =
                new HashMap<Long, List<ExternalData>>();
        for (Entry<Long, Set<Long>> entry : childIdToParentIdsMap.entrySet())
        {
            List<ExternalData> parents = findDatasetsWithIds(entry.getValue(), parentDatasets);
            // NOTE: if a child data set has more than one parent data set, all the
            // parents will be ignored.
            if (parents.size() == 1)
            {
                Long childId = entry.getKey();
                childDataSetToParentDataSetsMap.put(childId, parents);
            }
        }
        return childDataSetToParentDataSetsMap;
    }

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

    HCSImageDatasetLoader(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            String homeSpaceOrNull, Set<? extends PlateIdentifier> plates,
            String... datasetTypeCodes)
    {
        super(session, businessObjectFactory, homeSpaceOrNull, plates,
                (datasetTypeCodes.length == 0) ? new String[]
                    { ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN } : datasetTypeCodes);
    }

    /**
     * Return the image datasets for the specified plates.
     */
    public List<ExternalData> getImageDatasets()
    {
        load();
        return filterImageDatasets();
    }

    /**
     * Return the raw image datasets for the specified plates.
     */
    private List<ExternalData> getRawImageDatasets()
    {
        load();
        return filterRawImageDatasets();
    }

    /**
     * Return the segmentation image datasets (overlays) for the specified plates.
     */
    private List<ExternalData> getSegmentationImageDatasets()
    {
        load();
        List<ExternalData> imageDatasets = new ArrayList<ExternalData>();
        for (ExternalData dataset : getDatasets())
        {
            if (ScreeningUtils.isBasicHcsImageDataset(dataset))
            {
                imageDatasets.add(dataset);
            }
        }
        Map<Long, ExternalData> segmentationImageDataSets = new HashMap<Long, ExternalData>();
        gatherChildrenDataSets(segmentationImageDataSets, imageDatasets,
                ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN);
        return new ArrayList<ExternalData>(segmentationImageDataSets.values());
    }

    /**
     * Return the image datasets references for the specified plates.
     */
    public List<ImageDatasetReference> getImageDatasetReferences()
    {
        return asImageDatasetReferences(getImageDatasets());
    }

    /**
     * Return the raw image datasets references for the specified plates.
     */
    public List<ImageDatasetReference> getRawImageDatasetReferences()
    {
        return asImageDatasetReferences(getRawImageDatasets());
    }

    /**
     * Return the segmentation image datasets references for the specified plates.
     */
    public List<ImageDatasetReference> getSegmentationImageDatasetReferences()
    {
        return asImageDatasetReferences(getSegmentationImageDatasets());
    }

    private List<ExternalData> filterImageDatasets()
    {
        List<ExternalData> result = new ArrayList<ExternalData>();
        for (ExternalData externalData : getDatasets())
        {
            if (ScreeningUtils.isBasicHcsImageDataset(externalData))
            {
                result.add(externalData);
            }
        }
        return result;
    }

    private List<ExternalData> filterRawImageDatasets()
    {
        List<ExternalData> result = new ArrayList<ExternalData>();
        for (ExternalData externalData : getDatasets())
        {
            if (ScreeningUtils.isRawHcsImageDataset(externalData))
            {
                result.add(externalData);
            }
        }
        return result;
    }

    private List<ImageDatasetReference> asImageDatasetReferences(List<ExternalData> imageDatasets)
    {
        List<ImageDatasetReference> references = new ArrayList<ImageDatasetReference>();
        for (ExternalData imageDataset : imageDatasets)
        {
            ImageDatasetReference reference = tryAsImageDataset(imageDataset);
            if (reference != null)
            {
                references.add(reference);
            }
        }
        return references;
    }

    /** Sets parents of all datasets in 'childrenDataSets'. */
    protected void gatherChildrenDataSets(final Map<Long, ExternalData> childrenDataSets,
            List<ExternalData> parentDataSets, String childTypePattern)
    {
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        List<ExternalData> filteredChildrenDataSets =
                ScreeningUtils.filterExternalDataByTypePattern(
                        datasetLister.listByParentTechIds(extractIds(parentDataSets)),
                        childTypePattern);
        Map<Long, Set<Long>> childIdToParentIdsMap =
                datasetLister.listParentIds(extractIds(filteredChildrenDataSets));
        Map<Long, List<ExternalData>> childIdToParentDataSetsMap =
                createChildDataSetToParentDataSetsMap(childIdToParentIdsMap, parentDataSets);
        // Implementation note: some data sets in this loop may overwrite data from the first loop.
        // This is intended as we want to keep the parent relationship of the feature vector data
        // sets, if they exist.
        for (ExternalData child : filteredChildrenDataSets)
        {
            Long childId = child.getId();
            if (childrenDataSets.containsKey(childId))
            {
                List<ExternalData> parentImageDatasets = childIdToParentDataSetsMap.get(childId);
                if (parentImageDatasets != null)
                {
                    child.setParents(parentImageDatasets);
                }
                childrenDataSets.put(childId, child);
            }
        }
    }

    private ExternalData tryGetParent(ExternalData externalData)
    {
        if (externalData.getParents() != null && externalData.getParents().size() == 1)
        {
            return externalData.getParents().iterator().next();
        } else
        {
            return null;
        }
    }

    protected ImageDatasetReference tryAsImageDataset(ExternalData externalData)
    {
        if (externalData == null || ScreeningUtils.isHcsImageDataset(externalData) == false)
        {
            return null;
        }
        DataStore dataStore = externalData.getDataStore();
        DataSetType dataSetType = externalData.getDataSetType();
        String dataSetTypeCodeOrNull = dataSetType == null ? null : dataSetType.getCode();
        return new ImageDatasetReference(externalData.getCode(), dataSetTypeCodeOrNull,
                getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(externalData),
                createExperimentIdentifier(externalData), extractPlateGeometry(externalData),
                externalData.getRegistrationDate(), extractProperties(externalData),
                tryAsImageDataset(tryGetParent(externalData)));
    }
}
