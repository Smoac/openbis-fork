/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations.ConvertToolImageTransformerFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations.ImageTransformationBuffer;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Allows to configure extraction of images for a plate or microscopy sample.
 * 
 * @author Tomasz Pylak
 */
abstract public class SimpleImageDataConfig
{
    // --- one of the following two methods has to be overridden -----------------

    /**
     * Extracts tile number, channel code and well code for a given relative path to a single image.
     * This method should overridden to deal with files containing single images. It is ignored if
     * {@link #extractImagesMetadata(String, List)} is overridden as well.
     * <p>
     * It will be called for each file found in the incoming directory which has the extension
     * returned by {@link #getRecognizedImageExtensions()}.
     * </p>
     * To deal with image containers (like multi-page TIFF files) override
     * {@link #extractImagesMetadata(String, List)} instead, otherwise leave that method unchanged.
     */
    public ImageMetadata extractImageMetadata(String imagePath)
    {
        throw new UnsupportedOperationException(
                "One of the extractImageMetadata() methods has to be implemented.");
    }

    /**
     * Returns meta-data for each image in the specified image container. This method should
     * overridden to deal with container images (like multi-page TIFF files).
     * <p>
     * This implementation returns the result of {@link #extractImageMetadata(String)} wrapped in an
     * array, image identifiers are ignored.
     * </p>
     * 
     * @param imagePath path to the single image or container of many images
     * @param imageIdentifiers Identifiers of all images contained in the image file.
     */
    public ImageMetadata[] extractImagesMetadata(String imagePath,
            List<ImageIdentifier> imageIdentifiers)
    {

        ImageMetadata imageMetadata = extractImageMetadata(imagePath);
        return (imageMetadata != null) ? new ImageMetadata[]
            { imageMetadata } : new ImageMetadata[0];
    }

    // --- methods which can be overridden -----------------

    /**
     * By default layouts all images in one row by returning (1, maxTileNumber) geometry.Can be
     * overridden in subclasses.
     * 
     * @param imageMetadataList a list of metadata for each encountered image
     * @param maxTileNumber the biggest tile number among all encountered images
     * @return the width and height of the matrix of tiles (a.k.a. fields or sides) in the well.
     */
    public Geometry getTileGeometry(List<? extends ImageMetadata> imageMetadataList,
            int maxTileNumber)
    {
        return Geometry.createFromRowColDimensions(1, maxTileNumber);
    }

    /**
     * For a given tile number and tiles geometry returns (x,y) which describes where the tile is
     * located on the well. Can be overridden in subclasses.<br>
     * Note: The top left tile has coordinates (1,1).
     * 
     * @param tileNumber number of the tile extracted by {@link #extractImageMetadata}.
     * @param tileGeometry the geometry of the well matrix returned by {@link #getTileGeometry}.
     */
    public Location getTileCoordinates(int tileNumber, Geometry tileGeometry)
    {
        int columns = tileGeometry.getWidth();
        int row = ((tileNumber - 1) / columns) + 1;
        int col = ((tileNumber - 1) % columns) + 1;
        return new Location(row, col);
    }

    /**
     * <p>
     * Creates channel description for a given code. Can be overridden in subclasses.
     * </p>
     * By default the channel label will be equal to the code. Channel color returned by
     * {@link #getChannelColor(String)} will be used.
     */
    public Channel createChannel(String channelCode)
    {
        ChannelColor channelColor = getChannelColor(channelCode);
        ImageTransformation[] availableTransformations =
                getAvailableChannelTransformations(channelCode);
        String label = channelCode;
        String normalizedChannelCode = CodeNormalizer.normalize(channelCode);
        Channel channel = new Channel(normalizedChannelCode, label, channelColor);
        channel.setAvailableTransformations(availableTransformations);
        return channel;
    }

    /**
     * Sets available transformations which can be applied to images of the specified channel (on
     * user's request).
     * <p>
     * Can be overridden in subclasses. The easiest way to create transformations is to use
     * {@link ImageTransformationBuffer} class.
     * <p>
     * By default returns null.
     */
    public ImageTransformation[] getAvailableChannelTransformations(String channelCode)
    {
        return null;
    }

