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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
class File extends AbstractNode implements IFile
{
    File(java.io.File file)
    {
        super(file);
        assert file.isFile() : "Not a file " + file.getAbsolutePath();
    }

    public byte[] getBinaryContent()
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(nodeFile);
            return IOUtils.toByteArray(inputStream);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Couldn't get data from file " + nodeFile.getAbsolutePath(), ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public String getStringContent()
    {
        return FileUtilities.loadToString(nodeFile);
    }

    public final void copyTo(final java.io.File directory) throws EnvironmentFailureException
    {
        try
        {
            FileUtils.copyFileToDirectory(nodeFile, directory);
        } catch (IOException ex)
        {
            throw EnvironmentFailureException.fromTemplate(ex, "Couldn't not copy file '%s' to directory '%s'.",
                    nodeFile.getAbsolutePath(), directory.getAbsolutePath());
        }
    }

    public final void moveTo(java.io.File directory) throws EnvironmentFailureException
    {
        assert directory != null;
        final java.io.File destination = new java.io.File(directory, getName());
        if (destination.exists() == false)
        {
            // Note that 'renameTo' does not change 'nodeFile' path
            final boolean successful = nodeFile.renameTo(destination);
            if (successful == false)
            {
                throw EnvironmentFailureException.fromTemplate("Couldn't not move file '%s' to directory '%s'.",
                        nodeFile.getAbsolutePath(), directory.getAbsolutePath());
            }
        }
        if (nodeFile.equals(destination) == false)
        {
            nodeFile = destination;
        }
    }
}
