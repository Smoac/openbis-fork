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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = OccurrencesMarker.class)
public class OccurrencesMarkerTest extends AssertJUnit
{
    @Test
    public void testFindNonOverlappingOccurrences()
    {
        OccurrencesMarker marker = createMarker();
        String marked = marker.mark("hello, my beautiful world!", Arrays.asList("hello", "world"));
        assertEquals("<hello>, my beautiful <world>!", marked);
    }

    private OccurrencesMarker createMarker()
    {
        return new OccurrencesMarker('<', '>');
    }

    @Test
    public void testFindOverlappingOccurrences()
    {
        OccurrencesMarker marker = createMarker();
        assertEquals("<aaaa>", marker.mark("aaaa", Arrays.asList("aa")));
        assertEquals("h<ello> w<orld>, h<ello> universe!", marker.mark("hello world, hello universe!", Arrays.asList("ell", "o", "orld")));
    }

    @Test
    public void testFindNoOccurrences()
    {
        OccurrencesMarker marker = createMarker();
        String marked = marker.mark("aaaa", Arrays.asList("x"));
        assertEquals("aaaa", marked);
    }

    @Test
    public void testFindContainingOccurrences()
    {
        OccurrencesMarker marker = createMarker();
        String marked = marker.mark("xabcx", Arrays.asList("abc", "b"));
        assertEquals("x<abc>x", marked);
    }

    @Test
    public void testBreakLinesLastLineNotFull()
    {
        String lines = createMarker().breakLines("1234567", 3, "x", 1, " ");
        assertEquals("1 2 3x4 5 6x7", lines);
    }

    @Test
    public void testBreakLinesLastLineFull()
    {
        String lines = createMarker().breakLines("123456", 3, "x", 2, " ");
        assertEquals("12 3x45 6", lines);
    }

    @Test
    public void testBreakLinesIgnoreMarkers()
    {
        String lines = createMarker().breakLines("1<2>3<4><56><7>", 3, "x", 7, "!");
        assertEquals("1<2>3x<4><56x><7>", lines);
    }

    @Test
    public void testBreakLinesEqualBlocks()
    {
        String lines = createMarker().breakLines("12345678", 4, "x", 2, " ");
        assertEquals("12 34x56 78", lines);
    }

    @Test
    public void testReplaceTags()
    {
        String marked = new OccurrencesMarker('(', ')').replaceTags("a(a)a", "<", ">");
        assertEquals("a<a>a", marked);

        marked = new OccurrencesMarker('<', '>').replaceTags("a<a>a", "(", ")");
        assertEquals("a(a)a", marked);
    }

}
