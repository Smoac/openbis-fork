/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.dss;

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=HeaderUtils.class)
public class HeaderUtilsTest extends AssertJUnit
{
    @Test
    public void testJoin()
    {
        assertEquals("a, b, c", HeaderUtils.join(Arrays.asList("a", "b", "c"), ", ", 8));
        assertEquals("a, b, c", HeaderUtils.join(Arrays.asList("a", "b", "c"), ", ", 7));
        assertEquals("..., c", HeaderUtils.join(Arrays.asList("a", "b", "c"), ", ", 6));
        
        assertEquals("123, 345, 789", HeaderUtils.join(Arrays.asList("123", "345", "789"), ", ", 14));
        assertEquals("123, 345, 789", HeaderUtils.join(Arrays.asList("123", "345", "789"), ", ", 13));
        assertEquals("..., 789", HeaderUtils.join(Arrays.asList("123", "345", "789"), ", ", 12));
        
        assertEquals("100, 1000, 10000", HeaderUtils.join(Arrays.asList("100", "1000", "10000"), ", ", 17));
        assertEquals("100, 1000, 10000", HeaderUtils.join(Arrays.asList("100", "1000", "10000"), ", ", 16));
        assertEquals("100, ..., 10000", HeaderUtils.join(Arrays.asList("100", "1000", "10000"), ", ", 15));
        assertEquals("..., 10000", HeaderUtils.join(Arrays.asList("100", "1000", "10000"), ", ", 14));
        
        assertEquals("1, 10, 100, 1000, 10000", HeaderUtils.join(Arrays.asList("1", "10", "100",
                "1000", "10000"), ", ", 24));
        assertEquals("1, 10, 100, 1000, 10000", HeaderUtils.join(Arrays.asList("1", "10", "100",
                "1000", "10000"), ", ", 23));
        assertEquals("1, 10, 100, ..., 10000", HeaderUtils.join(Arrays.asList("1", "10", "100",
                "1000", "10000"), ", ", 22));
        assertEquals("1, 10, ..., 10000", HeaderUtils.join(Arrays.asList("1", "10", "100",
                "1000", "10000"), ", ", 21));
    }
}
