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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.etl.IHCSDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Helper class for easy handling of HCS image dataset standard structure.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class HCSDatasetLoader implements IHCSDatasetLoader
{
    private final IImagingUploadDAO query;

    private final ImgDatasetDTO dataset;

    private final IContentRepository contentRepository;

    private ImgContainerDTO container;

    private Integer channelCount;

    private List<String> channelNames;

    public HCSDatasetLoader(IImagingUploadDAO query, String datasetPermId,
            IContentRepository contentRepository)
    {
        this.contentRepository = contentRepository;
        this.query = query;
        this.dataset = query.tryGetDatasetByPermId(datasetPermId);
        if (dataset == null)
        {
            throw new IllegalStateException(String.format("Dataset '%s' not found", datasetPermId));
        }
    }

    /** has to be called at the end */
    public void close()
    {
        query.close();
    }

    private ImgContainerDTO getContainer()
    {
        if (container == null)
        {
            container = query.getContainerById(dataset.getContainerId());
        }
        return container;
    }

    public Geometry getPlateGeometry()
    {
        return new Geometry(getContainer().getNumberOfRows(), getContainer().getNumberOfColumns());
    }

    private ImgDatasetDTO getDataset()
    {
        return dataset;
    }

    public Geometry getWellGeometry()
    {
        return new Geometry(getDataset().getFieldNumberOfRows(), getDataset()
                .getFieldNumberOfColumns());
    }

    public int getChannelCount()
    {
        if (channelCount == null)
        {
            channelCount = getChannelsNames().size();
        }
        return channelCount;
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

        long chosenChannelId =
                query.getChannelIdByChannelNameDatasetIdOrExperimentId(getDataset().getId(),
                        getContainer().getExperimentId(), chosenChannel);

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

    public List<String> getChannelsNames()
    {
        if (channelNames == null)
        {
            String[] namesAsArray =
                    query.getChannelNamesByDatasetIdOrExperimentId(getDataset().getId(),
                            getContainer().getExperimentId());
            channelNames = new ArrayList<String>(Arrays.asList(namesAsArray));
        }
        return channelNames;
    }
}