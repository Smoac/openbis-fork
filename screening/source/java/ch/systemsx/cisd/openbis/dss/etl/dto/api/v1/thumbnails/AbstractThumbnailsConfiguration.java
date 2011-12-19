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
public abstract class AbstractThumbnailsConfiguration implements IThumbnailsConfiguration
{
    private String fileName;

    private String fileFormat;

    public ThumbnailsStorageFormat getThumbnailsStorageFormat(SimpleImageDataConfig config)
    {
        ThumbnailsStorageFormat thumbnailsStorageFormat = new ThumbnailsStorageFormat();
        thumbnailsStorageFormat.setAllowedMachineLoadDuringGeneration(config
                .getAllowedMachineLoadDuringThumbnailsGeneration());
        thumbnailsStorageFormat.setThumbnailsFileName(getFileName());
        thumbnailsStorageFormat.setMaxWidth(config.getMaxThumbnailWidthAndHeight());
        thumbnailsStorageFormat.setMaxHeight(config.getMaxThumbnailWidthAndHeight());
        thumbnailsStorageFormat.setGenerateWithImageMagic(config
                .getGenerateThumbnailsWithImageMagic());
        thumbnailsStorageFormat.setImageMagicParams(config
                .getThumbnailsGenerationImageMagicParams());
        thumbnailsStorageFormat.setHighQuality(config.getGenerateThumbnailsIn8BitHighQuality());
        setFileFormat(thumbnailsStorageFormat, config.getThumbnailsFileFormat());
        return thumbnailsStorageFormat;
    }

    protected abstract String getDefaultFileName();

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        if (fileName != null)
        {
            return fileName;
        } else
        {
            return getDefaultFileName();
        }
    }

    public void setFileFormat(String fileFormat)
    {
        this.fileFormat = fileFormat;
    }

    public String getFileFormat()
    {
        return this.fileFormat;
    }

    private void setFileFormat(ThumbnailsStorageFormat thumbnailsStorageFormat, String defaultValue)
    {
        if (fileFormat != null)
        {
            thumbnailsStorageFormat.setFileFormat(fileFormat);
        } else if (defaultValue != null)
        {
            thumbnailsStorageFormat.setFileFormat(defaultValue);
        }
    }
}
