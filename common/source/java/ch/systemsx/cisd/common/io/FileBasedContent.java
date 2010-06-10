/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * File content. Wraps an instance of {@link File}.
 *
 * @author Franz-Josef Elmer
 */
public class FileBasedContent implements IContent
{
    private final File file;

    /**
     * Creates an instance based on the specified file.
     */
    public FileBasedContent(File file)
    {
        this.file = file;
    }

    /**
     * Returns the name of the wrapped file.
     */
    public String getName()
    {
        return file.getName();
    }

    /**
     * Returns the length of the wrapped file.
     */
    public long getSize()
    {
        return file.length();
    }
    
    /**
     * Returns <code>true</code> if the wrapped file exists.
     */
    public boolean exists()
    {
        return file.exists();
    }

    /**
     * Returns a new instance of {@link FileInputStream} for the wrapped file.
     */
    public InputStream getInputStream()
    {
        try
        {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
}
