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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

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
    /**
     * A <code>LineFilter</code> implementation that filters out comment and empty lines (lines starting with '#').
     */
    private final static ILineFilter COMMENT_AND_EMPTY_LINE_FILTER = new ILineFilter()
        {
            ///////////////////////////////////////////////////////
            // LineFilter
            ///////////////////////////////////////////////////////

            public boolean acceptLine(String line)
            {
                String trimmed = line.trim();
                return trimmed.length() > 0 && trimmed.startsWith("#") == false;
            }
        };

    private final ILineTokenizer lineTokenizer;
    
    private IParserObjectFactory<E> factory;
    
    private IPropertyMapperFactory mapperFactory;
    
    public DefaultReaderParser()
    {
        this(new DefaultLineTokenizer());
    }
    
    public DefaultReaderParser(ILineTokenizer lineTokenizer)
    {
        this.lineTokenizer = lineTokenizer;
    }
    
    protected E createObject(String[] tokens) {
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
        return lineTokenizer.tokenize(lineNumber, line);
    }
    
    ///////////////////////////////////////////////////////
    // Parser
    ///////////////////////////////////////////////////////

    public final List<E> parse(Reader reader)
    {
        return parse(reader, COMMENT_AND_EMPTY_LINE_FILTER);
    }

    public final List<E> parse(Reader reader, ILineFilter lineFilter)
    {
        BufferedReader bufferedReader;
        if (reader instanceof BufferedReader)
        {
            bufferedReader = (BufferedReader) reader;
        } else
        {
            bufferedReader = new BufferedReader(reader);
        }
        List<E> elements = new ArrayList<E>();
        synchronized (lineTokenizer)
        {
            // Inits <code>ILineTokenizer</code>
            lineTokenizer.init();
            String line;
            try
            {
                for (int lineNumber = 0; (line = bufferedReader.readLine()) != null; lineNumber++)
                {
                    if (mapperFactory != null && mapperFactory.getHeaderLine() > -1)
                    {
                        String[] tokens = parseLine(lineNumber, line);
                        factory.setPropertyMapper(mapperFactory.createPropertyMapper(tokens));
                        continue;
                    }
                    if (lineFilter.acceptLine(line))
                    {
                        String[] tokens = parseLine(lineNumber, line);
                        elements.add(createObject(tokens));
                    }
                }
                return elements;
            } catch (IOException ex)
            {
                throw new CheckedExceptionTunnel(ex);
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

    public final void setPropertyMapperFactory(IPropertyMapperFactory mapperFactory)
    {
        this.mapperFactory = mapperFactory;
    }
}