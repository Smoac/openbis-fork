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

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.IFileBasedDirectory;
import ch.systemsx.cisd.bds.storage.IFileBasedFile;
import ch.systemsx.cisd.bds.storage.IFileBasedNode;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;

/**
 * A <code>INode</code> factory class.
 * <p>
 * You should prefer to use this class instead of directly instantiate the corresponding
 * <code>INode</code> implementations.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class NodeFactory
{

    /**
     * A <code>INode</code> factory method for given <var>file</var>. It does not support creating
     * links, because only symbolic links could be recognized.
     */
    public static INode createNode(final java.io.File file) throws EnvironmentFailureException
    {
        assert file != null : "Given node can not be null";
        return internalCreateNode(file);
    }

    /**
     * Creates a new <code>ILink</code> with given <var>name</var> which points to given
     * <var>file</var>.
     */
    public final static ILink createLinkNode(final String name, final java.io.File file)
    {
        assert file != null : "Given file can not be null";
        assert name != null : "Given name can not be null";
        IFileBasedNode reference = internalCreateNode(file);
        assert reference instanceof IFile : "Given reference must be of type IFile: "
                + reference.getClass().getName();
        return new Link(name, reference);
    }

    /**
     * Creates a new <code>ILink</code> assuming that the given file is a symbolic link.
     * <p>
     * IMPORTANT NOTE: we compare the absolute path against the canonical one to find out whether it
     * is a link or not. This only works for <i>symbolic</i> links and not for <i>hard</i> links.
     * </p>
     */
    public final static ILink createSymbolicLinkNode(final java.io.File file)
            throws EnvironmentFailureException
    {
        final String absolutePath = file.getAbsolutePath();
        final String canonicalPath =
                FileOperations.getMonitoredInstanceForCurrentThread().getCanonicalPath(file);
        if (absolutePath.equals(canonicalPath))
        {
            throw new IllegalArgumentException(String.format(
                    "Given file must be a link [path=%s].", absolutePath));
        }
        return new Link(file.getName(), internalCreateNode(new java.io.File(canonicalPath)));
    }

    /**
     * Creates a <code>IFile</code> from given <var>file</var>.
     */
    public final static IFile createFileNode(final java.io.File file)
    {
        assert file != null : "Given file must not be null";
        if (FileOperations.getMonitoredInstanceForCurrentThread().isFile(file) == false)
        {
            throw new IllegalArgumentException("File '" + file.getAbsolutePath()
                    + "' is not a file.");
        }
        return internalCreateFileNode(file);
    }

    /**
     * Creates a <code>IDirectory</code> from given <var>file</var>.
     */
    public final static IDirectory createDirectoryNode(final java.io.File file)
    {
        assert file != null : "Given file must not be null";
        if (FileOperations.getMonitoredInstanceForCurrentThread().isDirectory(file) == false)
        {
            throw new IllegalArgumentException("File '" + file.getAbsolutePath()
                    + "' is not a directory.");
        }
        return internalCreateDirectoryNode(file);
    }

    static IFileBasedNode internalCreateNode(final java.io.File file)
    {
        if (FileOperations.getMonitoredInstanceForCurrentThread().isDirectory(file))
        {
            return internalCreateDirectoryNode(file);
        }
        return internalCreateFileNode(file);
    }

    static IFileBasedFile internalCreateFileNode(final java.io.File file)
    {
        return new File(file);
    }

    static IFileBasedDirectory internalCreateDirectoryNode(final java.io.File file)
    {
        return new Directory(file);
    }

    private NodeFactory()
    {
        // Can not be instantiated.
    }
}
