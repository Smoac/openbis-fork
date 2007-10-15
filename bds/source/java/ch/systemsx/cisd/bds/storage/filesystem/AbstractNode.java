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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractNode implements INode
{
    protected final File nodeFile;

    AbstractNode(File file)
    {
        if (file == null)
        {
            throw new UserFailureException("Unspecified file");
        }
        if (file.exists() == false)
        {
            throw new UserFailureException("Non existing file " + file);
        }
        this.nodeFile = file;
    }
    
    public String getName()
    {
        return nodeFile.getName();
    }

    public IDirectory tryToGetParent()
    {
        File dir = nodeFile.getParentFile();
        return dir == null ? null : new Directory(dir);
    }

    @Override
    public String toString()
    {
        return nodeFile.getAbsolutePath();
    }
    
    
    
}
