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
import java.util.Set;

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

    private final Set<Channel> channels;

    public HCSImageFileExtractionResult(List<AcquiredPlateImage> images, List<File> invalidFiles,
            Set<Channel> channels)
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

    public Set<Channel> getChannels()
    {
        return channels;
    }

    /**
     * A channel in which the image has been acquired.
     * <p>
     * Each channel has its <code>name</code> which uniquely identifies it in one experiment or
     * dataset.
     * </p>
     * 
     * @author Tomasz Pylak
     */
    public static final class Channel
    {
        private final String name;

        private final String description;

        private final int wavelength;

        public Channel(String name, String description, int wavelength)
        {
            assert name != null : "name is null";
            this.name = name;
            this.description = description;
            this.wavelength = wavelength;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public int getWavelength()
        {
            return wavelength;
        }
    }

}