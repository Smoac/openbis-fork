/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.etl.custom.incell;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import ch.rinn.restrictions.Friend;

/**
 * Test of {@link IncellImageMetadataParser}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = IncellImageMetadataParser.class)
public class IncellImageMetadataParserTest extends AssertJUnit
{
    @Test
    public void test() throws ParserConfigurationException, SAXException, IOException
    {
        IncellImageMetadataParser parser =
                new IncellImageMetadataParser(
                        "sourceTest/java/ch/systemsx/cisd/openbis/dss/etl/custom/incell/small-example.xdce");
        String[] channelCodes = parser.getChannelCodes();
        int[] wavelengths = parser.getChannelWavelengths();
        assertEquals("FITC", channelCodes[0]);
        assertEquals("DAPI", channelCodes[1]);
        assertEquals("Cy5", channelCodes[2]);
        assertEquals(525, wavelengths[0]);
        assertEquals(455, wavelengths[1]);
        assertEquals(705, wavelengths[2]);
    }
}