    /**
     * Returns color for the specified channel. It will be used to display merged channels images.
     * <p>
     * Can be overridden in subclasses. It is ignored if {@link #createChannel(String)} is
     * overridden as well.
     * </p>
     * By default returns null (the arbitrary color will be set).
     */
    public ChannelColor getChannelColor(String channelCode)
    {
        return null;
    }

    // --- auxiliary structures ----------------------------------------------

    private String datasetTypeCode;

    private String fileFormatCode = ScreeningConstants.UNKNOWN_FILE_FORMAT;

    private String plateCode;

    private String spaceCode;

    private boolean isMeasured = false;

    private String[] recognizedImageExtensions = new String[]
        { "tiff", "tif", "png", "gif", "jpg", "jpeg" };

    private boolean generateThumbnails = false;

    private int maxThumbnailWidthAndHeight = 256;

    private boolean generateThumbnailsWithImageMagic = true;

    private List<String> thumbnailsGenerationImageMagicParams = Collections.emptyList();

    private boolean generateThumbnailsInHighQuality = false;

    private double allowedMachineLoadDuringThumbnailsGeneration = 1.0;

    private boolean storeChannelsOnExperimentLevel = false;

    private OriginalDataStorageFormat originalDataStorageFormat =
            OriginalDataStorageFormat.UNCHANGED;

    private String convertTransformationCliArgumentsOrNull;

    private ImageLibraryInfo imageLibraryInfoOrNull;

    private boolean isMicroscopy;

    // If null then no common intensity rescaling parameters are computed.
    // If empty the computation will take place for all channels.
    // Otherwise all images of these channels are analysed during dataset registration (costly
    // operation!)
    private List<String> computeCommonIntensityRangeOfAllImagesForChannelsOrNull = null;

    private float computeCommonIntensityRangeOfAllImagesThreshold = 0.005f;

    // --- getters & setters ----------------------------------------------

    public ImageStorageConfiguraton getImageStorageConfiguration()
    {
        ImageStorageConfiguraton imageStorageConfiguraton =
                ImageStorageConfiguraton.createDefault();
        imageStorageConfiguraton
                .setStoreChannelsOnExperimentLevel(isStoreChannelsOnExperimentLevel());
        imageStorageConfiguraton.setOriginalDataStorageFormat(getOriginalDataStorageFormat());
        if (isGenerateThumbnails())
        {
            ThumbnailsStorageFormat thumbnailsStorageFormat = new ThumbnailsStorageFormat();
            thumbnailsStorageFormat
                    .setAllowedMachineLoadDuringGeneration(getAllowedMachineLoadDuringThumbnailsGeneration());
            thumbnailsStorageFormat.setMaxWidth(getMaxThumbnailWidthAndHeight());
            thumbnailsStorageFormat.setMaxHeight(getMaxThumbnailWidthAndHeight());
            thumbnailsStorageFormat.setGenerateWithImageMagic(generateThumbnailsWithImageMagic);
            thumbnailsStorageFormat.setImageMagicParams(thumbnailsGenerationImageMagicParams);
            thumbnailsStorageFormat.setHighQuality(generateThumbnailsInHighQuality);
            imageStorageConfiguraton.setThumbnailsStorageFormat(thumbnailsStorageFormat);
        }
        if (false == StringUtils.isBlank(convertTransformationCliArgumentsOrNull))
        {
            IImageTransformerFactory convertTransformerFactory =
                    new ConvertToolImageTransformerFactory(convertTransformationCliArgumentsOrNull);
            imageStorageConfiguraton.setImageTransformerFactory(convertTransformerFactory);

        }
        imageStorageConfiguraton.setImageLibrary(imageLibraryInfoOrNull);
        return imageStorageConfiguraton;
    }

    public String getPlateSpace()
    {
        return spaceCode;
    }

    public String getPlateCode()
    {
        return plateCode;
    }

    public String[] getRecognizedImageExtensions()
    {
        return recognizedImageExtensions;
    }

    public boolean isGenerateThumbnails()
    {
        return generateThumbnails;
    }

    public int getMaxThumbnailWidthAndHeight()
    {
        return maxThumbnailWidthAndHeight;
    }

    public double getAllowedMachineLoadDuringThumbnailsGeneration()
    {
        return allowedMachineLoadDuringThumbnailsGeneration;
    }

    public boolean isStoreChannelsOnExperimentLevel()
    {
        return storeChannelsOnExperimentLevel;
    }

