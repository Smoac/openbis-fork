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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelStackReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.TileImageReference;

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
    protected abstract ResponseContentStream createImageResponse(TileImageReference params,
            File datasetRoot, String datasetCode) throws IOException, EnvironmentFailureException;

    protected static class RequestParams extends TileImageReference
    {
        // -- optional servlet parameters

        private final static String MERGE_CHANNELS_PARAM = "mergeChannels";

        // -- mandatory servlet parameters

        private final static String DATASET_CODE_PARAM = "dataset";

        private final static String CHANNEL_STACK_ID_PARAM = "channelStackId";

        private final static String WELL_ROW_PARAM = "wellRow";

        private final static String WELL_COLUMN_PARAM = "wellCol";

        private final static String TILE_ROW_PARAM = "tileRow";

        private final static String TILE_COL_PARAM = "tileCol";

        private final static String CHANNEL_PARAM = "channel";

        public static TileImageReference createTileImageReference(HttpServletRequest request)
        {
            return new RequestParams(request);
        }

        private RequestParams(HttpServletRequest request)
        {
            this.sessionId = getParam(request, SESSION_ID_PARAM);

            String displayModeText = request.getParameter(DISPLAY_MODE_PARAM);
            String displayMode = displayModeText == null ? "" : displayModeText;
            this.thumbnailSizeOrNull = tryAsThumbnailDisplayMode(displayMode);
            this.datasetCode = getParam(request, DATASET_CODE_PARAM);
            this.channelStackReference = getImageChannelStackReference(request);

            this.channel = getParam(request, CHANNEL_PARAM);
            String mergeChannelsText = request.getParameter(MERGE_CHANNELS_PARAM);
            this.mergeAllChannels =
                    (mergeChannelsText == null) ? false : mergeChannelsText
                            .equalsIgnoreCase("true");
        }

        private ImageChannelStackReference getImageChannelStackReference(HttpServletRequest request)
        {
            Integer channelStackId = tryGetIntParam(request, CHANNEL_STACK_ID_PARAM);
            if (channelStackId == null)
            {
                int wellRow = getIntParam(request, WELL_ROW_PARAM);
                int wellCol = getIntParam(request, WELL_COLUMN_PARAM);
                int tileRow = getIntParam(request, TILE_ROW_PARAM);
                int tileCol = getIntParam(request, TILE_COL_PARAM);
                Location wellLocation = new Location(wellCol, wellRow);
                Location tileLocation = new Location(tileCol, tileRow);
                return ImageChannelStackReference.createFromLocations(wellLocation, tileLocation);
            } else
            {
                return ImageChannelStackReference.createFromId(channelStackId);
            }
        }

        private static int getIntParam(HttpServletRequest request, String paramName)
        {

            Integer value = tryGetIntParam(request, paramName);
            if (value == null)
            {
                throw new UserFailureException("parameter " + paramName
                        + " should be an integer, but is: " + value);
            }
            return value.intValue();
        }

        private static Integer tryGetIntParam(HttpServletRequest request, String paramName)
        {
            String value = request.getParameter(paramName);
            if (value == null)
            {
                return null;
            }
            try
            {
                return Integer.valueOf(value);
            } catch (NumberFormatException e)
            {
                return null;
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
    }

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        TileImageReference reference = null;
        try
        {
            reference = RequestParams.createTileImageReference(request);
            HttpSession session = tryGetOrCreateSession(request, reference.getSessionId());
            if (session == null)
            {
                printSessionExpired(response);
            } else
            {
                deliverFile(response, reference, session);
            }
        } catch (Exception e)
        {
            String message = "Error: Couldn't deliver image";
            if (reference != null)
            {
                message +=
                        " for data set " + reference.getDatasetCode() + " and channel "
                                + reference.getChannel();
            }
            operationLog.error(message, e);
            printErrorResponse(response, message);
        }

    }

    protected void deliverFile(HttpServletResponse response, TileImageReference params,
            HttpSession session) throws IOException
    {
        ensureDatasetAccessible(params.getDatasetCode(), session, params.getSessionId());
        File datasetRoot = createDataSetRootDirectory(params.getDatasetCode(), session);

        long start = System.currentTimeMillis();
        ResponseContentStream responseStream;
        try
        {
            responseStream = createImageResponse(params, datasetRoot, params.getDatasetCode());
        } catch (EnvironmentFailureException e)
        {
            operationLog.warn(e.getMessage());
            printErrorResponse(response, e.getMessage());
            return;
        }
        logImageDelivery(params, responseStream, (System.currentTimeMillis() - start));
        writeResponseContent(responseStream, response);
    }

    protected final static void logImageDelivery(TileImageReference params,
            ResponseContentStream responseStream, long timeTaken)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For data set '" + params.getDatasetCode() + "' delivering image ("
                    + responseStream.getSize() + " bytes) took " + timeTaken + " msec");
        }
    }
}
