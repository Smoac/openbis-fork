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

package ch.systemsx.cisd.common.parser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.parser.filter.AlwaysAcceptLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * Utilities for parsing files.
 * 
 * @author Bernd Rinn
 */
public final class ParserUtilities
{
    private ParserUtilities()
    {
        // Can not be instantiated.
    }

    private final static Line tryFirstAcceptedLine(final ILineFilter filter,
            final LineIterator lineIterator)
    {
        for (int line = 0; lineIterator.hasNext(); line++)
        {
            final String nextLine = lineIterator.nextLine();
            if (filter.acceptLine(nextLine, line))
            {
                return new Line(line, nextLine);
            }
        }
        return null;
    }

    private final static ILineFilter getLineFilter(final ILineFilter lineFilter)
    {
        return lineFilter == null ? AlwaysAcceptLineFilter.INSTANCE : lineFilter;
    }

    /**
     * Returns the first <code>Line</code> that is not filtered out by given
     * <code>ILineFilter</code>.
     * <p>
     * You should not call this method if given <var>content</var> is <code>null</code>.
     * </p>
     * 
     * @param lineFilter could be <code>null</code>. In this case, the
     *            {@link AlwaysAcceptLineFilter} implementation will be used.
     * @param content the content that is going to be analyzed. Can not be <code>null</code>.
     * @return <code>null</code> if all lines have been filtered out.
     */
    public final static Line tryGetFirstAcceptedLine(final String content,
            final ILineFilter lineFilter)
    {
        assert content != null : "Unspecified reader.";
        final ILineFilter filter = getLineFilter(lineFilter);
        LineIterator lineIterator = null;
        Reader reader = null;
        try
        {
            reader = new StringReader(content);
            lineIterator = IOUtils.lineIterator(reader);
            return tryFirstAcceptedLine(filter, lineIterator);
        } finally
        {
            IOUtils.closeQuietly(reader);
            LineIterator.closeQuietly(lineIterator);
        }
    }

    /**
     * Returns the first <code>Line</code> that is not filtered out by given
     * <code>ILineFilter</code>.
     * <p>
     * You should not call this method if given <var>file</var> does not exist.
     * </p>
     * 
     * @param lineFilter could be <code>null</code>. In this case, the
     *            {@link AlwaysAcceptLineFilter} implementation will be used.
     * @param file the file that is going to be analyzed. Can not be <code>null</code> and must
     *            exists.
     * @return <code>null</code> if all lines have been filtered out.
     */
    public final static Line tryGetFirstAcceptedLine(final File file, final ILineFilter lineFilter)
    {
        assert file != null && file.exists() : "Given file must not be null and must exist.";
        final ILineFilter filter = getLineFilter(lineFilter);
        LineIterator lineIterator = null;
        try
        {
            lineIterator = FileUtils.lineIterator(file);
            return tryFirstAcceptedLine(filter, lineIterator);
        } catch (final IOException ex)
        {
            throw new WrappedIOException(ex);
        } finally
        {
            LineIterator.closeQuietly(lineIterator);
        }
    }
}