    public OriginalDataStorageFormat getOriginalDataStorageFormat()
    {
        return originalDataStorageFormat;
    }

    public String tryGetConvertTransformationCliArguments()
    {
        return convertTransformationCliArgumentsOrNull;
    }

    public List<String> getComputeCommonIntensityRangeOfAllImagesForChannels()
    {
        return computeCommonIntensityRangeOfAllImagesForChannelsOrNull;
    }

    public float getComputeCommonIntensityRangeOfAllImagesThreshold()
    {
        return computeCommonIntensityRangeOfAllImagesThreshold;
    }

    // ----- Setters -------------------------

    /**
     * Sets the existing plate to which the dataset should belong.
     * 
     * @param spaceCode space where the plate for which the dataset has been acquired exist
     * @param plateCode code of the plate to which the dataset will belong
     */
    public void setPlate(String spaceCode, String plateCode)
    {
        this.spaceCode = spaceCode;
        this.plateCode = plateCode;
    }

    /**
     * Only files with these extensions will be recognized as images (e.g. ["jpg", "png"]).<br>
     * By default it is set to [ "tiff", "tif", "png", "gif", "jpg", "jpeg" ].
     */
    public void setRecognizedImageExtensions(String[] recognizedImageExtensions)
    {
        this.recognizedImageExtensions = recognizedImageExtensions;
    }

    /** should thumbnails be generated? False by default. */
    public void setGenerateThumbnails(boolean generateThumbnails)
    {
        this.generateThumbnails = generateThumbnails;
    }

    /** the maximal width and height of the generated thumbnails */
    public void setMaxThumbnailWidthAndHeight(int maxThumbnailWidthAndHeight)
    {
        this.maxThumbnailWidthAndHeight = maxThumbnailWidthAndHeight;
    }

    /**
     * Valid only if thumbnails generation is switched on. Set it to a value lower than 1 if you
     * want only some of your processor cores to be used for thumbnails generation. Number of
     * threads that are used for thumbnail generation will be equal to: this constant * number of
     * processor cores.
     */
    public void setAllowedMachineLoadDuringThumbnailsGeneration(
            double allowedMachineLoadDuringThumbnailsGeneration)
    {
        this.allowedMachineLoadDuringThumbnailsGeneration =
                allowedMachineLoadDuringThumbnailsGeneration;
    }

    /**
     * Decides if ImageMagic 'convert' utility will be used to generate thumbnails. True by default.
     * <p>
     * The tool should be installed and accessible, otherwise set this option to false and set
     * {@link #setAllowedMachineLoadDuringThumbnailsGeneration(double)} to
     * 1/numberOfYourProcessorCores. Internal library will be used to generate thumbnails, but it is
     * not able to generate thumbnails in parallel.
     */
    public void setUseImageMagicToGenerateThumbnails(boolean generateWithImageMagic)
    {
        this.generateThumbnailsWithImageMagic = generateWithImageMagic;
    }

    /**
     * Sets additional parameters which should be passed to ImageMagic 'convert' utility when it is
     * used to generate thumbnails.
     * <p>
     * Example: pass "-contrast-stretch 2%" to discard 2% of brightest and darkest pixels in the
     * thumbnails.
     */
    public void setThumbnailsGenerationImageMagicParams(String[] imageMagicParams)
    {
        this.thumbnailsGenerationImageMagicParams = Arrays.asList(imageMagicParams);
    }

    /**
     * if true and thumbnails generation is switched on, thumbnails will be generated with high
     * quality.
     */
    public void setGenerateHighQualityThumbnails(boolean highQualityThumbnails)
    {
        this.generateThumbnailsInHighQuality = highQualityThumbnails;
    }

