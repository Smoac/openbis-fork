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

import ch.systemsx.cisd.cina.shared.labview.Cluster;
import ch.systemsx.cisd.cina.shared.labview.DBL;
import ch.systemsx.cisd.cina.shared.labview.EW;
import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataBoolean;
import ch.systemsx.cisd.cina.shared.labview.LVDataParser;
import ch.systemsx.cisd.cina.shared.labview.LVDataString;
import ch.systemsx.cisd.cina.shared.labview.U32;
import ch.systemsx.cisd.cina.shared.labview.U8;

/**
 * Class for extracting the metadata from a folder containing an image + metadata.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ImageMetadataExtractor
{
    private final Map<String, String> metadataMap;

    private final File folder;

    private LVData lvdata;

    private static final String METADATA_FILE_NAME = "metadata.xml";

    private static final HashMap<String, String> prefixMap;
    static
    {
        prefixMap = new HashMap<String, String>();
        prefixMap.put("Pixelsize (nm)", "Size");
        prefixMap.put("Image dimension (px)", "Dimension");
    }

    /**
     * Create an image metadata with a parent metadata.
     * 
     * @param parentMetadata The inherited parent metadata
     * @param folder The folder containing the metadata
     */
    public ImageMetadataExtractor(Map<String, String> parentMetadata, File folder)
    {
        this.metadataMap = new HashMap<String, String>(parentMetadata);
        this.folder = folder;
    }

    /**
     * Get the metadata extracted from the file in the form of a hash map. Parse must be called
     * before getting the metadata.
     */
    public Map<String, String> getMetadataMap()
    {
        assert lvdata != null;
        return metadataMap;
    }

    /**
     * Parse the metadata file.
     */
    public void parse()
    {
        File metadataFile = new File(folder, METADATA_FILE_NAME);
        lvdata = LVDataParser.parse(metadataFile);
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
        parseCluster(cluster, "");
    }

    private void parseCluster(Cluster cluster, String prefix)
    {
        for (LVDataString lvdataString : cluster.getStrings())
        {
            metadataMap.put(prefix + lvdataString.getName(), lvdataString.getValue());
        }

        for (U8 u8 : cluster.getU8s())
        {
            metadataMap.put(prefix + u8.getName(), u8.getValue().toString());
        }

        for (U32 u32 : cluster.getU32s())
        {
            metadataMap.put(prefix + u32.getName(), u32.getValue().toString());
        }

        for (DBL dbl : cluster.getDbls())
        {
            metadataMap.put(prefix + dbl.getName(), dbl.getValue().toString());
        }

        for (LVDataBoolean bool : cluster.getBooleans())
        {
            metadataMap.put(prefix + bool.getName(), bool.getValue().toString());
        }

        for (EW ew : cluster.getEws())
        {
            metadataMap.put(prefix + ew.getName(), ew.getChosenValue());
        }

        for (Cluster subcluster : cluster.getClusters())
        {
            String clusterPrefix = prefixMap.get(subcluster.getName());
            if (null == clusterPrefix)
            {
                clusterPrefix = "";
            }
            parseCluster(subcluster, clusterPrefix);
        }
    }
}
