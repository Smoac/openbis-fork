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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils.IDatasetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.DatasetAcquiredImagesReference;
import ch.systemsx.cisd.openbis.dss.generic.server.images.dto.ImageGenerationDescription;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

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
    protected abstract ResponseContentStream createImageResponse(ImageGenerationDescription params,
            IDatasetDirectoryProvider datasetDirectoryProvider) throws IOException,
            EnvironmentFailureException;

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        ImageGenerationDescription imageGenDesc = null;
        try
        {
            HttpSession session =
                    tryGetOrCreateSession(request,
                            ImageGenerationDescriptionFactory.getSessionId(request));
            if (session == null)
            {
                printSessionExpired(response);
            } else
            {
                imageGenDesc = ImageGenerationDescriptionFactory.create(request);
                deliverFile(response, imageGenDesc, session);
            }
        } catch (Exception e)
        {
            String message = "Error: Couldn't deliver image";
            if (imageGenDesc != null)
            {
                message += " for " + imageGenDesc;
            }
            operationLog.error(message, e);
            printErrorResponse(response, message);
        }

    }

    protected void deliverFile(HttpServletResponse response, ImageGenerationDescription params,
            HttpSession session) throws IOException
    {
        ensureDatasetsAccessible(params, session, params.getSessionId());

        long start = System.currentTimeMillis();
        ResponseContentStream responseStream;
        try
        {
            responseStream = createImageResponse(params, createDatasetDirectoryProvider(session));
        } catch (EnvironmentFailureException e)
        {
            operationLog.warn(e.getMessage());
            printErrorResponse(response, e.getMessage());
            return;
        }
        logImageDelivery(params, responseStream, (System.currentTimeMillis() - start));
        writeResponseContent(responseStream, response);
    }

    private IDatasetDirectoryProvider createDatasetDirectoryProvider(HttpSession session)
    {
        final DatabaseInstance databaseInstance = getDatabaseInstance(session);
        final File storeRootPath = getStoreRootPath();
        return new IDatasetDirectoryProvider()
            {
                public File getDatasetRoot(String datasetCode)
                {
                    return DatasetLocationUtil.getDatasetLocationPathCheckingIfExists(datasetCode,
                            databaseInstance, storeRootPath);
                }
            };
    }

    private void ensureDatasetsAccessible(ImageGenerationDescription params, HttpSession session,
            String sessionId)
    {
        ensureDatasetAccessible(params.tryGetImageChannels(), session, sessionId);
        List<DatasetAcquiredImagesReference> overlayChannels = params.getOverlayChannels();
        for (DatasetAcquiredImagesReference dataset : overlayChannels)
        {
            ensureDatasetAccessible(dataset, session, sessionId);
        }
    }

    private void ensureDatasetAccessible(DatasetAcquiredImagesReference dataset,
            HttpSession session, String sessionId)
    {
        ensureDatasetAccessible(dataset.getDatasetCode(), session, sessionId);
    }

    protected final static void logImageDelivery(ImageGenerationDescription params,
            ResponseContentStream responseStream, long timeTaken)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("For '" + params + "' delivering image (" + responseStream.getSize()
                    + " bytes) took " + timeTaken + " msec");
        }
    }
}
