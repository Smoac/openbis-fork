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

import java.util.Map;

import ch.systemsx.cisd.bds.handler.ChecksumHandler;
import ch.systemsx.cisd.bds.handler.MappingFileHandler;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * Data structure Version 1.0.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStructureV1_0 extends AbstractDataStructure
{
    public static final String DIR_METADATA = "metadata";

    public static final String DIR_PARAMETERS = "parameters";

    public static final String DIR_DATA = "data";

    /** The directory where <i>original</i> data could be found. */
    public static final String DIR_ORIGINAL = "original";

    /** The directory where <i>standardized</i> data could be found. */
    public static final String DIR_STANDARD = "standard";

    private static final Version VERSION = new Version(1, 0);

    private final FormatParameters formatParameters = new FormatParameters();

    private MappingFileHandler mappingFileHandler;

    private Format format;

    /**
     * Creates a new instance relying on the specified storage.
     */
    public DataStructureV1_0(final IStorage storage)
    {
        super(storage);
    }

    private final void registerHandlers()
    {
        mappingFileHandler = new MappingFileHandler(getMetaDataDirectory(), getStandardData(), getOriginalData());
        registerHandler(mappingFileHandler);
        registerHandler(new ChecksumHandler(getMetaDataDirectory().makeDirectory(ChecksumHandler.CHECKSUM_DIRECTORY),
                getOriginalData()));
    }

    /**
     * Returns the directory containing the original data.
     */
    public final IDirectory getOriginalData()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(getDataDirectory(), DIR_ORIGINAL);
    }

    /**
     * Returns the directory containing the standardized data.
     */
    public final IDirectory getStandardData()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(getDataDirectory(), DIR_STANDARD);
    }

    public final IDirectory getDataDirectory()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(root, DIR_DATA);
    }

    public final IDirectory getMetaDataDirectory()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(root, DIR_METADATA);
    }

    public final IDirectory getParametersDirectory()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(getMetaDataDirectory(), DIR_PARAMETERS);
    }

    /**
     * Returns the formated data. This method can be called only after method {@link #setFormat(Format)} has been
     * invoked. If the format is not known {@link UnknownFormat1_0} will be assumed.
     * 
     * @throws DataStructureException if this method has been invoked before the format has been set.
     */
    public IFormattedData getFormattedData() throws DataStructureException
    {
        assertOpenOrCreated();
        if (format == null)
        {
            throw new DataStructureException("Couldn't create formated data because of unspecified format.");
        }
        return FormatedDataFactory.createFormatedData(getDataDirectory(), format, UnknownFormat1_0.UNKNOWN_1_0,
                formatParameters);
    }

    /**
     * Sets the data format of this structure.
     */
    public final void setFormat(final Format format)
    {
        assert format != null : "Unspecified format.";
        assertOpenOrCreated();
        this.format = format;
        formatParameters.setFormatParameterFactory(format.getFormatParameterFactory());
    }

    /**
     * Adds the specified format parameter.
     * 
     * @throws IllegalArgumentException if they is already a parameter with same name as <code>parameter</code>.
     */
    public void addFormatParameter(final FormatParameter formatParameter)
    {
        assert formatParameter != null : "Unspecified format parameter.";
        formatParameters.addParameter(formatParameter);
    }

    /**
     * Returns the experiment identifier.
     * 
     * @throws DataStructureException if the experiment identifier hasn't be loaded nor hasn't be set by
     *             {@link #setExperimentIdentifier(ExperimentIdentifier)}.
     */
    public ExperimentIdentifier getExperimentIdentifier()
    {
        assertOpenOrCreated();
        return ExperimentIdentifier.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the experiment identifier. Overwrites an already set or loaded value.
     */
    public void setExperimentIdentifier(ExperimentIdentifier id)
    {
        assert id != null : "Unspecified experiment identifier";
        assertOpenOrCreated();
        id.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the date of registration of the experiment.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setExperimentRegistrator(ExperimentRegistrator)}.
     */
    public ExperimentRegistratorDate getExperimentRegistratorDate()
    {
        assertOpenOrCreated();
        return ExperimentRegistratorDate.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the date of registration of the experiment.
     */
    public void setExperimentRegistrationDate(ExperimentRegistratorDate date)
    {
        assertOpenOrCreated();
        date.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the experiment registrator.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setExperimentRegistrator(ExperimentRegistrator)}.
     */
    public ExperimentRegistrator getExperimentRegistrator()
    {
        assertOpenOrCreated();
        return ExperimentRegistrator.loadFrom(getMetaDataDirectory());
    }

    public void setExperimentRegistrator(ExperimentRegistrator registrator)
    {
        assert registrator != null : "Unspecified experiment registrator.";
        assertOpenOrCreated();
        registrator.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the measurement entity.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setProcessingType(ProcessingType)}.
     */
    public MeasurementEntity getMeasurementEntity()
    {
        assertOpenOrCreated();
        return MeasurementEntity.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the measurement entity. Overwrites an already set or loaded value.
     */
    public void setMeasurementEntity(MeasurementEntity entity)
    {
        assert entity != null : "Unspecified measurement entity.";
        assertOpenOrCreated();
        entity.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the processing type.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setProcessingType(ProcessingType)}.
     */
    public ProcessingType getProcessingType()
    {
        assertOpenOrCreated();
        return ProcessingType.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the processing type. Overwrites an already set or loaded value.
     */
    public void setProcessingType(ProcessingType type)
    {
        assert type != null : "Unspecified processing type.";
        assertOpenOrCreated();
        type.saveTo(getMetaDataDirectory());
    }

    public final void addReference(final Reference reference)
    {
        assertOpenOrCreated();
        mappingFileHandler.addReference(reference);
    }

    public final Map<String, Reference> getStandardOriginalMapping()
    {
        return mappingFileHandler.getStandardOriginalMapping();
    }

    //
    // AbstractDataStructure
    //

    @Override
    protected void afterCreation()
    {
        registerHandlers();
    }

    @Override
    public final void assertValid()
    {
        super.assertValid();
        if (getOriginalData().iterator().hasNext() == false)
        {
            throw new DataStructureException("Empty original data directory.");
        }
        IDirectory metaDataDirectory = getMetaDataDirectory();
        if (metaDataDirectory.tryGetNode(Format.FORMAT_DIR) == null && format == null)
        {
            throw new DataStructureException("Unspecified format.");
        }
        if (metaDataDirectory.tryGetNode(ExperimentIdentifier.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified experiment identifier.");
        }
        if (metaDataDirectory.tryGetNode(ExperimentRegistratorDate.FILE_NAME) == null)
        {
            throw new DataStructureException("Unspecified experiment registration date.");
        }
        if (metaDataDirectory.tryGetNode(ExperimentRegistrator.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified experiment registrator.");
        }
        if (metaDataDirectory.tryGetNode(MeasurementEntity.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified measurement entity.");
        }
        if (metaDataDirectory.tryGetNode(ProcessingType.PROCESSING_TYPE) == null)
        {
            throw new DataStructureException("Unspecified processing type.");
        }
    }

    @Override
    public final void performOpening()
    {
        registerHandlers();
        super.performOpening();
        setFormat(Format.loadFrom(getMetaDataDirectory()));
        formatParameters.loadFrom(getParametersDirectory());
    }

    @Override
    public final void performClosing()
    {
        super.performClosing();
        final IDirectory metaDataDirectory = getMetaDataDirectory();
        formatParameters.saveTo(getParametersDirectory());
        if (metaDataDirectory.tryGetNode(Format.FORMAT_DIR) == null && format != null)
        {
            format.saveTo(metaDataDirectory);
        }
    }

    public final Version getVersion()
    {
        return VERSION;
    }

}
