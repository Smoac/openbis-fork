package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.python.util.PythonInterpreter;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.etl.Hdf5ThumbnailGenerator;
import ch.systemsx.cisd.openbis.dss.etl.PlateGeometryOracle;
import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageContainerDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailFilePaths;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImagingDatasetFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvFeatureVectorParser;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Jython dropbox for HCS and Microscopy image datasets.
 * 
 * @author Tomasz Pylak
 */
public class JythonPlateDataSetHandler extends JythonTopLevelDataSetHandler<DataSetInformation>
{

    private final String ORIGINAL_DIRNAME_KEY = "image-datasets-original-dir-name";

    private final String originalDirName;

    public JythonPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState)
    {
        super(globalState);
        originalDirName = parseOriginalDir(globalState.getThreadParameters().getThreadProperties());
    }

    private String parseOriginalDir(Properties threadProperties)
    {
        String originalDir =
                PropertyUtils.getProperty(threadProperties, ORIGINAL_DIRNAME_KEY,
                        ScreeningConstants.ORIGINAL_DATA_DIR);
        if (false == FileUtilities.isValidFileName(originalDir))
        {
            throw ConfigurationFailureException.fromTemplate(
                    "Invalid folder name specified in '%s': '%s'.", ORIGINAL_DIRNAME_KEY,
                    originalDir);
        }
        return originalDir;
    }

    /**
     * Create a screening specific factory available to the python script.
     */
    @Override
    protected IDataSetRegistrationDetailsFactory<DataSetInformation> createObjectFactory(
            PythonInterpreter interpreter, DataSetInformation userProvidedDataSetInformationOrNull)
    {
        return new JythonPlateDatasetFactory(getRegistratorState(),
                userProvidedDataSetInformationOrNull);
    }

    public static class JythonImageDataSetRegistrationFactory extends
            AbstractDataSetRegistrationDetailsFactory<ImageDataSetInformation>
    {

        public JythonImageDataSetRegistrationFactory(
                ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState registratorState,
                DataSetInformation userProvidedDataSetInformationOrNull)
        {
            super(registratorState, userProvidedDataSetInformationOrNull);
        }

        @Override
        protected ImageDataSetInformation createDataSetInformation()
        {
            return new ImageDataSetInformation();
        }
    }

    public static class JythonImageContainerDataSetRegistrationFactory extends
            AbstractDataSetRegistrationDetailsFactory<DataSetInformation>
    {

        public JythonImageContainerDataSetRegistrationFactory(
                ch.systemsx.cisd.etlserver.registrator.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState registratorState,
                DataSetInformation userProvidedDataSetInformationOrNull)
        {
            super(registratorState, userProvidedDataSetInformationOrNull);
        }

        @Override
        public ImageContainerDataSet createDataSet(
                DataSetRegistrationDetails<DataSetInformation> registrationDetails, File stagingFile)
        {
            return new ImageContainerDataSet(registrationDetails, stagingFile);
        }

        @Override
        protected DataSetInformation createDataSetInformation()
        {
            return new DataSetInformation();
        }
    }

    public static class JythonPlateDatasetFactory extends JythonObjectFactory<DataSetInformation>
            implements IImagingDatasetFactory
    {
        private final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

        private final IDataSetRegistrationDetailsFactory<DataSetInformation> imageContainerDatasetFactory;

        private final IDataSetRegistrationDetailsFactory<FeatureVectorDataSetInformation> featureVectorDatasetFactory;

        public JythonPlateDatasetFactory(
                OmniscientTopLevelDataSetRegistratorState registratorState,
                DataSetInformation userProvidedDataSetInformationOrNull)
        {
            super(registratorState, userProvidedDataSetInformationOrNull);
            this.imageContainerDatasetFactory =
                    new JythonImageContainerDataSetRegistrationFactory(this.registratorState,
                            this.userProvidedDataSetInformationOrNull);
            this.imageDatasetFactory =
                    new JythonImageDataSetRegistrationFactory(this.registratorState,
                            this.userProvidedDataSetInformationOrNull);
            this.featureVectorDatasetFactory =
                    new JythonObjectFactory<FeatureVectorDataSetInformation>(this.registratorState,
                            this.userProvidedDataSetInformationOrNull)
                        {
                            @Override
                            protected FeatureVectorDataSetInformation createDataSetInformation()
                            {
                                return new FeatureVectorDataSetInformation();
                            }
                        };
        }

        /** By default a starndard dataset is created. */
        @Override
        protected DataSetInformation createDataSetInformation()
        {
            return new DataSetInformation();
        }

        public DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails(
                SimpleImageDataConfig imageDataSet, File incomingDatasetFolder)
        {
            return SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                    incomingDatasetFolder, imageDatasetFactory);
        }

        /** a simple method to register the described image dataset in a separate transaction */
        public boolean registerImageDataset(SimpleImageDataConfig imageDataSet,
                File incomingDatasetFolder,
                DataSetRegistrationService<ImageDataSetInformation> service)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails =
                    createImageRegistrationDetails(imageDataSet, incomingDatasetFolder);
            return registerImageDataset(imageDatasetDetails, incomingDatasetFolder, service);
        }

        public boolean registerImageDataset(
                DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails,
                File incomingDatasetFolder,
                DataSetRegistrationService<ImageDataSetInformation> service)
        {
            DataSetRegistrationTransaction<ImageDataSetInformation> transaction =
                    service.transaction(incomingDatasetFolder, imageDatasetFactory);
            IDataSet newDataset = transaction.createNewDataSet(imageDatasetDetails);
            transaction.moveFile(incomingDatasetFolder.getPath(), newDataset);
            return transaction.commit();
        }

        /**
         * @return a constant which can be used as a vocabulary term value for $PLATE_GEOMETRY
         *         property of a plate/
         * @throws UserFailureException if all available geometries in openBIS are too small (there
         *             is a well outside).
         */
        public String figureGeometry(
                DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails)
        {
            List<ImageFileInfo> images =
                    registrationDetails.getDataSetInformation().getImageDataSetStructure()
                            .getImages();
            List<Location> locations = extractLocations(images);
            List<String> plateGeometries =
                    loadPlateGeometries(registratorState.getGlobalState().getOpenBisService());
            return PlateGeometryOracle.figureGeometry(locations, plateGeometries);
        }

        private static List<String> loadPlateGeometries(IEncapsulatedOpenBISService openbisService)
        {
            Collection<VocabularyTerm> terms =
                    openbisService.listVocabularyTerms(ScreeningConstants.PLATE_GEOMETRY);
            List<String> plateGeometries = new ArrayList<String>();
            for (VocabularyTerm v : terms)
            {
                plateGeometries.add(v.getCode());
            }
            return plateGeometries;
        }

        private static List<Location> extractLocations(List<ImageFileInfo> images)
        {
            List<Location> locations = new ArrayList<Location>();
            for (ImageFileInfo image : images)
            {
                locations.add(image.tryGetWellLocation());
            }
            return locations;
        }

        // ----

        public IFeaturesBuilder createFeaturesBuilder()
        {
            return new FeaturesBuilder();
        }

        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
                IFeaturesBuilder featureBuilder)
        {
            FeaturesBuilder myFeatureBuilder = (FeaturesBuilder) featureBuilder;
            List<FeatureDefinition> featureDefinitions =
                    myFeatureBuilder.getFeatureDefinitionValuesList();
            return createFeatureVectorRegistrationDetails(featureDefinitions);
        }

        /**
         * Parses the feature vactors from the specified CSV file. CSV format can be configured with
         * following properties:
         * 
         * <pre>
         *   # Separator character between headers and row cells.
         *   separator = ,
         *   ignore-comments = true
         *   # Header of the column denoting the row of a well.
         *   well-name-row = row
         *   # Header of the column denoting the column of a well.
         *   well-name-col = col
         *   well-name-col-is-alphanum = true
         * </pre>
         * 
         * @throws IOException if file cannot be parsed
         */
        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
                String csvFilePath, Properties properties) throws IOException
        {
            List<FeatureDefinition> featureDefinitions =
                    CsvFeatureVectorParser.parse(new File(csvFilePath), properties);
            return createFeatureVectorRegistrationDetails(featureDefinitions);
        }

        private DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                List<FeatureDefinition> featureDefinitions)
        {
            DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails =
                    featureVectorDatasetFactory.createDataSetRegistrationDetails();
            FeatureVectorDataSetInformation featureVectorDataSet =
                    registrationDetails.getDataSetInformation();
            featureVectorDataSet.setFeatures(featureDefinitions);
            registrationDetails
                    .setDataSetType(ScreeningConstants.DEFAULT_ANALYSIS_WELL_DATASET_TYPE);
            registrationDetails.setMeasuredData(false);
            return registrationDetails;
        }

        // -------- backward compatibility methods

        /**
         * This method exists just for backward compatibility. It used to have the second parameter,
         * which is now ignored.
         * 
         * @deprecated use {@link #createFeatureVectorDatasetDetails(IFeaturesBuilder)} instead.
         */
        @Deprecated
        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                IFeaturesBuilder featureBuilder, Object incomingDatasetFolder)
        {
            return createFeatureVectorDatasetDetails(featureBuilder);
        }

        /**
         * @deprecated Changed to {@link #createFeatureVectorDatasetDetails(String, Properties)} due
         *             to naming convention change.
         */
        @Deprecated
        public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
                String csvFilePath, Properties properties) throws IOException
        {
            return createFeatureVectorDatasetDetails(csvFilePath, properties);
        }

    }

    @Override
    protected DataSetRegistrationService<DataSetInformation> createDataSetRegistrationService(
            File incomingDataSetFile, DataSetInformation callerDataSetInformationOrNull,
            IDelegatedActionWithResult<Boolean> cleanAfterwardsAction,
            ITopLevelDataSetRegistratorDelegate delegate)
    {
        return new JythonDataSetRegistrationService<DataSetInformation>(this, incomingDataSetFile,
                callerDataSetInformationOrNull, cleanAfterwardsAction, delegate,
                new PythonInterpreter(), getGlobalState())
            {
                @SuppressWarnings("unchecked")
                @Override
                protected DataSetRegistrationTransaction<DataSetInformation> createTransaction(
                        File rollBackStackParentFolder,
                        File workingDirectory,
                        File stagingDirectory,
                        IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory)
                {
                    return new ImagingDataSetRegistrationTransaction(rollBackStackParentFolder,
                            workingDirectory, stagingDirectory, this, registrationDetailsFactory,
                            originalDirName);
                }
            };
    }

    /**
     * Imaging-specific transaction. Handles image datasets in a special way, other datasets are
     * registered using a standard procedure.
     * <p>
     * Note that this transaction is not parametrized by a concrete {@link DataSetInformation}
     * subclass. It has to deal with {@link ImageDataSetInformation},
     * {@link FeatureVectorDataSetInformation} and {@link DataSetInformation} at the same time.
     */
    private static class ImagingDataSetRegistrationTransaction extends
            DataSetRegistrationTransaction implements IImagingDataSetRegistrationTransaction
    {
        private final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

        private final IDataSetRegistrationDetailsFactory<DataSetInformation> imageContainerDatasetFactory;

        private final String originalDirName;

        @SuppressWarnings("unchecked")
        public ImagingDataSetRegistrationTransaction(File rollBackStackParentFolder,
                File workingDirectory, File stagingDirectory,
                DataSetRegistrationService<DataSetInformation> registrationService,
                IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory,
                String originalDirName)
        {
            super(rollBackStackParentFolder, workingDirectory, stagingDirectory,
                    registrationService, registrationDetailsFactory);

            assert registrationDetailsFactory instanceof JythonPlateDatasetFactory : "JythonPlateDatasetFactory expected, but got: "
                    + registrationDetailsFactory.getClass().getCanonicalName();

            JythonPlateDatasetFactory factory =
                    (JythonPlateDatasetFactory) registrationDetailsFactory;
            this.imageDatasetFactory = factory.imageDatasetFactory;
            this.imageContainerDatasetFactory = factory.imageContainerDatasetFactory;
            this.originalDirName = originalDirName;
        }

        public IImageDataSet createNewImageDataSet(SimpleImageDataConfig imageDataSet,
                File incomingFolderWithImages)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> details =
                    SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                            incomingFolderWithImages, imageDatasetFactory);
            return createNewImageDataSet(details);
        }

        /**
         * Creates container dataset which contains dataset with original images (created on the
         * fly). If thumbnails are required they are generated and moved to a thumbnail dataset
         * which becomes a part of the container as well.
         * <p>
         * The original images dataset is special - it contains description of what should be saved
         * in imaging database by the storage processor.
         * 
         * @return container dataset.
         */
        public IImageDataSet createNewImageDataSet(
                DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails)
        {
            ImageDataSetInformation imageDataSetInformation =
                    imageRegistrationDetails.getDataSetInformation();
            ImageDataSetStructure imageDataSetStructure =
                    imageDataSetInformation.getImageDataSetStructure();
            File incomingDirectory = imageDataSetInformation.getIncomingDirectory();
            List<String> containedDataSetCodes = new ArrayList<String>();

            // create thumbnails dataset if needed
            IDataSet thumbnailDatasetOrNull = null;
            boolean generateThumbnails = imageDataSetStructure.areThumbnailsGenerated();
            if (generateThumbnails)
            {
                thumbnailDatasetOrNull = createThumbnailDataset();

                ThumbnailFilePaths thumbnailPaths =
                        generateThumbnails(imageDataSetStructure, incomingDirectory,
                                thumbnailDatasetOrNull);
                imageDataSetInformation.setThumbnailFilePaths(thumbnailPaths);
                containedDataSetCodes.add(thumbnailDatasetOrNull.getDataSetCode());
            }
            // create main dataset (with original images)
            @SuppressWarnings("unchecked")
            DataSet<ImageDataSetInformation> mainDataset =
                    (DataSet<ImageDataSetInformation>) super
                            .createNewDataSet(imageRegistrationDetails);
            containedDataSetCodes.add(mainDataset.getDataSetCode());

            if (thumbnailDatasetOrNull != null)
            {
                setSameDatasetOwner(mainDataset, thumbnailDatasetOrNull);
            }

            ImageContainerDataSet containerDataset =
                    createImageContainerDataset(mainDataset, imageDataSetInformation,
                            containedDataSetCodes);
            containerDataset.setOriginalDataset(mainDataset);
            if (thumbnailDatasetOrNull != null)
            {
                containerDataset.setThumbnailDatasets(Arrays.asList(thumbnailDatasetOrNull));
            }
            imageDataSetInformation.setContainerDatasetPermId(containerDataset.getDataSetCode());

            return containerDataset;
        }

        private File prependOriginalDirectory(String directoryPath)
        {
            return new File(originalDirName + File.separator + directoryPath);
        }

        private ThumbnailFilePaths generateThumbnails(ImageDataSetStructure imageDataSetStructure,
                File incomingDirectory, IDataSet thumbnailDataset)
        {
            String thumbnailFile =
                    createNewFile(thumbnailDataset, Constants.HDF5_CONTAINER_THUMBNAILS_FILE_NAME);

            List<RelativeImageFile> images = asRelativeImageFile(imageDataSetStructure.getImages());
            ThumbnailFilePaths thumbnailPaths =
                    Hdf5ThumbnailGenerator.tryGenerateThumbnails(images, incomingDirectory,
                            thumbnailFile, imageDataSetStructure.getImageStorageConfiguraton(),
                            thumbnailDataset.getDataSetCode());
            return thumbnailPaths;
        }

        private static List<RelativeImageFile> asRelativeImageFile(List<ImageFileInfo> images)
        {
            List<RelativeImageFile> imageFiles = new ArrayList<RelativeImageFile>();
            for (ImageFileInfo image : images)
            {
                imageFiles.add(RelativeImageFile.create(image));
            }
            return imageFiles;
        }

        private IDataSet createThumbnailDataset()
        {
            IDataSet thumbnailDataset =
                    createNewDataSet(ScreeningConstants.DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE);
            thumbnailDataset
                    .setFileFormatType(ScreeningConstants.DEFAULT_OVERVIEW_IMAGE_DATASET_FILE_FORMAT);
            thumbnailDataset.setMeasuredData(false);

            return thumbnailDataset;
        }

        private ImageContainerDataSet createImageContainerDataset(IDataSet mainDataset,
                ImageDataSetInformation imageDataSetInformation, List<String> containedDataSetCodes)
        {
            String containerDatasetTypeCode = findContainerDatasetTypeCode(imageDataSetInformation);
            @SuppressWarnings("unchecked")
            ImageContainerDataSet containerDataset =
                    (ImageContainerDataSet) createNewDataSet(imageContainerDatasetFactory,
                            containerDatasetTypeCode);
            setSameDatasetOwner(mainDataset, containerDataset);
            moveDatasetRelations(mainDataset, containerDataset);

            containerDataset.setContainedDataSetCodes(containedDataSetCodes);
            return containerDataset;
        }

        // Copies properties and relations to datasets from the main dataset to the container and
        // resets them in the main dataset.
        private static void moveDatasetRelations(IDataSet mainDataset, IDataSet containerDataset)
        {
            containerDataset.setParentDatasets(mainDataset.getParentDatasets());
            mainDataset.setParentDatasets(Collections.<String> emptyList());

            for (String propertyCode : mainDataset.getAllPropertyCodes())
            {
                containerDataset.setPropertyValue(propertyCode,
                        mainDataset.getPropertyValue(propertyCode));
                mainDataset.setPropertyValue(propertyCode, null);
            }
        }

        private static String findContainerDatasetTypeCode(
                ImageDataSetInformation imageDataSetInformation)
        {
            String mainDatasetTypeCode = imageDataSetInformation.getDataSetType().getCode();
            String prefix = ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX;
            if (mainDatasetTypeCode.toUpperCase().startsWith(prefix) == false)
            {
                throw UserFailureException.fromTemplate(
                        "The image dataset type '%s' does not start with '%s'.",
                        mainDatasetTypeCode, prefix);
            }
            if (mainDatasetTypeCode.toUpperCase().contains(
                    ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER))
            {
                throw UserFailureException
                        .fromTemplate(
                                "The specified image dataset type '%s' should not be of container type, but contains '%s' in the type code.",
                                mainDatasetTypeCode,
                                ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER);
            }
            return prefix + ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER
                    + mainDatasetTypeCode.substring(prefix.length());
        }

        private static void setSameDatasetOwner(IDataSet templateDataset,
                IDataSet destinationDataset)
        {
            destinationDataset.setExperiment(templateDataset.getExperiment());
            destinationDataset.setSample(templateDataset.getSample());

        }

        @SuppressWarnings(
            { "cast", "unchecked" })
        @Override
        public IDataSet createNewDataSet(DataSetRegistrationDetails registrationDetails)
        {
            if (registrationDetails.getDataSetInformation() instanceof ImageDataSetInformation)
            {
                DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails =
                        (DataSetRegistrationDetails<ImageDataSetInformation>) registrationDetails;
                return createNewImageDataSet(imageRegistrationDetails);
            } else
            {
                return super.createNewDataSet(registrationDetails);
            }
        }

        /**
         * If we are dealing with the image dataset container then the move operation is delegated
         * to the original dataset. Otherwise a default implementation is used.
         */
        @Override
        public String moveFile(String src, IDataSet dst)
        {
            return moveFile(src, dst, new File(src).getName());
        }

        /**
         * If we are dealing with the image dataset container then the move operation is delegated
         * to the original dataset. Otherwise a default implementation is used.
         */
        @Override
        public String moveFile(String src, IDataSet dst, String dstInDataset)
        {
            ImageContainerDataSet imageContainerDataset = tryAsImageContainerDataset(dst);
            if (imageContainerDataset != null)
            {
                String destination = dstInDataset;
                if (destination.startsWith(originalDirName) == false)
                {
                    destination = prependOriginalDirectory(destination).getPath();
                }
                DataSet<ImageDataSetInformation> originalDataset =
                        imageContainerDataset.getOriginalDataset();
                if (originalDataset == null)
                {
                    throw new UserFailureException(
                            "Cannot move the files because the original dataset is missing: " + src);
                }
                originalDataset.getRegistrationDetails().getDataSetInformation()
                        .setDatasetRelativeImagesFolderPath(new File(destination));

                return super.moveFile(src, originalDataset, destination);
            } else
            {
                return super.moveFile(src, dst, dstInDataset);
            }
        }

        private static ImageContainerDataSet tryAsImageContainerDataset(IDataSet dataset)
        {
            if (dataset instanceof ImageContainerDataSet)
            {
                return (ImageContainerDataSet) dataset;
            } else
            {
                return null;
            }
        }

    }

}
