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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Data structure Version 1.0.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStructureV1_0 extends AbstractDataStructure
{
    static final String CHECKSUM_DIRECTORY = "md5sum";

    static final String DIR_METADATA = "metadata";
    
    static final String DIR_PARAMETERS = "parameters";

    static final String DIR_DATA = "data";

    static final String DIR_ORIGINAL = "original";
    
    static final String MAPPING_FILE = "standard_original_mapping";

    private static final Version VERSION = new Version(1, 0);
    
    private final ChecksumBuilder checksumBuilder = new ChecksumBuilder(new MD5ChecksumCalculator());
    private final Map<String, Reference> standardOriginalMapping = new LinkedHashMap<String, Reference>();
    private final FormatParameters formatParameters = new FormatParameters();

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
        assertOpenOrCreated();
        return Utilities.getOrCreateSubDirectory(getDataDirectory(), DIR_ORIGINAL);
    }

    /**
     * Returns the formated data. This method can be called only after method {@link #setFormat(Format)} has been
     * invoked. If the format is not known {@link UnknownFormat1_0} will be assumed.
     * 
     * @throws DataStructureException if this method has been invoked before the format has been set.
     */
    public IFormattedData getFormattedData()
    {
        assertOpenOrCreated();
        if (format == null)
        {
            throw new DataStructureException("Couldn't create formated data because of unspecified format.");
        }
        IDirectory metaData = getMetaDataDirectory();
        return FormatedDataFactory.createFormatedData(metaData, format, UnknownFormat1_0.UNKNOWN_1_0, formatParameters);
    }

    /**
     * Sets the data format of this structure.
     */
    public void setFormat(Format format)
    {
        assert format != null : "Unspecified format.";
        assertOpenOrCreated();
        this.format = format;
    }

    /**
     * Adds the specified format parameter.
     * 
     * @throws IllegalArgumentException if they is already a parameter with same name as <code>parameter</code>.
     */
    public void addFormatParameter(FormatParameter formatParameter)
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
    public void setExperimentRegistartionDate(ExperimentRegistratorDate date)
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
    
    /**
     * Returns the standard-original mapping.
     * 
     * @return an unmodifiable version of this map.
     */
    public Map<String, Reference> getStandardOriginalMapping()
    {
        return Collections.unmodifiableMap(standardOriginalMapping);
    }
    
    /**
     * Adds a reference to the standard-original mapping.
     * 
     * @throws DataStructureException if a reference with the same path has already been registered.
     */
    public void addReference(Reference reference)
    {
        assert reference != null : "Unspecified reference.";
        assertOpenOrCreated();
        String path = reference.getPath();
        if (standardOriginalMapping.containsKey(path))
        {
            throw new DataStructureException("There is already a reference for file '" + path + "'.");
        }
        standardOriginalMapping.put(path, reference);
    }
    
    @Override
    protected void assertValid()
    {
        if (getOriginalData().iterator().hasNext() == false)
        {
            throw new DataStructureException("Empty original data directory.");
        }
        IDirectory metaDataDirectory = getMetaDataDirectory();
        if (metaDataDirectory.tryToGetNode(Format.FORMAT_DIR) == null && format == null)
        {
            throw new DataStructureException("Unspecified format.");
        }
        if (metaDataDirectory.tryToGetNode(ExperimentIdentifier.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified experiment identifier.");
        }
        if (metaDataDirectory.tryToGetNode(ExperimentRegistratorDate.FILE_NAME) == null)
        {
            throw new DataStructureException("Unspecified experiment registration date.");
        }
        if (metaDataDirectory.tryToGetNode(ExperimentRegistrator.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified experiment registrator.");
        }
        if (metaDataDirectory.tryToGetNode(MeasurementEntity.FOLDER) == null)
        {
            throw new DataStructureException("Unspecified measurement entity.");
        }
        if (metaDataDirectory.tryToGetNode(ProcessingType.PROCESSING_TYPE) == null)
        {
            throw new DataStructureException("Unspecified processing type.");
        }
    }

    @Override
    protected void performOpening()
    {
        IDirectory metaDataDirectory = getMetaDataDirectory();
        setFormat(Format.loadFrom(metaDataDirectory));
        formatParameters.loadFrom(getParametersDirectory());
        loadStandardOriginalMapping(metaDataDirectory);
    }

    private void loadStandardOriginalMapping(IDirectory metaDataDirectory)
    {
        StringReader stringReader = new StringReader(Utilities.getString(metaDataDirectory, MAPPING_FILE));
        BufferedReader reader = new BufferedReader(stringReader);
        List<String> lines = new ArrayList<String>();
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Unexpected IOException.", ex);
        }
        standardOriginalMapping.clear();
        for (int i = 0; i < lines.size(); i++)
        {
            String referenceDefinition = lines.get(i);
            int i1 = referenceDefinition.indexOf('\t');
            if (i1 < 0)
            {
                throw new DataStructureException("Error in standard-original mapping line " + (i + 1)
                        + ": missing first tab character: " + referenceDefinition);
            }
            String path = referenceDefinition.substring(0, i1);
            int i2 = referenceDefinition.indexOf('\t', i1 + 1);
            if (i2 < 0)
            {
                throw new DataStructureException("Error in standard-original mapping line " + (i + 1)
                        + ": missing second tab character: " + referenceDefinition);
            }
            ReferenceType type = ReferenceType.tryToResolveByShortName(referenceDefinition.substring(i1 + 1, i2));
            standardOriginalMapping.put(path, new Reference(path, referenceDefinition.substring(i2 + 1), type));
        }
    }
    
    @Override
    protected void performClosing()
    {
        IDirectory metaDataDirectory = getMetaDataDirectory();
        IDirectory checksumDirectory = metaDataDirectory.makeDirectory(CHECKSUM_DIRECTORY);
        String checksumsOfOriginal = checksumBuilder.buildChecksumsForAllFilesIn(getOriginalData());
        checksumDirectory.addKeyValuePair(DIR_ORIGINAL, checksumsOfOriginal);
        
        formatParameters.saveTo(getParametersDirectory());
        
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer, true);
        Collection<Reference> values = standardOriginalMapping.values();
        for (Reference reference : values)
        {
            String path = reference.getPath();
            String shortName = reference.getReferenceType().getShortName();
            String originalPath = reference.getOriginalPath();
            printWriter.println(path + "\t" + shortName + "\t" + originalPath);
        }
        printWriter.close();
        metaDataDirectory.addKeyValuePair(MAPPING_FILE, writer.toString());
        
        if (metaDataDirectory.tryToGetNode(Format.FORMAT_DIR) == null && format != null)
        {
            format.saveTo(metaDataDirectory);
        }
    }

    private IDirectory getDataDirectory()
    {
        return Utilities.getOrCreateSubDirectory(root, DIR_DATA);
    }

    private IDirectory getMetaDataDirectory()
    {
        return Utilities.getOrCreateSubDirectory(root, DIR_METADATA);
    }
    
    private IDirectory getParametersDirectory()
    {
        return Utilities.getOrCreateSubDirectory(getMetaDataDirectory(), DIR_PARAMETERS);
    }
    
}
