/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.v1_1;

import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.Version;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.bds.v1_0.DataStructureV1_0;

/**
 * Data structure Version 1.1.
 * 
 * @author Christian Ribeaud
 */
public final class DataStructureV1_1 extends DataStructureV1_0 implements IDataStructureV1_1
{
    private static final Version VERSION = new Version(1, 1);

    public DataStructureV1_1(final IStorage storage)
    {
        super(storage);
    }

    //
    // IDataStructureV1_1
    //

    /**
     * Returns the sample with its owner (a space or a database instance).
     * <p>
     * This is only available in version 1.1. Using this method with data structure version 1.0
     * throws an exception.
     * </p>
     * 
     * @throws DataStructureException if trying to use this method with data structure of version
     *             1.0.
     */
    public final SampleWithOwner getSampleWithOwner()
    {
        final Sample sample = getSample();
        if (sample instanceof SampleWithOwner == false)
        {
            throw new DataStructureException("Can not be used in data structure v1.0.");
        }
        return (SampleWithOwner) sample;
    }

    /**
     * Returns the experiment identifier with the database instance <i>UUID</i>.
     * <p>
     * This is only available in version 1.1. Using this method with data structure version 1.0
     * throws an exception.
     * </p>
     * 
     * @throws DataStructureException if trying to use this method with data structure of version
     *             1.0.
     */
    public final ExperimentIdentifierWithUUID getExperimentIdentifierWithUUID()
    {
        final ExperimentIdentifier experimentIdentifier = getExperimentIdentifier();
        if (experimentIdentifier instanceof ExperimentIdentifierWithUUID == false)
        {
            throw new DataStructureException("Can not be used in data structure v1.0.");
        }
        return (ExperimentIdentifierWithUUID) experimentIdentifier;
    }

    //
    // DataStructureV1_0
    //

    @Override
    public final Version getVersion()
    {
        return VERSION;
    }

    /**
     * Returns the sample.
     * <p>
     * For backward compatibility, loads a {@link Sample} when no
     * <code>SampleWithOwner.SPACE_CODE</code> node could be found in sample directory.
     * </p>
     * 
     * @throws DataStructureException if the sample hasn't be loaded nor hasn't be set by
     *             {@link #setSample(Sample)}.
     * @return a {@link Sample} or {@link SampleWithOwner} (if v1.1).
     */
    @Override
    public final Sample getSample()
    {
        final IDirectory metaDataDirectory = getMetaDataDirectory();
        final IDirectory sampleDir = metaDataDirectory.tryGetNode(Sample.FOLDER).tryAsDirectory();
        if (sampleDir.tryGetNode(SampleWithOwner.SPACE_CODE) == null)
        {
            return Sample.loadFrom(metaDataDirectory);
        }
        return SampleWithOwner.loadFrom(metaDataDirectory);
    }

    /**
     * Sets the sample. Overwrites an already set or loaded value.
     * 
     * @param sample Must be an instance of {@link SampleWithOwner}.
     */
    @Override
    public final void setSample(final Sample sample)
    {
        assert sample != null : "Unspecified sample.";
        if (sample instanceof SampleWithOwner == false)
        {
            throw new DataStructureException("Must be an instance of SampleWithOwner.");
        }
        super.setSample(sample);
    }

    /**
     * Sets the experiment identifier. Overwrites an already set or loaded value.
     * 
     * @param experimentIdentifier Must be an instance of {@link ExperimentIdentifierWithUUID}.
     */
    @Override
    public final void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null : "Unspecified experiment identifier.";
        if (experimentIdentifier instanceof ExperimentIdentifierWithUUID == false)
        {
            throw new DataStructureException("Must be an instance of ExperimentIdentifierWithUUID.");
        }
        super.setExperimentIdentifier(experimentIdentifier);
    }

    /**
     * Returns the experiment identifier.
     * <p>
     * For backward compatibility, loads a {@link ExperimentIdentifier} when no
     * <code>ExperimentIdentifierWithUUID.INSTANCE_UUID</code> node could be found in experiment
     * identifier directory.
     * </p>
     * 
     * @throws DataStructureException if the experiment identifier hasn't be loaded nor hasn't be
     *             set by {@link #setExperimentIdentifier(ExperimentIdentifier)}.
     * @return a {@link Sample} or {@link SampleWithOwner} (if v1.1).
     */
    @Override
    public final ExperimentIdentifier getExperimentIdentifier()
    {
        final IDirectory metaDataDirectory = getMetaDataDirectory();
        final IDirectory experimentIdentifierDirectory =
                metaDataDirectory.tryGetNode(ExperimentIdentifier.FOLDER).tryAsDirectory();
        if (experimentIdentifierDirectory.tryGetNode(ExperimentIdentifierWithUUID.INSTANCE_UUID) == null)
        {
            return ExperimentIdentifier.loadFrom(metaDataDirectory);
        }
        return ExperimentIdentifierWithUUID.loadFrom(metaDataDirectory);
    }

    @Override
    public final void performClosing()
    {
        if (getSample() instanceof SampleWithOwner == false)
        {
            throw new DataStructureException(
                    "The owner (space or database instance) has not been set.");
        }
        if (getExperimentIdentifier() instanceof ExperimentIdentifierWithUUID == false)
        {
            throw new DataStructureException(
                    "Instance UUID not specified for exeperiment identifier.");
        }
        super.performClosing();
    }
}
