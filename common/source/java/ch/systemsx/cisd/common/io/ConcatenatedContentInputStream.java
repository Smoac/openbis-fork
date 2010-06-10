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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Special <code>InputStream</code> that will concatenate the contents of an array of
 * {@link IContent} instances into one stream. Each content will be preceded by one long which tells
 * what is the size of the content in bytes.
 * 
 * @author Tomasz Pylak
 */
public class ConcatenatedContentInputStream extends InputStream
{
    private static final int EOF = -1;
    
    /**
     * Wraps specified {@link File} objects into {@link FileBasedContent} objects.
     */
    public static IContent[] wrap(List<File> files)
    {
        List<IContent> list = new ArrayList<IContent>();
        for (File file : files)
        {
            list.add(new FileBasedContent(file));
        }
        return list.toArray(new IContent[files.size()]);
    }

    private int currentIndex = -1;

    private boolean eof = false;

    private IContent[] contents;

    // If true then currentStream is not the content, but a short stream
    // which encodes one long number which describes content size.
    // This stream is created every time before a new content is appended to the stream.
    private boolean readingContentSize;

    private InputStream currentStream;

    private final boolean ignoreNonExistingContents;

    /**
     * @param ignoreNonExistingContents If <code>true</code> non-existing contents or elements on
     *            the list which are null will be handled like empty contents. Otherwise an
     *            exception is thrown for a non-existing content.
     * @param contents InputStreams provided by these contents will be concatenated into one stream.
     */
    ConcatenatedContentInputStream(boolean ignoreNonExistingContents, IContent... contents)
    {
        this.ignoreNonExistingContents = ignoreNonExistingContents;
        this.contents = contents;
        this.readingContentSize = false;
    }

    /**
     * @param ignoreNonExistingContents If <code>true</code> non-existing contents or elements on
     *            the list which are null will be handled like empty contents. Otherwise an
     *            exception is thrown for a non-existing content.
     * @param contents InputStreams provided by these contents will be concatenated into one stream.
     */
    public ConcatenatedContentInputStream(boolean ignoreNonExistingContents, List<IContent> contents)
    {
        this(ignoreNonExistingContents, contents.toArray(new IContent[contents.size()]));
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
        // we have a loop instead of an if to ignore contents which are empty
        while (result == EOF && !eof)
        {
            closeCurrentStream();
            if (readingContentSize)
            {
                currentStream = createContentStream(ignoreNonExistingContents, tryGetCurrentContent());
                readingContentSize = false;
            } else
            {
                currentIndex++;
                if (hasCurrentContent() == false)
                {
                    eof = true;
                    return EOF;
                }
                currentStream = createSizeStream(tryGetCurrentContent());
                readingContentSize = true;
            }
            result = currentStream.read();
        }
        return result;
    }

    // null means that the current content has not been specified. This case will be treated in the
    // same way as an empty content if ignoreNonExistingContents is true
    private IContent tryGetCurrentContent()
    {
        if (hasCurrentContent() == false)
        {
            throw new IllegalStateException("there are no more content to read");
        }
        return contents[currentIndex]; // can be null
    }

    // returns true if there is a content to read
    private boolean hasCurrentContent()
    {
        return (contents != null && currentIndex < contents.length);
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

    private static InputStream createContentStream(boolean ignoreNonExistingContents,
            IContent currentContentOrNull)
    {
        InputStream stream;
        if (ignoreNonExistingContents
                && (currentContentOrNull == null || currentContentOrNull.exists() == false))
        {
            stream = createEmptyStream();
        } else
        {
            // exception will be thrown if the content is null or does not exist
            stream = currentContentOrNull.getInputStream();
        }
        return new BufferedInputStream(stream);
    }

    private static ByteArrayInputStream createEmptyStream()
    {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static InputStream createSizeStream(IContent contentOrNull) throws IOException
    {
        long size = (contentOrNull == null) ? 0 : contentOrNull.getSize();
        byte[] data = longToBytes(size);
        return new ByteArrayInputStream(data);
    }

    private static byte[] longToBytes(long size) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeLong(size);
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
