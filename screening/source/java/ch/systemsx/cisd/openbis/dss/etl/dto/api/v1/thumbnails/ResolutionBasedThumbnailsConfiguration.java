/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.thumbnails;

import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat;

/**
 * @author Pawel Glyzewski
 */
public class ResolutionBasedThumbnailsConfiguration extends AbstractThumbnailsConfiguration
{
    private final int maxWidth;

    private final int maxHeight;

    public ResolutionBasedThumbnailsConfiguration(int maxWidth, int maxHeight)
    {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public ThumbnailsStorageFormat getThumbnailsStorageFormat(SimpleImageDataConfig config)
    {
        ThumbnailsStorageFormat thumbnailsStorageFormat = super.getThumbnailsStorageFormat(config);
        thumbnailsStorageFormat.setMaxWidth(maxWidth);
        thumbnailsStorageFormat.setMaxHeight(maxHeight);
        return thumbnailsStorageFormat;
    }

    @Override
    protected String getDefaultFileName()
    {
        return String.format("thumbnails_%dx%d.h5", maxWidth, maxHeight);
    }
}
