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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.DataSet;
import ch.systemsx.cisd.bds.DataStructureFactory;
import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.ExperimentRegistrationTimestamp;
import ch.systemsx.cisd.bds.ExperimentRegistrator;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.IFormatParameterFactory;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Reference;
import ch.systemsx.cisd.bds.ReferenceType;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.UnknownFormatV1_0;
import ch.systemsx.cisd.bds.Version;
import ch.systemsx.cisd.bds.IDataStructure.Mode;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.HCSImageAnnotations;
import ch.systemsx.cisd.bds.hcs.IHCSImageFormattedData;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.PlateGeometry;
import ch.systemsx.cisd.bds.hcs.IHCSImageFormattedData.NodePath;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.bds.v1_1.ExperimentIdentifierWithUUID;
import ch.systemsx.cisd.bds.v1_1.IDataStructureV1_1;
import ch.systemsx.cisd.bds.v1_1.SampleWithOwner;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.etlserver.HCSImageCheckList.FullLocation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * The <code>AbstractStorageAdapter</code> extension for <i>BDS</i> (Biological Data Standards).
 * <p>
 * When declared in <code>service.properties</code> file, it must specify a property called
 * <code>storage-adapter.version</code>. Otherwise instantiation will fail.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class BDSStorageProcessor extends AbstractStorageProcessor implements
        IHCSImageFileAccepter
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BDSStorageProcessor.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BDSStorageProcessor.class);

    private static final String PROPERTY_PREFIX = "Property '%s': ";

    private static final String NO_FORMAT_FORMAT =
            PROPERTY_PREFIX + "no valid and known format could be extracted from text '%s'.";

    static final String VERSION_KEY = "version";

    static final String SAMPLE_TYPE_DESCRIPTION_KEY = "sampleTypeDescription";

    static final String SAMPLE_TYPE_CODE_KEY = "sampleTypeCode";

    static final String FORMAT_KEY = "format";

    static final String FILE_EXTRACTOR_KEY = "file-extractor";

    static final String NO_VERSION_FORMAT =
            PROPERTY_PREFIX + "no version could be extracted from text '%s'.";

    private final Format format;

    private final String sampleTypeCode;

    private final String sampleTypeDescription;

    private IDataStructureV1_1 dataStructure;

    private File imageFileRootDirectory;

    private File dataStructureDir;

    private IHCSImageFileExtractor imageFileExtractor;

    private List<FormatParameter> formatParameters;

    private IHCSImageFormattedData imageFormattedData;

    private HCSImageCheckList imageCheckList;

    private final Version version;

    public BDSStorageProcessor(final Properties properties)
    {
        super(properties);
        version = parseVersion(getMandatoryProperty(VERSION_KEY));
        if (version.equals(new Version(1, 1)) == false)
        {
            throw new ConfigurationFailureException("Invalid version: " + version);
        }
        format = parseFormat(getMandatoryProperty(FORMAT_KEY));
        createFormatParameters();
        sampleTypeDescription = getMandatoryProperty(SAMPLE_TYPE_DESCRIPTION_KEY);
        if (needsImageFileExtractor())
        {
            final String property = getMandatoryProperty(FILE_EXTRACTOR_KEY);
            imageFileExtractor =
                    ClassUtils.create(IHCSImageFileExtractor.class, property, properties);
        }
        try
        {
            sampleTypeCode = getMandatoryProperty(SAMPLE_TYPE_CODE_KEY);
        } catch (final IllegalArgumentException ex)
        {
            throw new ConfigurationFailureException(ex.getMessage());
        }
    }

    private void createFormatParameters()
    {
        final IFormatParameterFactory formatParameterFactory = format.getFormatParameterFactory();
        formatParameters = new ArrayList<FormatParameter>();
        for (final String parameterName : format.getMandatoryParameterNames())
        {
            final String value = properties.getProperty(parameterName);
            if (value == null)
            {
                throw ConfigurationFailureException.fromTemplate(
                        "No value has been defined for parameter '%s'", parameterName);
            }
            addFormatParameter(formatParameterFactory, parameterName, value);
        }
        for (final String parameterName : format.getOptionalParameterNames())
        {
            final String value = properties.getProperty(parameterName);
            if (value != null)
            {
                addFormatParameter(formatParameterFactory, parameterName, value);
            }
        }
    }

    private void addFormatParameter(final IFormatParameterFactory formatParameterFactory,
            final String parameterName, final String value)
    {
        final FormatParameter formatParameter =
                formatParameterFactory.createFormatParameter(parameterName, value);
        if (formatParameter == null)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "Given value '%s' is not understandable for parameter '%s'", value,
                    parameterName);
        }
        formatParameters.add(formatParameter);
    }

    final static Version parseVersion(final String versionString)
    {
        final Version version = Version.createVersionFromString(versionString);
        if (version == null)
        {
            throw ConfigurationFailureException.fromTemplate(NO_VERSION_FORMAT, VERSION_KEY,
                    versionString);
        }
        return version;
    }

    final static Format parseFormat(final String formatString)
    {
        final Format format = Format.tryToCreateFormatFromString(formatString);
        if (format == null)
        {
            throw ConfigurationFailureException.fromTemplate(NO_FORMAT_FORMAT, FORMAT_KEY,
                    formatString);
        }
        return format;
    }

    private final ExperimentIdentifierWithUUID createExperimentIdentifier(
            final DataSetInformation dataSetInformation)
    {
        final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentIdentifier =
                dataSetInformation.getExperimentIdentifier();
        final String projectCode = experimentIdentifier.getProjectCode();
        final String experimentCode = experimentIdentifier.getExperimentCode();
        final String spaceCode = experimentIdentifier.getSpaceCode();
        final String instanceCode = dataSetInformation.getInstanceCode();
        return new ExperimentIdentifierWithUUID(instanceCode, dataSetInformation.getInstanceUUID(),
                spaceCode, projectCode, experimentCode);
    }

    private final static void checkDataSetInformation(final DataSetInformation dataSetInformation)
    {
        assert dataSetInformation != null : "Unspecified data set information";
        assert dataSetInformation.getSampleIdentifier() != null : "Unspecified sample identifier";
        final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentIdentifier =
                dataSetInformation.getExperimentIdentifier();
        assert experimentIdentifier != null : "Unspecified experiment identifier";
        checkExperimentIdentifier(experimentIdentifier);
    }

    private final static void checkExperimentIdentifier(
            final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier.getSpaceCode() != null : "Space code is null";
        assert experimentIdentifier.getExperimentCode() != null : "Experiment code is null";
        assert experimentIdentifier.getProjectCode() != null : "Project code is null";
    }

    private final IDataStructureV1_1 createDataStructure(final Experiment experiment,
            final DataSetInformation dataSetInformation, final ITypeExtractor typeExtractor,
            final File incomingDataSetPath, final File rootDir)
    {
        final FileStorage storage = new FileStorage(rootDir);
        final IDataStructureV1_1 structure =
                (IDataStructureV1_1) DataStructureFactory.createDataStructure(storage, version);

        List<FormatParameter> parameters = new ArrayList<FormatParameter>(formatParameters);
        final Geometry plateGeometry = getPlateGeometry(dataSetInformation);
        parameters.add(new FormatParameter(PlateGeometry.PLATE_GEOMETRY, plateGeometry));

        structure.create(parameters);
        structure.setFormat(format);
        structure.setExperimentIdentifier(createExperimentIdentifier(dataSetInformation));
        structure.setExperimentRegistrationTimestamp(new ExperimentRegistrationTimestamp(experiment
                .getRegistrationDate()));
        final Person registrator = experiment.getRegistrator();
        final String firstName = registrator.getFirstName();
        final String lastName = registrator.getLastName();
        final String email = registrator.getEmail();
        structure.setExperimentRegistrator(new ExperimentRegistrator(firstName, lastName, email));
        structure.setSample(createSample(dataSetInformation));
        structure.setDataSet(createDataSet(dataSetInformation, typeExtractor, incomingDataSetPath));

        return structure;
    }

    private Geometry getPlateGeometry(final DataSetInformation dataSetInformation)
    {
        final IEntityProperty[] sampleProperties = dataSetInformation.getProperties();
        final PlateDimension plateDimension =
                PlateDimensionParser.tryToGetPlateDimension(sampleProperties);
        if (plateDimension == null)
        {
            throw new EnvironmentFailureException(
                    "Missing plate geometry for the plate registered for sample identifier '"
                            + dataSetInformation.getSampleIdentifier() + "'.");
        }
        final Geometry plateGeometry =
                new PlateGeometry(plateDimension.getRowsNum(), plateDimension.getColsNum());
        return plateGeometry;
    }

    private final Sample createSample(final DataSetInformation dataSetInformation)
    {
        final String spaceCode = StringUtils.defaultString(dataSetInformation.getSpaceCode());
        final String instanceCode = dataSetInformation.getInstanceCode();
        final String instanceUUID = dataSetInformation.getInstanceUUID();
        assert instanceCode != null : "Unspecified database instance code";
        assert instanceUUID != null : "Unspecified database instance UUID";
        // TODO 2009-09-15, Tomasz Pylak: deal with the case when sample is null
        return new SampleWithOwner(dataSetInformation.getSampleCode(), sampleTypeCode,
                sampleTypeDescription, instanceUUID, instanceCode, spaceCode);
    }

    private final static DataSet createDataSet(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final File incomingDataSetPath)
    {
        final String dataSetCode = dataSetInformation.getDataSetCode();
        final List<String> parentCodes =
                new ArrayList<String>(dataSetInformation.getParentDataSetCodes());
        final boolean isMeasured = typeExtractor.isMeasuredData(incomingDataSetPath);
        final DataSetType dataSetType = typeExtractor.getDataSetType(incomingDataSetPath);
        final DataSet dataSet =
                new DataSet(dataSetCode, dataSetType.getCode(),
                        ch.systemsx.cisd.bds.Utilities.Boolean.fromBoolean(isMeasured),
                        dataSetInformation.getProductionDate(), dataSetInformation
                                .getProducerCode(), parentCodes);
        return dataSet;
    }

    private final static String getPathOf(final INode node)
    {
        final StringBuilder builder = new StringBuilder(node.getName());
        IDirectory parent = node.tryGetParent();
        while (parent != null)
        {
            builder.insert(0, '/');
            builder.insert(0, parent.getName());
            parent = parent.tryGetParent();
        }
        return builder.toString();
    }

    // TODO 2007-12-09, Christian Ribeaud: It will be a better choice to make two different
    // implementations here: one for 'UnknownFormat1_0' and one for 'HCSImageFormat1_0'.
    private final boolean needsImageFileExtractor()
    {
        return format.getCode().equals(UnknownFormatV1_0.UNKNOWN_1_0.getCode()) == false;
    }

    // Although this check should be performed in the BDS library when closing is performed, we set
    // the complete flag here as we want to inform the registrator about the incompleteness.
    private void checkCompleteness(final DataSetInformation dataSetInformation,
            final String dataSetFileName, final IMailClient mailClientOrNull)
    {
        final List<FullLocation> fullLocations = imageCheckList.getCheckedOnFullLocations();
        final boolean complete = fullLocations.size() == 0;
        final IDataStructureV1_1 thisStructure = getDataStructure(dataStructureDir);
        final DataSet dataSet = thisStructure.getDataSet();
        dataSet.setComplete(complete);
        thisStructure.setDataSet(dataSet);
        dataSetInformation.setComplete(complete);
        if (complete == false)
        {
            final String message =
                    String.format("Incomplete data set '%s': %d image file(s) "
                            + "are missing (locations: %s)", dataSetFileName, fullLocations.size(),
                            CollectionUtils.abbreviate(fullLocations, 10));
            operationLog.warn(message);
            if (mailClientOrNull != null)
            {
                final ExperimentRegistrator registrator = thisStructure.getExperimentRegistrator();
                final String email = registrator.getEmail();
                if (StringUtils.isBlank(email) == false)
                {
                    try
                    {
                        mailClientOrNull.sendMessage("Incomplete data set '" + dataSetFileName
                                + "'", message, null, null, email);
                    } catch (final EnvironmentFailureException e)
                    {
                        notificationLog.error("Couldn't send the following e-mail to '" + email
                                + "': " + message, e);
                    }
                } else
                {
                    notificationLog.error("Unspecified e-mail address of experiment registrator "
                            + registrator);
                }
            }
        }
    }

    /**
     * For given <var>storedDataDirectory</var> returns the {@link IDataStructureV1_1}.
     * <p>
     * In ideal case returns internally saved <code>dataStructureDir</code> but when given
     * <var>storedDataDirectory</var> changed (meaning no longer equal to storage root), then we
     * have to reload the data structure.
     * </p>
     */
    private final IDataStructureV1_1 getDataStructure(final File storedDataDirectory)
    {
        final IDataStructureV1_1 thisStructure;
        if (storedDataDirectory.equals(dataStructureDir) == false)
        {
            final DataStructureLoader dataStructureLoader =
                    new DataStructureLoader(storedDataDirectory.getParentFile());
            thisStructure =
                    (IDataStructureV1_1) dataStructureLoader.load(storedDataDirectory.getName(),
                            true);
        } else
        {
            thisStructure = dataStructure;
            if (thisStructure.isOpenOrCreated() == false)
            {
                thisStructure.open(Mode.READ_ONLY);
            }
        }
        return thisStructure;
    }

    //
    // AbstractStorageProcessor
    //

    public final File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDirectory)
    {
        checkDataSetInformation(dataSetInformation);
        assert rootDirectory != null : "Root directory can not be null.";
        assert incomingDataSetDirectory != null : "Incoming data set directory can not be null.";
        assert typeExtractor != null : "Unspecified IProcedureAndDataTypeExtractor implementation.";

        dataStructureDir = rootDirectory;
        dataStructureDir.mkdirs();
        Experiment experiment = dataSetInformation.tryToGetExperiment();
        if (experiment == null)
        {
            throw new UserFailureException("Experiment unknown for data set " + dataSetInformation);
        }
        dataStructure =
                createDataStructure(experiment, dataSetInformation, typeExtractor,
                        incomingDataSetDirectory, dataStructureDir);
        final IFormattedData formattedData = dataStructure.getFormattedData();
        if (formattedData instanceof IHCSImageFormattedData)
        {
            imageFormattedData = (IHCSImageFormattedData) formattedData;
            final int channels = imageFormattedData.getChannelCount();
            final Geometry plateGeometry = imageFormattedData.getPlateGeometry();
            final Geometry wellGeometry = imageFormattedData.getWellGeometry();
            imageCheckList = new HCSImageCheckList(channels, plateGeometry, wellGeometry);
        }
        if (needsImageFileExtractor())
        {
            imageFileRootDirectory = incomingDataSetDirectory;
            final HCSImageFileExtractionResult result =
                    imageFileExtractor.process(NodeFactory
                            .createDirectoryNode(incomingDataSetDirectory), dataSetInformation,
                            this);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("Extraction of %d files took %s.", result
                        .getTotalFiles(), DurationFormatUtils.formatDurationHMS(result
                        .getDuration())));
            }
            if (result.getInvalidFiles().size() > 0)
            {
                throw UserFailureException.fromTemplate(
                        "Following invalid files %s have been found.", CollectionUtils.abbreviate(
                                result.getInvalidFiles(), 10));
            }
            if (result.getTotalFiles() == 0)
            {
                throw UserFailureException.fromTemplate(
                        "No extractable files were found inside a dataset '%s'."
                                + " Have you changed your naming convention?",
                        incomingDataSetDirectory.getAbsolutePath());
            }
            dataStructure.setAnnotations(new HCSImageAnnotations(result.getChannels()));
            checkCompleteness(dataSetInformation, incomingDataSetDirectory.getName(), mailClient);
        } else
        {
            if (imageFormattedData != null && imageFormattedData.isIncomingSymbolicLink())
            {
                ILink symbolicLink = NodeFactory.createSymbolicLinkNode(incomingDataSetDirectory);
                dataStructure.getOriginalData().tryAddLink(symbolicLink.getName(),
                        symbolicLink.getReference());

            } else
            {
                dataStructure.getOriginalData().addFile(incomingDataSetDirectory, null, true);
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("File '%s' added to original data.",
                        incomingDataSetDirectory));
            }
        }
        dataStructure.close();
        return dataStructureDir;
    }

    public UnstoreDataAction rollback(File incomingDataSetDirectory, File storedDataDirectory,
            Throwable exception)
    {
        unstoreData(incomingDataSetDirectory, storedDataDirectory);
        return UnstoreDataAction.MOVE_TO_ERROR;
    }

    private final void unstoreData(final File incomingDataSetDirectory,
            final File storedDataDirectory)
    {
        checkParameters(incomingDataSetDirectory, storedDataDirectory);

        if (dataStructure == null)
        {
            // Nothing to do here.
            return;
        }
        final IDirectory originalData = getDataStructure(dataStructureDir).getOriginalData();
        final INode node = originalData.tryGetNode(incomingDataSetDirectory.getName());
        // If the 'incoming' data have been moved to 'original' directory. This only happens if
        // 'containsOriginalData' returns 'true'.
        if (node != null)
        {
            final File incomingDirectory = incomingDataSetDirectory.getParentFile();
            try
            {
                node.moveTo(incomingDirectory);
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(
                            "Node '%s' has moved to incoming directory '%s'.", node,
                            incomingDirectory.getAbsolutePath()));
                }
            } catch (final EnvironmentFailureException ex)
            {
                notificationLog.error(String.format(
                        "Could not move '%s' to incoming directory '%s'.", node, incomingDirectory
                                .getAbsolutePath()), ex);
                return;
            }
        }
        final IFileOperations fileOps = FileOperations.getMonitoredInstanceForCurrentThread();
        if (fileOps.exists(incomingDataSetDirectory))
        {
            if (fileOps.removeRecursivelyQueueing(storedDataDirectory) == false)
            {
                operationLog
                        .error("Cannot delete '" + storedDataDirectory.getAbsolutePath() + "'.");
            }
        } else
        {
            notificationLog.error(String.format("Incoming data set directory '%s' does not "
                    + "exist, keeping store directory '%s'.", incomingDataSetDirectory,
                    storedDataDirectory));
        }
    }

    public final File tryGetProprietaryData(final File storedDataDirectory)
    {
        assert storedDataDirectory != null : "Unspecified stored data directory.";
        if (dataStructure == null)
        {
            operationLog.error("No data structure defined.");
            return null;
        }
        if (imageFormattedData != null)
        {
            if (imageFormattedData.containsOriginalData() == false)
            {
                operationLog.warn("Original data are not available.");
                return null;
            }
        }
        final IDataStructureV1_1 thisStructure = getDataStructure(storedDataDirectory);
        final IDirectory originalData = thisStructure.getOriginalData();
        final Iterator<INode> iterator = originalData.iterator();
        if (iterator.hasNext() == false)
        {
            return null;
        }
        final INode node = iterator.next();
        final String path = getPathOf(node);
        final File originalDataFile = new File(path);
        if (originalDataFile.exists() == false)
        {
            operationLog.error("Original data set file '" + originalDataFile.getAbsolutePath()
                    + "' does not exist.");
            return null;
        }
        return originalDataFile;
    }

    public final StorageFormat getStorageFormat()
    {
        return StorageFormat.BDS_DIRECTORY;
    }

    //
    // IHCSImageFileAccepter
    //

    public final void accept(final int channel, final Location wellLocation,
            final Location tileLocation, final IFile imageFile)
    {
        assert imageFileRootDirectory != null : "Incoming data set directory has not been set.";
        final String imageRelativePath =
                FileUtilities
                        .getRelativeFile(imageFileRootDirectory, new File(imageFile.getPath()));
        assert imageRelativePath != null : "Image relative path should not be null.";
        final NodePath nodePath =
                imageFormattedData.addStandardNode(imageFileRootDirectory, imageRelativePath,
                        channel, wellLocation, tileLocation);
        imageCheckList.checkOff(channel, wellLocation, tileLocation);
        if (nodePath.getNode() instanceof ILink)
        {
            // We made a link in the 'standard' directory to the 'original' directory image file
            // name. The image file did not change during the operation.
            final Reference reference =
                    new Reference(nodePath.getPath(), imageFileRootDirectory.getName()
                            + Constants.PATH_SEPARATOR + imageRelativePath, ReferenceType.IDENTICAL);
            getDataStructure(dataStructureDir).addReference(reference);
        }
    }
}
