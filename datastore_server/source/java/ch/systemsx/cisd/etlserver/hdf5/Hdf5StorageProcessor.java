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
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessor;
import ch.systemsx.cisd.etlserver.IStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * Storage processor that keeps data in an HDF5 container on the file system.
 * <p>
 * In the data store, the data set is stored in a file named container.h5 .
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class Hdf5StorageProcessor extends AbstractStorageProcessor
{
    private static final String HDF5_CONTAINER_FILE_NAME = "container.h5";

    public final static String COMPRESS_DATA_PROPERTY = "compress-data";

    private final boolean isDataCompressed;

    // The file that we are currently processing -- need to store this to implement commit
    private File fileBeingProcessed;

    /**
     * Constructor.
     * 
     * @param properties
     */
    public Hdf5StorageProcessor(Properties properties)
    {
        super(properties);
        isDataCompressed = PropertyUtils.getBoolean(properties, COMPRESS_DATA_PROPERTY, false);
        fileBeingProcessed = null;
    }

    /**
     * Write the content of the incomingDataSetDirectory into an HDF5 container with the same
     * hierarchical structure as the incomingDataSetDirectory. Don't delete the
     * incomingDataSetDirectory yet (this happens in @link{#commit}).
     * 
     * @see IStorageProcessor#storeData(DataSetInformation, ITypeExtractor, IMailClient, File, File)
     */
    public File storeData(DataSetInformation dataSetInformation, ITypeExtractor typeExtractor,
            IMailClient mailClient, File incomingDataSetDirectory, File rootDir)
    {
        checkParameters(incomingDataSetDirectory, rootDir);

        Hdf5Container container = getHdf5Container(rootDir);
        IHDF5SimpleWriter writer = container.createSimpleWriter(isDataCompressed);
        HierarchicalStructureDuplicatorFileToHdf5 duplicator =
                new HierarchicalStructureDuplicatorFileToHdf5(incomingDataSetDirectory, writer);

        duplicator.makeDuplicate();

        fileBeingProcessed = incomingDataSetDirectory;

        return rootDir;
    }

    @Override
    public void commit()
    {
        super.commit();

        // fileBeingProcessed cannot be null at this point
        FileUtilities.deleteRecursively(fileBeingProcessed);
        fileBeingProcessed = null;
    }

    /**
     * @see IStorageProcessor#rollback(File, File, Throwable)
     */
    public UnstoreDataAction rollback(File incomingDataSetDirectory, File storedDataDirectory,
            Throwable exception)
    {
        checkParameters(incomingDataSetDirectory, storedDataDirectory);

        // Just delete the file in the store -- no need to touch the incomingDataSet because we
        // haven't done anything to it.
        File storedFile = getHdf5ContainerFile(storedDataDirectory);
        storedFile.delete();

        fileBeingProcessed = null;

        return UnstoreDataAction.MOVE_TO_ERROR;
    }

    /**
     * @see IStorageProcessor#getStorageFormat()
     */
    public StorageFormat getStorageFormat()
    {
        return StorageFormat.HDF5;
    }

    /**
     * @see IStorageProcessor#tryGetProprietaryData(File)
     */
    public File tryGetProprietaryData(File storedDataDirectory)
    {
        // We don't keep data around in the original format -- only in an HDF5 container.
        return null;
    }

    /**
     * Given a directory in the store, return the HDF5 container file.
     * 
     * @return A file with HDF5 content
     */
    public static File getHdf5ContainerFile(final File storedDataDirectory)
    {
        return new File(storedDataDirectory, HDF5_CONTAINER_FILE_NAME);
    }

    /**
     * Given a directory in the store, return an {@link Hdf5Container} object wrapping the file.
     * 
     * @return An Hdf5Container object.
     */
    public static Hdf5Container getHdf5Container(final File storedDataDirectory)
    {
        return new Hdf5Container(getHdf5ContainerFile(storedDataDirectory));
    }

}
