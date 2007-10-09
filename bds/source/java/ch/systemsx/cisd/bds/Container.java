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

import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.bds.storage.filesystem.FileStorage;
import ch.systemsx.cisd.bds.storage.hdf5.HDF5Storage;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Container
{
    private final File baseDir;

    public Container(File baseDir)
    {
        assert baseDir != null : "Unspecified base directory.";
        assert baseDir.isDirectory() : "Is not a directory : " + baseDir.getAbsolutePath();
        this.baseDir = baseDir;
    }
    
    public AbstractDataStructure load(String name)
    {
        IStorage storage = createStorage(name);
        storage.load();
        Version version = Version.loadFrom(storage.getRoot());
        return DataStructureFactory.createDataStructure(storage, version);
    }
    
    private IStorage createStorage(String name)
    {
        File file = new File(baseDir, name);
        if (file.exists() == false)
        {
            throw new UserFailureException("No container name '" + name + "' exists in " + baseDir.getAbsolutePath());
        }
        if (file.isDirectory())
        {
            return new FileStorage(file);
        }
        File hdf5File = new File(baseDir, name + ".hdf5");
        if (hdf5File.exists())
        {
            return new HDF5Storage(hdf5File);
        }
        throw new UserFailureException("Couldn't found appropriate container named '" + name + "' in "
                + baseDir.getAbsolutePath());
        
    }
}
