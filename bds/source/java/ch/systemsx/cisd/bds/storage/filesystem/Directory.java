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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.bds.Constants;
import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.IFileBasedDirectory;
import ch.systemsx.cisd.bds.storage.IFileBasedLink;
import ch.systemsx.cisd.bds.storage.IFileBasedNode;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileOperations;

/**
 * An <code>IDirectory</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
final class Directory extends AbstractNode implements IFileBasedDirectory
{

    Directory(final java.io.File directory)
    {
        super(directory);
        if (FileOperations.getMonitoredInstanceForCurrentThread().isDirectory(directory) == false)
        {
            throw new StorageException(String.format("Not a directory '%s'.", directory
                    .getAbsolutePath()));
        }
    }

    private final static java.io.File getNodeFile(final INode node)
    {
        assert node instanceof IFileBasedNode : "Must be an instance of IFileBasedNode.";
        return ((IFileBasedNode) node).getNodeFile();
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

    public IDirectory tryAsDirectory()
    {
        return this;
    }

    public IFile tryAsFile()
    {
        return null;
    }

    //
    // IDirectory
    //

    public final INode tryGetNode(final String name)
    {
        assert name != null : "Given name can not be null.";
        final String path = cleanName(name.replace('\\', Constants.PATH_SEPARATOR));

        java.io.File childrenNodeFile = new java.io.File(this.nodeFile, path);
        if (FileOperations.getMonitoredInstanceForCurrentThread().exists(childrenNodeFile))
        {
            return NodeFactory.internalCreateNode(childrenNodeFile);
        } else
        {
            return null;
        }
    }

    public final IDirectory makeDirectory(final String name)
    {
        assert name != null : "Given name can not be null.";
        java.io.File dir = new java.io.File(nodeFile, name);
        if (FileOperations.getMonitoredInstanceForCurrentThread().exists(dir))
        {
            if (FileOperations.getMonitoredInstanceForCurrentThread().isDirectory(dir) == false)
            {
                throw new StorageException("There already exists a file named '" + name
                        + "' in directory " + this);
            }
            return NodeFactory.internalCreateDirectoryNode(dir);
        }
        boolean successful = FileOperations.getMonitoredInstanceForCurrentThread().mkdir(dir);
        if (successful == false)
        {
            throw new EnvironmentFailureException("Couldn't create directory "
                    + dir.getAbsolutePath() + " for some unknown reason.");
        }
        return NodeFactory.internalCreateDirectoryNode(dir);
    }

    public final IFile addKeyValuePair(final String key, final String value)
    {
        assert key != null : "Given key can not be null.";
        if (value == null)
        {
            throw new IllegalArgumentException("Value for key '" + key + "' not specified.");
        }
        java.io.File file = new java.io.File(nodeFile, key);
        FileOperations.getMonitoredInstanceForCurrentThread().writeToFile(file, value);
        return NodeFactory.internalCreateFileNode(file);
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
                if (FileOperations.getMonitoredInstanceForCurrentThread().isDirectory(file))
                {
                    FileOperations.getMonitoredInstanceForCurrentThread().copyDirectory(file,
                            newFile);
                } else
                {
                    FileOperations.getMonitoredInstanceForCurrentThread().copyFile(file, newFile);
                }
            } catch (IOExceptionUnchecked ex)
            {
                throw EnvironmentFailureException.fromTemplate(ex,
                        "Can not copy file '%s' to directory '%s'.", file, nodeFile
                                .getAbsolutePath());
            }
        }
        return NodeFactory.internalCreateNode(newFile);
    }

    public final ILink tryAddLink(final String name, final INode node)
    {
        assert node != null : "Node can not be null.";
        assert name != null : "Name can not be null.";
        final java.io.File file = getNodeFile(node);
        final boolean ok = LinkMakerProvider.getLinkMaker().copyImmutably(file, nodeFile, name);
        if (ok)
        {
            final IFileBasedLink link = (IFileBasedLink) NodeFactory.createLinkNode(name, file);
            link.setParent(this);
            return link;
        }
        return null;
    }

    public final Iterator<INode> iterator()
    {
        return new Iterator<INode>()
            {
                private List<java.io.File> files =
                        FileOperations.getMonitoredInstanceForCurrentThread()
                                .listFilesAndDirectories(nodeFile, false);

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
                    return index >= files.size() ? null : NodeFactory.internalCreateNode(files
                            .get(index++));
                }

                public boolean hasNext()
                {
                    return index < files.size();
                }
            };
    }

    public final void extractTo(final java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null : "Directory could not be null";
        // ...but might not exist
        try
        {
            FileOperations.getMonitoredInstanceForCurrentThread().copyDirectoryToDirectory(
                    nodeFile, directory);
        } catch (IOExceptionUnchecked ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex,
                    "Can not copy directory '%s' to directory '%s'.", nodeFile.getAbsolutePath(),
                    directory.getAbsolutePath());
        }
    }

    public final void removeNode(final INode node)
    {
        assert node != null : "Node could not be null";
        final java.io.File file = getNodeFile(node);
        if (FileOperations.getMonitoredInstanceForCurrentThread().isDirectory(file))
        {
            try
            {
                FileOperations.getMonitoredInstanceForCurrentThread().deleteRecursively(file);
            } catch (IOExceptionUnchecked ex)
            {
                throw EnvironmentFailureException.fromTemplate("Can not remove directory '%s'.",
                        file.getAbsolutePath());
            }
        } else if (FileOperations.getMonitoredInstanceForCurrentThread().isFile(file))
        {
            if (FileOperations.getMonitoredInstanceForCurrentThread().delete(file) == false)
            {
                throw EnvironmentFailureException.fromTemplate("Can not remove file '%s'.", file
                        .getAbsolutePath());
            }
        }
    }

    public List<IFile> listFiles(String[] extensionsOrNull, boolean recursive)
    {
        final List<java.io.File> files =
                FileOperations.getMonitoredInstanceForCurrentThread().listFiles(nodeFile,
                        extensionsOrNull, recursive);
        final List<IFile> nodes = new ArrayList<IFile>(files.size());
        for (java.io.File f : files)
        {
            nodes.add(NodeFactory.internalCreateFileNode(f));
        }
        return Collections.unmodifiableList(nodes);
    }

    public List<IDirectory> listDirectories(boolean recursive)
    {
        final List<java.io.File> files =
                FileOperations.getMonitoredInstanceForCurrentThread().listDirectories(nodeFile,
                        recursive);
        final List<IDirectory> nodes = new ArrayList<IDirectory>(files.size());
        for (java.io.File f : files)
        {
            nodes.add(NodeFactory.internalCreateDirectoryNode(f));
        }
        return Collections.unmodifiableList(nodes);
    }

}
