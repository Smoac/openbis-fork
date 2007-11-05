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

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * A <code>INode</code> factory class.
 * <p>
 * You should prefer to use this class instead of directly instantiate the corresponding <code>INode</code>
 * implementations.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public final class NodeFactory
{

    /**
     * A <code>INode</code> factory method for given <var>file</var>.
     */
    public static INode createNode(final java.io.File file) throws EnvironmentFailureException
    {
        assert file != null : "Unspecified node";
        final String absolutePath = file.getAbsolutePath();
        final String canonicalPath = getCanonicalPath(file);
        if (absolutePath.equals(canonicalPath) == false)
        {
            return createLinkNode(file);
        }
        if (file.isDirectory())
        {
            return createDirectoryNode(file);
        }
        return createFileNode(file);
    }

    private final static String getCanonicalPath(final java.io.File file)
    {
        assert file != null : "Unspecified node";
        try
        {
            return file.getCanonicalPath();
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Couldn't get canonical path of file '" + file.getAbsolutePath()
                    + "'", ex);
        }
    }

    private static Link createLinkNode(final java.io.File file) throws EnvironmentFailureException
    {
        final String absolutePath = file.getAbsolutePath();
        final String canonicalPath = getCanonicalPath(file);
        assert absolutePath.equals(canonicalPath) == false : "Given file must be a link";
        return new Link(file.getName(), createNode(new java.io.File(getCanonicalPath(file))));
    }

    /**
     * Creates a <code>IFile</code> from given <var>file</var>.
     */
    public final static IFile createFileNode(final java.io.File file)
    {
        assert file != null && file.isFile() : "Given file must be a file";
        return new File(file);
    }

    /**
     * Creates a <code>IDirectory</code> from given <var>file</var>.
     */
    public final static IDirectory createDirectoryNode(final java.io.File file)
    {
        assert file != null && file.isDirectory() : "Given file must be a directory";
        return new Directory(file);
    }

    private NodeFactory()
    {
        // Can not be instantiated.
    }
}
