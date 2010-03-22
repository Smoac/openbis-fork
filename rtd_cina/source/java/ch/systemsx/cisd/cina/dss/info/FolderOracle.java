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
     * A little helper class for storing the location and type of the sidecar files for a folder. If
     * the type is UNKNOWN, the files will be null. If the type is known, the marker file will be
     * non-null. The metadata file might still be null.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class FolderMetadata
    {
        private final FolderType type;

        private final File markerFileOrNull;

        private final File metadataXMLFileOrNull;

        FolderMetadata(FolderType type, File markerFile, File metadataXMLFile)
        {
            this.type = type;
            this.markerFileOrNull = markerFile;
            this.metadataXMLFileOrNull = metadataXMLFile;
        }

        /**
         * The type of the folder.
         */
        FolderType getType()
        {
            return type;
        }

        /**
         * The marker file for this folder -- this is a properties file.
         */
        File tryGetMarkerFile()
        {
            return markerFileOrNull;
        }

        /**
         * The metadata file for this folder -- this is an XML file.
         */
        File tryGetMetadataXMLFile()
        {
            return metadataXMLFileOrNull;
        }
    }

    // The known conventions for naming metadata files. If a new metadata file is created, cases
    // should be added to getFilenameFilter() and getFolderMetadataForFile() as well.
    static String EXPERIMENT_MARKER_FILENAME = "experiment.properties";

    static String SAMPLE_MARKER_FILENAME = "sample.properties";

    static String DATA_SET_MARKER_FILENAME = "dataset.properties";

    static String METADATA_XML_FILENAME = "metadata.xml";

    FolderOracle()
    {

    }

    /**
     * Scan the folder and return the metadata file for the folder along with its associated type.
     * <p>
     * Returns type UNKNOWN if the folder cannot be identified and throws an error if the
     * identification is ambiguous.
     */
    FolderMetadata getFolderMetadataForFolder(File incomingDataSetFolder)
            throws UserFailureException
    {
        // Don't know what to do with this file
        if (!incomingDataSetFolder.isDirectory())
        {
            return new FolderMetadata(FolderType.UNKNOWN, null, null);
        }

        File[] metadata = incomingDataSetFolder.listFiles(getMarkerFilenameFilter());

        // Didn't find any metadata
        if (metadata.length < 1)
        {
            return new FolderMetadata(FolderType.UNKNOWN, null, null);
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
        FolderMetadata metadata = getFolderMetadataForFolder(incomingDataSetFolder);
        return metadata.getType();
    }

    /**
     * After it has been determined that the folder contains metadata for some known type, this
     * method can be invoked to return a folder metadata object.
     */
    private FolderMetadata getFolderMetadataForFile(File metadataFile)
    {
        String filename = metadataFile.getName();
        File parentDir = metadataFile.getParentFile();
        File metadataXMLFileOrNull = null;
        if (parentDir != null)
        {

            File[] metadataXMLFiles = parentDir.listFiles(getMetadataXMLFilenameFilter());
            if (metadataXMLFiles.length > 0)
            {
                metadataXMLFileOrNull = metadataXMLFiles[0];
            }
        }

        if (EXPERIMENT_MARKER_FILENAME.equals(filename))
        {
            return new FolderMetadata(FolderType.EXPERIMENT, metadataFile, metadataXMLFileOrNull);
        }

        if (SAMPLE_MARKER_FILENAME.equals(filename))
        {
            return new FolderMetadata(FolderType.SAMPLE, metadataFile, metadataXMLFileOrNull);
        }

        if (DATA_SET_MARKER_FILENAME.equals(filename))
        {
            return new FolderMetadata(FolderType.DATA_SET, metadataFile, metadataXMLFileOrNull);
        }

        throw new EnvironmentFailureException(
                "A metadata file convention was added without modifing getFolderMetadataForFile()");
    }

    private FilenameFilter getMarkerFilenameFilter()
    {
        return new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    if (EXPERIMENT_MARKER_FILENAME.equals(name))
                    {
                        return true;
                    } else if (SAMPLE_MARKER_FILENAME.equals(name))
                    {
                        return true;
                    } else if (DATA_SET_MARKER_FILENAME.equals(name))
                    {
                        return true;
                    } else
                    {
                        return false;
                    }
                }
            };
    }

    private FilenameFilter getMetadataXMLFilenameFilter()
    {
        return new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    if (METADATA_XML_FILENAME.equals(name))
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
