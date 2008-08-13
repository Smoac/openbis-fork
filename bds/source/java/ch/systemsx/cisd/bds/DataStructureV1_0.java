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

import java.util.Set;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.handler.ChecksumHandler;
import ch.systemsx.cisd.bds.handler.MappingFileHandler;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * Data structure Version 1.0.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStructureV1_0 extends AbstractDataStructure implements IDataStructureV1_X
{
    public static final String DIR_METADATA = "metadata";

    public static final String DIR_ANNOTATIONS = "annotations";

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

    private IAnnotations annotations;

    /**
     * Creates a new instance relying on the specified storage.
     */
    public DataStructureV1_0(final IStorage storage)
    {
        super(storage);
    }

    private final void registerHandlers()
    {
        mappingFileHandler =
                new MappingFileHandler(getMetaDataDirectory(), getStandardData(), getOriginalData());
        registerHandler(mappingFileHandler);
        registerHandler(new ChecksumHandler(getMetaDataDirectory().makeDirectory(
                ChecksumHandler.CHECKSUM_DIRECTORY), getOriginalData()));
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

    private final IDirectory getDataDirectory()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(root, DIR_DATA);
    }

    final IDirectory getMetaDataDirectory()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(root, DIR_METADATA);
    }

    private final IDirectory getAnnotationsDirectory()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(root, DIR_ANNOTATIONS);
    }

    private final IDirectory getParametersDirectory()
    {
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(getMetaDataDirectory(), DIR_PARAMETERS);
    }

    /**
     * Returns the formatted data. This method can be called only after method
     * {@link #setFormat(Format)} has been invoked. If the format is not known
     * {@link UnknownFormatV1_0} will be assumed.
     * 
     * @throws DataStructureException if this method has been invoked before the format has been
     *             set.
     */
    public final IFormattedData getFormattedData() throws DataStructureException
    {
        assertOpenOrCreated();
        if (format == null)
        {
            throw new DataStructureException(
                    "Couldn't create formatted data because of unspecified format.");
        }
        return FormattedDataFactory.createFormattedData(getDataDirectory(), format,
                UnknownFormatV1_0.UNKNOWN_1_0, formatParameters);
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
     * @throws IllegalArgumentException if they is already a parameter with same name as
     *             <code>parameter</code>.
     */
    public final void addFormatParameter(final FormatParameter formatParameter)
    {
        assert formatParameter != null : "Unspecified format parameter.";
        formatParameters.addParameter(formatParameter);
    }

    public final void setAnnotations(final IAnnotations annotations)
    {
        this.annotations = annotations;
    }

    /**
     * Returns the experiment identifier.
     * 
     * @throws DataStructureException if the experiment identifier hasn't be loaded nor hasn't be
     *             set by {@link #setExperimentIdentifier(ExperimentIdentifier)}.
     */
    public final ExperimentIdentifier getExperimentIdentifier()
    {
        assertOpenOrCreated();
        return ExperimentIdentifier.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the experiment identifier. Overwrites an already set or loaded value.
     */
    public final void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null : "Unspecified experiment identifier";
        assertOpenOrCreated();
        experimentIdentifier.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the date of registration of the experiment.
     * 
     * @throws DataStructureException if the processing type hasn't be loaded nor hasn't be set by
     *             {@link #setExperimentRegistrationTimestamp(ExperimentRegistrationTimestamp)}.
     */
    public final ExperimentRegistrationTimestamp getExperimentRegistratorTimestamp()
    {
        assertOpenOrCreated();
        return ExperimentRegistrationTimestamp.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the date of registration of the experiment.
     */
    public final void setExperimentRegistrationTimestamp(final ExperimentRegistrationTimestamp date)
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
    public final ExperimentRegistrator getExperimentRegistrator()
    {
        assertOpenOrCreated();
        return ExperimentRegistrator.loadFrom(getMetaDataDirectory());
    }

    public final void setExperimentRegistrator(final ExperimentRegistrator registrator)
    {
        assert registrator != null : "Unspecified experiment registrator.";
        assertOpenOrCreated();
        registrator.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the sample.
     * 
     * @throws DataStructureException if the sample hasn't be loaded nor hasn't be set by
     *             {@link #setSample(Sample)}.
     */
    public Sample getSample()
    {
        assertOpenOrCreated();
        return Sample.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the measurement entity. Overwrites an already set or loaded value.
     */
    public void setSample(final Sample sample)
    {
        assert sample != null : "Unspecified sample.";
        assertOpenOrCreated();
        sample.saveTo(getMetaDataDirectory());
    }

    public final void addReference(final Reference reference)
    {
        assertOpenOrCreated();
        mappingFileHandler.addReference(reference);
    }

    public final Set<Reference> getStandardOriginalMapping()
    {
        return mappingFileHandler.getReferences();
    }

    /**
     * Sets given <var>dataSet</var> in metadata directory.
     */
    public final void setDataSet(final DataSet dataSet)
    {
        assert dataSet != null : "Unspecified data set.";
        assertOpenOrCreated();
        dataSet.saveTo(getMetaDataDirectory());
    }

    /**
     * Returns the data set.
     * 
     * @throws DataStructureException if the data set hasn't be loaded nor hasn't be set by
     *             {@link #setDataSet(DataSet)}.
     */
    public final DataSet getDataSet()
    {
        assertOpenOrCreated();
        return DataSet.loadFrom(getMetaDataDirectory());
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
        final IDirectory metaDataDirectory = getMetaDataDirectory();
        if (metaDataDirectory.tryGetNode(Format.FORMAT) == null && format == null)
        {
            throw new DataStructureException("Unspecified format.");
        }
        if (metaDataDirectory.tryGetNode(ExperimentIdentifier.EXPERIMENT_IDENTIFIER) == null)
        {
            throw new DataStructureException("Unspecified experiment identifier.");
        }
        if (metaDataDirectory
                .tryGetNode(ExperimentRegistrationTimestamp.EXPERIMENT_REGISTRATION_TIMESTAMP) == null)
        {
            throw new DataStructureException("Unspecified experiment registration timestamp.");
        }
        if (metaDataDirectory.tryGetNode(ExperimentRegistrator.EXPERIMENT_REGISTRATOR) == null)
        {
            throw new DataStructureException("Unspecified experiment registrator.");
        }
        if (metaDataDirectory.tryGetNode(Sample.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified sample.");
        }
        if (metaDataDirectory.tryGetNode(DataSet.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified data set.");
        }
        if (annotations != null)
        {
            annotations.assertValid(getFormattedData());
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
    public void performClosing()
    {
        super.performClosing();
        final IDirectory metaDataDirectory = getMetaDataDirectory();
        formatParameters.saveTo(getParametersDirectory());
        if (metaDataDirectory.tryGetNode(Format.FORMAT) == null && format != null)
        {
            format.saveTo(metaDataDirectory);
        }
        if (annotations != null)
        {
            annotations.saveTo(getAnnotationsDirectory());
        }
    }

    public Version getVersion()
    {
        return VERSION;
    }

}
