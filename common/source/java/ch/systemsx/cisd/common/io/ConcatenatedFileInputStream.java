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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Special <code>InputStream</code> that will concatenate the contents of an array of files into one
 * stream. Content of each file will be preceded by one long which tells what is the size of the
 * file in bytes.<BR>
 * 
 * @author Tomasz Pylak
 */
public class ConcatenatedFileInputStream extends InputStream
{
    private static final int EOF = -1;

    private int currentIndex = -1;

    private boolean eof = false;

    private File[] files;

    // If true then currentStream is not the content of the file, but a short stream
    // which encodes one long number which describes file size.
    // This stream is created every time before a new file content is appended to the stream.
    private boolean readingFileSize;

    private InputStream currentStream;

    private final boolean ignoreNonExistingFiles;

    /**
     * @param ignoreNonExistingFiles If <code>true</code> non-existing files or elements on the list
     *            which are null will be handled like empty files. Otherwise an exception is thrown
     *            for a non-existing file.
     * @param files content of these files will be concatenated into one stream.
     */
    public ConcatenatedFileInputStream(boolean ignoreNonExistingFiles, File... files)
    {
        this.ignoreNonExistingFiles = ignoreNonExistingFiles;
        this.files = files;
        this.readingFileSize = false;
    }

    /**
     * @param ignoreNonExistingFiles If <code>true</code> non-existing files or elements on the list
     *            which are null will be handled like empty files. Otherwise an exception is thrown
     *            for a non-existing file.
     * @param files content of these files will be concatenated into one stream.
     */
    public ConcatenatedFileInputStream(boolean ignoreNonExistingFiles, List<File> files)
    {
        this(ignoreNonExistingFiles, files.toArray(new File[files.size()]));
    }

    @Override
    public void close() throws IOException
    {
        closeCurrentStream();
        eof = true;
    }

    @Override
    public int read() throws IOException
    {
        int result = readCurrent();
        // we have a loop instead of an if to ignore files which are empty
        while (result == EOF && !eof)
        {
            closeCurrentStream();
            if (readingFileSize)
            {
                currentStream = createFileStream(ignoreNonExistingFiles, tryGetCurrentFile());
                readingFileSize = false;
            } else
            {
                currentIndex++;
                if (hasCurrentFile() == false)
                {
                    eof = true;
                    return EOF;
                }
                currentStream = createFileSizeStream(tryGetCurrentFile());
                readingFileSize = true;
            }
            result = currentStream.read();
        }
        return result;
    }

    // null means that the current file has not been specified. This case will be treated in the
    // same way as an empty file if ignoreNonExistingFiles is true
    private File tryGetCurrentFile()
    {
        if (hasCurrentFile() == false)
        {
            throw new IllegalStateException("there are no more files to read");
        }
        return files[currentIndex]; // can be null
    }

    // returns true if there is a file to read
    private boolean hasCurrentFile()
    {
        return (files != null && currentIndex < files.length);
    }

    private int readCurrent() throws IOException
    {
        return (eof || currentStream == null) ? EOF : currentStream.read();
    }

    private void closeCurrentStream()
    {
        close(currentStream);
        currentStream = null;
    }

    // -------------- static helper ---------------

    private static InputStream createFileStream(boolean ignoreNonExistingFiles,
            File currentFileOrNull) throws FileNotFoundException
    {
        InputStream stream;
        if (ignoreNonExistingFiles
                && (currentFileOrNull == null || currentFileOrNull.exists() == false))
        {
            stream = createEmptyStream();
        } else
        {
            // exception will be thrown if the file is null or does not exist
            stream = new FileInputStream(currentFileOrNull);
        }
        return new BufferedInputStream(stream);
    }

    private static ByteArrayInputStream createEmptyStream()
    {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static InputStream createFileSizeStream(File fileOrNull) throws IOException
    {
        long fileSize = (fileOrNull == null) ? 0 : fileOrNull.length();
        byte[] data = longToBytes(fileSize);
        return new ByteArrayInputStream(data);
    }

    private static byte[] longToBytes(long fileSize) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(fileSize);
        dos.flush();
        byte[] data = bos.toByteArray();
        dos.close();
        return data;
    }

    /**
     * Close a stream without throwing any exception if something went wrong. Do not attempt to
     * close it if the argument is null.
     */
    private static void close(InputStream streamOrNull)
    {
        if (streamOrNull != null)
        {
            try
            {
                streamOrNull.close();
            } catch (IOException ioex)
            {
                // ignore
            }
        }
    }
}
