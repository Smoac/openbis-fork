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

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.ByteArrayBasedContent;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.common.utilities.DataTypeUtil;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDatasetDownloadServlet.Size;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference.LocationImageChannelStackReference;
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
    public AbsoluteImageReference tryGetImage(String chosenChannel,
            ImageChannelStackReference channelStackReference, Size thumbnailSizeOrNull)
    {
        assert StringUtils.isBlank(chosenChannel) == false;
        LocationImageChannelStackReference stackLocations =
                channelStackReference.tryGetChannelStackLocations();
        if (stackLocations != null)
        {
            assert stackLocations.getTileLocation().getX() <= getDataset()
                    .getFieldNumberOfColumns();
            assert stackLocations.getTileLocation().getY() <= getDataset().getFieldNumberOfRows();
            assert stackLocations.getWellLocation().getX() <= getContainer().getNumberOfColumns();
            assert stackLocations.getWellLocation().getY() <= getContainer().getNumberOfRows();
        }

        Long chosenChannelId =
                query.tryGetChannelIdByChannelNameDatasetIdOrExperimentId(getDataset().getId(),
                        getContainer().getExperimentId(), chosenChannel);
        if (chosenChannelId == null)
        {
            return null;
        }

        ImgImageDTO imageDTO;
        IContent content = null;
        long datasetId = getDataset().getId();
        if (thumbnailSizeOrNull != null)
        {
            imageDTO = tryGetThumbnail(chosenChannelId, channelStackReference, datasetId);
            if (imageDTO == null)
            {
                imageDTO = tryGetImage(chosenChannelId, channelStackReference, datasetId);
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
            imageDTO = tryGetImage(chosenChannelId, channelStackReference, datasetId);
            if (imageDTO != null)
            {
                content = contentRepository.getContent(imageDTO.getFilePath());
                final InputStream is = content.getInputStream();
                final String fileType = DataTypeUtil.tryToFigureOutFileTypeOf(is);
                if (DataTypeUtil.isTiff(fileType) == false || imageDTO.getColorComponent() != null
                        || imageDTO.getPage() != null)
                {
                    final int page = (imageDTO.getPage() != null) ? imageDTO.getPage() : 0;
                    BufferedImage image = ImageUtil.loadImage(is, fileType, page);
                    if (imageDTO.getColorComponent() != null)
                    {
                        image =
                                ImageChannelsUtils.transformToChannel(image, imageDTO
                                        .getColorComponent());
                    }
                    content = asContent(image, fileType, content.getName(), content.getUniqueId());
                }
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

    private ImgImageDTO tryGetImage(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId)
    {
        LocationImageChannelStackReference locations =
                channelStackReference.tryGetChannelStackLocations();
        if (locations != null)
        {
            return query.tryGetImage(channelId, datasetId, locations.getTileLocation(), locations
                    .getWellLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            return query.tryGetImage(channelId, channelStackId, datasetId);
        }
    }

    private ImgImageDTO tryGetThumbnail(long channelId,
            ImageChannelStackReference channelStackReference, long datasetId)
    {
        LocationImageChannelStackReference locations =
                channelStackReference.tryGetChannelStackLocations();
        if (locations != null)
        {
            return query.tryGetThumbnail(channelId, datasetId, locations.getTileLocation(),
                    locations.getWellLocation());
        } else
        {
            Long channelStackId = channelStackReference.tryGetChannelStackId();
            assert channelStackId != null : "invalid specification of the channel stack: "
                    + channelStackReference;
            return query.tryGetThumbnail(channelId, channelStackId, datasetId);
        }
    }

    private static IContent asContent(BufferedImage image, String fileType, String name, String id)
    {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final TIFFEncodeParam param = new TIFFEncodeParam();
        param.setLittleEndian(true);
        if (DataTypeUtil.isJpeg(fileType))
        {
            param.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2);
        } else
        {
            param.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
        }
        final ImageEncoder enc = ImageCodec.createImageEncoder("tiff", out, param);
        try
        {
            enc.encode(image);
            return new ByteArrayBasedContent(out.toByteArray(), name, id);
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate("Cannot encode image.", ex);
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