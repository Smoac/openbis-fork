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
import java.io.IOException;

import org.apache.commons.lang.ObjectUtils;

import ch.systemsx.cisd.bds.exception.StorageException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFileBasedNode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;

/**
 * An abstract implementation of <code>INode</code>.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractNode implements IFileBasedNode
{
    final static File moveFileToDirectory(final File source, final File directory,
            final String nameOrNull) throws EnvironmentFailureException
    {
        assert source != null;
        IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        assert directory != null && fileOperations.isDirectory(directory);
        final String newName;
        if (nameOrNull == null)
        {
            newName = source.getName();
        } else
        {
            newName = nameOrNull;
        }
        final File destination = new File(directory, newName);
        if (fileOperations.exists(destination) == false)
        {
            if (FileUtilities.isSymbolicLink(source))
            {
                moveSymbolicLink(source, destination);
            } else
            {
                final boolean successful = fileOperations.rename(source, destination);
                if (successful == false)
                {
                    throw EnvironmentFailureException.fromTemplate(
                            "Can not move file '%s' to directory '%s'.", source.getAbsolutePath(),
                            directory.getAbsolutePath());
                }
            }
        }
        return destination;
    }

    // WORKAROUND there were cases where it was impossible to move an absolute symbolic link
    // It happened on a CIFS share. So instead of moving the link we create a file which points to
    // the same place and delete the link.
    private static void moveSymbolicLink(File source, File destination)
    {
        File referencedSource;
        try
        {
            referencedSource = source.getCanonicalFile();
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("cannot get the canonical path of " + source);
        }
        boolean ok = SoftLinkMaker.createSymbolicLink(referencedSource, destination);
        if (ok == false)
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Can not create symbolic link to '%s' in '%s'.", referencedSource.getPath(),
                    destination.getPath());
        }
        ok = source.delete();
        if (ok == false)
        {
            throw EnvironmentFailureException.fromTemplate("Can not delete symbolic link '%s'.",
                    source.getPath());
        }
    }

    protected final File nodeFile;

    AbstractNode(File file)
    {
        checkFile(file);
        this.nodeFile = file;
    }

    static void checkFile(final File file)
    {
        if (file == null)
        {
            throw new StorageException("Unspecified file.");
        }
        if (FileOperations.getMonitoredInstanceForCurrentThread().exists(file) == false)
        {
            throw new StorageException(String.format("Non existing file '%s'.", file
                    .getAbsolutePath()));
        }
    }

    //
    // IFileBasedNode
    //

    public java.io.File getNodeFile()
    {
        return nodeFile;
    }

    //
    // INode
    //

    public final String getName()
    {
        return nodeFile.getName();
    }

    public String getPath()
    {
        return nodeFile.getAbsolutePath();
    }

    public final IDirectory tryGetParent()
    {
        File dir = nodeFile.getParentFile();
        return dir == null ? null : NodeFactory.internalCreateDirectoryNode(dir);
    }

    public final void moveTo(final File directory)
    {
        moveFileToDirectory(nodeFile, directory, null);
    }

    public boolean isValid()
    {
        return FileOperations.getMonitoredInstanceForCurrentThread().canRead(nodeFile);
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return nodeFile.getAbsolutePath();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof IFileBasedNode == false)
        {
            return false;
        }
        return ObjectUtils.equals(nodeFile, ((IFileBasedNode) obj).getNodeFile());
    }

    @Override
    public int hashCode()
    {
        return nodeFile.hashCode();
    }

}