    /**
     * <p>
     * Can be used only for grayscale images, Useful when images do not use the whole available
     * color depth of the format in which they are stored (e.g. 10 bits out of 12). By default
     * switched off. Causes that the conversion to 8 bit color depth looses less information. At the
     * same time allows to compare images of one dataset to each other.<br>
     * Warning: causes that all images have to be analysed before registration, this is a costly
     * operation!
     * </p>
     * <p>
     * If isComputed is set to true all dataset images will be analysed and one range of pixel
     * intensities used across all images will be computed (with 0.5% threshold). The result will be
     * saved and it will be possible to apply on-the-fly transformation when browsing images.
     * </p>
     * <p>
     * Example: let's assume that all plate images are saved as 12 bit grayscales. Each image has
     * ability to use pixel intensities from 0 to 4095. In our example only a range of the available
     * intensities is used, let's say from 1024 to 2048. Before the image is displayed to the user
     * it has to be converted to 8-bit color depth (range of intensities from 0 to 255). Without
     * taking the effectively used intensities into account the range 1024...2048 would be converted
     * to a range of 64..128 and other intensities would be unused. Analysing the images allows to
     * convert 1024...2048 range to the full 0..255 range.
     * </p>
     */
    public void setComputeCommonIntensityRangeOfAllImagesForAllChannels()
    {
        this.computeCommonIntensityRangeOfAllImagesForChannelsOrNull = Collections.emptyList();
    }

    /**
     * See {@link #setComputeCommonIntensityRangeOfAllImagesForAllChannels()}.
     * 
     * @param channelCodesOrNull list of channel codes for which the optimal intensity rescaling
     *            parameters will be computed. If empty all channels will be analysed. If null
     *            nothing will be analysed (default behavior).
     */
    public void setComputeCommonIntensityRangeOfAllImagesForChannels(String[] channelCodesOrNull)
    {
        this.computeCommonIntensityRangeOfAllImagesForChannelsOrNull =
                Arrays.asList(channelCodesOrNull);
    }

    /**
     * Sets the threshold of intensities which should be ignored when computing common intensity
     * range of all images. By default equal to 0.5%. Note that
     * {@link #setComputeCommonIntensityRangeOfAllImagesForAllChannels()} or
     * {@link #setComputeCommonIntensityRangeOfAllImagesForChannels(String[])} has to be called to
     * switch on analysis.
     * 
     * @param threshold value from 0 to 1. If set to e.g. 0.1 then 10% of brightest and darkest
     *            pixels will be ignored.
     */
    public void setComputeCommonIntensityRangeOfAllImagesThreshold(float threshold)
    {
        this.computeCommonIntensityRangeOfAllImagesThreshold = threshold;
    }

    /** Should all dataset in one experiment use the same channels? By default set to false. */
    public void setStoreChannelsOnExperimentLevel(boolean storeChannelsOnExperimentLevel)
    {
        this.storeChannelsOnExperimentLevel = storeChannelsOnExperimentLevel;
    }

    /**
     * Should the original data be stored in the original form or should we pack them into one
     * container? Available values are {@link OriginalDataStorageFormat#UNCHANGED},
     * {@link OriginalDataStorageFormat#HDF5}, {@link OriginalDataStorageFormat#HDF5_COMPRESSED}.
     * The default is {@link OriginalDataStorageFormat#UNCHANGED}.
     */
    public void setOriginalDataStorageFormat(OriginalDataStorageFormat originalDataStorageFormat)
    {
        this.originalDataStorageFormat = originalDataStorageFormat;
    }

    /**
     * Sets parameters for the 'convert' command line tool, which will be used to apply an image
     * transformation on the fly when an image is fetched.
     */
    public void setConvertTransformationCliArguments(String convertTransformationCliArguments)
    {
        this.convertTransformationCliArgumentsOrNull = convertTransformationCliArguments;
    }

