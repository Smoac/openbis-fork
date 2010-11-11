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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
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
    private final List<AcquiredPlateImage> plateImages;

    private final File imagesInStoreFolder;

    private final int thumbnailMaxWidth;

    private final int thumbnailMaxHeight;

    private final String relativeThumbnailFilePath;

    private final Logger operationLog;

    Hdf5ThumbnailGenerator(List<AcquiredPlateImage> plateImages, File imagesInStoreFolder,
            int thumbnailMaxWidth, int thumbnailMaxHeight, String relativeThumbnailFilePath,
            Logger operationLog)
    {
        this.plateImages = plateImages;
        this.imagesInStoreFolder = imagesInStoreFolder;
        this.thumbnailMaxWidth = thumbnailMaxWidth;
        this.thumbnailMaxHeight = thumbnailMaxHeight;
        this.relativeThumbnailFilePath = relativeThumbnailFilePath;
        this.operationLog = operationLog;
    }

    public void runWithSimpleWriter(IHDF5SimpleWriter writer)
    {

        for (AcquiredPlateImage plateImage : plateImages)
        {
            RelativeImageReference imageReference = plateImage.getImageReference();
            String imagePath = imageReference.getRelativeImagePath();
            File img = new File(imagesInStoreFolder, imagePath);
            BufferedImage image = ImageUtil.loadImage(img);
            BufferedImage thumbnail =
                    ImageUtil.createThumbnail(image, thumbnailMaxWidth, thumbnailMaxHeight);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try
            {
                ImageIO.write(thumbnail, "png", output);
                int lastIndex = imagePath.lastIndexOf('.');
                if (lastIndex > 0)
                {
                    imagePath = imagePath.substring(0, lastIndex);
                }
                imagePath += ".png";
                String path =
                        relativeThumbnailFilePath + ContentRepository.ARCHIVE_DELIMITER + imagePath;
                plateImage.setThumbnailFilePathOrNull(new RelativeImageReference(path,
                        imageReference.tryGetPage(), imageReference.tryGetColorComponent()));
                byte[] byteArray = output.toByteArray();
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("thumbnail " + imagePath + " (" + byteArray.length
                            + " bytes)");
                }
                writer.writeByteArray(imagePath, byteArray);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

    }

}
