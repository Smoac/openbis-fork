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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractFileNode<T> extends AbstractNode implements IFile<T>
{
    AbstractFileNode(File file)
    {
        super(file);
        assert file.isFile() : "Not a file " + file.getAbsolutePath();
    }

    public void extractTo(File directory) throws UserFailureException, EnvironmentFailureException
    {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try
        {
            inputStream = new FileInputStream(nodeFile);
            outputStream = new FileOutputStream(new File(directory, getName()));
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Couldn't extract file '" + this + "' to directory "
                    + directory.getAbsolutePath());
        } finally
        {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        
    }

}
