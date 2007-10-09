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

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractNode implements INode
{
    protected final File fileNode;

    AbstractNode(File file)
    {
        assert file != null : "Unspecified file";
        this.fileNode = file;
    }
    
    public String getName()
    {
        return fileNode.getName();
    }

    public IDirectory tryToGetParent()
    {
        File dir = fileNode.getParentFile();
        return dir == null ? null : new Directory(dir);
    }
    
}
