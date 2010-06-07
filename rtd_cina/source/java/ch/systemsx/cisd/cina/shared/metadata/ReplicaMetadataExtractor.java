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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataParser;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ReplicaMetadataExtractor
{
    private final ArrayList<ImageMetadataExtractor> metadataExtractors;

    private final File folder;

    private final HashMap<String, String> metadataMap;

    private LVData lvdata;

    private static final String METADATA_FILE_NAME = "ReplicaMetadata.xml";

    public static boolean doesFolderContainReplicaMetadata(File folder)
    {
        File metadataFile = new File(folder, METADATA_FILE_NAME);
        return metadataFile.exists();
    }

    public ReplicaMetadataExtractor(File folder)
    {
        metadataExtractors = new ArrayList<ImageMetadataExtractor>();
        this.folder = folder;
        this.metadataMap = new HashMap<String, String>();
    }

    /**
     * Read the files and fill the metadata map.
     */
    public void prepare()
    {
        if (null != lvdata)
        {
            // We've already parsed this
            return;
        }

        // Firse parse the metadata for the replica
        File metadataFile = new File(folder, METADATA_FILE_NAME);
        lvdata = LVDataParser.parse(metadataFile);
        if (null == lvdata)
        {
            throw new IllegalArgumentException("Could not parse metadata in file "
                    + metadataFile.getAbsolutePath());
        }

        new LabViewXMLToHashMap(lvdata, metadataMap).appendIntoMap();

        // Then get the image metadata
        File[] replicaContents = folder.listFiles();
        for (File replicaFile : replicaContents)
        {
            if (false == replicaFile.isDirectory())
            {
                continue;
            }

            // The folders containing the metadata are two levels down
            File[] imageContents = replicaFile.listFiles();
            for (File imageFolder : imageContents)
            {
                if (false == imageFolder.isDirectory())
                {
                    continue;
                }

                processDirectory(imageFolder);
            }
        }
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

    /**
     * Get the metadata extractors for each of the image files for this replica. The method
     * {@link #prepare} must be called before getting the metadata extractors.
     */
    public List<ImageMetadataExtractor> getMetadataExtractors()
    {
        checkPrepared();
        return metadataExtractors;
    }

    private void checkPrepared()
    {
        assert lvdata != null;
    }

    private void processDirectory(File file)
    {
        if (false == file.isDirectory())
        {
            return;
        }

        if (false == ImageMetadataExtractor.doesFolderContainImageMetadata(file))
        {
            return;
        }

        ImageMetadataExtractor imageMetadataExtractor = new ImageMetadataExtractor(null, file);
        imageMetadataExtractor.prepare();

        metadataExtractors.add(imageMetadataExtractor);
    }
}
