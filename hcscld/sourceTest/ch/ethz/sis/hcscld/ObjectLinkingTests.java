/*
 * Copyright 2012-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.hcscld.ObjectTracking;
import ch.ethz.sis.hcscld.ObjectTrackingBuilder;
import ch.ethz.sis.hcscld.ObjectTracking.IIndexIterator;
import ch.ethz.sis.hcscld.ObjectTracking.IIndexList;
import ch.ethz.sis.hcscld.ObjectTrackingBuilder.StorageForm;
import ch.systemsx.cisd.base.mdarray.MDIntArray;

/**
 * Test cases for {@link ObjectTracking}.
 * 
 * @author Bernd Rinn
 */
public class ObjectLinkingTests
{
    @Test
    public void testBuildLinkingByte()
    {
        final ObjectTrackingBuilder b = new ObjectTrackingBuilder();

        b.addLink(0, 4);
        b.addLink(0, 17);
        b.addLink(0, 42);
        b.addLink(1, 17);
        b.addLink(1, 88);
        b.addLink(1, 3);
        b.addLink(2, 45);
        b.addLink(2, 1);
        b.addLink(2, 4);

        assertEquals(new MDIntArray(new int[]
            { 0, 4, 0, 17, 0, 42, 1, 3, 1, 17, 1, 88, 2, 1, 2, 4, 2, 45 }, new int[]
            { 9, 2 }), b.getLinking());
        assertEquals(StorageForm.UNSIGNED_BYTE, b.getStorageForm());
    }

    @Test
    public void testBuildLinkingShort()
    {
        final ObjectTrackingBuilder b = new ObjectTrackingBuilder();

        b.addLink(0, 4);
        b.addLink(0, 17);
        b.addLink(0, 42);
        b.addLink(1, 17);
        b.addLink(1, 256);
        b.addLink(1, 3);
        b.addLink(2, 45);
        b.addLink(2, 1);
        b.addLink(2, 4);

        assertEquals(new MDIntArray(new int[]
            { 0, 4, 0, 17, 0, 42, 1, 3, 1, 17, 1, 256, 2, 1, 2, 4, 2, 45 }, new int[]
            { 9, 2 }), b.getLinking());
        assertEquals(StorageForm.UNSIGNED_SHORT, b.getStorageForm());
    }

    @Test
    public void testBuildLinkingInt()
    {
        final ObjectTrackingBuilder b = new ObjectTrackingBuilder();

        b.addLink(0, 4);
        b.addLink(0, 17);
        b.addLink(0, 42);
        b.addLink(1, 17);
        b.addLink(1, 65536);
        b.addLink(1, 3);
        b.addLink(2, 45);
        b.addLink(2, 1);
        b.addLink(2, 4);

        assertEquals(new MDIntArray(new int[]
            { 0, 4, 0, 17, 0, 42, 1, 3, 1, 17, 1, 65536, 2, 1, 2, 4, 2, 45 }, new int[]
            { 9, 2 }), b.getLinking());
        assertEquals(StorageForm.UNSIGNED_INT, b.getStorageForm());
    }

    @Test
    public void testBuildLinkingWithDuplicates()
    {
        final ObjectTrackingBuilder b = new ObjectTrackingBuilder();

        b.addLink(0, 4);
        b.addLink(0, 17);
        b.addLink(0, 42);
        b.addLink(1, 17);
        b.addLink(1, 88);
        b.addLink(1, 3);
        b.addLink(1, 3);
        b.addLink(2, 45);
        b.addLink(2, 1);
        b.addLink(2, 4);
        b.addLink(0, 42);
        b.addLink(0, 42);

        assertEquals(new MDIntArray(new int[]
            { 0, 4, 0, 17, 0, 42, 1, 3, 1, 17, 1, 88, 2, 1, 2, 4, 2, 45 }, new int[]
            { 9, 2 }), b.getLinking());
        assertEquals(StorageForm.UNSIGNED_BYTE, b.getStorageForm());
    }

    @Test
    public void testQueryLinking()
    {
        final ObjectTrackingBuilder b = new ObjectTrackingBuilder();

        b.addLink(0, 4);
        b.addLink(0, 17);
        b.addLink(0, 42);
        b.addLink(1, 17);
        b.addLink(1, 88);
        b.addLink(1, 3);
        b.addLink(2, 45);
        b.addLink(2, 1);
        b.addLink(2, 4);
        ObjectTracking l = new ObjectTracking(null, b.getLinking());
        assertTrue(Arrays.equals(new int[]
            { 4, 17, 42 }, l.getChildIds(0).toArray()));
        assertTrue(Arrays.equals(new int[]
            { 3, 17, 88 }, l.getChildIds(1).toArray()));
        assertTrue(Arrays.equals(new int[]
            { 1, 4, 45 }, l.getChildIds(2).toArray()));
        assertEquals(0, l.getChildIds(3).toArray().length);
        IIndexList list = l.getChildIds(0);
        assertEquals(3, list.size());
        assertFalse(list.isEmpty());
        assertEquals(4, list.get(0));
        assertEquals(17, list.get(1));
        assertEquals(42, list.get(2));
        assertTrue(list.contains(4));
        assertTrue(list.contains(17));
        assertTrue(list.contains(42));
        assertFalse(list.contains(18));
        assertFalse(list.contains(88));
        IIndexIterator it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals(4, it.next());
        assertTrue(it.hasNext());
        assertEquals(17, it.next());
        assertTrue(it.hasNext());
        assertEquals(42, it.next());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals(42, it.previous());
        assertTrue(it.hasPrevious());
        assertEquals(17, it.previous());
        assertTrue(it.hasPrevious());
        assertEquals(4, it.previous());
        assertFalse(it.hasPrevious());

        assertTrue(Arrays.toString(l.getParentIds(17).toArray()), Arrays.equals(new int[]
                { 0, 1 }, l.getParentIds(17).toArray()));
        assertTrue(Arrays.toString(l.getParentIds(4).toArray()), Arrays.equals(new int[]
                { 0, 2 }, l.getParentIds(4).toArray()));
        assertTrue(l.getParentIds(0).isEmpty());
        list = l.getParentIds(4);
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        assertEquals(0, list.get(0));
        assertEquals(2, list.get(1));
        assertTrue(list.contains(0));
        assertFalse(list.contains(1));
        assertTrue(list.contains(2));
        it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals(0, it.next());
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
        assertFalse(it.hasNext());
        assertTrue(it.hasPrevious());
        assertEquals(2, it.previous());
        assertTrue(it.hasPrevious());
        assertEquals(0, it.previous());
        assertFalse(it.hasPrevious());
    }

}
