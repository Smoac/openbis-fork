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
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.cina.shared.metadata.ReplicaMetadataExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Registers a metadata data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReplicaMetadataRegistrator extends BundleDataSetHelper
{
    private final ReplicaMetadataExtractor replicaMetadataExtractor;

    private final SampleIdentifier replicaSampleId;

    private final DataSetInformation bundleMetadataDataSetInformation;

    private File metadataDataSetFile = null;

    ReplicaMetadataRegistrator(BundleRegistrationState globalState,
            ReplicaMetadataExtractor replicaMetadataExtractor, Sample replicaSample,
            SampleIdentifier replicaSampleId, DataSetInformation bundleMetadataDataSetInformation)
    {
        super(globalState, replicaMetadataExtractor.getFolder());
        this.replicaMetadataExtractor = replicaMetadataExtractor;
        this.replicaSampleId = replicaSampleId;
        this.bundleMetadataDataSetInformation = bundleMetadataDataSetInformation;
    }

    /**
     * Register the metadata data set. Initialize the file object for the data set in the store.
     */
    public List<DataSetInformation> register()
    {
        String dataSetFileName = dataSet.getName();

        // Create a DataSetInformation
        DataSetInformation metadataDataSetInfo = createDataSetInformation();

        // Import the metadata
        ArrayList<NewProperty> properties = createDataSetProperties(replicaMetadataExtractor);
        metadataDataSetInfo.setDataSetProperties(properties);
        registerDataSet(dataSet, metadataDataSetInfo);

        // Get the data set information for the data set we just registered
        DataSetInformation registeredDataSetInformation = getDataSetInformation().get(0);
        initializeMetadataDataSetFile(registeredDataSetInformation, dataSetFileName);

        return getDataSetInformation();
    }

    public File getMetadataDataSetFile()
    {
        assert metadataDataSetFile != null : "Can get metadataDataSetFile only after calling register";
        return metadataDataSetFile;
    }

    private DataSetInformation createDataSetInformation()
    {
        DataSetInformation metadataDataSetInfo = new DataSetInformation();
        metadataDataSetInfo.setSampleCode(replicaSampleId.getSampleCode());
        metadataDataSetInfo.setSpaceCode(replicaSampleId.getSpaceLevel().getSpaceCode());
        metadataDataSetInfo.setInstanceCode(replicaSampleId.getSpaceLevel()
                .getDatabaseInstanceCode());
        metadataDataSetInfo.setDataSetType(globalState.getMetadataDataSetType().getDataSetType());
        List<String> parentDataSetCodes =
                Collections.singletonList(bundleMetadataDataSetInformation.getDataSetCode());
        metadataDataSetInfo.setParentDataSetCodes(parentDataSetCodes);
        return metadataDataSetInfo;
    }

    private void initializeMetadataDataSetFile(DataSetInformation registeredDataSetInformation,
            String dataSetFileName)
    {
        ExternalData metadataExternalData =
                getOpenbisService().tryGetDataSet(getSessionContext().getSessionToken(),
                        registeredDataSetInformation.getDataSetCode());
        File containerFile = getDelegator().getFileForExternalData(metadataExternalData);
        metadataDataSetFile = new File(containerFile, dataSetFileName);
    }
}
