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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * An <code>IFile</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
final class File extends AbstractNode implements IFile
{
    File(final java.io.File file)
    {
        super(file);
        assert file.isFile() : "Not a file " + file.getAbsolutePath();
    }

    //
    // IFile
    //

    public final byte[] getBinaryContent()
    {
        InputStream inputStream = getInputStream();
        try
        {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Couldn't get data from file " + nodeFile.getAbsolutePath(), ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public final InputStream getInputStream()
    {
        try
        {
            return new FileInputStream(nodeFile);
        } catch (FileNotFoundException ex)
        {
            throw new EnvironmentFailureException("Couldn't open input stream for file " + nodeFile.getAbsolutePath());
        }
    }

    public final String getStringContent()
    {
        return FileUtilities.loadToString(nodeFile);
    }

    public final List<String> getStringContentList()
    {
        return FileUtilities.loadToStringList(nodeFile);
    }

    public final void extractTo(final java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null && directory.isDirectory();
        try
        {
            FileUtils.copyFileToDirectory(nodeFile, directory);
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Couldn't not copy file '%s' to directory '%s'.",
                    nodeFile.getAbsolutePath(), directory.getAbsolutePath());
        }
    }

}
