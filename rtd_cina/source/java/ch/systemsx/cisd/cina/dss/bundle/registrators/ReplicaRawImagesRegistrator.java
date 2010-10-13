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

package ch.systemsx.cisd.cina.dss.bundle.registrators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.cina.shared.metadata.ReplicaMetadataExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Registers a replica's raw image data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReplicaRawImagesRegistrator extends BundleDataSetHelper
{
    private final ReplicaMetadataExtractor replicaMetadataExtractor;

    private final SampleIdentifier replicaSampleId;

    ReplicaRawImagesRegistrator(BundleRegistrationState globalState,
            ReplicaMetadataExtractor replicaMetadataExtractor, Sample replicaSample,
            SampleIdentifier replicaSampleId, File dataSet)
    {
        super(globalState, dataSet);
        this.replicaMetadataExtractor = replicaMetadataExtractor;
        this.replicaSampleId = replicaSampleId;
    }

    public List<DataSetInformation> register()
    {
        // Create a DataSetInformation
        DataSetInformation imagesDataSetInfo = createDataSetInformation();

        // Import the metadata
        ArrayList<NewProperty> properties = createDataSetProperties(replicaMetadataExtractor);
        imagesDataSetInfo.setDataSetProperties(properties);
        registerDataSet(dataSet, imagesDataSetInfo);

        return getDataSetInformation();
    }

    private DataSetInformation createDataSetInformation()
    {
        DataSetInformation imagesDataSetInfo = new DataSetInformation();
        imagesDataSetInfo.setSampleCode(replicaSampleId.getSampleCode());
        imagesDataSetInfo.setSpaceCode(replicaSampleId.getSpaceLevel().getSpaceCode());
        imagesDataSetInfo
                .setInstanceCode(replicaSampleId.getSpaceLevel().getDatabaseInstanceCode());
        imagesDataSetInfo.setDataSetType(globalState.getRawImagesDataSetType().getDataSetType());
        return imagesDataSetInfo;
    }
}
