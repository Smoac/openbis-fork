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
            IScreeningBusinessObjectFactory businessObjectFactory, String dataStoreBaseURL,
            List<? extends PlateIdentifier> plates)
    {
        super(session, businessObjectFactory, dataStoreBaseURL, plates);
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
        // TODO 2010-05-26, CR, : This is slow if there are a large number of image data sets
        // Need to add a query to the dataset lister that returns, for a collection of tech ids, a
        // child datasets and their parents.
        featureVectorDatasets = new ArrayList<ExternalData>();
        IDatasetLister datasetLister =
                businessObjectFactory.createDatasetLister(session, dataStoreBaseURL);

        for (ExternalData data : getDatasets())
        {
            List<ExternalData> children =
                    datasetLister.listByParentTechId(new TechId(data.getId()));
            ArrayList<ExternalData> parentList = new ArrayList<ExternalData>();
            parentList.add(data);
            for (ExternalData child : children)
            {
                child.setParents(parentList);
            }
            featureVectorDatasets.addAll(children);
        }

        featureVectorDatasets =
                ScreeningUtils.filterExternalDataByType(featureVectorDatasets,
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
        ExternalData parentDataset = externalData.getParents().iterator().next();
        return new FeatureVectorDatasetReference(externalData.getCode(),
                getDataStoreUrlFromDataStore(dataStore), createPlateIdentifier(parentDataset),
                extractPlateGeometry(parentDataset), externalData.getRegistrationDate(),
                asImageDataset(parentDataset));
    }

}
