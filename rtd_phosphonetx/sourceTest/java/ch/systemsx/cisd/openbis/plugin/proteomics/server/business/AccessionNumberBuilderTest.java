/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.AccessionNumberBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class AccessionNumberBuilderTest extends AssertJUnit
{
    @Test
    public void test()
    {
        check(null, "", "");
        check(null, "abc", "abc");
        check(null, "DECOY_abc", "DECOY_abc");
        check("", "abc", "|abc");
        check("", "DECOY_abc", "DECOY_|abc");
        check("", "ab", "|ab|c");
        check("a", "b", "a|b");
        check("a", "DECOY_b", "DECOY_a|b");
        check("a", "DECOY_b", "DECOY_a|DECOY_b");
        check("a", "b", "a|b|c");
        check("a", "DECOY_b", "DECOY_a|b|c");
        check("a", "DECOY_b", "DECOY_a|DECOY_b|c");
        check("a", "", "a||c");
        check("a", "DECOY_", "DECOY_a||c");
        check("", "", "||c");
        check("", "", "||");
        check("", "", "|");
    }

    private void check(String expectedType, String expectedAccessionNumber, String fullAccessionNumber)
    {
        AccessionNumberBuilder builder = new AccessionNumberBuilder(fullAccessionNumber);
        assertEquals(expectedType, builder.getTypeOrNull());
        assertEquals(expectedAccessionNumber, builder.getAccessionNumber());
    }
}
