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

import java.io.InputStream;
import java.util.List;

import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.IFileBasedFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.filesystem.FileOperations;

/**
 * An <code>IFile</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
final class File extends AbstractNode implements IFileBasedFile
{
    File(final java.io.File file)
    {
        super(file);
        assert FileOperations.getMonitoredInstanceForCurrentThread().isFile(file) : "Not a file "
                + file.getAbsolutePath();
    }

    public IDirectory tryAsDirectory()
    {
        return null;
    }

    public IFile tryAsFile()
    {
        return this;
    }

    //
    // IFile
    //

    public final byte[] getBinaryContent()
    {
        try
        {
            return FileOperations.getMonitoredInstanceForCurrentThread().getContentAsByteArray(
                    nodeFile);
        } catch (WrappedIOException ex)
        {
            throw new EnvironmentFailureException("Can not load data from file "
                    + nodeFile.getAbsolutePath(), ex.getCause());
        }
    }

    public final InputStream getInputStream()
    {
        try
        {
            return FileOperations.getMonitoredInstanceForCurrentThread().getInputStream(nodeFile);
        } catch (WrappedIOException ex)
        {
            throw new EnvironmentFailureException("Can not open input stream for file "
                    + nodeFile.getAbsolutePath(), ex.getCause());
        }
    }

    public final String getStringContent()
    {
        return FileOperations.getMonitoredInstanceForCurrentThread().getContentAsString(nodeFile);
    }

    public final String getExactStringContent()
    {
        return FileOperations.getMonitoredInstanceForCurrentThread().getExactContentAsString(
                nodeFile);
    }

    public final List<String> getStringContentList()
    {
        return FileOperations.getMonitoredInstanceForCurrentThread().getContentAsStringList(
                nodeFile);
    }

    public final void extractTo(final java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null && directory.isDirectory();
        try
        {
            FileOperations.getMonitoredInstanceForCurrentThread().copyFileToDirectory(nodeFile,
                    directory);
        } catch (WrappedIOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex.getCause(),
                    "Can not copy file '%s' to directory '%s'.", nodeFile.getAbsolutePath(),
                    directory.getAbsolutePath());
        }
    }

}
