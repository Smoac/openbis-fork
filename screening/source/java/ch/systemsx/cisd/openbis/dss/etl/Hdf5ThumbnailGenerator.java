/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.FailureRecord;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.hdf5.HDF5Container;
import ch.systemsx.cisd.common.hdf5.HDF5Container.IHDF5WriterClient;
import ch.systemsx.cisd.common.hdf5.IHDF5ContainerWriter;
import ch.systemsx.cisd.common.io.FileBasedContentNode;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.RelativeImageFile;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailsInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageIdentifier;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageStorageConfiguraton;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Utility class for generating thumbnails into an HDF5 container.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class Hdf5ThumbnailGenerator implements IHDF5WriterClient
{
    private static class ThumbnailData
    {
        private final byte[] data;

        private final int width;

        private final int height;

        private ThumbnailData(byte[] data, int width, int height)
        {
            this.data = data;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Generates thumbnails of specified images whose paths are relative to the specified image
     * parent directory.
     * 
     * @param imageDataSetStructure the images dataset structure, including paths relative to the
     *            imagesParentDirectory
     * @param thumbnailFilePath absolute path to the file where thumbnails will be saved
     * @param imageStorageConfiguraton describes how the thumbnails should be generated
     */
    public static void tryGenerateThumbnails(ImageDataSetStructure imageDataSetStructure,
            File imagesParentDirectory, String thumbnailFilePath,
            ImageStorageConfiguraton imageStorageConfiguraton,
            String thumbnailPhysicalDatasetPermId,
            ThumbnailsStorageFormat thumbnailsStorageFormatOrNull, ThumbnailsInfo thumbnailPaths)
    {
        if (thumbnailsStorageFormatOrNull != null)
        {
            thumbnailPaths.putDataSet(thumbnailPhysicalDatasetPermId,
                    thumbnailsStorageFormatOrNull.getThumbnailsFileName());
            File thumbnailsFile = new File(thumbnailFilePath);

            HDF5Container container = new HDF5Container(thumbnailsFile);
            ImageLibraryInfo imageLibrary = imageStorageConfiguraton.tryGetImageLibrary();
            Hdf5ThumbnailGenerator thumbnailsGenerator =
                    new Hdf5ThumbnailGenerator(imageDataSetStructure, imagesParentDirectory,
                            thumbnailPhysicalDatasetPermId, thumbnailsStorageFormatOrNull,
                            imageLibrary, thumbnailPaths, operationLog);
            container.runWriterClient(thumbnailsStorageFormatOrNull.isStoreCompressed(),
                    thumbnailsGenerator);
        }
    }

    private static final File convertUtilityOrNull = OSUtilities.findExecutable("convert");

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            Hdf5ThumbnailGenerator.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            Hdf5ThumbnailGenerator.class);

    private static final int MAX_RETRY_OF_FAILED_GENERATION = 3;

    private final ImageDataSetStructure imageDataSetStructure;

    private final File imagesParentDirectory;

    private String thumbnailPhysicalDatasetPermId;

    private final ThumbnailsStorageFormat thumbnailsStorageFormat;

    private final ImageLibraryInfo imageLibraryOrNull;

    private final ThumbnailsInfo thumbnailPathCollector;

    private final Logger logger;

    private Hdf5ThumbnailGenerator(ImageDataSetStructure imageDataSetStructure,
            File imagesParentDirectory, String thumbnailPhysicalDatasetPermId,
            ThumbnailsStorageFormat thumbnailsStorageFormat, ImageLibraryInfo imageLibraryOrNull,
            ThumbnailsInfo thumbnailPathCollector, Logger operationLog)
    {
        this.imageDataSetStructure = imageDataSetStructure;
        this.imagesParentDirectory = imagesParentDirectory;
        this.thumbnailPhysicalDatasetPermId = thumbnailPhysicalDatasetPermId;
        this.thumbnailsStorageFormat = thumbnailsStorageFormat;
        this.imageLibraryOrNull = imageLibraryOrNull;
        this.thumbnailPathCollector = thumbnailPathCollector;
        this.logger = operationLog;
    }

    /**
     * @param bufferOutputStream auxiliary stream which can be used as a temporary buffer to save
     *            the thumbnail. Using it allows not to allocate memory each time when a thumbnail
     *            is generated.
     */
    private Status generateThumbnail(IHDF5ContainerWriter writer, ImageFileInfo image,
            ByteArrayOutputStream bufferOutputStream)
    {
        String imagePath = image.getImageRelativePath();
        String thumbnailPath = createThumbnailPath(image);
        File img = new File(imagesParentDirectory, imagePath);

        try
        {
            long start = System.currentTimeMillis();
            String imageIdOrNull = tryExtractImageID(image);
            ThumbnailData thumbnailData = generateThumbnail(bufferOutputStream, img, imageIdOrNull);
            thumbnailPathCollector.saveThumbnailPath(thumbnailPhysicalDatasetPermId,
                    RelativeImageFile.create(image), thumbnailPath, thumbnailData.width,
                    thumbnailData.height);

            if (logger.isDebugEnabled())
            {
                long now = System.currentTimeMillis();
                logger.debug(Thread.currentThread().getName() + " thumbnail " + thumbnailPath
                        + " (" + thumbnailData.data.length + " bytes) generated in "
                        + (now - start) + " msec");
            }
            synchronized (writer)
            {
                writer.writeToHDF5Container(thumbnailPath, new ByteArrayInputStream(
                        thumbnailData.data), thumbnailData.data.length);
            }
        } catch (IOException ex)
        {
            return createStatus(thumbnailPath, ex);
        }
        return Status.OK;
    }

    private String createThumbnailPath(ImageFileInfo plateImage)
    {
        String imagePath = plateImage.getImageRelativePath();
        String newImagePath = imagePath;
        int lastIndex = imagePath.lastIndexOf('.');
        if (lastIndex > 0)
        {
            newImagePath = imagePath.substring(0, lastIndex);
        }

        String imageIdOrNull = tryExtractImageID(plateImage);
        if (imageIdOrNull != null)
        {
            newImagePath += "_" + imageIdOrNull;
        }
        newImagePath += ".png";
        return newImagePath;
    }

    private static String tryExtractImageID(ImageFileInfo image)
    {
        ImageIdentifier imageIdentifier = image.tryGetImageIdentifier();
        return imageIdentifier == null ? null : imageIdentifier.getUniqueStringIdentifier();
    }

    private ThumbnailData generateThumbnail(ByteArrayOutputStream bufferOutputStream, File img,
            String imageIdOrNull) throws IOException
    {
        ThumbnailData thumbnailData;
        if (thumbnailsStorageFormat.isGenerateWithImageMagic())
        {
            thumbnailData = generateThumbnailWithImageMagic(img);
        } else
        {
            thumbnailData = generateThumbnailInternally(img, imageIdOrNull, bufferOutputStream);
        }
        return thumbnailData;
    }

    private ThumbnailData generateThumbnailWithImageMagic(File imageFile) throws IOException
    {
        int width = thumbnailsStorageFormat.getMaxWidth();
        int height = thumbnailsStorageFormat.getMaxHeight();

        if (thumbnailsStorageFormat.getZoomLevel() != null)
        {
            Dimension originalSize = loadUnchangedImageDimension(imageFile, null);
            double zoomLevel = thumbnailsStorageFormat.getZoomLevel();
            width = (int) Math.round(zoomLevel * originalSize.getWidth());
            height = (int) Math.round(zoomLevel * originalSize.getHeight());
        }
        String size = width + "x" + height;

        String imageFilePath = imageFile.getPath();
        List<String> params = new ArrayList<String>();
        params.addAll(Arrays.asList(convertUtilityOrNull.getPath(), imageFilePath, "-scale", size));
        List<String> additionalParams = thumbnailsStorageFormat.getImageMagicParams();
        if (additionalParams != null)
        {
            params.addAll(additionalParams);
        }
        params.add("png:-");
        final ProcessResult result =
                ProcessExecutionHelper.run(params, logger, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.BINARY_DISCARD_STDERR_IO_STRATEGY, false);
        if (result.isOK() == false)
        {
            throw new IOException(String.format(
                    "Error calling 'convert' for image '%s'. Exit value: %d, I/O status: %s",
                    imageFilePath, result.getExitValue(), result.getProcessIOResult().getStatus()));
        } else
        {
            return new ThumbnailData(result.getBinaryOutput(), width, height);
        }
    }

    private ThumbnailData generateThumbnailInternally(File imageFile, String imageIdOrNull,
            ByteArrayOutputStream bufferOutputStream) throws IOException
    {
        BufferedImage image = loadUnchangedImage(imageFile, imageIdOrNull);

        int widht = thumbnailsStorageFormat.getMaxWidth();
        int height = thumbnailsStorageFormat.getMaxHeight();
        if (thumbnailsStorageFormat.getZoomLevel() != null)
        {
            widht = (int) Math.round(thumbnailsStorageFormat.getZoomLevel() * image.getWidth());
            height = (int) Math.round(thumbnailsStorageFormat.getZoomLevel() * image.getHeight());
        }

        BufferedImage thumbnail =
                ImageUtil.rescale(image, widht, height, false,
                        thumbnailsStorageFormat.isHighQuality());
        ImageUtil.writeImageToPng(thumbnail, bufferOutputStream);
        return new ThumbnailData(bufferOutputStream.toByteArray(), thumbnail.getWidth(),
                thumbnail.getHeight());
    }

    private BufferedImage loadUnchangedImage(File imageFile, String imageIdOrNull)
    {
        return AbsoluteImageReference.loadUnchangedImage(new FileBasedContentNode(imageFile),
                imageIdOrNull, imageLibraryOrNull);
    }

    private Dimension loadUnchangedImageDimension(File imageFile, String imageIdOrNull)
    {
        return AbsoluteImageReference.loadUnchangedImageDimension(new FileBasedContentNode(
                imageFile), imageIdOrNull, imageLibraryOrNull);
    }

    private Status createStatus(String thumbnailPath, IOException ex)
    {
        logger.warn("Retriable error when creating thumbnail '" + thumbnailPath + "'", ex);
        return Status.createRetriableError(String.format("Could not generate a thumbnail '%s': %s",
                thumbnailPath, ex.getMessage()));
    }

    private ITaskExecutor<ImageFileInfo> createThumbnailGenerator(final IHDF5ContainerWriter writer)
    {
        return new ITaskExecutor<ImageFileInfo>()
            {
                private ThreadLocal<ByteArrayOutputStream> outputStreamBuffers =
                        new ThreadLocal<ByteArrayOutputStream>()
                            {
                                @Override
                                protected ByteArrayOutputStream initialValue()
                                {
                                    return new ByteArrayOutputStream();
                                }
                            };

                public Status execute(ImageFileInfo image)
                {
                    // each thread will get its own buffer to avoid allocating memory for the
                    // internal array each time
                    ByteArrayOutputStream outputStreamBuffer = outputStreamBuffers.get();
                    outputStreamBuffer.reset();
                    return generateThumbnail(writer, image, outputStreamBuffer);
                }
            };
    }

    public void runWithSimpleWriter(IHDF5ContainerWriter writer)
    {
        final String thumbnailsName = " (" + thumbnailsStorageFormat.getThumbnailsFileName() + ")";

        Collection<FailureRecord<ImageFileInfo>> errors =
                ParallelizedExecutor.process(imageDataSetStructure.getImages(),
                        createThumbnailGenerator(writer),
                        thumbnailsStorageFormat.getAllowedMachineLoadDuringGeneration(), 100,
                        "Thumbnails generation" + thumbnailsName, MAX_RETRY_OF_FAILED_GENERATION,
                        true);
        if (errors.size() > 0)
        {
            throw new IllegalStateException(String.format(
                    "There were errors when generating %d thumbnails" + thumbnailsName
                            + ", the whole thumbnails generation process fails.", errors.size()));
        }
    }
}
