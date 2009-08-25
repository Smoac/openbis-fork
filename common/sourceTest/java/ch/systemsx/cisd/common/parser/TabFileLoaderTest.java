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

import static org.testng.AssertJUnit.assertEquals;

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

        assertEquals(0, list.size());
    }

    @Test
    public void testFirstLineHasHeadersWithoutHashSymbolButNoRows()
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        List<ABC> list = loader.load(new StringReader("A\tB\tC\n"));

        assertEquals(list.toString(), 0, list.size());
    }

    @Test
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

    private void loadAndCheck(String preamble)
    {
        TabFileLoader<ABC> loader = new TabFileLoader<ABC>(new ABCFactoryFactory());
        List<ABC> list =
                loader.load(new StringReader(preamble + "A\tB\tC\n" + "a1\tb1\tc1\n"
                        + "a2\tb2\tc2\n"));

        assertEquals(list.toString(), 2, list.size());
        assertEquals("a1b1c1", list.get(0).toString());
        assertEquals("a2b2c2", list.get(1).toString());
    }

}
