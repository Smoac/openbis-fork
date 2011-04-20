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

package ch.systemsx.cisd.openbis.dss.etl.dto;

/**
 * Stores information about the library and its reader which should be used to read the image.
 * 
 * @author Tomasz Pylak
 */
public class ImageLibraryInfo
{
    // Which image library should be used to read the image?
    private final String name;

    // Which reader in the library should be used?
    private final String readerName;

    public ImageLibraryInfo(String imageLibraryName, String readerName)
    {
        assert imageLibraryName != null : "library name not specified!";
        assert readerName != null : "reader name not specified!";

        this.name = imageLibraryName;
        this.readerName = readerName;
    }

    public String getName()
    {
        return name;
    }

    public String getReaderName()
    {
        return readerName;
    }

    @Override
    public String toString()
    {
        return name + " (reader: " + readerName + ")";
    }
}
