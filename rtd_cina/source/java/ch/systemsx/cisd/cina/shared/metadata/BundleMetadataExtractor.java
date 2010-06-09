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
import java.util.List;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class BundleMetadataExtractor
{
    private static final String METADATA_FOLDER_NAME = "Annotations";

    private final ArrayList<ReplicaMetadataExtractor> metadataExtractors;

    private final File bundle;

    private boolean hasBeenPrepared = false;

    /**
     * Create a metadata extractor for the bundle located at bundle
     * 
     * @param bundle The bundle file
     */
    public BundleMetadataExtractor(File bundle)
    {
        this.bundle = bundle;
        metadataExtractors = new ArrayList<ReplicaMetadataExtractor>();
    }

    /**
     * Read the files
     */
    public void prepare()
    {
        if (hasBeenPrepared)
        {
            // we've already prepared
            return;
        }

        File metadataFolder = new File(bundle, METADATA_FOLDER_NAME);

        // Then get the replica metadata
        File[] replicaContents = metadataFolder.listFiles();
        for (File replicaFile : replicaContents)
        {
            if (false == replicaFile.isDirectory())
            {
                continue;
            }

            processDirectory(replicaFile);
        }

        hasBeenPrepared = true;
    }

    /**
     * Get the metadata extractors for each of the replica files in this bundle. The method
     * {@link #prepare} must be called before getting the metadata extractors.
     */
    public List<ReplicaMetadataExtractor> getMetadataExtractors()
    {
        assert hasBeenPrepared;
        return metadataExtractors;
    }

    private void processDirectory(File file)
    {
        if (false == file.isDirectory())
        {
            return;
        }

        if (false == ReplicaMetadataExtractor.doesFolderContainReplicaMetadata(file))
        {
            return;
        }

        ReplicaMetadataExtractor replicaMetadataExtractor = new ReplicaMetadataExtractor(file);
        replicaMetadataExtractor.prepare();

        metadataExtractors.add(replicaMetadataExtractor);
    }

}
