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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class OccurrenceUtilTest extends AssertJUnit
{
    @Test
    public void testNoCoverage()
    {
        check("[]", "");
        check("[]", "abc", "xx");
        check("[]", "xx");
        check("[]", "abc");
    }
    
    @Test
    public void testFullCoverage()
    {
        check("[[abc@0]]", "abc", "abc");
    }
    
    @Test
    public void testPartialAndOverlappingCoverage()
    {
        check("[[ab@0], [abcd@4], [ab@13]]", "abc abcde hahab", "ab", "bcd");
        check("[[ab@0], [abcd@4], [ab@13]]", "abc abcde hahab", "ab", "cd");
        check("[[abc@0], [abc@4], [ab@13]]", "abc abcde hahab", "ab", "b", "c");
        check("[[abc@0], [abc@4], [b@14]]", "abc abcde hahab", "abc", "b");
        check("[[abcde@0]]", "abcdef", "abcd", "b", "de");
        check("[[haha@10]]", "abc abcde hahab", "haha", "h");
    }
    
    private void check(String expectedList, String sequence, String... words)
    {
        assertEquals(expectedList, OccurrenceUtil.getCoverage(sequence, Arrays.asList(words)).toString());
    }
}
