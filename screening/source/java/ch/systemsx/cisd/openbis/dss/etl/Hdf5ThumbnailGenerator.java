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
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailFilePaths;
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
    /**
     * Generates thumbnails of specified images whose paths are relative to the specified image
     * parent directory.
     * 
     * @param images path to the images relative to the imagesParentDirectory
     * @param thumbnailFilePath absolute path to the file where thumbnails will be saved
     * @param imageStorageConfiguraton describes how the thumbnails should be generated
     * @return null if thumbnails generation was not requested
     */
    public static ThumbnailFilePaths tryGenerateThumbnails(List<RelativeImageFile> images,
            File imagesParentDirectory, String thumbnailFilePath,
            ImageStorageConfiguraton imageStorageConfiguraton,
            String thumbnailPhysicalDatasetPermId,
            ThumbnailsStorageFormat thumbnailsStorageFormatOrNull)
    {
        if (thumbnailsStorageFormatOrNull != null)
        {
            ThumbnailFilePaths thumbnailPaths =
                    new ThumbnailFilePaths(thumbnailPhysicalDatasetPermId);

            File thumbnailsFile = new File(thumbnailFilePath);
            final String relativeThumbnailFilePath = thumbnailsFile.getName();

            HDF5Container container = new HDF5Container(thumbnailsFile);
            ImageLibraryInfo imageLibrary = imageStorageConfiguraton.tryGetImageLibrary();
            Hdf5ThumbnailGenerator thumbnailsGenerator =
                    new Hdf5ThumbnailGenerator(images, imagesParentDirectory,
                            thumbnailsStorageFormatOrNull, imageLibrary, relativeThumbnailFilePath,
                            thumbnailPaths, operationLog);
            container.runWriterClient(thumbnailsStorageFormatOrNull.isStoreCompressed(),
                    thumbnailsGenerator);
            return thumbnailPaths;
        } else
        {
            return null;
        }
    }

    private static final File convertUtilityOrNull = OSUtilities.findExecutable("convert");

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            Hdf5ThumbnailGenerator.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            Hdf5ThumbnailGenerator.class);

    private static final int MAX_RETRY_OF_FAILED_GENERATION = 3;

    private final List<RelativeImageFile> images;

    private final File imagesParentDirectory;

    private final ThumbnailsStorageFormat thumbnailsStorageFormat;

    private final ImageLibraryInfo imageLibraryOrNull;

    private final String relativeThumbnailFilePath;

    private final ThumbnailFilePaths thumbnailPathCollector;

    private final Logger logger;

    private Hdf5ThumbnailGenerator(List<RelativeImageFile> images, File imagesParentDirectory,
            ThumbnailsStorageFormat thumbnailsStorageFormat, ImageLibraryInfo imageLibraryOrNull,
            String relativeThumbnailFilePath, ThumbnailFilePaths thumbnailPathCollector,
            Logger operationLog)
    {
        this.images = images;
        this.imagesParentDirectory = imagesParentDirectory;
        this.thumbnailsStorageFormat = thumbnailsStorageFormat;
        this.imageLibraryOrNull = imageLibraryOrNull;
        this.relativeThumbnailFilePath = relativeThumbnailFilePath;
        this.thumbnailPathCollector = thumbnailPathCollector;
        this.logger = operationLog;
    }

    /**
     * @param bufferOutputStream auxiliary stream which can be used as a temporary buffer to save
     *            the thumbnail. Using it allows not to allocate memory each time when a thumbnail
     *            is generated.
     */
    private Status generateThumbnail(IHDF5ContainerWriter writer, RelativeImageFile image,
            ByteArrayOutputStream bufferOutputStream)
    {
        String imagePath = image.getImageRelativePath();
        String thumbnailPath = createThumbnailPath(image);
        File img = new File(imagesParentDirectory, imagePath);

        try
        {
            long start = System.currentTimeMillis();
            String imageIdOrNull = image.tryGetImageID();
            byte[] byteArray = generateThumbnail(bufferOutputStream, img, imageIdOrNull);
            String path =
                    relativeThumbnailFilePath + AbstractImageStorageProcessor.ARCHIVE_DELIMITER
                            + thumbnailPath;
            thumbnailPathCollector.saveThumbnailPath(image, path);

            if (logger.isDebugEnabled())
            {
                long now = System.currentTimeMillis();
                logger.debug(Thread.currentThread().getName() + " thumbnail " + thumbnailPath
                        + " (" + byteArray.length + " bytes) generated in " + (now - start)
                        + " msec");
            }
            synchronized (writer)
            {
                writer.writeToHDF5Container(thumbnailPath, new ByteArrayInputStream(byteArray),
                        byteArray.length);
            }
        } catch (IOException ex)
        {
            return createStatus(thumbnailPath, ex);
        }
        return Status.OK;
    }

    private String createThumbnailPath(RelativeImageFile plateImage)
    {
        String imagePath = plateImage.getImageRelativePath();
        String newImagePath = imagePath;
        int lastIndex = imagePath.lastIndexOf('.');
        if (lastIndex > 0)
        {
            newImagePath = imagePath.substring(0, lastIndex);
        }
        String imageIdOrNull = plateImage.tryGetImageID();
        if (imageIdOrNull != null)
        {
            newImagePath += "_" + imageIdOrNull;
        }
        newImagePath += ".png";
        return newImagePath;
    }

    private byte[] generateThumbnail(ByteArrayOutputStream bufferOutputStream, File img,
            String imageIdOrNull) throws IOException
    {
        byte[] byteArray;
        if (thumbnailsStorageFormat.isGenerateWithImageMagic())
        {
            byteArray = generateThumbnailWithImageMagic(img);
        } else
        {
            byteArray = generateThumbnailInternally(img, imageIdOrNull, bufferOutputStream);
        }
        return byteArray;
    }

    private byte[] generateThumbnailWithImageMagic(File imageFile) throws IOException
    {
        String size =
                thumbnailsStorageFormat.getMaxWidth() + "x"
                        + thumbnailsStorageFormat.getMaxHeight();

        if (thumbnailsStorageFormat.getZoomLevel() != null)
        {
            Dimension originalSize = loadUnchangedImageDimension(imageFile, null);
            long x =
                    (int) Math.round(thumbnailsStorageFormat.getZoomLevel()
                            * originalSize.getWidth());
            long y =
                    (int) Math.round(thumbnailsStorageFormat.getZoomLevel()
                            * originalSize.getHeight());
            size = x + "x" + y;
        }

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
            return result.getBinaryOutput();
        }
    }

    private byte[] generateThumbnailInternally(File imageFile, String imageIdOrNull,
            ByteArrayOutputStream bufferOutputStream) throws IOException
    {
        BufferedImage image = loadUnchangedImage(imageFile, imageIdOrNull);

        long x = thumbnailsStorageFormat.getMaxWidth();
        long y = thumbnailsStorageFormat.getMaxHeight();
        if (thumbnailsStorageFormat.getZoomLevel() != null)
        {
            x = Math.round(thumbnailsStorageFormat.getZoomLevel() * image.getWidth());
            y = Math.round(thumbnailsStorageFormat.getZoomLevel() * image.getHeight());
        }

        BufferedImage thumbnail =
                ImageUtil.rescale(image, (int) x, (int) y, false,
                        thumbnailsStorageFormat.isHighQuality());
        ImageUtil.writeImageToPng(thumbnail, bufferOutputStream);
        return bufferOutputStream.toByteArray();
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

    private ITaskExecutor<RelativeImageFile> createThumbnailGenerator(
            final IHDF5ContainerWriter writer)
    {
        return new ITaskExecutor<RelativeImageFile>()
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

                public Status execute(RelativeImageFile image)
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
        Collection<FailureRecord<RelativeImageFile>> errors =
                ParallelizedExecutor.process(images, createThumbnailGenerator(writer),
                        thumbnailsStorageFormat.getAllowedMachineLoadDuringGeneration(), 100,
                        "Thumbnails generation", MAX_RETRY_OF_FAILED_GENERATION, true);
        if (errors.size() > 0)
        {
            throw new IllegalStateException(
                    String.format(
                            "There were errors when generating %d thumbnails, the whole thumbnails generation process fails.",
                            errors.size()));
        }
    }
}
