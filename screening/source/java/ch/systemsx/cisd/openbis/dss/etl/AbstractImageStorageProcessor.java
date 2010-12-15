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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
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
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.hdf5.Hdf5Container;
import ch.systemsx.cisd.etlserver.hdf5.HierarchicalStructureDuplicatorFileToHdf5;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Abstract superclass for storage processor which stores images in a special-purpose imaging
 * database besides putting it into the store. It has ability to compress the whole dataset as an
 * HDF5 container. It can also generate thumbnails for each image.
 * <p>
 * Accepts following properties:
 * <ul>
 * <li>generate-thumbnails - should the thumbnails be generated? It slows down the dataset
 * registration, but increases the performance when the user wants to see the image. Can be 'true'
 * or 'false', 'false' is the default value
 * <li>compress-thumbnails - should the thumbnails be compressed? Used if generate-thumbnails is
 * true, otherwise ignored
 * <li>thumbnail-max-width, thumbnail-max-height - thumbnails size in pixels
 * <li>[deprecated] channel-names - names of the channels in which images have been acquired
 * <li>channel-codes - codes of the channels in which images have been acquired
 * <li>channel-labels - labels of the channels in which images have been acquired
 * <li>well_geometry - format: [width]>x[height], e.g. 3x4. Specifies the grid into which a
 * microscope divided the well to acquire images.
 * <li>file-extractor - implementation of the {@link IImageFileExtractor} interface which maps
 * images to the location on the plate and particular channel
 * <li>data-source - specification of the imaging db
 * <li>extract-single-image-channels - optional comma separated list of color components. Available
 * values: RED, GREEN or BLUE. If specified then the channels are extracted from the color
 * components and override 'file-extractor' results.
 * </p>
 * 
 * @author Tomasz Pylak
 */
abstract class AbstractImageStorageProcessor extends AbstractStorageProcessor
{
    /**
     * Stores the references to the extracted images in the imaging database.
     * 
     * @param dao should not be commited or rollbacked, it's done outside of this method.
     */
    abstract protected void storeInDatabase(IImagingQueryDAO dao,
            DataSetInformation dataSetInformation, ImageFileExtractionResult extractedImages);

    /**
     * Additional imgae validation (e.g. are there all images that were expected?). Prints warnings
     * to the log, does not throw exceptions.
     */
    abstract protected void validateImages(DataSetInformation dataSetInformation,
            IMailClient mailClient, File incomingDataSetDirectory,
            ImageFileExtractionResult extractionResult);

    // --------------------------------------------

