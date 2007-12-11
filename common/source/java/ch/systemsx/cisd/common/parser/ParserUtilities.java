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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Utilities for parsing files.
 * 
 * @author Bernd Rinn
 */
public final class ParserUtilities
{

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, ParserUtilities.class);

    private ParserUtilities()
    {
        // Can not be instantiated.
    }

    /**
     * A small object that represents a line in a <code>File</code> context.
     * 
     * @author Christian Ribeaud
     */
    public final static class Line
    {
        public final String text;

        public final int number;

        Line(final int number, final String text)
        {
            this.number = number;
            this.text = text;
        }
    }

    /**
     * Returns the first <code>Line</code> that is not filtered out by given <code>ILineFilter</code>.
     * <p>
     * You should not call this method if given <var>file</var> does not exist.
     * </p>
     * 
     * @param lineFilter could be <code>null</code>. In this case, the {@link AlwaysAcceptLineFilter} implementation
     *            will be used.
     * @param file the file that is going to be analyzed. Can not be <code>null</code> and must exists.
     * @return <code>null</code> if all lines have been filtered out.
     */
    public final static Line getFirstAcceptedLine(final File file, final ILineFilter lineFilter)
    {
        assert file != null && file.exists() : "Given file must not be null and must exist.";
        final ILineFilter filter = lineFilter == null ? AlwaysAcceptLineFilter.INSTANCE : lineFilter;

        LineIterator lineIterator = null;
        try
        {
            lineIterator = FileUtils.lineIterator(file);
            for (int line = 0; lineIterator.hasNext(); line++)
            {
                String nextLine = lineIterator.nextLine();
                if (filter.acceptLine(nextLine, line))
                {
                    return new Line(line, nextLine);
                }

            }
        } catch (IOException ex)
        {
            machineLog.error("An I/O exception has occurred while reading file '" + file + "'.", ex);
        } finally
        {
            LineIterator.closeQuietly(lineIterator);
        }
        return null;
    }

}
