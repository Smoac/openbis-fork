/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import ch.systemsx.cisd.common.exceptions.WrappedIOException;

/**
 * Class to handle the opening and closing of a {@link RandomAccessFile}.
 * 
 *
 * @author Franz-Josef Elmer
 */
final class RandomAccessFileProvider
{
    private final File file;

    private RandomAccessFile randomAccessFile;

    RandomAccessFileProvider(File file)
    {
        this.file = file;
    }

    RandomAccessFile getRandomAccessFile()
    {
        if (randomAccessFile == null)
        {
            try
            {
                randomAccessFile = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException ex)
            {
                throw new WrappedIOException(ex);
            }
        }
        return randomAccessFile;
    }

    void closeFile()
    {
        if (randomAccessFile != null)
        {
            try
            {
                randomAccessFile.close();
            } catch (IOException ex)
            {
                throw new WrappedIOException(ex);
            }
        }
    }
}