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

package ch.systemsx.cisd.bds.storage.hdf5;

import java.io.File;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IStorage;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class HDF5Storage implements IStorage
{
    public HDF5Storage(File hdf5File)
    {
        assert hdf5File != null : "Unspecified HDF5 file.";
    }
    
    public IDirectory getRoot()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void load()
    {
        // TODO Auto-generated method stub

    }

    public void save()
    {
        // TODO Auto-generated method stub

    }

}
