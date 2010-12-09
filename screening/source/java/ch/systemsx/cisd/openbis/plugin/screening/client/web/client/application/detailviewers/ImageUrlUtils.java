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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;

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
            WellImages images, String channel, int row, int col, int imageWidth, int imageHeight,
            boolean createImageLinks)
    {
        String imageURL =
                createDatastoreImageUrl(sessionId, images, channel, row, col, imageWidth,
                        imageHeight, createImageLinks);
        addUrlWidget(container, imageURL, imageWidth, imageHeight);
    }

    /**
     * Creates a widget which displays the URL to the specified image on DSS and adds it to the
     * container.
     */
    public static void addImageUrlWidget(LayoutContainer container, String sessionId,
            WellImages images, String channel, ImageChannelStack channelStackRef,
            int imageWidth, int imageHeight)
    {
        String imageURL =
                createDatastoreImageUrl(sessionId, images, channel, channelStackRef, imageWidth,
                        imageHeight);
        addUrlWidget(container, imageURL, imageWidth, imageHeight);
    }

    /** generates URL of an image on Data Store server */
    private static String createDatastoreImageUrl(String sessionID, WellImages images,
            String channel, ImageChannelStack channelStackRef, int width, int height)
    {
        URLMethodWithParameters methodWithParameters =
                createBasicImageURL(sessionID, images, channel);

        methodWithParameters
                .addParameter("channelStackId", channelStackRef.getChannelStackTechId());
        String linkURL = methodWithParameters.toString();
        methodWithParameters.addParameter("mode", "thumbnail" + width + "x" + height);

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
     * generates URL of an image on Data Store server
     * 
     * @param createImageLinks
     */
    private static String createDatastoreImageUrl(String sessionID, WellImages images,
            String channel, int tileRow, int tileCol, int width, int height,
            boolean createImageLinks)
    {
        URLMethodWithParameters methodWithParameters =
                createBasicImageURL(sessionID, images, channel);

        methodWithParameters.addParameter("wellRow", images.getWellLocation().getRow());
        methodWithParameters.addParameter("wellCol", images.getWellLocation().getColumn());
        methodWithParameters.addParameter("tileRow", tileRow);
        methodWithParameters.addParameter("tileCol", tileCol);
        String signature = images.getTransformerFactorySignatureOrNull(channel);
        if (signature != null)
        {
            methodWithParameters.addParameter("transformerFactorySignature", signature);
        }
        String linkURL = createImageLinks ? methodWithParameters.toString() : null;
        methodWithParameters.addParameter("mode", "thumbnail" + width + "x" + height);

        String imageURL = methodWithParameters.toString();
        // do not specify width to get correct aspect ratio of the original image
        return URLMethodWithParameters.createEmbededImageHtml(imageURL, linkURL, -1, height);
    }

    private static URLMethodWithParameters createBasicImageURL(String sessionID, WellImages images,
            String channel)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(images.getDatastoreHostUrl() + "/"
                        + ScreeningConstants.DATASTORE_SCREENING_SERVLET_URL);
        methodWithParameters.addParameter("sessionID", sessionID);
        methodWithParameters.addParameter("dataset", images.getDatasetCode());
        methodWithParameters.addParameter("channel", channel);
        if (channel.equals(ScreeningConstants.MERGED_CHANNELS))
        {
            methodWithParameters.addParameter("mergeChannels", "true");
        }
        return methodWithParameters;
    }
}