    /**
     * Which image library and reader should be used to read the image? <br>
     * Available libraries [readers]:<br>
     * - IJ: [tiff]<br>
     * - ImageIO: [jpg, bmp, jpeg, wbmp, png, gif]<br>
     * - JAI: [pnm, jpeg, fpx, gif, tiff, wbmp, png, bmp]<br>
     * - BioFormats: [ZipReader, APNGReader, JPEGReader, PGMReader, FitsReader, PCXReader,
     * GIFReader, BMPReader, IPLabReader, IvisionReader, DeltavisionReader, MRCReader, GatanReader,
     * GatanDM2Reader, ImarisReader, OpenlabRawReader, OMEXMLReader, LIFReader, AVIReader,
     * PictReader, SDTReader, EPSReader, SlidebookReader, AliconaReader, MNGReader, KhorosReader,
     * VisitechReader, LIMReader, PSDReader, InCellReader, L2DReader, FEIReader, NAFReader,
     * MINCReader, QTReader, MRWReader, TillVisionReader, ARFReader, CellomicsReader, LiFlimReader,
     * TargaReader, OxfordInstrumentsReader, VGSAMReader, HISReader, WATOPReader, SeikoReader,
     * TopometrixReader, UBMReader, QuesantReader, BioRadGelReader, RHKReader,
     * MolecularImagingReader, CellWorxReader, Ecat7Reader, VarianFDFReader, AIMReader, FakeReader,
     * JEOLReader, NiftiReader, AnalyzeReader, APLReader, NRRDReader, ICSReader, PerkinElmerReader,
     * AmiraReader, ScanrReader, BDReader, UnisokuReader, PDSReader, BioRadReader, FV1000Reader,
     * ZeissZVIReader, IPWReader, ND2Reader, JPEG2000Reader, PCIReader, ImarisHDFReader,
     * ZeissLSMReader, SEQReader, GelReader, ImarisTiffReader, FlexReader, SVSReader, ImaconReader,
     * LEOReader, JPKReader, MIASReader, TCSReader, LeicaReader, NikonReader, FluoviewReader,
     * PrairieReader, MetamorphReader, MicromanagerReader, ImprovisionTiffReader,
     * MetamorphTiffReader, NikonTiffReader, OMETiffReader, PhotoshopTiffReader, FEITiffReader,
     * SimplePCITiffReader, NikonElementsTiffReader, TiffDelegateReader, TextReader, BurleighReader,
     * OpenlabReader, DicomReader, SMCameraReader, SBIGReader]
     */
    public void setImageLibrary(String imageLibraryName, String readerName)
    {
        this.imageLibraryInfoOrNull = new ImageLibraryInfo(imageLibraryName, readerName);
    }

    /**
     * Sets the image library to be used for reading images. Available libraries are: IJ, ImageIO,
     * JAI, and BioFormats. The first image file is used to determine the actual reader. Note, that
     * all images are read with the same image reader.
     */
    public void setImageLibrary(String imageLibraryName)
    {
        this.imageLibraryInfoOrNull = new ImageLibraryInfo(imageLibraryName);
    }

    // --- predefined image dataset types

    /**
     * Sets dataset type to the one which should be used for storing raw images. Marks the dataset
     * as a "measured" one.
     */
    public void setRawImageDatasetType()
    {
        setDataSetType(ScreeningConstants.DEFAULT_RAW_IMAGE_DATASET_TYPE);
        setMeasuredData(true);
    }

    /**
     * Sets dataset type to the one which should be used for storing overview images generated from
     * raw images. Marks the dataset as a "derived" one.
     */
    public void setOverviewImageDatasetType()
    {
        setDataSetType(ScreeningConstants.DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE);
        setMeasuredData(false);
    }

    /**
     * Sets dataset type to the one which should be used for storing overlay images. Marks the
     * dataset as a "derived" one.
     */
    public void setSegmentationImageDatasetType()
    {
        setDataSetType(ScreeningConstants.DEFAULT_SEGMENTATION_IMAGE_DATASET_TYPE);
        setMeasuredData(false);
    }

    // --- standard

    /** Sets the type of the dataset. */
    public void setDataSetType(String datasetTypeCode)
    {
        this.datasetTypeCode = datasetTypeCode;
    }

    /** Sets the file type of the dataset. */
    public void setFileFormatType(String fileFormatCode)
    {
        this.fileFormatCode = fileFormatCode;
    }

    /**
     * Set whether the data is measured or not. By default false.
     */
    public void setMeasuredData(boolean isMeasured)
    {
        this.isMeasured = isMeasured;
    }

    /**
     * Sets the microscopy flag which is by default <code>false</code>. This flag is used to check
     * whether well in {@link ImageMetadata} is specified or not. In case of microscopy well is
     * ignored. Otherwise it is mandatory.
     */
    public void setMicroscopyData(boolean isMicroscopy)
    {
        this.isMicroscopy = isMicroscopy;
    }

    public String getDataSetType()
    {
        return datasetTypeCode;
    }

    public String getFileFormatType()
    {
        return fileFormatCode;
    }

    public boolean isMeasuredData()
    {
        return isMeasured;
    }

    public boolean isMicroscopyData()
    {
        return isMicroscopy;
    }

}
