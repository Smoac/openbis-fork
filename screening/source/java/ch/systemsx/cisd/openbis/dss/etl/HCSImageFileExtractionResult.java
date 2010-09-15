/*
 * Copyright 2008 ETH Zuerich, CISD
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
package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.List;

/**
 * Class which contains the image extraction process results.
 * 
 * @author Tomasz Pylak
 */
public final class HCSImageFileExtractionResult
{
    /** The images files with description. */
    private final List<AcquiredPlateImage> images;

    /** The invalid files found. */
    private final List<File> invalidFiles;

    private final List<Channel> channels;

    public HCSImageFileExtractionResult(List<AcquiredPlateImage> images, List<File> invalidFiles,
            List<Channel> channels)
    {
        this.images = images;
        this.invalidFiles = invalidFiles;
        this.channels = channels;
    }

    public List<AcquiredPlateImage> getImages()
    {
        return images;
    }

    public List<File> getInvalidFiles()
    {
        return invalidFiles;
    }

    public List<Channel> getChannels()
    {
        return channels;
    }

    /**
     * A channel in which the image has been acquired.
     * <p>
     * Each channel has its <code>code</code> which uniquely identifies it in one experiment or
     * dataset.
     * </p>
     * 
     * @author Tomasz Pylak
     */
    public static final class Channel
    {
        private final String code;

        private final String label;

        private final String description;

        private final Integer wavelength;

        public Channel(String code, String descriptionOrNull, Integer wavelengthOrNull, String label)
        {
            assert code != null : "code is null";
            assert label != null : "label is null";
            this.label = label;
            this.code = code;
            this.description = descriptionOrNull;
            this.wavelength = wavelengthOrNull;
        }

        public String getCode()
        {
            return code;
        }

        public String tryGetDescription()
        {
            return description;
        }

        public Integer tryGetWavelength()
        {
            return wavelength;
        }

        public String getLabel()
        {
            return label;
        }

    }

}