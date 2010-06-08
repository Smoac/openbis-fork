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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.hcs.Channel;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessor;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.PlateDimension;
import ch.systemsx.cisd.etlserver.PlateDimensionParser;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageCheckList.FullLocation;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Storage processor which stores HCS images in a special-purpose database.
 * 
 * @author Tomasz Pylak
 */
public final class PlateStorageProcessor extends AbstractStorageProcessor
{
    /** The directory where <i>original</i> data could be found. */
    private static final String DIR_ORIGINAL = "original";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PlateStorageProcessor.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PlateStorageProcessor.class);

    private static final String SPOT_GEOMETRY_PROPERTY = "well_geometry";

    private static final String FILE_EXTRACTOR_PROPERTY = "file-extractor";

    private static final String DEPRECATED_FILE_EXTRACTOR_PROPERTY = "deprecated-file-extractor";

    // comma separated list of channel names, order matters
    private static final String CHANNEL_NAMES = "channel-names";

    private static final String CHANNEL_SEPARATOR = ",";

    // -----------

    private final DataSource dataSource;

    private final Geometry spotGeometry;

    private final String[] channelNames;

    // one of the extractors is always null and one not null
    private final IHCSImageFileExtractor imageFileExtractor;

    private final ch.systemsx.cisd.etlserver.IHCSImageFileExtractor deprecatedImageFileExtractor;

    private IImagingUploadDAO currentTransaction;

    public PlateStorageProcessor(final Properties properties)
    {
        super(properties);
        String spotGeometryText = getMandatoryProperty(SPOT_GEOMETRY_PROPERTY);
        this.spotGeometry = Geometry.createFromString(spotGeometryText);

        this.channelNames = extractChannelNames(properties);

        String fileExtractorClass = PropertyUtils.getProperty(properties, FILE_EXTRACTOR_PROPERTY);
        if (fileExtractorClass != null)
        {
            this.imageFileExtractor =
                    ClassUtils.create(IHCSImageFileExtractor.class, fileExtractorClass, properties);
            this.deprecatedImageFileExtractor = null;
        } else
        {
            this.imageFileExtractor = null;
            fileExtractorClass = getMandatoryProperty(DEPRECATED_FILE_EXTRACTOR_PROPERTY);
            this.deprecatedImageFileExtractor =
                    ClassUtils.create(ch.systemsx.cisd.etlserver.IHCSImageFileExtractor.class,
                            fileExtractorClass, properties);
        }
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        this.currentTransaction = null;
    }

    public static String[] extractChannelNames(Properties properties)
    {
        String names = PropertyUtils.getMandatoryProperty(properties, CHANNEL_NAMES);
        String[] channelNames = names.split(CHANNEL_SEPARATOR);
        for (int i = 0; i < channelNames.length; i++)
        {
            channelNames[i] = channelNames[i].trim().toLowerCase();
        }
        return channelNames;
    }

    private IImagingUploadDAO createQuery()
    {
        return QueryTool.getQuery(dataSource, IImagingUploadDAO.class);
    }

    private final static void checkDataSetInformation(final DataSetInformation dataSetInformation)
    {
        assert dataSetInformation != null : "Unspecified data set information";
        assert dataSetInformation.getSampleIdentifier() != null : "Unspecified sample identifier";
        final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentIdentifier =
                dataSetInformation.getExperimentIdentifier();
        assert experimentIdentifier != null : "Unspecified experiment identifier";
        assert dataSetInformation.tryToGetExperiment() != null : "experiment not set";
        checkExperimentIdentifier(experimentIdentifier);
    }

    private final static void checkExperimentIdentifier(
            final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier.getSpaceCode() != null : "Space code is null";
        assert experimentIdentifier.getExperimentCode() != null : "Experiment code is null";
        assert experimentIdentifier.getProjectCode() != null : "Project code is null";
    }

    // ---------------------------------

    private ScreeningContainerDatasetInfo createScreeningDatasetInfo(Experiment experiment,
            DataSetInformation dataSetInformation, String relativeImagesDirectory)
    {
        ScreeningContainerDatasetInfo info = new ScreeningContainerDatasetInfo();
        info.setExperimentPermId(experiment.getPermId());
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                dataSetInformation.tryToGetSample();
        assert sample != null : "no sample connected to a dataset";
        info.setContainerPermId(sample.getPermId());
        info.setDatasetPermId(dataSetInformation.getDataSetCode());

        PlateDimension plateGeometry = getPlateGeometry(dataSetInformation);
        int plateRows = plateGeometry.getRowsNum();
        int plateCols = plateGeometry.getColsNum();
        info.setContainerRows(plateRows);
        info.setContainerColumns(plateCols);

        info.setTileRows(spotGeometry.getRows());
        info.setTileColumns(spotGeometry.getColumns());

        info.setRelativeImagesDirectory(relativeImagesDirectory);

        return info;
    }

    private PlateDimension getPlateGeometry(final DataSetInformation dataSetInformation)
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
        return plateDimension;
    }

    // ---------------------------------

    // Although this check should be performed in the BDS library when closing is performed, we set
    // the complete flag here as we want to inform the registrator about the incompleteness.
    private void checkCompleteness(HCSImageCheckList imageCheckList,
            final DataSetInformation dataSetInformation, final String dataSetFileName,
            final IMailClient mailClientOrNull)
    {
        final List<FullLocation> fullLocations = imageCheckList.getCheckedOnFullLocations();
        final boolean complete = fullLocations.size() == 0;
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
                Experiment experiment = dataSetInformation.tryToGetExperiment();
                assert experiment != null : "dataset not connected to an experiment: "
                        + dataSetInformation;
                final String email = experiment.getRegistrator().getEmail();
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
                            + experiment.getRegistrator());
                }
            }
        }
    }

    public final File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDirectory)
    {
        checkDataSetInformation(dataSetInformation);
        assert rootDirectory != null : "Root directory can not be null.";
        assert incomingDataSetDirectory != null : "Incoming data set directory can not be null.";
        assert typeExtractor != null : "Unspecified IProcedureAndDataTypeExtractor implementation.";

        File originalFolder = getOriginalFolder(rootDirectory);
        originalFolder.mkdirs();
        if (originalFolder.exists() == false)
        {
            throw new UserFailureException("Cannot create a directory: " + originalFolder);
        }

        Experiment experiment = dataSetInformation.tryToGetExperiment();
        if (experiment == null)
        {
            throw new UserFailureException("Experiment unknown for data set " + dataSetInformation);
        }
        HCSImageFileExtractionResult extractionResult =
                extractImages(dataSetInformation, incomingDataSetDirectory);

        validateImages(dataSetInformation, mailClient, incomingDataSetDirectory, extractionResult);

        File imagesInStoreFolder = moveFileToDirectory(incomingDataSetDirectory, originalFolder);
        String relativeImagesDirectory =
                getRelativeImagesDirectory(rootDirectory, imagesInStoreFolder);

        storeInDatabase(experiment, dataSetInformation, extractionResult, relativeImagesDirectory);
        return rootDirectory;
    }

    private String getRelativeImagesDirectory(File rootDirectory, File imagesInStoreFolder)
    {
        String root = rootDirectory.getAbsolutePath();
        String imgDir = imagesInStoreFolder.getAbsolutePath();
        if (imgDir.startsWith(root) == false)
        {
            throw UserFailureException.fromTemplate(
                    "Directory %s should be a subdirectory of directory %s.", imgDir, root);
        }
        return imgDir.substring(root.length());
    }

    private void validateImages(final DataSetInformation dataSetInformation,
            final IMailClient mailClient, final File incomingDataSetDirectory,
            HCSImageFileExtractionResult extractionResult)
    {
        HCSImageCheckList imageCheckList = createImageCheckList(dataSetInformation);
        checkImagesForDuplicates(extractionResult, imageCheckList);
        if (extractionResult.getInvalidFiles().size() > 0)
        {
            throw UserFailureException.fromTemplate("Following invalid files %s have been found.",
                    CollectionUtils.abbreviate(extractionResult.getInvalidFiles(), 10));
        }
        if (extractionResult.getImages().size() == 0)
        {
            throw UserFailureException.fromTemplate(
                    "No extractable files were found inside a dataset '%s'."
                            + " Have you changed your naming convention?", incomingDataSetDirectory
                            .getAbsolutePath());
        }
        checkCompleteness(imageCheckList, dataSetInformation, incomingDataSetDirectory.getName(),
                mailClient);
    }

    private static void checkImagesForDuplicates(HCSImageFileExtractionResult extractionResult,
            HCSImageCheckList imageCheckList)
    {
        List<AcquiredPlateImage> images = extractionResult.getImages();
        for (AcquiredPlateImage image : images)
        {
            imageCheckList.checkOff(image);
        }
    }

    private HCSImageCheckList createImageCheckList(DataSetInformation dataSetInformation)
    {
        PlateDimension plateGeometry = getPlateGeometry(dataSetInformation);
        return new HCSImageCheckList(channelNames, plateGeometry, spotGeometry);
    }

    private HCSImageFileExtractionResult extractImages(final DataSetInformation dataSetInformation,
            final File incomingDataSetDirectory)
    {
        long extractionStart = System.currentTimeMillis();
        IHCSImageFileExtractor extractor = imageFileExtractor;
        if (extractor == null)
        {
            extractor = adapt(deprecatedImageFileExtractor, incomingDataSetDirectory, channelNames);
        }
        final HCSImageFileExtractionResult result =
                extractor.extract(incomingDataSetDirectory, dataSetInformation);
        if (operationLog.isInfoEnabled())
        {
            long duration = System.currentTimeMillis() - extractionStart;
            operationLog.info(String.format("Extraction of %d files took %s.", result.getImages()
                    .size(), DurationFormatUtils.formatDurationHMS(duration)));
        }
        return result;
    }

    @Override
    public void commit()
    {
        commitDatabaseChanges();
    }

    private void commitDatabaseChanges()
    {
        if (currentTransaction == null)
        {
            throw new IllegalStateException("there is no transaction to commit");
        }
        try
        {
            currentTransaction.close(true);
        } finally
        {
            currentTransaction = null;
        }
    }

    public UnstoreDataAction rollback(File incomingDataSetDirectory, File storedDataDirectory,
            Throwable exception)
    {
        unstoreFiles(incomingDataSetDirectory, storedDataDirectory);
        rollbackDatabaseChanges();
        return UnstoreDataAction.MOVE_TO_ERROR;
    }

    private final void unstoreFiles(final File incomingDataSetDirectory,
            final File storedDataDirectory)
    {
        checkParameters(incomingDataSetDirectory, storedDataDirectory);

        final File originalDataFile = tryGetProprietaryData(storedDataDirectory);
        if (originalDataFile == null)
        {
            // nothing has been stored in the file system yet,
            // e.g. because images could not be validated
            return;
        }
        // Move the data from the 'original' directory back to the 'incoming' directory.
        final File incomingDirectory = incomingDataSetDirectory.getParentFile();
        try
        {
            moveFileToDirectory(originalDataFile, incomingDirectory);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(
                        "Directory '%s' has moved to incoming directory '%s'.", originalDataFile,
                        incomingDirectory.getAbsolutePath()));
            }
        } catch (final EnvironmentFailureException ex)
        {
            notificationLog.error(String.format("Could not move '%s' to incoming directory '%s'.",
                    originalDataFile, incomingDirectory.getAbsolutePath()), ex);
            return;
        }
        // Remove the dataset directory from the store
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

    private void storeInDatabase(Experiment experiment, DataSetInformation dataSetInformation,
            HCSImageFileExtractionResult extractionResult, String relativeImagesDirectory)
    {
        ScreeningContainerDatasetInfo info =
                createScreeningDatasetInfo(experiment, dataSetInformation, relativeImagesDirectory);

        if (currentTransaction != null)
        {
            throw new IllegalStateException("previous transaction has not been commited!");
        }
        currentTransaction = createQuery();

        HCSDatasetUploader.upload(currentTransaction, info, extractionResult.getImages(),
                extractionResult.getChannels());
    }

    private void rollbackDatabaseChanges()
    {
        if (currentTransaction == null)
        {
            return; // storing in the imaging db has not started
        }
        try
        {
            currentTransaction.rollback();
        } finally
        {
            currentTransaction.close();
            currentTransaction = null;
        }
    }

    /**
     * Moves source file/folder to the destination directory. If the source is a symbolic links to
     * the original data then we do not move any data. Instead we create symbolic link to original
     * data which points to the same place as the source link.
     * 
     * @return
     */
    private static File moveFileToDirectory(final File source, final File directory)
            throws EnvironmentFailureException
    {
        assert source != null;
        IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        assert directory != null && fileOperations.isDirectory(directory);
        final String newName = source.getName();
        final File destination = new File(directory, newName);
        if (fileOperations.exists(destination) == false)
        {
            if (FileUtilities.isSymbolicLink(source))
            {
                moveSymbolicLink(source, destination);
            } else
            {
                final boolean successful = fileOperations.rename(source, destination);
                if (successful == false)
                {
                    throw EnvironmentFailureException.fromTemplate(
                            "Can not move file '%s' to directory '%s'.", source.getAbsolutePath(),
                            directory.getAbsolutePath());
                }
            }
            return destination;
        } else
        {
            throw EnvironmentFailureException
                    .fromTemplate(
                            "Can not move file '%s' to directory '%s' because the destination directory already exists.",
                            source.getAbsolutePath(), directory.getAbsolutePath());
        }
    }

    // WORKAROUND there were cases where it was impossible to move an absolute symbolic link
    // It happened on a CIFS share. So instead of moving the link we create a file which points to
    // the same place and delete the link.
    private static void moveSymbolicLink(File source, File destination)
    {
        File referencedSource;
        try
        {
            referencedSource = source.getCanonicalFile();
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("cannot get the canonical path of " + source);
        }
        boolean ok = SoftLinkMaker.createSymbolicLink(referencedSource, destination);
        if (ok == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Can not create symbolic link to '%s' in '%s'.", referencedSource.getPath(),
                    destination.getPath());
        }
        ok = source.delete();
        if (ok == false)
        {
            throw EnvironmentFailureException.fromTemplate("Can not delete symbolic link '%s'.",
                    source.getPath());
        }
    }

    public final File tryGetProprietaryData(final File storedDataDirectory)
    {
        assert storedDataDirectory != null : "Unspecified stored data directory.";

        File originalFolder = getOriginalFolder(storedDataDirectory);
        File[] content = originalFolder.listFiles();
        if (content == null || content.length == 0)
        {
            return null;
        }
        if (content.length > 1)
        {
            operationLog.error("There should be exactly one original folder inside '"
                    + originalFolder + "', but " + originalFolder.length() + " has been found.");
            return null;
        }
        File originalDataFile = content[0];
        if (originalDataFile.exists() == false)
        {
            operationLog.error("Original data set file '" + originalDataFile.getAbsolutePath()
                    + "' does not exist.");
            return null;
        }
        return originalDataFile;
    }

    private static File getOriginalFolder(File storedDataDirectory)
    {
        return new File(storedDataDirectory, DIR_ORIGINAL);
    }

    public final StorageFormat getStorageFormat()
    {
        return StorageFormat.PROPRIETARY;
    }

    // adapts old-style image extractor to the new one which is stateless
    private static IHCSImageFileExtractor adapt(
            final ch.systemsx.cisd.etlserver.IHCSImageFileExtractor extractor,
            final File imageFileRootDirectory, final String[] channelNames)
    {
        return new IHCSImageFileExtractor()
            {
                public HCSImageFileExtractionResult extract(File incomingDataSetDirectory,
                        DataSetInformation dataSetInformation)
                {
                    HCSImageFileAccepter accepter =
                            new HCSImageFileAccepter(imageFileRootDirectory, channelNames);
                    ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult originalResult =
                            extractor.process(NodeFactory
                                    .createDirectoryNode(incomingDataSetDirectory),
                                    dataSetInformation, accepter);
                    Set<HCSImageFileExtractionResult.Channel> channels =
                            convert(originalResult.getChannels());
                    return new HCSImageFileExtractionResult(accepter.getImages(),
                            asRelativePaths(originalResult.getInvalidFiles()), channels);
                }

                private Set<HCSImageFileExtractionResult.Channel> convert(Set<Channel> channels)
                {
                    Set<HCSImageFileExtractionResult.Channel> result =
                            new HashSet<HCSImageFileExtractionResult.Channel>();
                    for (Channel channel : channels)
                    {
                        String name = getChannelName(channelNames, channel.getCounter());
                        result.add(new HCSImageFileExtractionResult.Channel(name, null, channel
                                .getWavelength()));
                    }
                    return result;
                }

                private List<File> asRelativePaths(List<IFile> files)
                {
                    List<File> result = new ArrayList<File>();
                    for (IFile file : files)
                    {
                        result.add(new File(file.getPath()));
                    }
                    return result;
                }
            };
    }

    private static String getChannelName(final String[] channelNames, int channelId)
    {
        if (channelId > channelNames.length)
        {
            throw UserFailureException.fromTemplate(
                    "Too large channel number %d, configured channels: %s.", channelId,
                    CollectionUtils.abbreviate(channelNames, -1));
        }
        return channelNames[channelId - 1];
    }

    private static final class HCSImageFileAccepter implements IHCSImageFileAccepter
    {
        private final List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();

        private final File imageFileRootDirectory;

        private final String[] channelNames;

        public HCSImageFileAccepter(File imageFileRootDirectory, String[] channelNames)
        {
            this.imageFileRootDirectory = imageFileRootDirectory;
            this.channelNames = channelNames;
        }

        public final void accept(final int channel, final Location wellLocation,
                final Location tileLocation, final IFile imageFile)
        {
            final String imageRelativePath =
                    FileUtilities.getRelativeFile(imageFileRootDirectory, new File(imageFile
                            .getPath()));
            assert imageRelativePath != null : "Image relative path should not be null.";
            String channelName = getChannelName(channelNames, channel);
            AcquiredPlateImage imageDesc =
                    new AcquiredPlateImage(wellLocation, tileLocation, channelName, null, null,
                            new RelativeImagePath(imageRelativePath));
            images.add(imageDesc);
        }

        public List<AcquiredPlateImage> getImages()
        {
            return images;
        }
    }
}
