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

package ch.systemsx.cisd.etlserver.hdf5;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.hdf5.HDF5Container;
import ch.systemsx.cisd.common.hdf5.HierarchicalStructureDuplicatorFileToHDF5;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Storage processor that keeps data in an HDF5 container on the file system.
 * <p>
 * In the data store, the data set is stored in a file named container.h5 .
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class HDF5StorageProcessor extends AbstractStorageProcessor
{
    private static final String HDF5_CONTAINER_FILE_NAME = "container.h5";

    public final static String COMPRESS_DATA_PROPERTY = "compress-data";

    private final boolean isDataCompressed;

    /**
     * Constructor.
     * 
     * @param properties
     */
    public HDF5StorageProcessor(Properties properties)
    {
        super(properties);
        isDataCompressed = PropertyUtils.getBoolean(properties, COMPRESS_DATA_PROPERTY, false);
    }

    public IStorageProcessorTransaction createTransaction()
    {
        return new IStorageProcessorTransaction()
            {
                private File storedDirectory;

                // The file that we are currently processing -- need to store this to implement
                // commit
                private File fileBeingProcessed;

                /**
                 * Write the content of the incomingDataSetDirectory into an HDF5 container with the
                 * same hierarchical structure as the incomingDataSetDirectory. Don't delete the
                 * incomingDataSetDirectory yet (this happens in @link{#commit}).
                 */
                public void storeData(DataSetInformation dataSetInformation,
                        ITypeExtractor typeExtractor, IMailClient mailClient,
                        File incomingDataSetDirectory, File rootDir)
                {
                    checkParameters(incomingDataSetDirectory, rootDir);

                    HDF5Container container = getHdf5Container(rootDir);
                    container.runWriterClient(isDataCompressed,
                            new HierarchicalStructureDuplicatorFileToHDF5.DuplicatorWriterClient(
                                    incomingDataSetDirectory));

                    fileBeingProcessed = incomingDataSetDirectory;
                    storedDirectory = rootDir;
                }

                public UnstoreDataAction rollback(Throwable exception)
                {
                    // Just delete the file in the store -- no need to touch the incomingDataSet
                    // because we haven't done anything to it.
                    File storedFile = getHDF5ContainerFile(storedDirectory);
                    storedFile.delete();

                    fileBeingProcessed = null;

                    return getDefaultUnstoreDataAction(exception);
                }

                public void commit()
                {
                    // fileBeingProcessed cannot be null at this point
                    FileUtilities.deleteRecursively(fileBeingProcessed);
                    fileBeingProcessed = null;
                }

                public File getStoredDataDirectory()
                {
                    return storedDirectory;
                }

                public File tryGetProprietaryData()
                {
                    // We don't keep data around in the original format -- only in an HDF5
                    // container.
                    return null;
                }

            };
    }

    /**
     * Given a directory in the store, return the HDF5 container file.
     * 
     * @return A file with HDF5 content
     */
    public static File getHDF5ContainerFile(final File storedDataDirectory)
    {
        return new File(storedDataDirectory, HDF5_CONTAINER_FILE_NAME);
    }

    /**
     * Given a directory in the store, return an {@link HDF5Container} object wrapping the file.
     * 
     * @return An HDF5Container object.
     */
    public static HDF5Container getHdf5Container(final File storedDataDirectory)
    {
        return new HDF5Container(getHDF5ContainerFile(storedDataDirectory));
    }

}
