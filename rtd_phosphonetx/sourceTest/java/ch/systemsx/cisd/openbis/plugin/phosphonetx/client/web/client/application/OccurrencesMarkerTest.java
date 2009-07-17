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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Tomasz Pylak
 */
public class OccurrencesMarkerTest extends AssertJUnit
{
    @Test
    public void testFindNonOverlappingOccurrences()
    {
        OccurrencesMarker marker = new OccurrencesMarker("<", ">");
        List<String> marked =
                marker.mark("hello, my beautiful world!", Arrays.asList("hello", "world"));
        assertEquals(1, marked.size());
        assertEquals("<hello>, my beautiful <world>!", marked.get(0));
    }

    @Test
    public void testFindOverlappingOccurrences()
    {
        OccurrencesMarker marker = new OccurrencesMarker("<", ">");
        List<String> marked = marker.mark("aaaa", Arrays.asList("aa"));
        assertEquals(2, marked.size());
        assertEquals("<aa><aa>", marked.get(0));
        assertEquals("a<aa>a", marked.get(1));
    }

    @Test
    public void testFindNoOccurrences()
    {
        OccurrencesMarker marker = new OccurrencesMarker("<", ">");
        List<String> marked = marker.mark("aaaa", Arrays.asList("x"));
        assertEquals(1, marked.size());
        assertEquals("aaaa", marked.get(0));
    }

}
