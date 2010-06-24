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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDTO;

/**
 * {@link HCSDatasetLoader} extension with code for handling images.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class HCSImageDatasetLoader extends HCSDatasetLoader implements IHCSImageDatasetLoader
{
    private final IContentRepository contentRepository;

    public HCSImageDatasetLoader(IImagingQueryDAO query, String datasetPermId,
            IContentRepository contentRepository)
    {
        super(query, datasetPermId);
        this.contentRepository = contentRepository;
    }

    /**
     * @param chosenChannel start from 1
     * @return image (with absolute path, page and color)
     */
    public AbsoluteImageReference tryGetImage(String chosenChannel, Location wellLocation,
            Location tileLocation, Size thumbnailSizeOrNull)
    {
        assert StringUtils.isBlank(chosenChannel) == false;
        assert tileLocation.getX() <= getDataset().getFieldNumberOfColumns();
        assert tileLocation.getY() <= getDataset().getFieldNumberOfRows();
        assert wellLocation.getX() <= getContainer().getNumberOfColumns();
        assert wellLocation.getY() <= getContainer().getNumberOfRows();

        Long chosenChannelId =
                query.tryGetChannelIdByChannelNameDatasetIdOrExperimentId(getDataset().getId(),
                        getContainer().getExperimentId(), chosenChannel);
        if (chosenChannelId == null)
        {
            return null;
        }

        ImgImageDTO imageDTO;
        IContent content = null;
        if (thumbnailSizeOrNull != null)
        {
            imageDTO =
                    query.tryGetThumbnail(chosenChannelId, getDataset().getId(), tileLocation,
                            wellLocation);
            if (imageDTO == null)
            {
                imageDTO =
                        query.tryGetImage(chosenChannelId, getDataset().getId(), tileLocation,
                                wellLocation);
                if (imageDTO != null)
                {
                    content =
                            new ThumbnailContent(contentRepository.getContent(imageDTO
                                    .getFilePath()), thumbnailSizeOrNull);
                }
            } else
            {
                content = contentRepository.getContent(imageDTO.getFilePath());
            }
        } else
        {
            imageDTO =
                    query.tryGetImage(chosenChannelId, getDataset().getId(), tileLocation,
                            wellLocation);
            if (imageDTO != null)
            {
                content = contentRepository.getContent(imageDTO.getFilePath());
            }
        }
        if (content != null && imageDTO != null)
        {
            return new AbsoluteImageReference(content, imageDTO.getPage(), imageDTO
                    .getColorComponent());
        } else
        {
            return null;
        }
    }

    private static final class ThumbnailContent implements IContent
    {
        private final IContent content;

        private final byte[] thumbnailBytes;

        ThumbnailContent(IContent content, Size size)
        {
            this.content = content;
            InputStream inputStream = content.getInputStream();
            BufferedImage image = ImageUtil.loadImage(inputStream);
            BufferedImage thumbnail =
                    ImageUtil.createThumbnail(image, size.getWidth(), size.getHeight());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try
            {
                ImageIO.write(thumbnail, "png", output);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            thumbnailBytes = output.toByteArray();
        }

        public boolean exists()
        {
            return content.exists();
        }

        public InputStream getInputStream()
        {
            return new ByteArrayInputStream(thumbnailBytes);
        }

        public String getName()
        {
            return content.getName();
        }

        public long getSize()
        {
            return 0;
        }

        public String getUniqueId()
        {
            return content.getUniqueId();
        }
    }

}