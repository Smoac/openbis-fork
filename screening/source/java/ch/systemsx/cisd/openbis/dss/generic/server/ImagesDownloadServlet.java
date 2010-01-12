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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * Allows to download screening images in a chosen size for a specified channels or with all
 * channels merged.
 * 
 * @author Tomasz Pylak
 */
public class ImagesDownloadServlet extends AbstractDatasetDownloadServlet
{
    private static final long serialVersionUID = 1L;

    private static class RequestParams
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

        private final String wellRow;

        private final String wellCol;

        private final String tileRow;

        private final String tileCol;

        private boolean mergeAllChannels;

        // contains the channel number or the number of all channels if all of them should be merged
        private int channel;

        public RequestParams(HttpServletRequest request)
        {
            sessionId = getParam(request, SESSION_ID_PARAM);
            String displayModeText = request.getParameter(DISPLAY_MODE_PARAM);
            displayMode = displayModeText == null ? "" : displayModeText;
            datasetCode = getParam(request, DATASET_CODE_PARAM);
            wellRow = getParam(request, WELL_ROW_PARAM);
            wellCol = getParam(request, WELL_COLUMN_PARAM);
            tileRow = getParam(request, TILE_ROW_PARAM);
            tileCol = getParam(request, TILE_COL_PARAM);
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

        public List<String> getImagePaths()
        {
            List<String> paths = new ArrayList<String>();
            if (mergeAllChannels)
            {
                for (int chosenChannel = 1; chosenChannel <= channel; chosenChannel++)
                {
                    paths.add(getPath(chosenChannel));
                }
            } else
            {
                paths.add(getPath(channel));
            }
            return paths;
        }

        private String getPath(int chosenChannel)
        {
            return "data/standard/channel" + chosenChannel + "/row" + wellRow + "/column" + wellCol
                    + "/row" + tileRow + "_column" + tileCol + ".tiff";
        }

        public int getChannel()
        {
            return channel;
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
            printErrorResponse(response, "Error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void deliverFile(HttpServletResponse response, RequestParams params, HttpSession session)
            throws IOException
    {
        ensureDatasetAccessible(params.getDatasetCode(), session, params.getSessionId());
        File datasetRoot = createDataSetRootDirectory(params.getDatasetCode(), session);
        List<File> imageFiles = getImageFiles(datasetRoot, params);

        BufferedImage image = mergeImages(imageFiles, params);
        File singleFileOrNull = imageFiles.size() == 1 ? imageFiles.get(0) : null;
        ResponseContentStream responseStream = createResponseContentStream(image, singleFileOrNull);
        logImageDelivery(params, responseStream);
        writeResponseContent(responseStream, response);
    }

    private static void logImageDelivery(RequestParams params, ResponseContentStream responseStream)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + params.getDatasetCode() + "' deliver image ("
                    + responseStream.getSize() + " bytes)");
        }
    }

    private static BufferedImage mergeImages(List<File> imageFiles, RequestParams params)
    {
        List<BufferedImage> images = loadImages(imageFiles, params);

        Size thumbnailSizeOrNull = tryAsThumbnailDisplayMode(params.getDisplayMode());

        BufferedImage resultImage;
        if (images.size() == 1)
        {
            resultImage = transformToChannel(images.get(0), params.getChannel());
        } else
        {
            resultImage = mergeChannels(images);
        }
        if (thumbnailSizeOrNull != null)
        {
            resultImage = createThumbnail(resultImage, thumbnailSizeOrNull);
        }
        return resultImage;
    }

    private static BufferedImage mergeChannels(List<BufferedImage> images)
    {
        assert images.size() > 1 : "more than 1 image expected, but found: " + images.size();
        BufferedImage newImage = createNewImage(images.get(0));
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                int mergedRGB = mergeRGBColor(images, x, y);
                newImage.setRGB(x, y, mergedRGB);
            }
        }
        return newImage;
    }

    private static BufferedImage transformToChannel(BufferedImage bufferedImage, int channelNumber)
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

    private static BufferedImage createNewImage(BufferedImage bufferedImage)
    {
        BufferedImage newImage =
                new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
        return newImage;
    }

    // NOTE: we handle only 3 channels until we know that more channels can be used and
    // what
    // kind of color manipulation makes sense
    private static int mergeRGBColor(List<BufferedImage> images, int x, int y)
    {
        int color[] = new int[]
            { 0, 0, 0 };
        for (int channel = 1; channel <= Math.min(3, images.size()); channel++)
        {
            int rgb = images.get(channel - 1).getRGB(x, y);
            color[getRGBColorIndex(channel)] = extractChannelColorIngredient(rgb, channel);
        }
        int mergedRGB = asRGB(color);
        return mergedRGB;
    }

    private static int getRGBColorIndex(int channel)
    {
        assert channel <= 3 : "to many channels: " + channel;
        return 3 - channel;
    }

    private static int getGrayscaleAsChannel(int rgb, int channelNumber)
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
    private static int extractChannelColorIngredient(int rgb, int channelNumber)
    {
        Color c = new Color(rgb);
        int channelColors[] = new int[]
            { c.getBlue(), c.getGreen(), c.getRed() };
        return channelColors[channelNumber - 1];
    }

    private static int asRGB(int[] rgb)
    {
        return new Color(rgb[0], rgb[1], rgb[2]).getRGB();
    }

    private static List<BufferedImage> loadImages(List<File> imageFiles, RequestParams params)
    {
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (File imageFile : imageFiles)
        {
            BufferedImage image = ImageUtil.loadImage(imageFile);
            images.add(image);
        }
        return images;
    }

    private static List<File> getImageFiles(File datasetRoot, RequestParams params)
    {
        List<File> imageFiles = new ArrayList<File>();
        for (String imagePath : params.getImagePaths())
        {
            imageFiles.add(new File(datasetRoot, imagePath));
        }
        return imageFiles;
    }

}
