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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.cina.shared.metadata.ImageMetadataExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Registers annotated image data sets. The annotated image data sets refer to files that are
 * already in the store.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReplicaAnnotatedImagesRegistrator extends BundleDataSetHelper
{
    private final ImageMetadataExtractor imageMetadataExtractor;

    private final SampleIdentifier replicaSampleId;

    /**
     * Constructor.
     * 
     * @param globalState The registration state
     * @param imageMetadataExtractor An image metadata extractor referring to a file that is already
     *            in the store
     * @param replicaSample The owning sample of the data set
     * @param replicaSampleId The owning sample id
     */
    ReplicaAnnotatedImagesRegistrator(BundleRegistrationState globalState,
            ImageMetadataExtractor imageMetadataExtractor, Sample replicaSample,
            SampleIdentifier replicaSampleId)
    {
        super(globalState, imageMetadataExtractor.getFolder());
        this.imageMetadataExtractor = imageMetadataExtractor;
        this.replicaSampleId = replicaSampleId;
    }

    public List<DataSetInformation> register()
    {
        // Create a DataSetInformation
        DataSetInformation metadataDataSetInfo = createDataSetInformation();

        // Import the metadata
        ArrayList<NewProperty> properties = createDataSetProperties(imageMetadataExtractor);
        metadataDataSetInfo.setDataSetProperties(properties);
        registerLinkedDataSet(dataSet, metadataDataSetInfo);

        return getDataSetInformation();
    }

    private DataSetInformation createDataSetInformation()
    {
        DataSetInformation metadataDataSetInfo = new DataSetInformation();
        metadataDataSetInfo.setSampleCode(replicaSampleId.getSampleCode());
        metadataDataSetInfo.setSpaceCode(replicaSampleId.getSpaceLevel().getSpaceCode());
        metadataDataSetInfo.setInstanceCode(replicaSampleId.getSpaceLevel()
                .getDatabaseInstanceCode());
        metadataDataSetInfo.setDataSetType(globalState.getImageDataSetType().getDataSetType());
        return metadataDataSetInfo;
    }
}
