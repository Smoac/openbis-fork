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

package ch.systemsx.cisd.cina.dss.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.cina.shared.metadata.BundleMetadataExtractor;
import ch.systemsx.cisd.cina.shared.metadata.ImageMetadataExtractor;
import ch.systemsx.cisd.cina.shared.metadata.ReplicaMetadataExtractor;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Helper class for processing bundle data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class BundleDataSetHelper
{
    static class BundleRegistrationGlobalState
    {
        private final IDataSetHandler delegator;

        private final IEncapsulatedOpenBISService openbisService;

        private final SampleType replicaSampleType;

        private final DataSetTypeWithVocabularyTerms imageDataSetType;

        BundleRegistrationGlobalState(IDataSetHandler delegator,
                IEncapsulatedOpenBISService openbisService, SampleType replicaSampleType,
                DataSetTypeWithVocabularyTerms imageDataSetType)
        {
            this.delegator = delegator;
            this.openbisService = openbisService;
            this.replicaSampleType = replicaSampleType;
            this.imageDataSetType = imageDataSetType;
        }

        IDataSetHandler getDelegator()
        {
            return delegator;
        }

        IEncapsulatedOpenBISService getOpenbisService()
        {
            return openbisService;
        }

        SampleType getReplicaSampleType()
        {
            return replicaSampleType;
        }

        DataSetTypeWithVocabularyTerms getImageDataSetType()
        {
            return imageDataSetType;
        }
    }

    private final BundleRegistrationGlobalState globalState;

    // Invocation-specific State
    private final String sessionOwnerUserId;

    private final File dataSet;

    private final ArrayList<DataSetInformation> dataSetInformation;

    BundleDataSetHelper(BundleRegistrationGlobalState globalState, String sessionOwnerUserId,
            File dataSet)
    {
        this.globalState = globalState;
        this.sessionOwnerUserId = sessionOwnerUserId;
        this.dataSet = dataSet;
        this.dataSetInformation = new ArrayList<DataSetInformation>();
    }

    /**
     *
     */
    public void process()
    {
        // Register the bundle as one data set
        List<DataSetInformation> bigDataSet = getDelegator().handleDataSet(dataSet);
        dataSetInformation.addAll(bigDataSet);
        if (dataSetInformation.isEmpty())
        {
            return;
        }

        BundleMetadataExtractor bundleMetadata = new BundleMetadataExtractor(dataSet);
        for (ReplicaMetadataExtractor replicaMetadata : bundleMetadata.getMetadataExtractors())
        {
            handleDerivedDataSets(replicaMetadata);
        }
    }

    /**
     * Get all the data set information that has been created as a result of {@link #process}. Only
     * makes sense to invoke after process has been called
     */
    public ArrayList<DataSetInformation> getDataSetInformation()
    {
        return dataSetInformation;
    }

    private void handleDerivedDataSets(ReplicaMetadataExtractor replicaMetadata)
    {
        SampleType replicaSampleType = globalState.getReplicaSampleType();
        long sampleCodeSuffix = getOpenbisService().drawANewUniqueID();
        String sampleCode =
                String.format("%s%d", replicaSampleType.getGeneratedCodePrefix(), sampleCodeSuffix);
        SampleIdentifier parentIdentifier = getBigDataSetInformation().getSampleIdentifier();

        // Register a sample with this ID of type GRID_REPLICA, derived from the GRID_TEMPLATE
        // registered by the client
        SampleIdentifier identifier =
                new SampleIdentifier(parentIdentifier.getSpaceLevel(), sampleCode);
        NewSample sample =
                new NewSample(identifier.toString(), replicaSampleType,
                        parentIdentifier.toString(), "");
        getOpenbisService().registerSample(sample, sessionOwnerUserId);

        // Register all the data sets derived from this sample
        for (ImageMetadataExtractor imageMetadata : replicaMetadata.getMetadataExtractors())
        {
            handleDerivedDataSet(identifier.toString(), imageMetadata);
        }
    }

    private void handleDerivedDataSet(String sampleId, ImageMetadataExtractor imageMetadata)
    {
        // Register a data set for the image

    }

    /**
     * Get the "big data set" -- the one that is the parent of all the derived data sets I register
     */
    private DataSetInformation getBigDataSetInformation()
    {
        // The big data set is the first one registered.
        return dataSetInformation.get(0);
    }

    private IDataSetHandler getDelegator()
    {
        return globalState.getDelegator();
    }

    private IEncapsulatedOpenBISService getOpenbisService()
    {
        return globalState.getOpenbisService();
    }
}
