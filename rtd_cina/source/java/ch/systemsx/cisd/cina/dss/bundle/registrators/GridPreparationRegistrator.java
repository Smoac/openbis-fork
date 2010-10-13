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

import ch.systemsx.cisd.cina.shared.metadata.BundleMetadataExtractor;
import ch.systemsx.cisd.cina.shared.metadata.ReplicaMetadataExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Creates/Updates a grid preparation sample. Spawns registrators for each of the replicas in the
 * bundle. Creates a data set that references the child data sets created by the replicas.
 * <p>
 * The GridPreparationRegistrator is the public interface to bundle registration. All other
 * registrators are internal to the package.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class GridPreparationRegistrator extends BundleDataSetHelper
{
    // Registration State
    private final BundleMetadataExtractor bundleMetadataExtractor;

    // Processing State (gets set during the execution of registration)
    private SampleIdentifier gridPrepSampleId;

    private Sample gridPrepSample;

    /**
     * Constructor.
     * 
     * @param globalState
     * @param dataSet
     */
    public GridPreparationRegistrator(BundleRegistrationState globalState, File dataSet)
    {
        super(globalState, dataSet);
        bundleMetadataExtractor = new BundleMetadataExtractor(dataSet);
        bundleMetadataExtractor.prepare();
    }

    /**
     * Create the grid preparation sample (if necessary). Register the bundle data set, run the
     * registration code on each replica, make the registered data sets children of the bundle data
     * set.
     */
    public void register()
    {
        retrieveOrCreateGridPrepSample();
        for (ReplicaMetadataExtractor replicaMetadataExtractor : bundleMetadataExtractor
                .getReplicaMetadataExtractors())
        {
            new ReplicaRegistrator(globalState, replicaMetadataExtractor, gridPrepSample,
                    gridPrepSampleId, dataSet).register();
        }
        // getDataSetInformation\
    }

    private void retrieveOrCreateGridPrepSample()
    {
        DataSetInformation dataSetInfo = getDelegator().getCallerDataSetInformation();
        ExperimentIdentifier expId = dataSetInfo.getExperimentIdentifier();
        String sampleCode = bundleMetadataExtractor.tryGridPrepSampleCode();
        assert expId != null;
        assert sampleCode != null;
        assert sampleCode.length() > 0;

        SpaceIdentifier spaceId =
                new SpaceIdentifier(expId.getDatabaseInstanceCode(), expId.getSpaceCode());
        gridPrepSampleId = new SampleIdentifier(spaceId, sampleCode);
        gridPrepSample = getOpenbisService().tryGetSampleWithExperiment(gridPrepSampleId);

        // Sample doesn't exist, create it
        if (gridPrepSample == null)
        {
            NewSample newSample =
                    NewSample.createWithParent(gridPrepSampleId.toString(),
                            globalState.getGridPrepSampleType(), null, null);
            newSample.setExperimentIdentifier(expId.toString());

            String userId = getSessionContext().getUserName();
            getOpenbisService().registerSample(newSample, userId);
            gridPrepSample = getOpenbisService().tryGetSampleWithExperiment(gridPrepSampleId);
        }

        assert gridPrepSample != null;
    }
}
