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

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
class Directory extends AbstractNode implements IDirectory
{
    Directory(java.io.File directory)
    {
        super(directory);
        if (directory.isDirectory() == false)
        {
            throw new UserFailureException("Not a directory: " + directory.getAbsolutePath());
        }
    }

    public INode tryToGetNode(String name)
    {
        final java.io.File[] files = Utilities.listFiles(nodeFile);
        for (java.io.File file : files)
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
        java.io.File dir = new java.io.File(nodeFile, name);
        if (dir.exists())
        {
            if (dir.isDirectory() == false)
            {
                throw new UserFailureException("There already exists a file named '" + name + "' in directory " + this);
            }
            return new Directory(dir);
        }
        boolean successful = dir.mkdir();
        if (successful == false)
        {
            throw new EnvironmentFailureException("Couldn't create directory " + dir.getAbsolutePath()
                    + " for some unknown reason.");
        }
        return new Directory(dir);
    }

    public IFile addKeyValuePair(String key, String value)
    {
        java.io.File file = new java.io.File(nodeFile, key);
        FileUtilities.writeToFile(file, value);
        return new File(file);
    }

    public INode addFile(java.io.File file) throws UserFailureException, EnvironmentFailureException
    {
        INode node = NodeFactory.createNode(file);
        node.moveTo(nodeFile);
        return node;
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
                private java.io.File[] files = Utilities.listFiles(nodeFile);

                private int index;

                //
                // Iterator
                //

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

    public final void copyTo(final java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null;
        try
        {
            FileUtils.copyDirectoryToDirectory(nodeFile, directory);
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Couldn't copy directory '%s' to directory '%s'.",
                    nodeFile.getAbsolutePath(), directory.getAbsolutePath());
        }
    }

    public final void moveTo(java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null;
        directory.mkdirs();
        final java.io.File destination = new java.io.File(directory, getName());
        if (destination.exists() == false)
        {
            // Note that 'renameTo' does not change 'nodeFile' path
            final boolean successful = nodeFile.renameTo(destination);
            if (successful == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't not move directory '%s' to directory '%s'.",
                        nodeFile.getAbsolutePath(), directory.getAbsolutePath());
            }
            assert nodeFile.exists() == false;
        }
        if (nodeFile.equals(destination) == false)
        {
            nodeFile = destination;
        }
        // Update children
        for (INode node : this)
        {
            node.moveTo(destination);
        }
    }
}
