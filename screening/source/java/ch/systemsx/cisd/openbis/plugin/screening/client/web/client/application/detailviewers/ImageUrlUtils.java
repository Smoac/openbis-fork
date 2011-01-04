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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.ImageDatasetChannel;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageChannelsReference;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ImageServletUrlParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Generates URLs pointing to the images on Data Store server.
 * 
 * @author Tomasz Pylak
 */
public class ImageUrlUtils
{
    /**
     * Creates a widget which displays the URL to the specified image on DSS and adds it to the
     * container.
     * 
     * @param createImageLinks if true, each thumbnail will link to the original image
     */
    public static void addImageUrlWidget(LayoutContainer container, String sessionId,
            LogicalImageChannelsReference channelReferences, int row, int col, int imageWidth,
            int imageHeight, boolean createImageLinks)
    {
        String imageURL =
                createDatastoreImageUrl(sessionId, channelReferences, row, col, imageWidth,
                        imageHeight, createImageLinks);
        addUrlWidget(container, imageURL, imageWidth, imageHeight);
    }

    /**
     * Creates a widget which displays the URL to the specified image on DSS and adds it to the
     * container.
     */
    public static void addImageUrlWidget(LayoutContainer container, String sessionId,
            LogicalImageChannelsReference channelReferences, ImageChannelStack channelStackRef,
            int imageWidth, int imageHeight)
    {
        String imageURL =
                createDatastoreImageUrl(sessionId, channelReferences, channelStackRef, imageWidth,
                        imageHeight);
        addUrlWidget(container, imageURL, imageWidth, imageHeight);
    }

    /** generates URL of an image on Data Store server */
    private static String createDatastoreImageUrl(String sessionID,
            LogicalImageChannelsReference channelReferences, ImageChannelStack channelStackRef,
            int width, int height)
    {
        URLMethodWithParameters methodWithParameters =
                createBasicImageURL(sessionID, channelReferences);

        methodWithParameters.addParameter(ImageServletUrlParameters.CHANNEL_STACK_ID_PARAM,
                channelStackRef.getChannelStackTechId());
        addImageTransformerSignature(methodWithParameters, channelReferences);
        String linkURL = methodWithParameters.toString();
        addThumbnailSize(methodWithParameters, width, height);

        String imageURL = methodWithParameters.toString();
        // do not specify width to get correct aspect ratio of the original image
        return URLMethodWithParameters.createEmbededImageHtml(imageURL, linkURL, -1, height);
    }

    /** creates a widget which displays the specified URL and adds it to the container */
    private static void addUrlWidget(LayoutContainer container, String url, int width, int height)
    {
        Component tileContent = new Html(url);
        // do not set the width to preserve aspect ratio
        tileContent.setHeight("" + height);
        PlateStyleSetter.setPointerCursor(tileContent);
        container.add(tileContent);
    }

    /**
     * Generates an HTML image tag with a thumbnail which links to the big image on Data Store
     * server.
     * 
     * @param createImageLinks
     */
    private static String createDatastoreImageUrl(String sessionID,
            LogicalImageChannelsReference channelReferences, int tileRow, int tileCol, int width,
            int height, boolean createImageLinks)
    {
        URLMethodWithParameters methodWithParameters =
                createBasicImageURL(sessionID, channelReferences);

        LogicalImageReference images = channelReferences.getBasicImage();
        WellLocation wellLocation = images.tryGetWellLocation();
        if (wellLocation != null)
        {
            methodWithParameters.addParameter(ImageServletUrlParameters.WELL_ROW_PARAM,
                    wellLocation.getRow());
            methodWithParameters.addParameter(ImageServletUrlParameters.WELL_COLUMN_PARAM,
                    wellLocation.getColumn());
        }
        methodWithParameters.addParameter(ImageServletUrlParameters.TILE_ROW_PARAM, tileRow);
        methodWithParameters.addParameter(ImageServletUrlParameters.TILE_COL_PARAM, tileCol);
        addImageTransformerSignature(methodWithParameters, channelReferences);
        String linkURLOrNull = createImageLinks ? methodWithParameters.toString() : null;
        addThumbnailSize(methodWithParameters, width, height);

        String imageURL = methodWithParameters.toString();
        // do not specify width to get correct aspect ratio of the original image
        return URLMethodWithParameters.createEmbededImageHtml(imageURL, linkURLOrNull, -1, height);
    }

    private static void addThumbnailSize(URLMethodWithParameters url, int width, int height)
    {
        url.addParameter("mode", "thumbnail" + width + "x" + height);
    }

    private static void addImageTransformerSignature(URLMethodWithParameters url,
            LogicalImageChannelsReference channelReferences)
    {
        LogicalImageReference images = channelReferences.getBasicImage();
        String signature =
                images.getTransformerFactorySignatureOrNull(channelReferences
                        .getBasicImageChannelCode());
        if (signature != null)
        {
            url.addParameter("transformerFactorySignature", signature);
        }
    }

    private static URLMethodWithParameters createBasicImageURL(String sessionID,
            LogicalImageChannelsReference channelReferences)
    {
        LogicalImageReference images = channelReferences.getBasicImage();
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(images.getDatastoreHostUrl() + "/"
                        + ScreeningConstants.DATASTORE_SCREENING_SERVLET_URL);
        methodWithParameters.addParameter("sessionID", sessionID);
        methodWithParameters.addParameter(ImageServletUrlParameters.DATASET_CODE_PARAM,
                images.getDatasetCode());
        String channel = channelReferences.getBasicImageChannelCode();
        if (channel.equals(ScreeningConstants.MERGED_CHANNELS))
        {
            methodWithParameters.addParameter(ImageServletUrlParameters.MERGE_CHANNELS_PARAM,
                    "true");
        } else
        {
            methodWithParameters.addParameter(ImageServletUrlParameters.CHANNEL_PARAM, channel);
        }
        addOverlayParameters(channelReferences.getOverlayChannels(), methodWithParameters);
        return methodWithParameters;
    }

    private static void addOverlayParameters(Set<ImageDatasetChannel> overlayChannels,
            URLMethodWithParameters methodWithParameters)
    {
        for (ImageDatasetChannel overlayChannel : overlayChannels)
        {
            String paramName =
                    ImageServletUrlParameters.OVERLAY_CHANNEL_PREFIX_PARAM
                            + overlayChannel.getDatasetCode();
            methodWithParameters.addParameter(paramName, overlayChannel.getChannelCode());
        }
    }
}
