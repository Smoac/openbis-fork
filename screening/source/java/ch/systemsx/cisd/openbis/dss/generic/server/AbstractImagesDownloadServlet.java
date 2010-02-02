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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.bds.hcs.HCSDatasetLoader;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * ABstract class for servlets which allow to download screening images in a chosen size for a
 * specified channel.
 * 
 * @author Tomasz Pylak
 */
abstract class AbstractImagesDownloadServlet extends AbstractDatasetDownloadServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * @throw EnvironmentFailureException if image does not exist
     */
    protected abstract ResponseContentStream createImageResponse(RequestParams params,
            File datasetRoot) throws IOException, EnvironmentFailureException;

    protected static class RequestParams
    {
        // -- optional servlet parameters

        private final static String MERGE_CHANNELS_PARAM = "mergeChannels";

        // -- mandatory servlet parameters

        private final static String DATASET_CODE_PARAM = "dataset";

        private final static String WELL_ROW_PARAM = "wellRow";

        private final static String WELL_COLUMN_PARAM = "wellCol";

        private final static String TILE_ROW_PARAM = "tileRow";

        private final static String TILE_COL_PARAM = "tileCol";

        private final static String CHANNEL_PARAM = "channel";

        private final String sessionId;

        private final String displayMode;

        private final String datasetCode;

        private final Location wellLocation;

        private final Location tileLocation;

        private boolean mergeAllChannels;

        // contains the channel number or the number of all channels if all of them should be merged
        private int channel;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getParam(request, SESSION_ID_PARAM);
            String displayModeText = request.getParameter(DISPLAY_MODE_PARAM);
            displayMode = displayModeText == null ? "" : displayModeText;
            datasetCode = getParam(request, DATASET_CODE_PARAM);
            int wellRow = getIntParam(request, WELL_ROW_PARAM);
            int wellCol = getIntParam(request, WELL_COLUMN_PARAM);
            int tileRow = getIntParam(request, TILE_ROW_PARAM);
            int tileCol = getIntParam(request, TILE_COL_PARAM);
            this.wellLocation = new Location(wellCol, wellRow);
            this.tileLocation = new Location(tileCol, tileRow);
            channel = getIntParam(request, CHANNEL_PARAM);
            String mergeChannelsText = request.getParameter(MERGE_CHANNELS_PARAM);
            mergeAllChannels =
                    (mergeChannelsText == null) ? false : mergeChannelsText
                            .equalsIgnoreCase("true");
        }

        private static int getIntParam(HttpServletRequest request, String paramName)
        {
            String value = getParam(request, paramName);
            try
            {
                return Integer.valueOf(value);
            } catch (NumberFormatException e)
            {
                throw new UserFailureException("parameter " + paramName
                        + " should be an integer, but is: " + value);
            }
        }

        private static String getParam(final HttpServletRequest request, String paramName)
        {
            String value = request.getParameter(paramName);
            if (value == null)
            {
                throw new UserFailureException("no value for the parameter " + paramName
                        + " found in the URL");
            }
            return value;
        }

        public String getSessionId()
        {
            return sessionId;
        }

        public String getDatasetCode()
        {
            return datasetCode;
        }

        public String getDisplayMode()
        {
            return displayMode;
        }

        public boolean isMergeAllChannels()
        {
            return mergeAllChannels;
        }

        public int getChannel()
        {
            return channel;
        }

        public Location getWellLocation()
        {
            return wellLocation;
        }

        public Location getTileLocation()
        {
            return tileLocation;
        }
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            RequestParams params = new RequestParams(request);
            HttpSession session = tryGetOrCreateSession(request, params.getSessionId());
            if (session == null)
            {
                printSessionExpired(response);
            } else
            {
                deliverFile(response, params, session);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            printErrorResponse(response, "Error: " + e.getMessage());
        }

    }

    protected void deliverFile(HttpServletResponse response, RequestParams params,
            HttpSession session) throws IOException
    {
        ensureDatasetAccessible(params.getDatasetCode(), session, params.getSessionId());
        File datasetRoot = createDataSetRootDirectory(params.getDatasetCode(), session);

        ResponseContentStream responseStream;
        try
        {
            responseStream = createImageResponse(params, datasetRoot);
        } catch (EnvironmentFailureException e)
        {
            operationLog.warn(e.getMessage());
            printErrorResponse(response, e.getMessage());
            return;
        }
        logImageDelivery(params, responseStream);
        writeResponseContent(responseStream, response);
    }

    /** throws {@link EnvironmentFailureException} when image does not exist */
    protected final static File getPath(HCSDatasetLoader imageAccessor, RequestParams params,
            int chosenChannel)
    {
        INode image =
                imageAccessor.tryGetStandardNodeAt(chosenChannel, params.getWellLocation(), params
                        .getTileLocation());
        if (image != null)
        {
            return new File(image.getPath());
        } else
        {
            throw EnvironmentFailureException.fromTemplate(
                    "No image found for well %s, tile %s and channel %d", params.getWellLocation(),
                    params.getTileLocation(), chosenChannel);
        }
    }

    protected final static void logImageDelivery(RequestParams params,
            ResponseContentStream responseStream)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + params.getDatasetCode() + "' deliver image ("
                    + responseStream.getSize() + " bytes)");
        }
    }

    protected final static BufferedImage asThumbnailIfRequested(RequestParams params,
            BufferedImage image)
    {
        Size thumbnailSizeOrNull = tryAsThumbnailDisplayMode(params.getDisplayMode());
        if (thumbnailSizeOrNull != null)
        {
            return createThumbnail(image, thumbnailSizeOrNull);
        } else
        {
            return image;
        }
    }

    protected final static BufferedImage transformToChannel(BufferedImage bufferedImage,
            int channelNumber)
    {
        BufferedImage newImage = createNewImage(bufferedImage);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int rgb = bufferedImage.getRGB(x, y);
                int channelColor = getGrayscaleAsChannel(rgb, channelNumber);
                newImage.setRGB(x, y, channelColor);
            }
        }
        return newImage;
    }

    protected final static BufferedImage createNewImage(BufferedImage bufferedImage)
    {
        BufferedImage newImage =
                new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
        return newImage;
    }

    protected final static int getRGBColorIndex(int channel)
    {
        assert channel <= 3 : "to many channels: " + channel;
        return 3 - channel;
    }

    protected final static int getGrayscaleAsChannel(int rgb, int channelNumber)
    {
        // NOTE: we handle only 3 channels until we know that more channels can be used and what
        // kind of color manipulation makes sense
        if (channelNumber <= 3)
        {
            // we assume that the color was in a grayscale
            // we reset all ingredients besides the one which should be shown
            int newColor[] = new int[]
                { 0, 0, 0 };
            newColor[getRGBColorIndex(channelNumber)] =
                    extractChannelColorIngredient(rgb, channelNumber);
            return asRGB(newColor);
        } else
        {
            return rgb;
        }
    }

    // returns the ingredient for the specified channel
    protected final static int extractChannelColorIngredient(int rgb, int channelNumber)
    {
        Color c = new Color(rgb);
        int channelColors[] = new int[]
            { c.getBlue(), c.getGreen(), c.getRed() };
        return channelColors[channelNumber - 1];
    }

    protected final static int asRGB(int[] rgb)
    {
        return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }
}
