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

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
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
    private List<ExternalData> featureVectorDatasets;

    FeatureVectorDatasetLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String homeSpaceOrNull,
            Collection<? extends PlateIdentifier> plates)
    {
        super(session, businessObjectFactory, homeSpaceOrNull, plates,
                ScreeningConstants.IMAGE_DATASET_TYPE,
                ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
        featureVectorDatasetTypeCode = ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE;
    }

    public List<FeatureVectorDatasetReference> getFeatureVectorDatasets()
    {
        // Load the image data sets
        load();
        loadFeatureVectorDatasets();
        return asFeatureVectorDatasets();
    }

    private void loadFeatureVectorDatasets()
    {
        final Long2ObjectSortedMap<ExternalData> featureVectorDatasetSet =
                new Long2ObjectLinkedOpenHashMap<ExternalData>();
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);

        for (ExternalData data : getDatasets())
        {
            if (ScreeningUtils.isTypeEqual(data, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE))
            {
                featureVectorDatasetSet.put(data.getId(), data);
            }
        }

        // Implementation note: some data sets in this loop may overwrite data from the first loop.
        // This is intended as we want to keep the parent relationship of the feature vector data
        // sets, if they exist.
        for (ExternalData data : getDatasets())
        {
            if (ScreeningUtils.isTypeEqual(data, ScreeningConstants.IMAGE_DATASET_TYPE))
            {
                // TODO 2010-05-26, CR, : This way to access the database one by one is slow if
                // there are a large number of image data sets
                // Need to add a query to the dataset lister that returns, for a collection of tech
                // ids, a child datasets and their parents.
                final List<ExternalData> children =
                        datasetLister.listByParentTechId(new TechId(data.getId()));
                for (ExternalData child : children)
                {
                    child.setParents(Collections.singleton(data));
                    featureVectorDatasetSet.put(child.getId(), child);
                }
            }
        }

        featureVectorDatasets =
                ScreeningUtils.filterExternalDataByType(featureVectorDatasetSet.values(),
                        featureVectorDatasetTypeCode);
    }

    private List<FeatureVectorDatasetReference> asFeatureVectorDatasets()
    {
        List<FeatureVectorDatasetReference> result = new ArrayList<FeatureVectorDatasetReference>();
        for (ExternalData externalData : featureVectorDatasets)
        {
            result.add(asFeatureVectorDataset(externalData));
        }
        return result;
    }

    protected FeatureVectorDatasetReference asFeatureVectorDataset(ExternalData externalData)
    {
        DataStore dataStore = externalData.getDataStore();
        if (externalData.getParents() == null || externalData.getParents().isEmpty())
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
            final ExternalData parentDataset = externalData.getParents().iterator().next();
            return new FeatureVectorDatasetReference(externalData.getCode(),
                    getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(parentDataset),
                    createExperimentIdentifier(externalData), extractPlateGeometry(parentDataset),
                    externalData.getRegistrationDate(), asImageDataset(parentDataset));
        }
    }
}
