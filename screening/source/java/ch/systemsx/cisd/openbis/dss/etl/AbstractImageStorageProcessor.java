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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
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
import ch.systemsx.cisd.etlserver.AbstractStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.DispatcherStorageProcessor.IDispatchableStorageProcessor;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.etlserver.hdf5.Hdf5Container;
import ch.systemsx.cisd.etlserver.hdf5.HierarchicalStructureDuplicatorFileToHdf5;
import ch.systemsx.cisd.etlserver.utils.Unzipper;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageSeriesPoint;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ChannelColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageStorageConfiguraton;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.OriginalDataStorageFormat;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

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
 * <p>
 * Subclasses of this storage processor can be used in the context of
 * {@link IDispatchableStorageProcessor} only if the given {@link DataSetInformation} can be casted
 * to {@link ImageDataSetInformation}. This requires using special {@link IDataSetInfoExtractor}
 * extension or {@link JythonPlateDataSetHandler}.
 * </p>
 * 
 * @author Tomasz Pylak
 */
abstract class AbstractImageStorageProcessor extends AbstractStorageProcessor
        implements IDispatchableStorageProcessor, IStorageProcessorTransactional
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

    protected static final String FILE_EXTRACTOR_PROPERTY = "file-extractor";

    // --- storage configuration properties

    private final static String ORIGINAL_DATA_STORAGE_FORMAT_PROPERTY =
            "original-data-storage-format";

    private static final String GENERATE_THUMBNAILS_PROPERTY = "generate-thumbnails";

    private final static String COMPRESS_THUMBNAILS_PROPERTY = "compress-thumbnails";

    private static final String THUMBNAIL_MAX_WIDTH_PROPERTY = "thumbnail-max-width";

    private static final String THUMBNAIL_MAX_HEIGHT_PROPERTY = "thumbnail-max-height";

    // ---

    private final DataSource dataSource;

    /**
     * Default configuration for all datasets, can be changed by {@link ImageDataSetInformation}.
     */
    private final ImageStorageConfiguraton globalImageStorageConfiguraton;

    // --- protected --------

    protected final IImageFileExtractor imageFileExtractorOrNull;

    // ---

    public AbstractImageStorageProcessor(final Properties properties)
    {
        this(tryCreateImageExtractor(properties), properties);
    }

    protected AbstractImageStorageProcessor(IImageFileExtractor imageFileExtractorOrNull,
            Properties properties)
    {
        super(properties);
        this.imageFileExtractorOrNull = imageFileExtractorOrNull;
        this.globalImageStorageConfiguraton = getGlobalImageStorageConfiguraton(properties);

        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
    }

    // --- ImageStorageConfiguraton ---

    private static ImageStorageConfiguraton getGlobalImageStorageConfiguraton(Properties properties)
    {
        ImageStorageConfiguraton storageFormatParameters = new ImageStorageConfiguraton();
        storageFormatParameters
                .setThumbnailsStorageFormat(tryCreateThumbnailsStorageFormat(properties));
        storageFormatParameters
                .setOriginalDataStorageFormat(getOriginalDataStorageFormat(properties));
        return storageFormatParameters;
    }

    private static ThumbnailsStorageFormat tryCreateThumbnailsStorageFormat(Properties properties)
    {
        boolean generateThumbnails =
                PropertyUtils.getBoolean(properties, GENERATE_THUMBNAILS_PROPERTY, false);
        if (generateThumbnails == false)
        {
            return null;
        }
        ThumbnailsStorageFormat thumbnailsStorageFormat = new ThumbnailsStorageFormat();
        int thumbnailMaxWidth =
                PropertyUtils.getInt(properties, THUMBNAIL_MAX_WIDTH_PROPERTY,
                        ThumbnailsStorageFormat.DEFAULT_THUMBNAIL_MAX_SIZE);
        int thumbnailMaxHeight =
                PropertyUtils.getInt(properties, THUMBNAIL_MAX_HEIGHT_PROPERTY,
                        ThumbnailsStorageFormat.DEFAULT_THUMBNAIL_MAX_SIZE);
        boolean areThumbnailsCompressed =
                PropertyUtils.getBoolean(properties, COMPRESS_THUMBNAILS_PROPERTY,
                        ThumbnailsStorageFormat.DEFAULT_COMPRESS_THUMBNAILS);

        thumbnailsStorageFormat.setMaxWidth(thumbnailMaxWidth);
        thumbnailsStorageFormat.setMaxHeight(thumbnailMaxHeight);
        thumbnailsStorageFormat.setStoreCompressed(areThumbnailsCompressed);
        return thumbnailsStorageFormat;
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

    // ----

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

    private IImagingQueryDAO createQuery()
    {
        return QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
    }

    // ---------------------------------
    private class AbstractImageStorageProcessorTransaction extends
            AbstractStorageProcessorTransaction
    {
        private IImagingQueryDAO dbTransaction;
        // used when HDF5 is used to store original data
        private boolean shouldDeleteOriginalDataOnCommit;


        @Override
        public void storeData(final DataSetInformation dataSetInformation,
                final ITypeExtractor typeExtractor, final IMailClient mailClient,
                final File incomingDataDirectory, final File rootDir)
        {
            assert rootDir != null : "Root directory can not be null.";
            assert incomingDataDirectory != null : "Incoming data set directory can not be null.";
            assert typeExtractor != null : "Unspecified IProcedureAndDataTypeExtractor implementation.";

            File unzipedFolder = tryUnzipToFolder(incomingDataDirectory);
            if (unzipedFolder != null)
            {
                storeData(dataSetInformation, typeExtractor, mailClient, unzipedFolder, rootDir);
            } else
            {
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataDirectory, rootDir);
            }

        }

        @Override
        public final File doStoreData(final DataSetInformation dataSetInformation,
                final ITypeExtractor typeExtractor, final IMailClient mailClient,
                final File incomingDataDirectory, final File rootDir)
        {

            ImageFileExtractionWithConfig extractionResultWithConfig =
                    extractImages(dataSetInformation, incomingDataSetDirectory);
            ImageFileExtractionResult extractionResult =
                    extractionResultWithConfig.getExtractionResult();

            validateImages(dataSetInformation, mailClient, incomingDataSetDirectory,
                    extractionResult);
            List<AcquiredSingleImage> plateImages = extractionResult.getImages();
            ImageStorageConfiguraton imageStorageConfiguraton =
                    extractionResultWithConfig.getImageStorageConfiguraton();

            File imagesInStoreFolder = moveToStore(incomingDataSetDirectory, rootDir);

            // NOTE: plateImages will be changed by reference
            processImages(rootDir, plateImages, imagesInStoreFolder, imageStorageConfiguraton);

            shouldDeleteOriginalDataOnCommit =
                    imageStorageConfiguraton.getOriginalDataStorageFormat().isHdf5();

            IImagingQueryDAO transaction = createQuery();
            storeInDatabase(transaction, dataSetInformation, extractionResult);

            return rootDir;
        }

        @Override
        protected void doCommit()
        {
            if (shouldDeleteOriginalDataOnCommit)
            {
                commitHdf5StorageFormatChanges(storedDataDirectory);
            }

            // commit the database transaction
            dbTransaction.close(true);
        }

        @Override
        protected UnstoreDataAction doRollback(Throwable exception)
        {
            unstoreFiles(incomingDataSetDirectory, storedDataDirectory);
            rollbackDatabaseChanges();
            return UnstoreDataAction.MOVE_TO_ERROR;
        }

        private void rollbackDatabaseChanges()
        {
            try
            {
                dbTransaction.rollback();
            } finally
            {
                dbTransaction.close();
            }
        }
    }

    public final IStorageProcessorTransaction createTransaction()
    {
        return new AbstractImageStorageProcessorTransaction();
    }

    private final class ImageFileExtractionWithConfig
    {
        private final ImageFileExtractionResult extractionResult;

        private final ImageStorageConfiguraton imageStorageConfiguraton;

        public ImageFileExtractionWithConfig(ImageFileExtractionResult extractionResult,
                ImageStorageConfiguraton imageStorageConfiguraton)
        {
            assert extractionResult != null : "extractionResult is null";
            assert imageStorageConfiguraton != null : "imageStorageConfiguraton is null";

            this.extractionResult = extractionResult;
            this.imageStorageConfiguraton = imageStorageConfiguraton;
        }

        public ImageFileExtractionResult getExtractionResult()
        {
            return extractionResult;
        }

        public ImageStorageConfiguraton getImageStorageConfiguraton()
        {
            return imageStorageConfiguraton;
        }
    }

    private File tryUnzipToFolder(File incomingDataSetDirectory)
    {
        if (isZipFile(incomingDataSetDirectory) == false)
        {
            return null;
        }
        String outputDirName = FilenameUtils.getBaseName(incomingDataSetDirectory.getName());
        File output = new File(incomingDataSetDirectory.getParentFile(), outputDirName);
        Status status = Unzipper.unzip(incomingDataSetDirectory, output, true);
        if (status.isError())
        {
            throw EnvironmentFailureException.fromTemplate("Cannot unzip '%s': %s",
                    incomingDataSetDirectory.getName(), status.tryGetErrorMessage());
        }
        return output;
    }

    private static void processImages(final File rootDirectory,
            List<AcquiredSingleImage> plateImages, File imagesInStoreFolder,
            ImageStorageConfiguraton imageStorageConfiguraton)
    {
        generateThumbnails(plateImages, rootDirectory, imagesInStoreFolder,
                imageStorageConfiguraton);
        String relativeImagesDirectory =
                packageImagesIfNecessary(rootDirectory, plateImages, imagesInStoreFolder,
                        imageStorageConfiguraton);
        updateImagesRelativePath(relativeImagesDirectory, plateImages);
    }

    // returns the prefix which should be added before each image path to create a path relative to
    // the dataset folder
    private static String packageImagesIfNecessary(final File rootDirectory,
            List<AcquiredSingleImage> plateImages, File imagesInStoreFolder,
            ImageStorageConfiguraton imageStorageConfiguraton)
    {
        OriginalDataStorageFormat originalDataStorageFormat =
                imageStorageConfiguraton.getOriginalDataStorageFormat();
        if (originalDataStorageFormat.isHdf5())
        {
            File hdf5OriginalContainer = getHdf5OriginalContainer(rootDirectory);
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

    private static File getHdf5OriginalContainer(final File rootDirectory)
    {
        return new File(rootDirectory, Constants.HDF5_CONTAINER_ORIGINAL_FILE_NAME);
    }

    private static void saveInHdf5(File sourceFolder, File hdf5DestinationFile,
            boolean compressFiles)
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
    private static void generateThumbnails(final List<AcquiredSingleImage> plateImages,
            final File rootDirectory, final File imagesInStoreFolder,
            ImageStorageConfiguraton imageStorageConfiguraton)
    {
        final File thumbnailsFile =
                new File(rootDirectory, Constants.HDF5_CONTAINER_THUMBNAILS_FILE_NAME);
        final String relativeThumbnailFilePath =
                getRelativeImagesDirectory(rootDirectory, thumbnailsFile);

        ThumbnailsStorageFormat thumbnailsStorageFormatOrNull =
                imageStorageConfiguraton.getThumbnailsStorageFormat();
        if (thumbnailsStorageFormatOrNull != null)
        {
            Hdf5Container container = new Hdf5Container(thumbnailsFile);
            container
                    .runWriterClient(thumbnailsStorageFormatOrNull.isStoreCompressed(),
                            new Hdf5ThumbnailGenerator(plateImages, imagesInStoreFolder,
                                    thumbnailsStorageFormatOrNull, relativeThumbnailFilePath,
                                    operationLog));
        }
    }

    private static void updateImagesRelativePath(String folderPathPrefix,
            final List<AcquiredSingleImage> plateImages)
    {
        for (AcquiredSingleImage plateImage : plateImages)
        {
            RelativeImageReference imageReference = plateImage.getImageReference();
            imageReference.setRelativeImageFolder(folderPathPrefix);
        }
    }

    private static String getRelativeImagesDirectory(File rootDirectory, File imagesInStoreFolder)
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

    /**
     * @return true if the dataset has been enriched before and already contains all the information
     *         about images.
     */
    public boolean accepts(DataSetInformation dataSetInformation, File incomingDataSet)
    {
        return dataSetInformation instanceof ImageDataSetInformation;
    }

    private ImageFileExtractionWithConfig extractImages(
            final DataSetInformation dataSetInformation, final File incomingDataSetDirectory)
    {
        long extractionStart = System.currentTimeMillis();
        IImageFileExtractor extractor = tryGetImageFileExtractor(incomingDataSetDirectory);
        if (extractor == null)
        {
            return extractImagesFromDatasetInfoOrDie(dataSetInformation);
        }
        ImageFileExtractionResult result =
                extractor.extract(incomingDataSetDirectory, dataSetInformation);

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
        return new ImageFileExtractionWithConfig(result, globalImageStorageConfiguraton);
    }

    private ImageFileExtractionWithConfig extractImagesFromDatasetInfoOrDie(
            final DataSetInformation dataSetInformation)
    {
        if (dataSetInformation instanceof ImageDataSetInformation == false)
        {
            throw ConfigurationFailureException
                    .fromTemplate(
                            "File extractor '%s' has not been configured or jython script in 'top-level-data-set-handler' is not '%s'.",
                            FILE_EXTRACTOR_PROPERTY,
                            JythonPlateDataSetHandler.class.getCanonicalName());
        } else
        {
            return extractImagesFromDatasetInfo((ImageDataSetInformation) dataSetInformation);
        }
    }

    private ImageFileExtractionWithConfig extractImagesFromDatasetInfo(
            ImageDataSetInformation imageDataSetInfo)
    {
        if (imageDataSetInfo.isValid() == false)
        {
            throw ConfigurationFailureException
                    .fromTemplate("Invalid image dataset info object, check if your jython script fills all the fields: "
                            + imageDataSetInfo);
        }
        Geometry tileGeometry =
                new Geometry(imageDataSetInfo.getTileRowsNumber(),
                        imageDataSetInfo.getTileColumnsNumber());

        List<AcquiredSingleImage> images = convertImages(imageDataSetInfo);

        List<File> invalidFiles = new ArrayList<File>(); // handles in an earlier phase
        ImageStorageConfiguraton imageStorageConfiguraton =
                imageDataSetInfo.getImageStorageConfiguraton();
        if (imageStorageConfiguraton == null)
        {
            imageStorageConfiguraton = globalImageStorageConfiguraton;
        }

        ImageFileExtractionResult extractionResult =
                new ImageFileExtractionResult(images, invalidFiles, imageDataSetInfo.getChannels(),
                        tileGeometry, imageStorageConfiguraton.getStoreChannelsOnExperimentLevel());
        return new ImageFileExtractionWithConfig(extractionResult, imageStorageConfiguraton);
    }

    private static List<AcquiredSingleImage> convertImages(ImageDataSetInformation imageDataSetInfo)
    {
        List<ImageFileInfo> imageInfos = imageDataSetInfo.getImages();
        List<ChannelColorComponent> channelColorComponentsOrNull =
                imageDataSetInfo.getChannelColorComponents();
        List<Channel> channels = imageDataSetInfo.getChannels();

        List<AcquiredSingleImage> images = new ArrayList<AcquiredSingleImage>();
        for (ImageFileInfo imageInfo : imageInfos)
        {
            if (channelColorComponentsOrNull != null)
            {
                for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
                {
                    ColorComponent colorComponent =
                            asColorComponent(channelColorComponentsOrNull.get(i));
                    Channel channel = channels.get(i);
                    AcquiredSingleImage image =
                            AbstractImageFileExtractor.createImage(imageInfo, channel.getCode(),
                                    colorComponent);
                    images.add(image);
                }
            } else
            {
                images.addAll(AbstractImageFileExtractor
                        .createImagesWithNoColorComponent(imageInfo));
            }
        }
        return images;
    }

    private static ColorComponent asColorComponent(ChannelColorComponent channelColorComponent)
    {
        return ColorComponent.valueOf(channelColorComponent.name());
    }

    protected IImageFileExtractor tryGetImageFileExtractor(File incomingDataSetDirectory)
    {
        return imageFileExtractorOrNull;
    }

    public final File storeData(DataSetInformation dataSetInformation,
            ITypeExtractor typeExtractor, IMailClient mailClient, File incomingDataSetDirectory,
            File rootDir)
    {
        throw new IllegalStateException(
                "This method is deprecated. Please use transactions (see 'createTransaction').");
    }

    @Override
    public final void commit(File incomingDataSetDirectory, File storedDataDirectory)
    {
        throw new IllegalStateException("You can only call 'commit' on a transaction object "
                + "obtained via the method 'storeDataTransactionally'.");
    }

    public final UnstoreDataAction rollback(File incomingDataSetDirectory,
            File storedDataDirectory, Throwable exception)
    {
        throw new IllegalStateException("You can only call 'rollback' on a transaction object "
                + "obtained via the method 'storeDataTransactionally'.");
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
                operationLog
                        .info(String
                                .format("Storage operation rollback: directory '%s' has moved to incoming directory '%s'.",
                                        originalDataFile, incomingDirectory.getAbsolutePath()));
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

    private static void commitHdf5StorageFormatChanges(File storedDataDirectory)
    {
        File originalFolder = getOriginalFolder(storedDataDirectory);
        File hdf5OriginalContainer = getHdf5OriginalContainer(storedDataDirectory);
        if (hdf5OriginalContainer.exists()) // this should be always true
        {
            final IFileOperations fileOps = FileOperations.getMonitoredInstanceForCurrentThread();
            if (fileOps.removeRecursivelyQueueing(originalFolder) == false)
            {
                operationLog.error("Cannot delete original data '"
                        + originalFolder.getAbsolutePath() + "'.");
            }
        } else
        {
            notificationLog.error(String.format(
                    "HDF5 container with original data '%s' could not be found, this should not happen! "
                            + "Dataset should be registered again! "
                            + "Keeping the original directory '%s'.", hdf5OriginalContainer,
                    originalFolder));
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
        Set<ImageSeriesPoint> points = new HashSet<ImageSeriesPoint>();
        for (AcquiredSingleImage image : images)
        {
            if (image.tryGetTimePoint() != null || image.tryGetDepth() != null
                    || image.tryGetSeriesNumber() != null)
            {
                points.add(new ImageSeriesPoint(image.tryGetTimePoint(), image.tryGetDepth(), image
                        .tryGetSeriesNumber()));
            }
        }
        return points.size() > 1;
    }

}
