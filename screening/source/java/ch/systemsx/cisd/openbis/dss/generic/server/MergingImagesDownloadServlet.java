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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.images.TileImageReference;

/**
 * Allows to download screening images in a chosen size for a specified channels or with all
 * channels merged.<br>
 * Assumes that originally there is one image for each channel and no image with all the channels
 * merged exist.
 * 
 * @author Tomasz Pylak
 */
public class MergingImagesDownloadServlet extends AbstractImagesDownloadServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * @throws EnvironmentFailureException if image does not exist
     **/
    @Override
    protected final ResponseContentStream createImageResponse(TileImageReference params,
            File datasetRoot, String datasetCode) throws IOException, EnvironmentFailureException
    {
        IContent image = ImageChannelsUtils.getImage(datasetRoot, datasetCode, params);
        // TODO 2010-08-31, Tomasz Pylak: uncomment, we want tiff files
        // String contentType = CONTENT_TYPE_TIFF;
        String contentType = CONTENT_TYPE_PNG;
        return createResponseContentStream(image.getInputStream(), image.getSize(), contentType,
                image.tryGetName());
    }
}
