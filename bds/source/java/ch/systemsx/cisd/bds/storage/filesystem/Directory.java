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
import java.util.Iterator;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Directory extends AbstractNode implements IDirectory
{
    Directory(File directory)
    {
        super(directory);
        assert directory.isDirectory() : "Not a directory: " + directory.getAbsolutePath();
    }
    
    public INode getNode(String name)
    {
        File[] files = nodeFile.listFiles();
        for (File file : files)
        {
            if (file.getName().equals(name))
            {
                return NodeFactory.createNode(file);
            }
        }
        return null;
    }

    public IDirectory makeDirectory(String name)
    {
        File dir = new File(nodeFile, name);
        if (dir.exists())
        {
            throw new UserFailureException("There already exists a file named '" + name + "' directory " + this);
        }
        boolean successful = dir.mkdir();
        if (successful == false)
        {
            throw new EnvironmentFailureException("Couldn't create directory " + dir.getAbsolutePath()
                                                  + " for some unknown reason.");
        }
        return new Directory(dir);
    }

    public IFile<String> addKeyValuePair(String key, String value)
    {
        File file = new File(nodeFile, key);
        FileUtilities.writeToFile(file, value);
        return new StringFile(file);
    }

    public IFile<File> addRealFile(File file)
    {
        File newFile = new File(nodeFile, file.getName());
        FileUtilities.copyFileTo(file, newFile, true);
        return new FileFile(newFile);
    }

    public ILink addLink(String name, INode node)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator<INode> iterator()
    {
        return new Iterator<INode>()
            {
                private File[] files = nodeFile.listFiles();
                private int index;
                
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
        
                public INode next()
                {
                    return index >= files.length ? null : NodeFactory.createNode(files[index++]);
                }
        
                public boolean hasNext()
                {
                    return index < files.length;
                }
            };
    }

    public void extractTo(File directory) throws UserFailureException, EnvironmentFailureException
    {
        File destination = new File(directory, getName());
        if (destination.mkdirs() == false)
        {
            throw new EnvironmentFailureException("Couldn't create directory for some unknown reason: "
                    + destination.getAbsolutePath());
        }
        for (INode node : this)
        {
            node.extractTo(destination);
        }
    }

}
