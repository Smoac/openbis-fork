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

package ch.systemsx.cisd.bds.container;

import java.io.File;

import ch.systemsx.cisd.bds.IDataStructure;
import ch.systemsx.cisd.bds.IDataStructureFactory;
import ch.systemsx.cisd.bds.fs.FileDataStructureFactory;
import ch.systemsx.cisd.bds.hdf5.HDF5DataStructureFactory;
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
    
    public IDataStructure load(String name)
    {
        File file = new File(baseDir, name);
        if (file.exists() == false)
        {
            throw new UserFailureException("No container name '" + name + "' exists in " + baseDir.getAbsolutePath());
        }
        IDataStructureFactory dataStructureFactory;
        if (file.isDirectory())
        {
            dataStructureFactory = new FileDataStructureFactory(baseDir);
        } else if (new File(baseDir, name + ".hdf5").exists())
        {
            dataStructureFactory = new HDF5DataStructureFactory(baseDir);
        } else
        {
            throw new UserFailureException("Couldn't found appropriate container named '" + name + "' in "
                    + baseDir.getAbsolutePath());
        }
        return dataStructureFactory.createDataStructure(name, null);
    }
}
