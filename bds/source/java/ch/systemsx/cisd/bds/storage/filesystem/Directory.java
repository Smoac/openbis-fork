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

import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * An <code>IDirectory</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
final class Directory extends AbstractNode implements IDirectory
{

    Directory(final java.io.File directory)
    {
        super(directory);
        if (directory.isDirectory() == false)
        {
            throw new StorageException(String.format("Not a directory '%s'.", directory
                    .getAbsolutePath()));
        }
    }

    private final static java.io.File getNodeFile(final INode node)
    {
        assert node instanceof AbstractNode : "Must be an instance of AbstractNode.";
        return ((AbstractNode) node).nodeFile;
    }

    private final static String cleanName(final String name)
    {
        final int index = name.indexOf(Constants.PATH_SEPARATOR);
        if (index == 0)
        {
            return name.substring(1);
        }
        return name;
    }

    //
    // IDirectory
    //

    public final INode tryGetNode(final String name)
    {
        assert name != null : "Given name can not be null.";
        final String path = cleanName(name.replace('\\', Constants.PATH_SEPARATOR));

        java.io.File childrenNodeFile = new java.io.File(this.nodeFile, path);
        if (childrenNodeFile.exists())
        {
            return NodeFactory.createNode(childrenNodeFile);
        } else
        {
            return null;
        }
    }

    public final IDirectory makeDirectory(final String name)
    {
        assert name != null : "Given name can not be null.";
        java.io.File dir = new java.io.File(nodeFile, name);
        if (dir.exists())
        {
            if (dir.isDirectory() == false)
            {
                throw new StorageException("There already exists a file named '" + name
                        + "' in directory " + this);
            }
            return new Directory(dir);
        }
        boolean successful = dir.mkdir();
        if (successful == false)
        {
            throw new EnvironmentFailureException("Couldn't create directory "
                    + dir.getAbsolutePath() + " for some unknown reason.");
        }
        return new Directory(dir);
    }

    public final IFile addKeyValuePair(final String key, final String value)
    {
        assert key != null : "Given key can not be null.";
        if (value == null)
        {
            throw new IllegalArgumentException("Value for key '" + key + "' not specified.");
        }
        java.io.File file = new java.io.File(nodeFile, key);
        FileUtilities.writeToFile(file, value);
        return new File(file);
    }

    public final INode addFile(final java.io.File file, final String name, final boolean move)
    {
        checkFile(file);
        final String fileName;
        if (name == null)
        {
            fileName = file.getName();
        } else
        {
            fileName = name;
        }
        final java.io.File newFile = new java.io.File(nodeFile, fileName);
        if (move)
        {
            moveFileToDirectory(file, nodeFile, name);
        } else
        {
            try
            {
                if (file.isDirectory())
                {
                    FileUtils.copyDirectory(file, newFile);
                } else
                {
                    FileUtils.copyFile(file, newFile);
                }
            } catch (IOException ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex,
                        "Couldn't not copy file '%s' to directory '%s'.", file, nodeFile
                                .getAbsolutePath());
            }
        }
        return NodeFactory.createNode(newFile);
    }

    public final ILink tryAddLink(final String name, final INode node)
    {
        assert node != null : "Node can not be null.";
        assert name != null : "Name can not be null.";
        final java.io.File file = getNodeFile(node);
        final boolean ok =
                LinkMakerProvider.getLinkMaker().copyImmutably(file, nodeFile, name);
        if (ok)
        {
            final Link link = (Link) NodeFactory.createLinkNode(name, file);
            link.setParent(this);
            return link;
        }
        return null;
    }

    public final Iterator<INode> iterator()
    {
        return new Iterator<INode>()
            {
                private java.io.File[] files = FileUtilities.listFiles(nodeFile);

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

    public final void extractTo(final java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null : "Directory could not be null";
        // ...but might not exist
        try
        {
            FileUtils.copyDirectoryToDirectory(nodeFile, directory);
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex,
                    "Couldn't copy directory '%s' to directory '%s'.", nodeFile.getAbsolutePath(),
                    directory.getAbsolutePath());
        }
    }

    public final void removeNode(final INode node)
    {
        assert node != null : "Node could not be null";
        final java.io.File file = getNodeFile(node);
        if (file.isDirectory())
        {
            if (FileUtilities.deleteRecursively(file) == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't remove directory '%s'.",
                        file.getAbsolutePath());
            }
        } else if (file.isFile())
        {
            if (file.delete() == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't remove file '%s'.", file
                        .getAbsolutePath());
            }
        }
    }

    @Override
    public final boolean isValid()
    {
        return super.isValid() && FileUtilities.checkDirectoryFullyAccessible(nodeFile, "") == null;
    }
}
