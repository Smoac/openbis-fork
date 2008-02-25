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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Convenient class to load a tab file and deliver a list of beans of type <code>T</code>. The following
 * formats for the column headers are recognized.
 * <ol><li>Column headers in first line:
 *     <pre>
 *     column1  column2 column2
 *     </pre>
 *     <li>Comment section:
 *     <pre>
 *     # 1. line of comment
 *     # 2. line of comment
 *     # ...
 *     column1  column2 column2
 *     </pre>
 *     <li>Column headers at the end of the comment section:
 *     <pre>
 *     # 1. line of comment
 *     # 2. line of comment
 *     # ...
 *     #
 *     #column1  column2 column2
 *     </pre>
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
     * @throws EnvironmentFailureException if a IOException occurred.
     */
    public List<T> load(final File file)
    {
        assert file != null : "Given file must not be null";
        assert file.isFile() : "Given file '" + file.getAbsolutePath() + "' is not a file.";

        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            return load(reader);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        } catch (final ParsingException ex)
        {
            throwParsingException(ex, file);
            throw new AssertionError("We should never reach this point.");
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }
    
    
    List<T> load(Reader reader)
    {
        List<T> result = new ArrayList<T>();
        Iterator<Line> lineIterator = createLineIterator(reader);
        if (lineIterator.hasNext() == false)
        {
            return result;
        }
        Line previousLine = null;
        Line line = null;
        boolean previousLineHasColumnHeaders = false;
        while (lineIterator.hasNext())
        {
            previousLineHasColumnHeaders = (previousLine != null) && PREFIX.equals(previousLine.getText());
            previousLine = line;
            line = lineIterator.next();
            if (line.getText().startsWith(PREFIX) == false)
            {
                break;
            }
        }
        @SuppressWarnings("null")
        final String headerLine = previousLineHasColumnHeaders ? previousLine.getText().substring(1) : line.getText();
        
        final DefaultParser<T> parser = new DefaultParser<T>();
        final String[] tokens = StringUtils.split(headerLine, "\t");
        notUnique(tokens);
        final IAliasPropertyMapper propertyMapper = new HeaderFilePropertyMapper(tokens);
        parser.setObjectFactory(factory.createFactory(propertyMapper));
        ILineFilter filter = AlwaysAcceptLineFilter.INSTANCE;
        if (previousLineHasColumnHeaders)
        {
            result.addAll(parser.parse(Arrays.asList(line).iterator(), filter));
        }
        result.addAll(parser.parse(lineIterator, filter));
        return result;
    }

    private Iterator<Line> createLineIterator(Reader reader)
    {
        final LineIterator lineIterator = IOUtils.lineIterator(reader);
        Iterator<Line> iterator = new Iterator<Line>()
            {
                private int lineNumber;
                public void remove()
                {
                    lineIterator.remove();
                }

                public Line next()
                {
                    return new Line(++lineNumber, lineIterator.nextLine());
                }

                public boolean hasNext()
                {
                    return lineIterator.hasNext();
                }

            };
        return iterator;
    }

    /**
     * Throws given <var>ex</var> or translates it into another kind of exception.
     * <p>
     * Default behavior just throws it.
     * </p>
     * 
     * @param file the parsed file.
     */
    protected void throwParsingException(final ParsingException parsingException, final File file)
    {
        throw parsingException;
    }

    /**
     * Checks given <var>tokens</var> whether there is no duplicate.
     * <p>
     * Note that the search is case-insensitive.
     * </p>
     * 
     * @throws UserFailureException if there is at least one duplicate in the given <var>tokens</var>.
     */
    private final static void notUnique(final String[] tokens)
    {
        assert tokens != null : "Given tokens can not be null.";
        final Set<String> unique = new HashSet<String>();
        for (final String token : tokens)
        {
            if (unique.add(token.toLowerCase()) == false)
            {
                throw new IllegalArgumentException(String.format("Duplicated column name '%s'.", token));
            }
        }
    }
}