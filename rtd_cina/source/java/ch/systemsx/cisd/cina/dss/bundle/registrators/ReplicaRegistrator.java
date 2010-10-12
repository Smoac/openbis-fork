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

import ch.systemsx.cisd.cina.shared.metadata.ReplicaMetadataExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Registers/Updates a replica, its metadata, the original images and the annotated images.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReplicaRegistrator extends BundleDataSetHelper
{
    private final ReplicaMetadataExtractor replicaMetadataExtractor;

    private final Sample gridPrepSample;

    private final SampleIdentifier gridPrepSampleId;

    // Processing State (gets set during the execution of registration)
    private SampleIdentifier replicaSampleId;

    private Sample replicaSample;

    /**
     * @param globalState
     * @param dataSet
     */
    ReplicaRegistrator(BundleRegistrationState globalState,
            ReplicaMetadataExtractor replicaMetadataExtractor, Sample gridPrepSample,
            SampleIdentifier gridPrepSampleId, File dataSet)
    {
        super(globalState, dataSet);

        this.replicaMetadataExtractor = replicaMetadataExtractor;
        this.gridPrepSample = gridPrepSample;
        this.gridPrepSampleId = gridPrepSampleId;
    }

    /**
     * Create the replica sample (if necessary). Register the original images (if the replica sample
     * did not exist), register the metadata dataset, and update the sample metadata, register the
     * annotated images.
     */
    public void register()
    {
        retrieveOrCreateGridPrepSample();
    }

    private void retrieveOrCreateGridPrepSample()
    {
        String sampleCode = replicaMetadataExtractor.tryReplicaSampleCode();
        replicaSampleId = new SampleIdentifier(gridPrepSampleId.getSpaceLevel(), sampleCode);
        replicaSample = getOpenbisService().tryGetSampleWithExperiment(replicaSampleId);

        // Sample doesn't exist, create it
        if (replicaSample == null)
        {
            NewSample newSample =
                    NewSample.createWithParent(replicaSampleId.toString(),
                            globalState.getReplicaSampleType(), null, gridPrepSampleId.toString());
            newSample.setExperimentIdentifier(gridPrepSample.getExperiment().getIdentifier());

            String userId = getSessionContext().getUserName();
            getOpenbisService().registerSample(newSample, userId);
            replicaSample = getOpenbisService().tryGetSampleWithExperiment(replicaSampleId);
        }

        assert replicaSample != null;
    }
}
