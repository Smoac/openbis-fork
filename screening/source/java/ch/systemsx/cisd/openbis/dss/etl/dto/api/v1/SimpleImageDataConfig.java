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

import java.util.List;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations.ConvertToolImageTransformerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Allows to configure extraction of images for a plate or microscopy sample.
 * 
 * @author Tomasz Pylak
 */
abstract public class SimpleImageDataConfig
{
    // --- one of the following two methods have to be overridden -----------------

    /**
     * Extracts tile number, channel code and well code for a given relative path to an image.
     * <p>
     * Will be called for each file found in the incoming directory which has the extension returned
     * by {@link #getRecognizedImageExtensions()}.
     * </p>
     */
    public ImageMetadata extractImageMetadata(String imagePath)
    {
        throw new UnsupportedOperationException(
                "One of the extractImageMetadata() methods has to be implemented.");
    }

    /**
     * Returns meta-data for each image contained in specified image file path. This method returns
     * just the {@link ImageMetadata} object returned by {@link #extractImageMetadata(String)}.
     * <p>
     * In case of a image container file format (like multi-page TIFF) this method should
     * overridden. 
     * 
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
     * By default rhe channel label will be equal to the code. Channel color returned by
     * {@link #getChannelColor(String)} will be used.
     */
    public Channel createChannel(String channelCode)
    {
        ChannelColor channelColor = getChannelColor(channelCode.toUpperCase());
        return new Channel(channelCode, channelCode, channelColor);
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

    private boolean generateThumbnailsWithImageMagic = false;

    private boolean generateThumbnailsInHighQuality = false;

    private double allowedMachineLoadDuringThumbnailsGeneration = 1.0;

    private boolean storeChannelsOnExperimentLevel = false;

    private OriginalDataStorageFormat originalDataStorageFormat =
            OriginalDataStorageFormat.UNCHANGED;

    private String convertTransformationCliArgumentsOrNull;

    private ImageLibraryInfo imageLibraryInfoOrNull;

    private boolean isMicroscopy;

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
     * if true ImageMagic 'convert' utility will be used to generate thumbnails. It should be
     * installed and accessible.
     */
    public void setUseImageMagicToGenerateThumbnails(boolean generateWithImageMagic)
    {
        this.generateThumbnailsWithImageMagic = generateWithImageMagic;
    }

    /**
     * if true and thumbnails generation is switched on, thumbnails will be generated with high
     * quality.
     */
    public void setGenerateHighQualityThumbnails(boolean highQualityThumbnails)
    {
        this.generateThumbnailsInHighQuality = highQualityThumbnails;
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
     * transformation before persisting the images in the data store.
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
