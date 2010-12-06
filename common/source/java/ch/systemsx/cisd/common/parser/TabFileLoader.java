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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.parser.filter.AlwaysAcceptLineFilter;
import ch.systemsx.cisd.common.parser.filter.ILineFilter;
import ch.systemsx.cisd.common.utilities.UnicodeUtils;

/**
 * Convenient class to load (or iterate over) a tab file, a reader or a stream. The loader delivers
 * either a list or an iterator of beans of type <code>T</code>. The following formats for the
 * column headers are recognized.
 * <ol>
 * <li>Column headers in first line:
 * 
 * <pre>
 *     column1  column2 column2
 * </pre>
 * <li>Comment section:
 * 
 * <pre>
 *     # 1. line of comment
 *     # 2. line of comment
 *     # ...
 *     column1  column2 column2
 * </pre>
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

    private static final String TOKENS_SEPARATOR = "\t";

    public static final String COMMENT_PREFIX = "#";

    private final IParserObjectFactoryFactory<T> factory;

    /**
     * Creates a new instance based on the factory which uses only bean annotations.
     */
    public TabFileLoader(final Class<T> beanClass)
    {
        this.factory = new IParserObjectFactoryFactory<T>()
            {
                public IParserObjectFactory<T> createFactory(IPropertyMapper propertyMapper)
                        throws ParserException
                {
                    return new AbstractParserObjectFactory<T>(beanClass, propertyMapper)
                        {
                        };
                }
            };
    }

    /**
     * Creates a new instance based on the specified factory.
     */
    public TabFileLoader(final IParserObjectFactoryFactory<T> factory)
    {
        assert factory != null : "Undefined factory";
        this.factory = factory;
    }

    /**
     * Iterates over the data in the specified file.
     * <p>
     * The header can contain comments which are ignored. The column names can be the first
     * uncommented line or the last commented line. The latter case is determined by the fact, that
     * the one before the last line is a single hash.
     * </p>
     */
    public Iterator<T> iterate(final File file) throws ParserException, ParsingException,
            IllegalArgumentException
    {
        assert file != null : "Given file must not be null";
        assert file.isFile() : "Given file '" + file.getAbsolutePath() + "' is not a file.";

        FileReader reader = null;
        try
        {
            reader = new FileReader(file);
            return iterate(reader);
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
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

        FileInputStream inputStream = null;
        try
        {
            inputStream = FileUtils.openInputStream(file);
            return load(inputStream);
        } catch (final IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Iterates over the data in the specified reader.
     * <p>
     * The header can contain comments which are ignored. The column names can be the first
     * uncommented line or the last commented line. The latter case is determined by the fact, that
     * the one before the last line is a single hash.
     * </p>
     */
    public Iterator<T> iterate(final Reader reader) throws ParserException, ParsingException,
            IllegalArgumentException
    {
        assert reader != null : "Unspecified reader";

        final Iterator<Line> lineIterator = createLineIterator(reader);
        return iterate(lineIterator);
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
        return load(lineIterator);
    }

    /**
     * Iterates over the data in the specified stream.
     * <p>
     * The header can contain comments which are ignored. The column names can be the first
     * uncommented line or the last commented line. The latter case is determined by the fact, that
     * the one before the last line is a single hash.
     * </p>
     */
    public Iterator<T> iterate(final InputStream stream) throws ParserException, ParsingException,
            IllegalArgumentException
    {
        assert stream != null : "Unspecified stream";

        try
        {
            final Iterator<Line> lineIterator = createLineIterator(stream);
            return iterate(lineIterator);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    /**
     * Loads data from the specified stream.
     * <p>
     * The header can contain comments which are ignored. The column names can be the first
     * uncommented line or the last commented line. The latter case is determined by the fact, that
     * the one before the last line is a single hash.
     * </p>
     */
    public List<T> load(final InputStream stream) throws ParserException, ParsingException,
            IllegalArgumentException
    {
        assert stream != null : "Unspecified stream";

        try
        {
            final Iterator<Line> lineIterator = createLineIterator(stream);
            return load(lineIterator);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    private List<T> load(final Iterator<Line> lineIterator)
    {
        Line previousLine = null;
        Line line = null;
        boolean previousLineHasColumnHeaders = false;
        while (lineIterator.hasNext())
        {
            previousLineHasColumnHeaders = (previousLine != null) && isComment(previousLine);
            previousLine = line;
            line = lineIterator.next();
            if (startsWithComment(line) == false)
            {
                break;
            }
        }
        if (line == null) // no lines present
        {
            return new ArrayList<T>();
        }

        final String headerLine;
        if (previousLineHasColumnHeaders && (previousLine != null /* just for eclipse */))
        {
            headerLine = trimComment(previousLine);
        } else
        {
            headerLine = line.getText();
        }

        final DefaultParser<T> parser = new DefaultParser<T>();
        final String[] tokens = StringUtils.split(headerLine, TOKENS_SEPARATOR);
        int lastEmptyHeadersToSkip = countLastEmptyTokens(headerLine);
        final int headerLength = tokens.length;
        notUnique(tokens);

        final IPropertyMapper propertyMapper = new DefaultPropertyMapper(tokens);
        parser.setObjectFactory(factory.createFactory(propertyMapper));

        Line firstContentLine = previousLineHasColumnHeaders ? line : null;
        Iterator<Line> contentLineIterator =
                createContentIterator(firstContentLine, lineIterator, lastEmptyHeadersToSkip);
        final ILineFilter filter = AlwaysAcceptLineFilter.INSTANCE;
        return parser.parse(contentLineIterator, filter, headerLength);
    }

    private Iterator<T> iterate(final Iterator<Line> lineIterator)
    {
        Line previousLine = null;
        Line line = null;
        boolean previousLineHasColumnHeaders = false;
        while (lineIterator.hasNext())
        {
            previousLineHasColumnHeaders = (previousLine != null) && isComment(previousLine);
            previousLine = line;
            line = lineIterator.next();
            if (startsWithComment(line) == false)
            {
                break;
            }
        }
        if (line == null) // no lines present
        {
            return new Iterator<T>()
                {
                    public boolean hasNext()
                    {
                        return false;
                    }

                    public T next()
                    {
                        throw new NoSuchElementException();
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
        }

        final String headerLine;
        if (previousLineHasColumnHeaders && (previousLine != null /* just for eclipse */))
        {
            headerLine = trimComment(previousLine);
        } else
        {
            headerLine = line.getText();
        }

        final DefaultParser<T> parser = new DefaultParser<T>();
        final String[] tokens = StringUtils.split(headerLine, TOKENS_SEPARATOR);
        int lastEmptyHeadersToSkip = countLastEmptyTokens(headerLine);
        final int headerLength = tokens.length;
        notUnique(tokens);

        final IPropertyMapper propertyMapper = new DefaultPropertyMapper(tokens);
        parser.setObjectFactory(factory.createFactory(propertyMapper));

        Line firstContentLine = previousLineHasColumnHeaders ? line : null;
        Iterator<Line> contentLineIterator =
                createContentIterator(firstContentLine, lineIterator, lastEmptyHeadersToSkip);
        final ILineFilter filter = AlwaysAcceptLineFilter.INSTANCE;
        return parser.parseIteratively(contentLineIterator, filter, headerLength);
    }

    private static boolean startsWithComment(Line line)
    {
        String text = line.getText();
        return text.startsWith(COMMENT_PREFIX);
    }

    private static String trimComment(Line previousLine)
    {
        String text = previousLine.getText();
        if (text.startsWith(COMMENT_PREFIX))
        {
            return text.substring(COMMENT_PREFIX.length());
        } else
        {
            return text;
        }
    }

    private static boolean isComment(Line line)
    {
        String text = line.getText();
        return COMMENT_PREFIX.equals(text);
    }

    /**
     * @param firstContentLineOrNull if not null, it will be returned as the first iterator element,
     *            followed by all iterator elements from the second parameter
     * @param lastEmptyTokensToSkip the number of token separators which will be removed form the
     *            end of each iterated line
     */
    private static Iterator<Line> createContentIterator(final Line firstContentLineOrNull,
            final Iterator<Line> lineIterator, final int lastEmptyTokensToSkip)
    {

        final String suffixToDelete = multiply(lastEmptyTokensToSkip, TOKENS_SEPARATOR);
        return new Iterator<Line>()
            {
                private Line firstLineOrNull = firstContentLineOrNull;

                public boolean hasNext()
                {
                    return firstLineOrNull != null || lineIterator.hasNext();
                }

                public Line next()
                {
                    return trim(nextUntrimmed());
                }

                private Line trim(Line line)
                {
                    if (lastEmptyTokensToSkip == 0)
                    {
                        return line;
                    }
                    String text = trim(line.getText(), line.getNumber());
                    return new Line(line.getNumber(), text);
                }

                private String trim(String text, int lineNumber)
                {
                    if (text.endsWith(suffixToDelete))
                    {
                        return text.substring(0, text.length() - suffixToDelete.length());
                    } else
                    {
                        throw new ParsingException(new String[]
                            { text }, lineNumber)
                            {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public final String getMessage()
                                {
                                    return super.getMessage()
                                            + " The line was expected to be followed by as many separators as the header.";
                                }
                            };
                    }
                }

                private Line nextUntrimmed()
                {
                    if (firstLineOrNull != null)
                    {
                        Line line = firstLineOrNull;
                        firstLineOrNull = null;
                        return line;
                    } else
                    {
                        return lineIterator.next();
                    }
                }

                public void remove()
                {
                    throw new NotImplementedException();
                }
            };
    }

    private static String multiply(int number, String text)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < number; i++)
        {
            sb.append(text);
        }
        return sb.toString();
    }

    // how many tokens at the end of the array are blank?
    private static int countLastEmptyTokens(String text)
    {
        String rest = text;
        int counter = 0;
        while (rest.endsWith(TOKENS_SEPARATOR))
        {
            rest = rest.substring(0, rest.length() - TOKENS_SEPARATOR.length());
            counter++;
        }
        return counter;
    }

    private Iterator<Line> createLineIterator(final Reader reader)
    {
        final LineIterator lineIterator = IOUtils.lineIterator(reader);
        final Iterator<Line> iterator = new TabFileLineIterator(lineIterator);
        return iterator;
    }

    private Iterator<Line> createLineIterator(final InputStream stream) throws IOException
    {
        final LineIterator lineIterator =
                IOUtils.lineIterator(stream, UnicodeUtils.DEFAULT_UNICODE_CHARSET);
        final Iterator<Line> iterator = new TabFileLineIterator(lineIterator);
        return iterator;
    }

    /**
     * Checks given <var>tokens</var> whether there is no duplicate.
     * <p>
     * Note that the search is case-insensitive.
     * </p>
     * 
     * @throws IllegalArgumentException if there is at least one duplicate in the given
     *             <var>tokens</var>.
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
        private static final String QUOTE = "" + '"';

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
            String text = unescapeQuotes(lineIterator.nextLine());
            return new Line(++lineNumber, text);
        }

        // if the line contains quotes, Excel escapes them surrounding the whole line in quotes and
        // doubling all quotes inside.
        private static String unescapeQuotes(String text)
        {
            // skips tabs at the end if line is quoted - see multisection tsv files problem
            String trimmedText = text.trim();
            if (trimmedText.length() > 1 && trimmedText.startsWith(QUOTE)
                    && trimmedText.endsWith(QUOTE))
            {
                String unquoted = trimmedText.substring(1, trimmedText.length() - 1);
                return unquoted.replaceAll(QUOTE + QUOTE, QUOTE);
            } else
            {
                return text;
            }
        }

        public final boolean hasNext()
        {
            return lineIterator.hasNext();
        }
    }
}