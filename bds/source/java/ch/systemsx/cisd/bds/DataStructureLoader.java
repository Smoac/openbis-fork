/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import java.io.File;

import ch.systemsx.cisd.bds.IDataStructure.Mode;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.bds.storage.hdf5.HDF5Storage;

/**
 * Loader for {@link IDataStructure}s from the file system.
 * <p>
 * Use {@link DataStructureFactory} to get an instance of {@link IDataStructure} appropriate for the
 * version found.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class DataStructureLoader
{
    private final File baseDir;

    /**
     * Creates an instance for the specified base directory where all data structures to be loaded
     * have to exist.
     */
    public DataStructureLoader(final File baseDir)
    {
        assert baseDir != null : "Unspecified base directory.";
        assert baseDir.isDirectory() : "Is not a directory : " + baseDir.getAbsolutePath();
        this.baseDir = baseDir;
    }

    /**
     * Loads the data structure with specified name. Validates the structure on request.
     * 
     * @return an unmodifiable {@link IDataStructure} (opened in {@link Mode#READ_ONLY READ_ONLY}
     *         mode).
     */
    public final IDataStructure load(final String name, boolean validate)
    {
        final IStorage storage = createStorage(name);
        storage.mount();
        final Version version = Version.loadFrom(storage.getRoot());
        final IDataStructure dataStructure =
                DataStructureFactory.createDataStructure(storage, version);
        dataStructure.open(Mode.READ_ONLY, validate);
        return dataStructure;
    }

    private final IStorage createStorage(final String name)
    {
        final File file = new File(baseDir, name);
        if (file.exists() == false)
        {
            throw new DataStructureException("No container name '" + name + "' exists in "
                    + baseDir.getAbsolutePath());
        }
        if (file.isDirectory())
        {
            return new FileStorage(file);
        }
        final File hdf5File = new File(baseDir, name + ".hdf5");
        if (hdf5File.exists())
        {
            return new HDF5Storage(hdf5File);
        }
        throw new DataStructureException("Couldn't found appropriate container named '" + name
                + "' in " + baseDir.getAbsolutePath());
    }
}
