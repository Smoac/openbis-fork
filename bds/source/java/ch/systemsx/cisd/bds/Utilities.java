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

package ch.systemsx.cisd.bds;


import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Storage utility methods.
 * 
 * @author Franz-Josef Elmer
 */
public class Utilities
{
    /**
     * Returns a subdirectory from the specified directory. If it does not exist it will be created.
     * 
     * @throws UserFailureException if there is already a node named <code>name</code> but which isn't a directory.
     */
    public static IDirectory getOrCreateSubDirectory(IDirectory directory, String name)
    {
        INode node = directory.tryToGetNode(name);
        if (node == null)
        {
            return directory.makeDirectory(name);
        }
        if (node instanceof IDirectory)
        {
            return (IDirectory) node;
        }
        throw new UserFailureException("There is already a node named '" + name + "' but which isn't a directory.");
    }

    /**
     * Returns a subdirectory from the specified directory.
     * 
     * @param directory Parent directory of the requested directory.
     * @param name Name of the requested directory.
     * @throws UserFailureException if requested directory not found.
     */
    public static IDirectory getSubDirectory(IDirectory directory, String name)
    {
        INode node = directory.tryToGetNode(name);
        if (node == null)
        {
            throw new UserFailureException("No directory named '" + name + "' found in " + directory);
        }
        if (node instanceof IDirectory == false)
        {
            throw new UserFailureException("Is not a directory: " + node);
        }
        return (IDirectory) node;
    }

    /**
     * Convenient short cut for <code>{@link #getString(IDirectory, String)}.trim()</code>.
     */
    public static String getTrimmedString(IDirectory directory, String name)
    {
        return getString(directory, name).trim();
    }

    /**
     * Returns the string content of a file from the specified directory.
     * 
     * @param directory Directory of the requested file.
     * @param name Name of the file.
     * @throws UserFailureException if the requested file does not exist.
     */
    public static String getString(IDirectory directory, String name)
    {
        INode node = directory.tryToGetNode(name);
        if (node == null)
        {
            throw new UserFailureException("File '" + name + "' missing in " + directory);
        }
        if (node instanceof IFile == false)
        {
            throw new UserFailureException(node + " is not a file.");
        }
        IFile file = (IFile) node;
        return file.getStringContent();
    }

    private Utilities()
    {
    }

}
