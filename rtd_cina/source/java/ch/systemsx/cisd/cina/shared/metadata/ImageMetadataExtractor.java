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

package ch.systemsx.cisd.cina.shared.metadata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataParser;

/**
 * Class for extracting the metadata from a folder containing an image + metadata.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ImageMetadataExtractor implements IMetadataExtractor
{
    private final HashMap<String, String> metadataMap;

    private final File folder;

    private LVData lvdata;

    private final static String METADATA_FILE_NAME = "metadata.xml";

    private final static HashMap<String, String> prefixMap;
    static
    {
        prefixMap = new HashMap<String, String>();
        prefixMap.put("Pixelsize (nm)", CinaConstants.SIZE_PREFIX);
        prefixMap.put("Image dimension (px)", CinaConstants.DIMENSION_PREFIX);
    }

    public static boolean doesFolderContainImageMetadata(File folder)
    {
        File metadataFile = new File(folder, METADATA_FILE_NAME);
        return metadataFile.exists();
    }

    /**
     * Create an image metadata with a parent metadata.
     * 
     * @param parentMetadataOrNull The inherited parent metadata or null if there is none
     * @param folder The folder containing the metadata
     */
    public ImageMetadataExtractor(Map<String, String> parentMetadataOrNull, File folder)
    {

        this.metadataMap =
                (null != parentMetadataOrNull) ? new HashMap<String, String>(parentMetadataOrNull)
                        : new HashMap<String, String>();
        this.folder = folder;
    }

    /**
     * Read the files and fill the metadata map.
     * 
     * @throws IllegalArgumentException If there is not metadata.xml file in the folder or if the
     *             file is faulty.
     */
    public void prepare() throws IllegalArgumentException
    {
        if (null != lvdata)
        {
            // We've already parsed this
            return;
        }

        if (false == doesFolderContainImageMetadata(folder))
        {
            throw new IllegalArgumentException("Folder " + folder
                    + " is not an image metadata file");
        }

        File metadataFile = new File(folder, METADATA_FILE_NAME);
        lvdata = LVDataParser.parse(metadataFile);
        if (null == lvdata)
        {
            throw new IllegalArgumentException("Could not parse metadata in file "
                    + metadataFile.getAbsolutePath());
        }

        new LabViewXMLToHashMap(lvdata, metadataMap, prefixMap).appendIntoMap();
    }

    /**
     * Get the metadata extracted from the file in the form of a map. The method {@link #prepare}
     * must be called before getting the metadata map.
     */
    public Map<String, String> getMetadataMap()
    {
        checkPrepared();
        return metadataMap;
    }

    public File getFolder()
    {
        return folder;
    }

    private void checkPrepared()
    {
        assert lvdata != null;
    }
}
