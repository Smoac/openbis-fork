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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * A default <code>IReaderParser</code> implementation.
 * <p>
 * The object type returned by this implementation is generic. This implementation defines a <code>ILineFilter</code>
 * that filters out comment and empty lines.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class DefaultReaderParser<E> implements IReaderParser<E>
{
    private final ILineTokenizer lineTokenizer;

    private IParserObjectFactory<E> factory;

    public DefaultReaderParser()
    {
        this(new DefaultLineTokenizer());
    }

    public DefaultReaderParser(ILineTokenizer lineTokenizer)
    {
        this.lineTokenizer = lineTokenizer;
    }

    protected E createObject(String[] tokens)
    {
        return factory.createObject(tokens);
    }

    /**
     * Parses given <code>line</code> into an element.
     * <p>
     * Uses <code>ILineTokenizer</code> to do its job.
     * </p>
     */
    protected String[] parseLine(int lineNumber, String line)
    {
        return lineTokenizer.tokenize(line);
    }

    //
    // Parser
    //

    public final List<E> parse(Reader reader) throws IOException
    {
        return parse(reader, AlwaysAcceptLineFilter.INSTANCE);
    }

    public final List<E> parse(Reader reader, ILineFilter lineFilter) throws IOException
    {
        final List<E> elements = new ArrayList<E>();
        synchronized (lineTokenizer)
        {
            // Inits ILineTokenizer
            lineTokenizer.init();
            try
            {
                LineIterator lineIterator = IOUtils.lineIterator(reader);
                for (int lineNumber = 0; lineIterator.hasNext(); lineNumber++)
                {
                    String nextLine = lineIterator.nextLine();
                    if (lineFilter.acceptLine(nextLine, lineNumber))
                    {
                        String[] tokens = parseLine(lineNumber, nextLine);
                        elements.add(createObject(tokens));
                    }
                }
                return elements;
            } finally
            {
                // Destroys line tokenizer.
                lineTokenizer.destroy();
            }

        }

    }

    public final void setObjectFactory(IParserObjectFactory<E> factory)
    {
        this.factory = factory;
    }
}