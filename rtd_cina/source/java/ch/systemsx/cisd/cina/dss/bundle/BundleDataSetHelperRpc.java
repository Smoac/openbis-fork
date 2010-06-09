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

import ch.systemsx.cisd.cina.shared.metadata.BundleMetadataExtractor;
import ch.systemsx.cisd.cina.shared.metadata.ImageMetadataExtractor;
import ch.systemsx.cisd.cina.shared.metadata.ReplicaMetadataExtractor;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Helper class for processing bundle data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class BundleDataSetHelperRpc extends BundleDataSetHelper
{
    // Invocation-specific State
    private final SessionContextDTO sessionContext;

    private final IDataSetHandlerRpc delegatorRpc;

    BundleDataSetHelperRpc(BundleRegistrationGlobalState globalState, File dataSet)
    {
        super(globalState, dataSet);
        IDataSetHandler delegator = globalState.getDelegator();
        if (delegator instanceof IDataSetHandlerRpc)
        {
            delegatorRpc = (IDataSetHandlerRpc) delegator;
            this.sessionContext = delegatorRpc.getSessionContext();
        } else
        {
            throw new IllegalArgumentException(
                    "BundleDataSetHelperRpc used with non-RPC data set handler.");
        }
    }

    /**
     *
     */
    @Override
    public void process()
    {
        String dataSetName = dataSet.getName();
        // Register the bundle as one data set
        super.process();
        if (dataSetInformation.isEmpty())
        {
            return;
        }

        ExternalData bigData =
                getOpenbisService().tryGetDataSet(sessionContext.getSessionToken(),
                        getBigDataSetInformation().getDataSetCode());
        File containerFile = delegatorRpc.getFileForExternalData(bigData);
        File bundle = new File(containerFile, dataSetName);

        BundleMetadataExtractor bundleMetadata = new BundleMetadataExtractor(bundle);
        bundleMetadata.prepare();
        System.out.println(bundleMetadata.getMetadataExtractors());
        for (ReplicaMetadataExtractor replicaMetadata : bundleMetadata.getMetadataExtractors())
        {
            handleDerivedDataSets(replicaMetadata);
        }
    }

    private void handleDerivedDataSets(ReplicaMetadataExtractor replicaMetadata)
    {
        SampleIdentifier sampleId = registerReplicaSample();

        // Register all the data sets derived from this sample
        for (ImageMetadataExtractor imageMetadata : replicaMetadata.getMetadataExtractors())
        {
            handleDerivedDataSet(sampleId, imageMetadata);
        }
    }

    /**
     * Register a sample with this ID of type GRID_REPLICA, either derived from the GRID_TEMPLATE
     * registered by the client or associated with the experiment.
     */
    private SampleIdentifier registerReplicaSample()
    {
        DataSetInformation bigDataSetInfo = getBigDataSetInformation();
        SampleType replicaSampleType = globalState.getReplicaSampleType();
        long sampleCodeSuffix = getOpenbisService().drawANewUniqueID();
        String sampleCode =
                String.format("%s%d", replicaSampleType.getGeneratedCodePrefix(), sampleCodeSuffix);
        ExperimentIdentifier experimentIdOrNull = bigDataSetInfo.getExperimentIdentifier();
        SampleIdentifier parentIdOrNull = bigDataSetInfo.getSampleIdentifier();

        // Either the experimentId or the parentId must be non-null
        assert experimentIdOrNull != null || parentIdOrNull != null;

        SampleIdentifier sampleId = null;
        NewSample sample = null;
        if (parentIdOrNull != null)
        {
            sampleId = new SampleIdentifier(parentIdOrNull.getSpaceLevel(), sampleCode);
            sample =
                    new NewSample(sampleId.toString(), replicaSampleType,
                            parentIdOrNull.toString(), null);
        } else if (experimentIdOrNull != null)
        {
            // experimentIdOrNull cannot be null here, but the compiler doesn't realize it
            SpaceIdentifier spaceId =
                    new SpaceIdentifier(experimentIdOrNull.getDatabaseInstanceCode(),
                            experimentIdOrNull.getSpaceCode());
            sampleId = new SampleIdentifier(spaceId, sampleCode);
            sample = new NewSample(sampleId.toString(), replicaSampleType, null, null);
            sample.setExperimentIdentifier(experimentIdOrNull.toString());
        }

        String userIdOrNull = (sessionContext == null) ? null : sessionContext.getUserName();
        getOpenbisService().registerSample(sample, userIdOrNull);
        return sampleId;
    }

    private void handleDerivedDataSet(SampleIdentifier sampleId,
            ImageMetadataExtractor imageMetadata)
    {
        // Register a data set for the image
        File imageDataSet = imageMetadata.getFolder();
        IDataSetHandler delegator = getDelegator();
        if (delegator instanceof IDataSetHandlerRpc)
        {
            DataSetInformation template = new DataSetInformation();
            template.setSampleCode(sampleId.getSampleCode());
            template.setSpaceCode(sampleId.getSpaceLevel().getSpaceCode());
            template.setInstanceCode(sampleId.getSpaceLevel().getDatabaseInstanceCode());
            // ((IDataSetHandlerRpc) delegator).handleDataSet(imageDataSet, template);
        } else
        {
            delegator.handleDataSet(imageDataSet);
        }
    }

    /**
     * Get the "big data set" -- the one that is the parent of all the derived data sets I register
     */
    private DataSetInformation getBigDataSetInformation()
    {
        // The big data set is the first one registered.
        return dataSetInformation.get(0);
    }
}
