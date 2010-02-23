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

package ch.systemsx.cisd.bds.v1_0;

import java.util.Set;

import ch.systemsx.cisd.bds.AbstractDataStructure;
import ch.systemsx.cisd.bds.DataSet;
import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.ExperimentRegistrationTimestamp;
import ch.systemsx.cisd.bds.ExperimentRegistrator;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.FormattedDataFactory;
import ch.systemsx.cisd.bds.IAnnotations;
import ch.systemsx.cisd.bds.IFormatParameters;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Reference;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.UnknownFormatV1_0;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.Version;
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
public class DataStructureV1_0 extends AbstractDataStructure implements IDataStructureV1_0
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
        if (figureComputeFileChecksums(formatParameters))
        {
            registerHandler(new ChecksumHandler(getMetaDataDirectory().makeDirectory(
                    ChecksumHandler.CHECKSUM_DIRECTORY), getOriginalData()));
        }
    }

    private static boolean figureComputeFileChecksums(IFormatParameters params)
    {
        String paramName = Format.COMPUTE_FILE_CHECKSUMS;
        if (params.containsParameter(paramName))
        {
            return (Utilities.Boolean.fromString((String) params.getValue(paramName))).toBoolean();
        } else
        {
            return true; // default value
        }
    }

    private final IDirectory getDataDirectory()
    {
        return Utilities.getOrCreateSubDirectory(root, DIR_DATA);
    }

    protected final IDirectory getMetaDataDirectory()
    {
        return Utilities.getOrCreateSubDirectory(root, DIR_METADATA);
    }

    private final IDirectory getAnnotationsDirectory()
    {
        return Utilities.getOrCreateSubDirectory(root, DIR_ANNOTATIONS);
    }

    private final IDirectory getParametersDirectory()
    {
        return Utilities.getOrCreateSubDirectory(getMetaDataDirectory(), DIR_PARAMETERS);
    }

    //
    // IDataStructureV1_X
    //

    public final IDirectory getStandardData()
    {
        return Utilities.getOrCreateSubDirectory(getDataDirectory(), DIR_STANDARD);
    }

    public final IDirectory getOriginalData()
    {
        return Utilities.getOrCreateSubDirectory(getDataDirectory(), DIR_ORIGINAL);
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

    public ExperimentIdentifier getExperimentIdentifier()
    {
        return ExperimentIdentifier.loadFrom(getMetaDataDirectory());
    }

    public void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null : "Unspecified experiment identifier";
        experimentIdentifier.saveTo(getMetaDataDirectory());
    }

    public final ExperimentRegistrationTimestamp getExperimentRegistratorTimestamp()
    {
        return ExperimentRegistrationTimestamp.loadFrom(getMetaDataDirectory());
    }

    public final void setExperimentRegistrationTimestamp(final ExperimentRegistrationTimestamp date)
    {
        date.saveTo(getMetaDataDirectory());
    }

    public final ExperimentRegistrator getExperimentRegistrator()
    {
        return ExperimentRegistrator.loadFrom(getMetaDataDirectory());
    }

    public final void setExperimentRegistrator(final ExperimentRegistrator registrator)
    {
        assert registrator != null : "Unspecified experiment registrator.";
        registrator.saveTo(getMetaDataDirectory());
    }

    public Sample getSample()
    {
        return Sample.loadFrom(getMetaDataDirectory());
    }

    /**
     * Sets the measurement entity. Overwrites an already set or loaded value.
     */
    public void setSample(final Sample sample)
    {
        assert sample != null : "Unspecified sample.";
        sample.saveTo(getMetaDataDirectory());
    }

    public final void addReference(final Reference reference)
    {
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
        return DataSet.loadFrom(getMetaDataDirectory());
    }

    //
    // AbstractDataStructure
    //

    @Override
    public void performCreating()
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
        if (metaDataDirectory.tryGetNode(ExperimentIdentifier.FOLDER) == null)
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
