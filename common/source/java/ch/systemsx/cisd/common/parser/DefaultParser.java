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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A default {@link IParser} implementation.
 * <p>
 * The object type returned by this implementation is generic. This implementation defines a <code>ILineFilter</code>
 * that filters out comment and empty lines.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class DefaultParser<E> implements IParser<E>
{
    private final ILineTokenizer lineTokenizer;

    private IParserObjectFactory<E> factory;

    /**
     * Creates an instance based on the {@link DefaultLineTokenizer}.
     */
    public DefaultParser()
    {
        this(new DefaultLineTokenizer());
    }

    /**
     * Creates an instance for the specified line tokenizer.
     */
    public DefaultParser(final ILineTokenizer lineTokenizer)
    {
        this.lineTokenizer = lineTokenizer;
    }

    protected E createObject(final String[] tokens) throws ParserException
    {
        return factory.createObject(tokens);
    }

    /**
     * Parses given <code>line</code> into an element.
     * <p>
     * Uses <code>ILineTokenizer</code> to do its job.
     * </p>
     * 
     * @param lineNumber line number.
     */
    private final String[] parseLine(final int lineNumber, final String line)
    {
        return lineTokenizer.tokenize(line);
    }

    //
    // Parser
    //

    public final List<E> parse(final Iterator<Line> lineIterator, final ILineFilter lineFilter, final int headerLength)
            throws ParsingException
    {
        final List<E> elements = new ArrayList<E>();
        synchronized (lineTokenizer)
        {
            lineTokenizer.init();
            while (lineIterator.hasNext())
            {
                final Line line = lineIterator.next();
                final String nextLine = line.getText();
                final int number = line.getNumber();
                if (lineFilter.acceptLine(nextLine, number))
                {
                    final String[] tokens = parseLine(number, nextLine);
                    E object = null;
                    try
                    {
                        if (tokens.length != headerLength)
                        {
                            throw new ColumnSizeMismatchException(tokens, number, headerLength);
                        }
                        object = createObject(tokens);
                    } catch (final ParserException parserException)
                    {
                        throw new ParsingException(parserException, tokens, number);
                    } catch (final ParsingException parsingException)
                    {
                        throw parsingException;
                    } catch (final RuntimeException runtimeException)
                    {
                        throw new ParsingException(runtimeException, tokens, number);
                    }
                    elements.add(object);
                }
            }
            lineTokenizer.destroy();
            return elements;
        }

    }

    public final void setObjectFactory(final IParserObjectFactory<E> factory)
    {
        this.factory = factory;
    }
}