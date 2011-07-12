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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.FailureRecord;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.hdf5.Hdf5Container.IHdf5WriterClient;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Utility class for generating thumbnails into an HDF5 container.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class Hdf5ThumbnailGenerator implements IHdf5WriterClient
{
    private static final File convertUtilityOrNull = OSUtilities.findExecutable("convert");

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            Hdf5ThumbnailGenerator.class);

    private static final int MAX_RETRY_OF_FAILED_GENERATION = 3;

    private final List<AcquiredSingleImage> plateImages;

    private final File imagesInStoreFolder;

    private final ThumbnailsStorageFormat thumbnailsStorageFormat;

    private final ImageLibraryInfo imageLibraryOrNull;

    private final String relativeThumbnailFilePath;

    private final Logger operationLog;

    Hdf5ThumbnailGenerator(List<AcquiredSingleImage> plateImages, File imagesInStoreFolder,
            ThumbnailsStorageFormat thumbnailsStorageFormat, ImageLibraryInfo imageLibraryOrNull,
            String relativeThumbnailFilePath, Logger operationLog)
    {
        this.plateImages = plateImages;
        this.imagesInStoreFolder = imagesInStoreFolder;
        this.thumbnailsStorageFormat = thumbnailsStorageFormat;
        this.imageLibraryOrNull = imageLibraryOrNull;
        this.relativeThumbnailFilePath = relativeThumbnailFilePath;
        this.operationLog = operationLog;
    }

    /**
     * @param bufferOutputStream auxiliary stream which can be used as a temporary buffer to save
     *            the thumbnail. Using it allows not to allocate memory each time when a thumbnail
     *            is generated.
     */
    private Status generateThumbnail(IHDF5SimpleWriter writer, AcquiredSingleImage plateImage,
            ByteArrayOutputStream bufferOutputStream)
    {
        RelativeImageReference imageReference = plateImage.getImageReference();
        String imagePath = imageReference.getRelativeImagePath();
        String thumbnailPath = createThumbnailPath(imageReference);
        File img = new File(imagesInStoreFolder, imagePath);

        try
        {
            long start = System.currentTimeMillis();
            String imageIdOrNull = imageReference.tryGetImageID();
            byte[] byteArray = generateThumbnail(bufferOutputStream, img, imageIdOrNull);
            String path =
                    relativeThumbnailFilePath + ContentRepository.ARCHIVE_DELIMITER + thumbnailPath;
            plateImage.setThumbnailFilePathOrNull(new RelativeImageReference(path, null,
                    imageReference.tryGetColorComponent()));

            if (operationLog.isDebugEnabled())
            {
                long now = System.currentTimeMillis();
                operationLog.debug(Thread.currentThread().getName() + " thumbnail " + thumbnailPath
                        + " (" + byteArray.length + " bytes) generated in " + (now - start)
                        + " msec");
            }
            synchronized (writer)
            {
                writer.writeByteArray(thumbnailPath, byteArray);
            }
        } catch (IOException ex)
        {
            return createStatus(thumbnailPath, ex);
        }
        return Status.OK;
    }

    public String createThumbnailPath(RelativeImageReference imageReference)
    {
        String imagePath = imageReference.getRelativeImagePath();
        String newImagePath = imagePath;
        int lastIndex = imagePath.lastIndexOf('.');
        if (lastIndex > 0)
        {
            newImagePath = imagePath.substring(0, lastIndex);
        }
        String imageIdOrNull = imageReference.tryGetImageID();
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
                ProcessExecutionHelper.run(params, operationLog, machineLog,
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
        BufferedImage image = loadImage(imageFile, imageIdOrNull);
        BufferedImage thumbnail =
                ImageUtil.rescale(image, thumbnailsStorageFormat.getMaxWidth(),
                        thumbnailsStorageFormat.getMaxHeight(), false,
                        thumbnailsStorageFormat.isHighQuality());
        ImageIO.write(thumbnail, "png", bufferOutputStream);
        return bufferOutputStream.toByteArray();
    }

    private BufferedImage loadImage(File imageFile, String imageIdOrNull)
    {
        // NOTE 2011-04-20, Tomasz Pylak: support paged tiffs when generating thumbnails
        return AbsoluteImageReference.loadImage(new FileBasedContent(imageFile), imageIdOrNull,
                imageLibraryOrNull);
    }

    private Status createStatus(String thumbnailPath, IOException ex)
    {
        operationLog.warn("Retriable error when creating thumbnail '" + thumbnailPath + "'", ex);
        return Status.createRetriableError(String.format("Could not generate a thumbnail '%s': %s",
                thumbnailPath, ex.getMessage()));
    }

    private ITaskExecutor<AcquiredSingleImage> createThumbnailGenerator(
            final IHDF5SimpleWriter writer)
    {
        return new ITaskExecutor<AcquiredSingleImage>()
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

                public Status execute(AcquiredSingleImage plateImage)
                {
                    // each thread will get its own buffer to avoid allocating memory for the
                    // internal array each time
                    ByteArrayOutputStream outputStreamBuffer = outputStreamBuffers.get();
                    outputStreamBuffer.reset();
                    return generateThumbnail(writer, plateImage, outputStreamBuffer);
                }
            };
    }

    public void runWithSimpleWriter(IHDF5SimpleWriter writer)
    {
        Collection<FailureRecord<AcquiredSingleImage>> errors =
                ParallelizedExecutor.process(plateImages, createThumbnailGenerator(writer),
                        thumbnailsStorageFormat.getAllowedMachineLoadDuringGeneration(), 100,
                        "Thumbnails generation", MAX_RETRY_OF_FAILED_GENERATION);
        if (errors.size() > 0)
        {
            throw new IllegalStateException(
                    String.format(
                            "There were errors when generating %d thumbnails, the whole thumbnails generation process fails.",
                            errors.size()));
        }
    }
}
