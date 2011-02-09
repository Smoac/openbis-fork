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
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.etlserver.hdf5.Hdf5Container.IHdf5WriterClient;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Utility class for generating thumbnails into an HDF5 container.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class Hdf5ThumbnailGenerator implements IHdf5WriterClient
{
    private final List<AcquiredSingleImage> plateImages;

    private final File imagesInStoreFolder;

    private final int thumbnailMaxWidth;

    private final int thumbnailMaxHeight;

    private final String relativeThumbnailFilePath;

    private final int allowedMachineLoadDuringGeneration;

    private final Logger operationLog;

    Hdf5ThumbnailGenerator(List<AcquiredSingleImage> plateImages, File imagesInStoreFolder,
            int thumbnailMaxWidth, int thumbnailMaxHeight, String relativeThumbnailFilePath,
            int allowedMachineLoadDuringGeneration, Logger operationLog)
    {
        this.plateImages = plateImages;
        this.imagesInStoreFolder = imagesInStoreFolder;
        this.thumbnailMaxWidth = thumbnailMaxWidth;
        this.thumbnailMaxHeight = thumbnailMaxHeight;
        this.relativeThumbnailFilePath = relativeThumbnailFilePath;
        this.allowedMachineLoadDuringGeneration = allowedMachineLoadDuringGeneration;
        this.operationLog = operationLog;
    }

    private Status generateThumbnail(IHDF5SimpleWriter writer, AcquiredSingleImage plateImage)
    {
        RelativeImageReference imageReference = plateImage.getImageReference();
        String imagePath = imageReference.getRelativeImagePath();
        File img = new File(imagesInStoreFolder, imagePath);
        BufferedImage image = ImageUtil.loadImage(img);
        BufferedImage thumbnail =
                ImageUtil.rescale(image, thumbnailMaxWidth, thumbnailMaxHeight, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(thumbnail, "png", output);
            String thumbnailPath = replaceExtensionToPng(imagePath);

            String path =
                    relativeThumbnailFilePath + ContentRepository.ARCHIVE_DELIMITER + thumbnailPath;
            plateImage.setThumbnailFilePathOrNull(new RelativeImageReference(path, imageReference
                    .tryGetPage(), imageReference.tryGetColorComponent()));
            byte[] byteArray = output.toByteArray();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("thumbnail " + thumbnailPath + " (" + byteArray.length
                        + " bytes)");
            }
            writer.writeByteArray(thumbnailPath, byteArray);
        } catch (IOException ex)
        {
            return Status.createError(ex.getMessage());
        }
        return Status.OK;
    }

    private static String replaceExtensionToPng(String imagePath)
    {
        String newImagePath = imagePath;
        int lastIndex = imagePath.lastIndexOf('.');
        if (lastIndex > 0)
        {
            newImagePath = imagePath.substring(0, lastIndex);
        }
        newImagePath += ".png";
        return newImagePath;
    }

    private ITaskExecutor<AcquiredSingleImage> createThumbnailGenerator(
            final IHDF5SimpleWriter writer)
    {
        return new ITaskExecutor<AcquiredSingleImage>()
            {
                public Status execute(AcquiredSingleImage plateImage)
                {
                    return generateThumbnail(writer, plateImage);
                }
            };
    }

    public void runWithSimpleWriter(IHDF5SimpleWriter writer)
    {
        ParallelizedExecutor.process(plateImages, createThumbnailGenerator(writer),
                allowedMachineLoadDuringGeneration, 100, 1);
    }
}
