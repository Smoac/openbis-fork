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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.utils;

import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.reveregroup.gwt.imagepreloader.FitImage;
import com.reveregroup.gwt.imagepreloader.FitImageLoadHandler;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateStyleSetter;
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
            int imageWidth, int imageHeight, FitImageLoadHandler imageLoadHandler)
    {
        Component tilePanel =
                createTilePanel(sessionId, channelReferences, channelStackRef, imageWidth,
                        imageHeight, imageLoadHandler);
        container.add(tilePanel);
    }

    /**
     * generates URL of an image on Data Store server
     */
    private static Component createTilePanel(String sessionID,
            LogicalImageChannelsReference channelReferences, ImageChannelStack channelStackRef,
            int width, int height, final FitImageLoadHandler imageLoadHandler)
    {
        final URLMethodWithParameters methodWithParameters =
                createBasicImageURL(sessionID, channelReferences);

        methodWithParameters.addParameter(ImageServletUrlParameters.CHANNEL_STACK_ID_PARAM,
                channelStackRef.getChannelStackTechId());
        addImageTransformerSignature(methodWithParameters, channelReferences);
        final String linkURL = methodWithParameters.toString();
        addThumbnailSize(methodWithParameters, width, height);

        final FitImage image = new FitImage();
        image.setFixedHeight(height);
        image.addFitImageLoadHandler(imageLoadHandler);
        image.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    Window.open(linkURL, "_blank", "");
                }
            });
        LayoutContainer tileContent = new LayoutContainer();
        // do not specify width to get correct aspect ratio of the original image
        tileContent.setHeight("" + height);
        tileContent.add(image);
        PlateStyleSetter.setPointerCursor(tileContent);

        // Set the url at the very end. This method triggers the image loading process
        // therefore it should be done after all image load handlers have been set.
        // Setting the url before defining the load handlers may result in handlers not 
        // being notified about the finished loading (when loading is finished before 
        // handlers are added). Moreover we are deferring setting the url to make sure that 
        // all actions that attach the image to DOM finish first. Otherwise again images 
        // are not displayed.
        GWTUtils.executeDelayed(new IDelegatedAction()
            {
                public void execute()
                {
                    image.setUrl(methodWithParameters.toString());
                }
            });

        return tileContent;
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
        if (channelReferences.tryGetImageTransformationCode() != null)
        {
            methodWithParameters.addParameter(
                    ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM,
                    channelReferences.tryGetImageTransformationCode());
        }
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

        List<String> channels = channelReferences.getChannelCodes();
        if (channels != null)
        {
            for (String channel : channels)
            {
                String channelOrNull = channel;
                if (channelOrNull.equalsIgnoreCase(ScreeningConstants.MERGED_CHANNELS))
                {
                    channelOrNull = null;
                }
                String signature =
                        images.tryGetTransformerFactorySignature(channelOrNull,
                                channelReferences.tryGetImageTransformationCode());
                if (signature != null)
                {
                    url.addParameter("transformerFactorySignature", signature);
                }

            }
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

        List<String> channels = channelReferences.getChannelCodes();
        if (channels.contains(ScreeningConstants.MERGED_CHANNELS))
        {
            methodWithParameters.addParameter(ImageServletUrlParameters.MERGE_CHANNELS_PARAM,
                    "true");
        } else
        {
            for (String channel : channelReferences.getChannelCodes())
            {
                methodWithParameters.addParameter(ImageServletUrlParameters.CHANNEL_PARAM, channel);
            }
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
