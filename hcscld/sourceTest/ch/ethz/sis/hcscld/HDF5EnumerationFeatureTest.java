/*
 * Copyright 2013 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.Arrays;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.ethz.sis.hcscld.HDF5EnumerationFeatureMatrix.IFeatureColumn;
import ch.systemsx.cisd.base.mdarray.MDByteArray;
import ch.systemsx.cisd.hdf5.EnumerationType;

/**
 * Tests for {@link HDF5EnumerationFeatureMatrix}.
 * 
 * @author Bernd Rinn
 */
public class HDF5EnumerationFeatureTest
{
    private static final File rootDirectory = new File("targets", "unit-test-wd");

    private static final File workingDirectory = new File(rootDirectory,
            "HDF5EnumerationFeatureTest");

    enum State1
    {
        A, B, C
    }

    enum State2
    {
        D, E, F, G
    }

    enum State3
    {
        H, I, J, K, L
    }

    @BeforeSuite
    public void init()
    {
        workingDirectory.mkdirs();
        assertTrue(workingDirectory.isDirectory());
        workingDirectory.deleteOnExit();
        rootDirectory.deleteOnExit();
    }

    @Test
    public void testCreateFromScratch()
    {
        final EnumerationType state1Type = new EnumerationType(State1.class);
        final EnumerationType state2Type = new EnumerationType(State2.class);
        final EnumerationType state3Type = new EnumerationType(State3.class);

        final MDByteArray ordinalArray = new MDByteArray(new int[]
            { 4, 3 });
        final HDF5EnumerationFeatureMatrix matrix =
                new HDF5EnumerationFeatureMatrix(new EnumerationType[]
                    { state1Type, state2Type,
                            state3Type }, ordinalArray);
        IFeatureColumn<State1> feature1 = matrix.getFeatureView(State1.class, 0);
        feature1.set(State1.B, 0);
        feature1.set(State1.A, 1);
        feature1.set(State1.C, 2);
        feature1.set(State1.A, 3);
        assertEquals(1, matrix.getOrdinal(0, 0));
        assertEquals("B", matrix.getValue(0, 0));
        assertEquals(0, matrix.getOrdinal(1, 0));
        assertEquals("A", matrix.getValue(1, 0));
        assertEquals(2, matrix.getOrdinal(2, 0));
        assertEquals("C", matrix.getValue(2, 0));
        assertEquals(0, matrix.getOrdinal(3, 0));
        assertEquals("A", matrix.getValue(3, 0));
        IFeatureColumn<State2> feature2 = matrix.getFeatureView(State2.class, 1);
        feature2.set(State2.F, 0);
        feature2.set(State2.D, 1);
        feature2.set(State2.G, 2);
        feature2.set(State2.E, 3);
        assertEquals(2, matrix.getOrdinal(0, 1));
        assertEquals(0, matrix.getOrdinal(1, 1));
        assertEquals(3, matrix.getOrdinal(2, 1));
        assertEquals(1, matrix.getOrdinal(3, 1));
        IFeatureColumn<State3> feature3 = matrix.getFeatureView(State3.class, 2);
        feature3.set(State3.I, 0);
        feature3.set(State3.H, 1);
        feature3.set(State3.L, 2);
        feature3.set(State3.K, 3);
        assertEquals(1, matrix.getOrdinal(0, 2));
        assertEquals(0, matrix.getOrdinal(1, 2));
        assertEquals(4, matrix.getOrdinal(2, 2));
        assertEquals(3, matrix.getOrdinal(3, 2));
        assertTrue(Arrays.equals(new byte[]
            { 1, 2, 1, 0, 0, 0, 2, 3, 4, 0, 1, 3 }, matrix.toStorageForm()));
        assertEquals(3, matrix.getMaxNumberOfBits());
    }
}
