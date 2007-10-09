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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Directory extends AbstractNode implements IDirectory
{
    public Directory(File directory)
    {
        super(directory);
        assert directory.isDirectory() : "Not a directory: " + directory.getAbsolutePath();
    }
    
    public INode getNode(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public IDirectory appendDirectory(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void appendKeyValuePair(String key, String value)
    {
        File file = new File(fileNode, key);
        try
        {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(value);
            fileWriter.close();
        } catch (IOException ex)
        {
            file.delete();
            throw new EnvironmentFailureException("Can not create " + file.getAbsolutePath() + ": " + ex);
        }
    }

    public void appendNode(INode node)
    {
        // TODO Auto-generated method stub

    }

    public void appendRealFile(File file)
    {
        File newFile = new File(fileNode, file.getName());
        if (file.renameTo(newFile) == false)
        {
            throw new EnvironmentFailureException("Couldn't move file " + file.getAbsolutePath() + " to "
                    + fileNode.getAbsolutePath());
        }
    }

    public void appendLink(String name, INode node)
    {
        // TODO Auto-generated method stub
        
    }

    public Iterator<INode> iterator()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void extractTo(File directory) throws UserFailureException, EnvironmentFailureException
    {
        // TODO Auto-generated method stub
        
    }

}
