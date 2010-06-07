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
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.cina.shared.labview.Cluster;
import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataParser;
import ch.systemsx.cisd.cina.shared.labview.LVDataString;

/**
 * Class for extracting the metadata from a folder containing an image + metadata.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ImageMetadataExtractor
{
    private final Map<String, String> metadata;

    private final File folder;

    private static final String METADATA_FILE_NAME = "metadata.xml";

    /**
     * Create an image metadata with a parent metadata.
     * 
     * @param parentMetadata The inherited parent metadata
     * @param folder The folder containing the metadata
     */
    public ImageMetadataExtractor(Map<String, String> parentMetadata, File folder)
    {
        this.metadata = new HashMap<String, String>(parentMetadata);
        this.folder = folder;
    }

    public Map<String, String> getMetadata()
    {
        return metadata;
    }

    public void parse()
    {
        File metadataFile = new File(folder, METADATA_FILE_NAME);
        LVData lvdata = LVDataParser.parse(metadataFile);
        if (null == lvdata)
        {
            throw new IllegalArgumentException("Could not parse metadata in file "
                    + metadataFile.getAbsolutePath());
        }

        if (1 != lvdata.getClusters().size())
        {
            throw new IllegalArgumentException("Parser expects only one cluster in file "
                    + metadataFile.getAbsolutePath() + "; Found " + lvdata.getClusters().size());
        }

        Cluster cluster = lvdata.getClusters().get(0);
        List<LVDataString> lvdataStrings = cluster.getStrings();
        for (LVDataString lvdataString : lvdataStrings)
        {
            metadata.put(lvdataString.getName(), lvdataString.getValue());
        }
    }
}