    /** The directory where <i>original</i> data could be found. */
    private static final String DIR_ORIGINAL = ScreeningConstants.ORIGINAL_DATA_DIR;

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PlateStorageProcessor.class);

    protected static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            PlateStorageProcessor.class);

    // tiles geometry, e.g. 3x4 if the well is divided into 12 tiles (3 rows, 4 columns)
    private static final String SPOT_GEOMETRY_PROPERTY = "well_geometry";

    private static final String GENERATE_THUMBNAILS_PROPERTY = "generate-thumbnails";

    private final static String COMPRESS_THUMBNAILS_PROPERTY = "compress-thumbnails";

    private final static String ORIGINAL_DATA_STORAGE_FORMAT_PROPERTY =
            "original-data-storage-format";

    private static final String THUMBNAIL_MAX_WIDTH_PROPERTY = "thumbnail-max-width";

    private static final int DEFAULT_THUMBNAIL_MAX_WIDTH = 200;

    private static final String THUMBNAIL_MAX_HEIGHT_PROPERTY = "thumbnail-max-height";

    private static final int DEFAULT_THUMBNAIL_MAX_HEIGHT = 120;

    protected static final String FILE_EXTRACTOR_PROPERTY = "file-extractor";

    // comma separated list of channel names, order matters
    @Deprecated
    public static final String CHANNEL_NAMES = "channel-names";

    // comma separated list of channel codes, order matters
    public static final String CHANNEL_CODES = "channel-codes";

    // comma separated list of channel labels, order matters
    public static final String CHANNEL_LABELS = "channel-labels";

    // how the original data should be stored
    private static enum OriginalDataStorageFormat
    {
        UNCHANGED, HDF5, HDF5_COMPRESSED;

        public boolean isHdf5()
        {
            return this == OriginalDataStorageFormat.HDF5
                    || this == OriginalDataStorageFormat.HDF5_COMPRESSED;
        }
    }

    // --- private --------

    private final DataSource dataSource;

    private final int thumbnailMaxWidth;

    private final int thumbnailMaxHeight;

    private final boolean generateThumbnails;

    private final boolean areThumbnailsCompressed;

    private final OriginalDataStorageFormat originalDataStorageFormat;

    // --- protected --------

    protected final IImageFileExtractor imageFileExtractor;

    protected final Geometry spotGeometry;

    protected final List<ChannelDescription> channelDescriptions;

    // --- internal state -------------

    private IImagingQueryDAO currentTransaction;

    // ---

    public AbstractImageStorageProcessor(final Properties properties)
    {
        this(getMandatorySpotGeometry(properties), extractChannelDescriptions(properties),
                tryCreateImageExtractor(properties), properties);
    }

    protected AbstractImageStorageProcessor(Geometry spotGeometry,
            List<ChannelDescription> channelDescriptions, IImageFileExtractor imageFileExtractor,
            Properties properties)
    {
        super(properties);
        this.spotGeometry = spotGeometry;
        this.channelDescriptions = channelDescriptions;
        this.imageFileExtractor = imageFileExtractor;
        this.thumbnailMaxWidth =
                PropertyUtils.getInt(properties, THUMBNAIL_MAX_WIDTH_PROPERTY,
                        DEFAULT_THUMBNAIL_MAX_WIDTH);
        this.thumbnailMaxHeight =
                PropertyUtils.getInt(properties, THUMBNAIL_MAX_HEIGHT_PROPERTY,
                        DEFAULT_THUMBNAIL_MAX_HEIGHT);
        this.generateThumbnails =
                PropertyUtils.getBoolean(properties, GENERATE_THUMBNAILS_PROPERTY, false);
        this.areThumbnailsCompressed =
                PropertyUtils.getBoolean(properties, COMPRESS_THUMBNAILS_PROPERTY, false);
        this.originalDataStorageFormat = getOriginalDataStorageFormat(properties);

        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        this.currentTransaction = null;
    }

    private static Geometry getMandatorySpotGeometry(Properties properties)
    {
        String spotGeometryText =
                PropertyUtils.getMandatoryProperty(properties, SPOT_GEOMETRY_PROPERTY);
        return Geometry.createFromString(spotGeometryText);
    }

    private static IImageFileExtractor tryCreateImageExtractor(final Properties properties)
    {
        String fileExtractorClass = PropertyUtils.getProperty(properties, FILE_EXTRACTOR_PROPERTY);
        if (fileExtractorClass != null)
        {
            return ClassUtils.create(IImageFileExtractor.class, fileExtractorClass, properties);
        } else
        {
            return null;
        }
    }

    private static OriginalDataStorageFormat getOriginalDataStorageFormat(
            final Properties properties)
    {
        String defaultValue = OriginalDataStorageFormat.UNCHANGED.name();
        String textValue =
                PropertyUtils.getProperty(properties, ORIGINAL_DATA_STORAGE_FORMAT_PROPERTY,
                        defaultValue);
        return OriginalDataStorageFormat.valueOf(textValue.toUpperCase());
    }

    private final static List<String> tryGetListOfLabels(Properties properties, String propertyKey)
    {
        String itemsList = PropertyUtils.getProperty(properties, propertyKey);
        if (itemsList == null)
        {
            return null;
        }
        String[] items = itemsList.split(",");
        for (int i = 0; i < items.length; i++)
        {
            items[i] = items[i].trim();
        }
        return Arrays.asList(items);
    }

    public final static List<ChannelDescription> extractChannelDescriptions(
            final Properties properties)
    {
        List<String> names = PropertyUtils.tryGetList(properties, CHANNEL_NAMES);
        List<String> codes = PropertyUtils.tryGetList(properties, CHANNEL_CODES);
        List<String> labels = tryGetListOfLabels(properties, CHANNEL_LABELS);
        if (names != null && (codes != null || labels != null))
        {
            throw new ConfigurationFailureException(String.format(
                    "Configure either '%s' or ('%s','%s') but not both.", CHANNEL_NAMES,
                    CHANNEL_CODES, CHANNEL_LABELS));
        }
        if (names != null)
        {
            List<ChannelDescription> descriptions = new ArrayList<ChannelDescription>();
            for (String name : names)
            {
                descriptions.add(new ChannelDescription(name));
            }
            return descriptions;
        }
        if (codes == null || labels == null)
        {
            throw new ConfigurationFailureException(String.format(
                    "Both '%s' and '%s' should be configured", CHANNEL_CODES, CHANNEL_LABELS));
        }
        if (codes.size() != labels.size())
        {
            throw new ConfigurationFailureException(String.format(
                    "Number of configured '%s' should be the same as number of '%s'.",
                    CHANNEL_CODES, CHANNEL_LABELS));
        }
        List<ChannelDescription> descriptions = new ArrayList<ChannelDescription>();
        for (int i = 0; i < codes.size(); i++)
        {
            descriptions.add(new ChannelDescription(codes.get(i), labels.get(i)));
        }
        return descriptions;
    }

    private IImagingQueryDAO createQuery()
    {
        return QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
    }

    // ---------------------------------

    public final File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDirectory)
    {
        assert rootDirectory != null : "Root directory can not be null.";
        assert incomingDataSetDirectory != null : "Incoming data set directory can not be null.";
        assert typeExtractor != null : "Unspecified IProcedureAndDataTypeExtractor implementation.";

        ImageFileExtractionResult extractionResult =
                extractImages(dataSetInformation, incomingDataSetDirectory);

        validateImages(dataSetInformation, mailClient, incomingDataSetDirectory, extractionResult);
        List<AcquiredSingleImage> plateImages = extractionResult.getImages();

        File imagesInStoreFolder = moveToStore(incomingDataSetDirectory, rootDirectory);

        // NOTE: plateImages will be changed by reference
        processImages(rootDirectory, plateImages, imagesInStoreFolder);

        storeInDatabase(dataSetInformation, extractionResult);
        return rootDirectory;
    }

    private void processImages(final File rootDirectory, List<AcquiredSingleImage> plateImages,
            File imagesInStoreFolder)
    {
        generateThumbnails(plateImages, rootDirectory, imagesInStoreFolder);
        String relativeImagesDirectory =
                packageImagesIfNecessary(rootDirectory, plateImages, imagesInStoreFolder);
        updateImagesRelativePath(relativeImagesDirectory, plateImages);
    }

    // returns the prefix which should be added before each image path to create a path relative to
    // the dataset folder
    private String packageImagesIfNecessary(final File rootDirectory,
            List<AcquiredSingleImage> plateImages, File imagesInStoreFolder)
    {
        if (originalDataStorageFormat.isHdf5())
        {
            File hdf5OriginalContainer = createHdf5OriginalContainer(rootDirectory);
            boolean isDataCompressed =
                    originalDataStorageFormat == OriginalDataStorageFormat.HDF5_COMPRESSED;
            saveInHdf5(imagesInStoreFolder, hdf5OriginalContainer, isDataCompressed);
            String hdf5ArchivePathPrefix =
                    hdf5OriginalContainer.getName() + ContentRepository.ARCHIVE_DELIMITER;
            return hdf5ArchivePathPrefix;
        } else
        {
            return getRelativeImagesDirectory(rootDirectory, imagesInStoreFolder) + "/";
        }
    }

    private static File createHdf5OriginalContainer(final File rootDirectory)
    {
        return new File(rootDirectory, Constants.HDF5_CONTAINER_ORIGINAL_FILE_NAME);
    }

    private void saveInHdf5(File sourceFolder, File hdf5DestinationFile, boolean compressFiles)
    {
        Hdf5Container container = new Hdf5Container(hdf5DestinationFile);
        container.runWriterClient(compressFiles,
                new HierarchicalStructureDuplicatorFileToHdf5.DuplicatorWriterClient(sourceFolder));
    }

    private File moveToStore(File incomingDataSetDirectory, File rootDirectory)
    {
        File originalFolder = getOriginalFolder(rootDirectory);
        originalFolder.mkdirs();
        if (originalFolder.exists() == false)
        {
            throw new UserFailureException("Cannot create a directory: " + originalFolder);
        }
        return moveFileToDirectory(incomingDataSetDirectory, originalFolder);

    }

    // modifies plateImages by setting the path to thumbnails
    private void generateThumbnails(final List<AcquiredSingleImage> plateImages,
            final File rootDirectory, final File imagesInStoreFolder)
    {
        final File thumbnailsFile =
                new File(rootDirectory, Constants.HDF5_CONTAINER_THUMBNAILS_FILE_NAME);
        final String relativeThumbnailFilePath =
                getRelativeImagesDirectory(rootDirectory, thumbnailsFile);

        if (generateThumbnails)
        {
            Hdf5Container container = new Hdf5Container(thumbnailsFile);
            container.runWriterClient(areThumbnailsCompressed, new Hdf5ThumbnailGenerator(
                    plateImages, imagesInStoreFolder, thumbnailMaxWidth, thumbnailMaxHeight,
                    relativeThumbnailFilePath, operationLog));
        }
    }

    private void updateImagesRelativePath(String folderPathPrefix,
            final List<AcquiredSingleImage> plateImages)
    {
        for (AcquiredSingleImage plateImage : plateImages)
        {
            RelativeImageReference imageReference = plateImage.getImageReference();
            imageReference.setRelativeImageFolder(folderPathPrefix);
        }
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

    private ImageFileExtractionResult extractImages(final DataSetInformation dataSetInformation,
            final File incomingDataSetDirectory)
    {
        long extractionStart = System.currentTimeMillis();
        final ImageFileExtractionResult result =
                getImageFileExtractor(incomingDataSetDirectory).extract(incomingDataSetDirectory,
                        dataSetInformation);

        if (operationLog.isInfoEnabled())
        {
            long duration = System.currentTimeMillis() - extractionStart;
            operationLog.info(String.format("Extraction of %d files took %s.", result.getImages()
                    .size(), DurationFormatUtils.formatDurationHMS(duration)));
        }
        if (result.getImages().size() == 0)
        {
            throw new UserFailureException("No images found in the incoming diretcory: "
                    + incomingDataSetDirectory);
        }
        return result;
    }

    protected IImageFileExtractor getImageFileExtractor(File incomingDataSetDirectory)
    {
        assert imageFileExtractor != null : "imageFileExtractor is null";
        return imageFileExtractor;
    }

    @Override
    public void commit(File incomingDataSetDirectory, File storedDataDirectory)
    {
        if (originalDataStorageFormat.isHdf5())
        {
            commitHdf5StorageFormatChanges(storedDataDirectory);
        }
        commitDatabaseChanges();
    }

    private static void commitHdf5StorageFormatChanges(File storedDataDirectory)
    {
        File originalFolder = getOriginalFolder(storedDataDirectory);
        File hdf5OriginalContainer = createHdf5OriginalContainer(storedDataDirectory);
        if (hdf5OriginalContainer.exists())
        {
            final IFileOperations fileOps = FileOperations.getMonitoredInstanceForCurrentThread();
            if (fileOps.removeRecursivelyQueueing(originalFolder) == false)
            {
                operationLog.error("Cannot delete '" + originalFolder.getAbsolutePath() + "'.");
            }
        } else
        {
            notificationLog.error(String.format("HDF5 container with original data '%s' does not "
                    + "exist, keeping the original directory '%s'.", hdf5OriginalContainer,
                    originalFolder));
        }
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

    private void storeInDatabase(DataSetInformation dataSetInformation,
            ImageFileExtractionResult extractionResult)
    {
        if (currentTransaction != null)
        {
            throw new IllegalStateException("previous transaction has not been commited!");
        }
        currentTransaction = createQuery();

        storeInDatabase(currentTransaction, dataSetInformation, extractionResult);
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

    protected static List<String> extractChannelCodes(final List<ChannelDescription> descriptions)
    {
        List<String> channelCodes = new ArrayList<String>();
        for (ChannelDescription cd : descriptions)
        {
            channelCodes.add(cd.getCode());
        }
        return channelCodes;
    }

    protected static List<String> extractChannelLabels(final List<ChannelDescription> descriptions)
    {
        List<String> channelLabels = new ArrayList<String>();
        for (ChannelDescription cd : descriptions)
        {
            channelLabels.add(cd.getLabel());
        }
        return channelLabels;
    }

    protected static String getChannelCodeOrLabel(final List<String> channelCodes, int channelId)
    {
        if (channelId > channelCodes.size())
        {
            throw UserFailureException.fromTemplate(
                    "Too large channel number %d, configured channels: %s.", channelId,
                    CollectionUtils.abbreviate(channelCodes, -1));
        }
        return channelCodes.get(channelId - 1);
    }

    protected static boolean hasImageSeries(List<AcquiredSingleImage> images)
    {
        for (AcquiredSingleImage image : images)
        {
            if (image.tryGetTimePoint() != null || image.tryGetDepth() != null
                    || image.tryGetSeriesNumber() != null)
            {
                return true;
            }
        }
        return false;
    }

}
