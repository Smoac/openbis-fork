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

package ch.systemsx.cisd.bds.fs;

import java.io.File;

import ch.systemsx.cisd.bds.AbstractDataStructureV1_0;
import ch.systemsx.cisd.bds.IDirectory;
import ch.systemsx.cisd.bds.IFormatedData;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DataStructureV1_0 extends AbstractDataStructureV1_0
{
    private final File baseDir;

    public DataStructureV1_0(String name, File baseDir)
    {
        super(name);
        assert baseDir != null : "Unspecified base directory.";
        assert baseDir.isDirectory() : "Is not a directory: " + baseDir.getAbsolutePath();
        this.baseDir = baseDir;
    }
    
    public IFormatedData getFormatedData()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IDirectory getOriginalData()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void save() throws EnvironmentFailureException
    {
        // TODO Auto-generated method stub
        System.out.println(baseDir);
        
    }
    
}
