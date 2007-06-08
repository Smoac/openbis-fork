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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link DefaultReaderParser} class.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultReaderParserTest
{
    private final String text =
            "\n# This is a comment\n" + "firstName\tlastName\taddress\tcity\n"
                    + "Christian\tRibeaud\tKapfrain 2/2\tEfringen-Kirchen\n"
                    + "Marcel\tOdiet\tRue des Pervenches 46\t2800 Delémont\n";

    @Test
    public final void testParseWithoutFactory() throws IOException
    {
        IReaderParser<String[]> parser = new DefaultReaderParser<String[]>();
        parser.setObjectFactory(IParserObjectFactory.DO_NOTHING_OBJECT_FACTORY);
        Reader reader = new StringReader(text);
        List<String[]> result = parser.parse(reader, new DefaultLineFilter());
        assert result.size() == 3;
        assertEquals(result.get(0)[0], "firstName");
        assertEquals(result.get(1)[1], "Ribeaud");
        assertEquals(result.get(2)[2], "Rue des Pervenches 46");
        IOUtils.closeQuietly(reader);
    }
}