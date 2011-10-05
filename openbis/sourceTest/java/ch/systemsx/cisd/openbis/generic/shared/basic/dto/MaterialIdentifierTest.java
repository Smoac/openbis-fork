/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class MaterialIdentifierTest extends AssertJUnit
{

    @Test
    public void testParseNormalCodes()
    {
        assertCorrectlyParsed("CODE", "TYPE", "CODE (TYPE)");
        assertCorrectlyParsed("12_CODE", "12_TYPE", "12_CODE (12_TYPE)");
        assertCorrectlyParsed("0", "0", "0 (0)");
    }

    @Test
    public void testParseWithArbitraryCharacters()
    {
        assertCorrectlyParsed(":123(ABC).PQ//", "TYPE", ":123(ABC).PQ// (TYPE)");
        assertCorrectlyParsed("/.()$%", "CONTROL_WELL", "/.()$% (CONTROL_WELL)");

    }

    void assertCorrectlyParsed(String materialCode, String materialTypeCode, String identifierString)
    {
        MaterialIdentifier identifier = MaterialIdentifier.tryParseIdentifier(identifierString);
        assertEquals(materialCode, identifier.getCode());
        assertEquals(materialTypeCode, identifier.getTypeCode());
    }

}
