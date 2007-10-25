/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * Data structure Version 1.0.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStructureV1_0 extends AbstractDataStructure
{
    static final String DIR_METADATA = "metadata";

    static final String DIR_DATA = "data";

    static final String DIR_ORIGINAL = "original";

    private static final Version VERSION = new Version(1, 0);

    private Format format;

    /**
     * Creates a new instance relying on the specified storage.
     */
    public DataStructureV1_0(IStorage storage)
    {
        super(storage);
    }

    /**
     * Returns version 1.0.
     */
    public Version getVersion()
    {
        return VERSION;
    }

    /**
     * Returns the directory containing the original data.
     */
    public IDirectory getOriginalData()
    {
        return Utilities.getOrCreateSubDirectory(getDataDirectory(), DIR_ORIGINAL);
    }

    /**
     * Returns the formated data. This method can be called only after method {@link #setFormat(Format)} has been
     * invoked. If the format is not known {@link UnknownFormat1_0} will be assumed.
     * 
     * @throws DataStructureException if this method has been invoked before the format has been set.
     */
    public IFormattedData getFormatedData()
    {
        if (format == null)
        {
            throw new DataStructureException("Couldn't create formated data because of undefined format.");
        }
        return FormatedDataFactory.createFormatedData(getMetaDataDirectory(), format, UnknownFormat1_0.UNKNOWN_1_0);
    }

    /**
     * Sets the data format of this structure.
     */
    public void setFormat(Format format)
    {
        assert format != null : "Unspecified format.";
        this.format = format;
    }

    /**
     * Returns the experiment identifier.
     * 
     * @throws DataStructureException if the experiment identifier hasn't be loaded nor hasn't be set by
     *             {@link #setExperimentIdentifier(ExperimentIdentifier)}.
     */
    public ExperimentIdentifier getExperimentIdentifier()
    {
        return ExperimentIdentifier.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the experiment identifier. Overwrites an already set or loaded value.
     */
    public void setExperimentIdentifier(ExperimentIdentifier id)
    {
        assert id != null : "Unspecified experiment identifier";
        id.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the processing type.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setProcessingType(ProcessingType)}.
     */
    public ProcessingType getProcessingType()
    {
        return ProcessingType.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the processing type. Overwrites an already set or loaded value.
     */
    public void setProcessingType(ProcessingType type)
    {
        assert type != null : "Unspecified processing type.";
        type.saveTo(getMetaDataDirectory());
    }

    /**
     * Loads the data structure from the storage and sets the format.
     */
    @Override
    public void load()
    {
        super.load();
        setFormat(Format.loadFrom(getMetaDataDirectory()));
    }

    @Override
    public void save()
    {
        if (getOriginalData().iterator().hasNext() == false)
        {
            throw new DataStructureException("Empty original data directory.");
        }
        IDirectory metaDataDirectory = getMetaDataDirectory();
        if (metaDataDirectory.tryToGetNode(Format.FORMAT_DIR) == null)
        {
            if (format == null)
            {
                throw new DataStructureException("Unspecified format.");
            }
            format.saveTo(metaDataDirectory);
        }
        if (metaDataDirectory.tryToGetNode(ExperimentIdentifier.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified experiment identifier.");
        }
        if (metaDataDirectory.tryToGetNode(ProcessingType.PROCESSING_TYPE) == null)
        {
            throw new DataStructureException("Unspecified processing type.");
        }
        super.save();
    }

    private IDirectory getDataDirectory()
    {
        return Utilities.getOrCreateSubDirectory(root, DIR_DATA);
    }

    private IDirectory getMetaDataDirectory()
    {
        return Utilities.getOrCreateSubDirectory(root, DIR_METADATA);
    }
}
