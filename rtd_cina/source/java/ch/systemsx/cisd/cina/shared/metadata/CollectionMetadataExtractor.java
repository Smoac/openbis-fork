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

import ch.systemsx.cisd.cina.shared.constants.BundleStructureConstants;
import ch.systemsx.cisd.cina.shared.labview.LVData;
import ch.systemsx.cisd.cina.shared.labview.LVDataParser;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CollectionMetadataExtractor implements IMetadataExtractor
{
    private static final String REPLICA_METADATA_FILE_NAME =
            BundleStructureConstants.COLLECTION_METADATA_FILE_NAME;

    private static final String REPLICA_SAMPLE_CODE_KEY =
            BundleStructureConstants.COLLECTION_SAMPLE_CODE_KEY;

    private static final String REPLICA_SAMPLE_DESCRIPTION_KEY =
            BundleStructureConstants.COLLECTION_SAMPLE_DESCRIPTION_KEY;

    private static final String REPLICA_SAMPLE_CREATOR_NAME =
            BundleStructureConstants.COLLECTION_SAMPLE_CREATOR_NAME;

    private final ArrayList<ImageMetadataExtractor> metadataExtractors;

    private final File folder;

    private final HashMap<String, String> metadataMap;

    private LVData lvdata;

    public static boolean doesFolderContainReplicaMetadata(File folder)
    {
        File metadataFile = new File(folder, REPLICA_METADATA_FILE_NAME);
        return metadataFile.exists();
    }

    public CollectionMetadataExtractor(File folder)
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

        // First parse the metadata for the replica
        File metadataFile = new File(folder, REPLICA_METADATA_FILE_NAME);
        lvdata = LVDataParser.parse(metadataFile);
        if (null == lvdata)
        {
            throw new IllegalArgumentException("Could not parse metadata in file "
                    + metadataFile.getAbsolutePath());
        }

        new LabViewXMLToHashMap(lvdata, metadataMap).appendIntoMap();

        // Then get the image metadata
        File[] collectionContents = folder.listFiles();
        for (File collectionFolder : collectionContents)
        {
            if (false == collectionFolder.isDirectory())
            {
                continue;
            }

            processCollectionItem(collectionFolder);
        }
    }

    /**
     * Get the metadata extracted from the file in the form of a map. The method {@link #prepare} must be called before getting the metadata map.
     */
    @Override
    public Map<String, String> getMetadataMap()
    {
        checkPrepared();
        return metadataMap;
    }

    /**
     * Get the metadata extractors for each of the image files for this replica. The method {@link #prepare} must be called before getting the
     * metadata extractors.
     */
    public List<ImageMetadataExtractor> getImageMetadataExtractors()
    {
        checkPrepared();
        return metadataExtractors;
    }

    /**
     * Return the sample code for the replica sample.
     * 
     * @return Return the code, or null if none was found
     */
    public String tryReplicaSampleCode()
    {
        checkPrepared();
        return metadataMap.get(REPLICA_SAMPLE_CODE_KEY);
    }

    /**
     * Return the description for the replica sample.
     * 
     * @return Return the code, or null if none was found
     */
    public String tryReplicaSampleDescription()
    {
        checkPrepared();
        return metadataMap.get(REPLICA_SAMPLE_DESCRIPTION_KEY);
    }

    /**
     * Return the creator name for the replica sample.
     * 
     * @return Return the code, or null if none was found
     */
    public String tryReplicaSampleCreatorName()
    {
        checkPrepared();
        return metadataMap.get(REPLICA_SAMPLE_CREATOR_NAME);
    }

    /**
     * The name of this collection. This is determined by the name of the folder in the bundle.
     */
    public String getCollectionName()
    {
        return folder.getParentFile().getName();
    }

    public File getFolder()
    {
        return folder;
    }

    private void checkPrepared()
    {
        assert lvdata != null;
    }

    private void processCollectionItem(File file)
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
