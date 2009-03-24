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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.parser.filter.AlwaysAcceptLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;

/**
 * Convenient class to load a tab file and deliver a list of beans of type <code>T</code>. The
 * following formats for the column headers are recognized.
 * <ol>
 * <li>Column headers in first line:
 * 
 * <pre>
 *     column1  column2 column2
 * </pre>
 * 
 * <li>Comment section:
 * 
 * <pre>
 *     # 1. line of comment
 *     # 2. line of comment
 *     # ...
 *     column1  column2 column2
 * </pre>
 * 
 * <li>Column headers at the end of the comment section:
 * 
 * <pre>
 *     # 1. line of comment
 *     # 2. line of comment
 *     # ...
 *     #
 *     #column1  column2 column2
 * </pre>
 * 
 * </ol>
 * 
 * @author Franz-Josef Elmer
 */
public class TabFileLoader<T>
{

    private static final String PREFIX = "#";

    private final IParserObjectFactoryFactory<T> factory;

    /**
     * Creates a new instance based on the specified factory.
     */
    public TabFileLoader(final IParserObjectFactoryFactory<T> factory)
    {
        assert factory != null : "Undefined factory";
        this.factory = factory;
    }

    /**
     * Loads from the specified tab file a list of objects of type <code>T</code>.
     * 
     * @throws IOExceptionUnchecked if a {@link IOException} has occurred.
     */
    public List<T> load(final File file) throws ParserException, ParsingException,
            IllegalArgumentException, IOExceptionUnchecked
    {
        assert file != null : "Given file must not be null";
        assert file.isFile() : "Given file '" + file.getAbsolutePath() + "' is not a file.";

        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            return load(reader);
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Loads data from the specified reader.
     * <p>
     * The header can contain comments which are ignored. The column names can be the first
     * uncommented line or the last commented line. The latter case is determined by the fact, that
     * the one before the last line is a single hash.
     * </p>
     */
    public List<T> load(final Reader reader) throws ParserException, ParsingException,
            IllegalArgumentException
    {
        assert reader != null : "Unspecified reader";

        final Iterator<Line> lineIterator = createLineIterator(reader);
        Line previousLine = null;
        Line line = null;
        boolean previousLineHasColumnHeaders = false;
        while (lineIterator.hasNext())
        {
            previousLineHasColumnHeaders =
                    (previousLine != null) && PREFIX.equals(previousLine.getText());
            previousLine = line;
            line = lineIterator.next();
            if (line.getText().startsWith(PREFIX) == false)
            {
                break;
            }
        }
        final List<T> result = new ArrayList<T>();
        if (line == null) // no lines present
        {
            return result;
        }

        final String headerLine;
        if (previousLineHasColumnHeaders && (previousLine != null /* just for eclipse */))
        {
            headerLine = previousLine.getText().substring(1);
        } else
        {
            headerLine = line.getText();
        }

        final DefaultParser<T> parser = new DefaultParser<T>();
        final String[] tokens = StringUtils.split(headerLine, "\t");
        final int headerLength = tokens.length;
        notUnique(tokens);
        final IPropertyMapper propertyMapper = new DefaultPropertyMapper(tokens);
        parser.setObjectFactory(factory.createFactory(propertyMapper));
        final ILineFilter filter = AlwaysAcceptLineFilter.INSTANCE;
        if (previousLineHasColumnHeaders)
        {
            result.addAll(parser.parse(Arrays.asList(line).iterator(), filter, headerLength));
        }
        result.addAll(parser.parse(lineIterator, filter, headerLength));
        return result;
    }

    private Iterator<Line> createLineIterator(final Reader reader)
    {
        final LineIterator lineIterator = IOUtils.lineIterator(reader);
        final Iterator<Line> iterator = new TabFileLineIterator(lineIterator);
        return iterator;
    }

    /**
     * Checks given <var>tokens</var> whether there is no duplicate.
     * <p>
     * Note that the search is case-insensitive.
     * </p>
     * 
     * @throws IllegalArgumentException if there is at least one duplicate in the given <var>tokens</var>.
     */
    private final static void notUnique(final String[] tokens)
    {
        assert tokens != null : "Given tokens can not be null.";
        final Set<String> unique = new HashSet<String>();
        for (final String token : tokens)
        {
            if (unique.add(token.toLowerCase()) == false)
            {
                throw new IllegalArgumentException(String.format("Duplicated column name '%s'.",
                        token));
            }
        }
    }

    //
    // Helper classes
    //

    private final static class TabFileLineIterator implements Iterator<Line>
    {
        private final LineIterator lineIterator;

        private int lineNumber;

        TabFileLineIterator(final LineIterator lineIterator)
        {
            this.lineIterator = lineIterator;
        }

        //
        // Iterator
        //

        public final void remove()
        {
            lineIterator.remove();
        }

        public final Line next()
        {
            return new Line(++lineNumber, lineIterator.nextLine());
        }

        public final boolean hasNext()
        {
            return lineIterator.hasNext();
        }
    }
}