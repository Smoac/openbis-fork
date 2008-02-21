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

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link DefaultParser} class.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultParserTest
{
    private final static List<String> text 
            = Arrays.asList("", "# This is a comment", "firstName\tlastName\taddress\tcity",
                            "Charles\tDarwin\tHumboldt Ave. 1865\t4242 Somewhere",
                            "Albert\tEinstein\tNewton Road 1905\t4711 Princton");

    @Test
    public final void testParseWithoutFactoryAndHeader()
    {
        final IParser<String[]> parser = new DefaultParser<String[]>();
        parser.setObjectFactory(IParserObjectFactory.STRING_ARRAY_OBJECT_FACTORY);
        final List<String[]> result = parser.parse(createLineIterator(), new HeaderLineFilter());
        assertEquals(3, result.size());
        assertEquals(result.get(0)[0], "firstName");
        assertEquals(result.get(1)[1], "Darwin");
        assertEquals(result.get(2)[2], "Newton Road 1905");
        assertEquals(result.get(1)[3], "4242 Somewhere");
    }

    @Test
    public final void testParseWithoutFactoryWithLineFilter()
    {
        final IParser<String[]> parser = new DefaultParser<String[]>();
        parser.setObjectFactory(IParserObjectFactory.STRING_ARRAY_OBJECT_FACTORY);
        final List<String[]> result = parser.parse(createLineIterator(), new HeaderLineFilter(2));
        assertEquals(2, result.size());
        assertEquals(result.get(0)[0], "Charles");
        assertEquals(result.get(1)[1], "Einstein");
    }

    @Test
    public final void testCreateObjectWithException()
    {
        final IParser<String[]> parser = new DefaultParser<String[]>()
            {
                //
                // DefaultReaderParser
                //
                @Override
                protected String[] createObject(String[] tokens)
                {
                    throw new ArrayIndexOutOfBoundsException();
                }
            };
        parser.setObjectFactory(IParserObjectFactory.STRING_ARRAY_OBJECT_FACTORY);
        try
        {
            parser.parse(createLineIterator(), new HeaderLineFilter(2));
        } catch (ParsingException ex)
        {
            assertEquals(
                    "Creating an object with following tokens '[Charles, Darwin, Humboldt Ave. 1865, 4242 Somewhere]' failed.",
                    ex.getMessage());
            assertEquals(3, ex.getLineNumber());
        }
    }
    
    private Iterator<Line> createLineIterator()
    {
        return new Iterator<Line>()
            {
                private Iterator<String> iterator = text.iterator();
                private int lineNumber;
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
                public Line next()
                {
                    return new Line(++lineNumber, iterator.next());
                }
        
                public boolean hasNext()
                {
                    return iterator.hasNext();
                }
            };
    }

}