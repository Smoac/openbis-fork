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

package ch.systemsx.cisd.cina.dss.info;

import java.io.File;
import java.io.FilenameFilter;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * The Folder Oracle knows the naming conventions for the different types of data provided by CINA
 * and can tell clients which of the types this data actually is.
 * <p>
 * Instances of this class are designed to be used within a single thread.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class FolderOracle
{
    /**
     * The different kinds of metadata known to the oracle.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static enum FolderType
    {
        EXPERIMENT, SAMPLE, DATA_SET, UNKNOWN;
    }

    /**
     * A little helper class (struct) that just stores a file and metadata type. If the metadata
     * type is UNKNOWN, the file will be null. Otherwise, the file will be non-null.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class FolderMetadata
    {
        private final FolderType type;

        private final File metadataFileOrNull;

        FolderMetadata(FolderType type, File metadataFile)
        {
            this.type = type;
            this.metadataFileOrNull = metadataFile;
        }

        FolderType getType()
        {
            return type;
        }

        File tryGetMetadataFile()
        {
            return metadataFileOrNull;
        }
    }

    // The known conventions for naming metadata files. If a new metadata file is created, cases
    // should be added to getFilenameFilter() and getFolderMetadataForFile() as well.
    static String EXPERIMENT_METADATA_FILENAME = "experiment.properties";

    static String SAMPLE_METADATA_FILENAME = "sample.properties";

    static String DATA_SET_METADATA_FILENAME = "dataset.properties";

    FolderOracle()
    {

    }

    /**
     * Scan the folder and return the metadata file for the folder along with its associated type.
     * <p>
     * Returns type UNKNOWN if the folder cannot be identified and throws an error if the
     * identification is ambiguous.
     */
    FolderMetadata getMetadataForFolder(File incomingDataSetFolder) throws UserFailureException
    {
        // Don't know what to do with this file
        if (!incomingDataSetFolder.isDirectory())
        {
            return new FolderMetadata(FolderType.UNKNOWN, null);
        }

        File[] metadata = incomingDataSetFolder.listFiles(getFilenameFilter());

        // Didn't find any metadata
        if (metadata.length < 1)
        {
            return new FolderMetadata(FolderType.UNKNOWN, null);
        }

        // Found too much metadata
        if (metadata.length > 1)
        {
            StringBuffer errorDescription = new StringBuffer();
            errorDescription.append("Folder (" + incomingDataSetFolder
                    + ") contains multiple description files: ");
            for (File file : metadata)
            {
                errorDescription.append("\n\t");
                errorDescription.append(file);
            }
            throw new UserFailureException(errorDescription.toString());
        }

        return getFolderMetadataForFile(metadata[0]);
    }

    /**
     * Returns the type of data stored in the folder
     */
    FolderType getTypeForFolder(File incomingDataSetFolder) throws UserFailureException
    {
        FolderMetadata metadata = getMetadataForFolder(incomingDataSetFolder);
        return metadata.getType();
    }

    /**
     * After it has been determined that the folder contains metadata for some known type, this
     * method can be invoked to return a folder metadata object.
     */
    private FolderMetadata getFolderMetadataForFile(File metadataFile)
    {
        String filename = metadataFile.getName();
        if (EXPERIMENT_METADATA_FILENAME.equals(filename))
        {
            return new FolderMetadata(FolderType.EXPERIMENT, metadataFile);
        }

        if (SAMPLE_METADATA_FILENAME.equals(filename))
        {
            return new FolderMetadata(FolderType.SAMPLE, metadataFile);
        }

        if (DATA_SET_METADATA_FILENAME.equals(filename))
        {
            return new FolderMetadata(FolderType.DATA_SET, metadataFile);
        }

        throw new EnvironmentFailureException(
                "A metadata file convention was added without modifing getFolderMetadataForFile()");
    }

    private FilenameFilter getFilenameFilter()
    {
        return new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    if (EXPERIMENT_METADATA_FILENAME.equals(name))
                    {
                        return true;
                    } else if (SAMPLE_METADATA_FILENAME.equals(name))
                    {
                        return true;
                    } else if (DATA_SET_METADATA_FILENAME.equals(name))
                    {
                        return true;
                    } else
                    {
                        return false;
                    }
                }
            };
    }
}
