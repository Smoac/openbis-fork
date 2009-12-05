/*
 * Copyright 2008 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.*;

import java.io.StringReader;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link TabFileLoader} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class TabFileLoaderTest
{
    public static final class ABC
    {
        private String a;

        private String b;

        private String c;

        public final String getA()
        {
            return a;
        }

        public final void setA(String a)
        {
            this.a = a;
        }

        public final String getB()
        {
            return b;
        }

        public final void setB(String b)
        {
            this.b = b;
        }

        public final String getC()
        {
            return c;
        }

        public final void setC(String c)
        {
            this.c = c;
        }

        //
        // Object
        //

        @Override
        public String toString()
        {
            return a + b + c;
        }

    }

    public static final class ABCFactoryFactory implements IParserObjectFactoryFactory<ABC>
    {

        //
        // IParserObjectFactoryFactory
        //

        public final IParserObjectFactory<ABC> createFactory(final IPropertyMapper propertyMapper)
                throws ParserException
        {
            return new IParserObjectFactory<ABC>()
                {

                    //
                    // IParserObjectFactory
                    //

                    public final ABC createObject(final String[] lineTokens) throws ParserException
                    {
                        ABC abc = new ABC();
                        abc.setA(lineTokens[0]);
                        abc.setB(lineTokens[1]);
                        abc.setC(lineTokens[2]);
                        return abc;
                    }
                };
        }
    }

    @Test
    public void testEmptyInput()
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        List<ABC> list = loader.load(new StringReader(""));

        assertEmptyResult(list);
    }

    @Test
    public void testEmptyInputIteratively()
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        assertFalse(loader.iterate(new StringReader("")).hasNext());
    }

    @Test
    public void testFirstLineHasHeadersWithoutHashSymbolButNoRows()
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        List<ABC> list = loader.load(new StringReader("A\tB\tC\n"));

        assertEmptyResult(list);
    }

    private void assertEmptyResult(List<ABC> list)
    {
        assertEquals(list.toString(), 0, list.size());
    }

    @Test
    // if the header is followed by some amount of tabs, we ignore the same amount of tabs in lines
    // below
    public void testSkipAdditionalLineSeparators()
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        String additionalSeparators = "\t\t\t";
        String header = "A\tB\tC" + additionalSeparators;
        String values = header;
        List<ABC> list = loader.load(new StringReader(header + "\n" + values));

        assertEquals(list.toString(), 1, list.size());
        ABC row = list.get(0);
        assertEquals("A", row.getA());
        assertEquals("B", row.getB());
        assertEquals("C", row.getC());
    }

    @Test(expectedExceptions = ParsingException.class)
    public void testDifferentNumberOfAdditionalLineSeparatorsFails()
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        String additionalSeparators = "\t\t\t";
        String header = "A\tB\tC" + additionalSeparators;
        String values = header + "\t";
        loader.load(new StringReader(header + "\n" + values));
    }

    @Test
    public void testFirstLineHasHeadersWithoutHashSymbol()
    {
        loadAndCheck("");
    }

    @Test
    public void testFirstLineWithHashSymbol()
    {
        loadAndCheck("#\n");
    }

    @Test
    public void testFirstLineWithHashSymbolAndSomething()
    {
        loadAndCheck("#blabla\n");
    }

    @Test
    public void testFirstLineHasMarkerAndSecondLineHasHeadersWithHashSymbol()
    {
        loadAndCheck("#\n#");
    }

    @Test
    public void testFirstTwoLinesWithHashAndSomething()
    {
        loadAndCheck("#blabla\n" + "#blubub\n");
    }

    @Test
    public void testSkipEscapingQuotesInComments()
    {
        String header =
                "\"#Comment surrounded by quotes with \"\"double quotes\"\" inside.\"\n"
                        + "#another \"comment\"\n";
        loadAndCheck(header);
    }

    private void loadAndCheck(String preamble)
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        String values1 = "a1\tb1\tc1\n";
        // here we check quotes escaping in non-comment lines
        String values2 = "\"a2\t\"\"b2\"\"\tc2\"\n";
        List<ABC> list = loader.load(new StringReader(preamble + "A\tB\tC\n" + values1 + values2));

        assertEquals(list.toString(), 2, list.size());
        assertEquals("a1b1c1", list.get(0).toString());
        assertEquals("a2\"b2\"c2", list.get(1).toString());
    }

}
