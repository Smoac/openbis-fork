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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exception.UserFailureException;

/**
 * Test cases for {@link HeaderUtils}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = HeaderUtils.class)
public class TimeSeriesHeaderUtilsTest extends AssertJUnit
{
    private static final String QUANTIFIED_PEPTIDES = "QuantifiedPeptides";

    private static final String PROTEIN_LCMS_RATIO = "ProteinLcmsRatio";

    private static final String CG_NEW1 = "CG1";

    private static final String TR_NEW1 = "TR1";

    private static final String TR_NEW2 = "TR2";

    private static final String INCONSISTENT_HEADERS_MESSAGE = "Inconsistent data column headers";

    private static final String DEFAULT_HEADER = "0::1::2::3::4::5::6::7::8::9::10::11::12::13";

    @Test
    public void testConsistentOneHeaderNoRequirement() throws Exception
    {
        String header = new Header().toString();
        assertEquals(DEFAULT_HEADER, header);
        Collection<DataColumnHeader> headers = Arrays.asList(new DataColumnHeader(header));
        HeaderUtils
                .assertMetadataConsistent(headers, new ArrayList<DataHeaderProperty>());
    }

    @Test
    public void testConsistentOneHeaderOneRequirement() throws Exception
    {
        String header = new Header().toString();
        assertEquals(DEFAULT_HEADER, header);
        Collection<DataColumnHeader> headers = Arrays.asList(new DataColumnHeader(header));
        HeaderUtils.assertMetadataConsistent(headers, Arrays
                .asList(DataHeaderProperty.BiologicalReplicatateCode));
    }

    @Test
    public void testConsistentOneHeaderTwoRequirements() throws Exception
    {
        String header = new Header().toString();
        assertEquals(DEFAULT_HEADER, header);
        Collection<DataColumnHeader> headers = Arrays.asList(new DataColumnHeader(header));
        HeaderUtils.assertMetadataConsistent(headers, Arrays.asList(
                DataHeaderProperty.BiologicalReplicatateCode, DataHeaderProperty.TimePoint));
    }

    @Test
    public void testConsistentTwoSameHeadersOneRequirement() throws Exception
    {
        String header1 = new Header().toString();
        assertEquals(DEFAULT_HEADER, header1);
        String header2 = new Header().toString();
        assertTrue(header1.equals(header2));
        Collection<DataColumnHeader> headers =
                Arrays.asList(new DataColumnHeader(header1), new DataColumnHeader(header2));
        HeaderUtils.assertMetadataConsistent(headers, Arrays
                .asList(DataHeaderProperty.BiologicalReplicatateCode));
    }

    @Test
    public void testConsistentTwoDifferentConsistentHeadersOneRequirement() throws Exception
    {
        String header1 = new Header().toString();
        assertEquals(DEFAULT_HEADER, header1);
        String header2 =
                new Header().set(DataHeaderProperty.TechnicalReplicateCode, "TR1").toString();
        assertFalse(header1.equals(header2));
        Collection<DataColumnHeader> headers =
                Arrays.asList(new DataColumnHeader(header1), new DataColumnHeader(header2));
        HeaderUtils.assertMetadataConsistent(headers, Arrays
                .asList(DataHeaderProperty.BiologicalReplicatateCode));
    }

    @Test
    public void testConsistentTwoDifferentInconsistentHeadersOneRequirement() throws Exception
    {
        String header1 = new Header().toString();
        assertEquals(DEFAULT_HEADER, header1);
        DataHeaderProperty property = DataHeaderProperty.TechnicalReplicateCode;
        String header2 = new Header().set(property, TR_NEW1).toString();
        assertFalse(header1.equals(header2));
        Collection<DataColumnHeader> headers =
                Arrays.asList(new DataColumnHeader(header1), new DataColumnHeader(header2));
        boolean exceptionThrown = false;
        try
        {
            HeaderUtils.assertMetadataConsistent(headers, Arrays.asList(property));
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains(INCONSISTENT_HEADERS_MESSAGE)
                    && ex.getMessage().contains(property.name())
                    && ex.getMessage().contains(property.ordinal() + "")
                    && ex.getMessage().contains(TR_NEW1));
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testConsistentManyInconsistentHeadersOneRequirement() throws Exception
    {
        DataHeaderProperty property = DataHeaderProperty.TechnicalReplicateCode;
        String header1 = new Header().toString();

        String header2 = new Header().set(property, TR_NEW1).toString();
        assertFalse(header1.equals(header2));

        String header3 =
                new Header().set(property, TR_NEW2).set(DataHeaderProperty.CG, CG_NEW1).toString();
        assertFalse(header1.equals(header3));
        assertFalse(header2.equals(header3));

        Collection<DataColumnHeader> headers =
                Arrays.asList(new DataColumnHeader(header1), new DataColumnHeader(header1),
                        new DataColumnHeader(header2), new DataColumnHeader(header3));
        boolean exceptionThrown = false;
        try
        {
            HeaderUtils.assertMetadataConsistent(headers, Arrays.asList(property));
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains(INCONSISTENT_HEADERS_MESSAGE)
                    && ex.getMessage().contains(property.name())
                    && ex.getMessage().contains(property.ordinal() + "")
                    && ex.getMessage().contains(TR_NEW1) && ex.getMessage().contains(TR_NEW2));
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testConsistentManyInconsistentHeadersManyRequirements() throws Exception
    {
        DataHeaderProperty firstRequirement = DataHeaderProperty.TechnicalReplicateCode;
        DataHeaderProperty secondRequirement = DataHeaderProperty.CG;
        String header1 = new Header().toString();

        String header2 = new Header().set(firstRequirement, TR_NEW1).toString();
        assertFalse(header1.equals(header2));

        String header3 =
                new Header().set(firstRequirement, TR_NEW2).set(secondRequirement, CG_NEW1)
                        .toString();
        assertFalse(header1.equals(header3));
        assertFalse(header2.equals(header3));

        Collection<DataColumnHeader> headers =
                Arrays.asList(new DataColumnHeader(header1), new DataColumnHeader(header1),
                        new DataColumnHeader(header2), new DataColumnHeader(header3));
        boolean exceptionThrown = false;
        try
        {
            HeaderUtils.assertMetadataConsistent(headers, Arrays.asList(firstRequirement,
                    secondRequirement));
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertTrue(ex.getMessage().contains(INCONSISTENT_HEADERS_MESSAGE));
            assertTrue(ex.getMessage().contains(firstRequirement.name()));
            assertTrue(ex.getMessage().contains(firstRequirement.ordinal() + ""));
            assertTrue(ex.getMessage().contains(TR_NEW1));
            assertTrue(ex.getMessage().contains(TR_NEW2));
            assertTrue(ex.getMessage().contains(secondRequirement.name()));
            assertTrue(ex.getMessage().contains(secondRequirement.ordinal() + ""));
            assertTrue(ex.getMessage().contains(CG_NEW1));
        }
        assertTrue(exceptionThrown);
    }

    class Header
    {
        private final String[] header;

        public Header()
        {
            header = new String[DataHeaderProperty.values().length];
            for (DataHeaderProperty p : DataHeaderProperty.values())
            {
                set(p, Integer.toString(p.ordinal()));
            }
        }

        public Header set(DataHeaderProperty property, String value)
        {
            header[property.ordinal()] = value;
            return this;
        }

        @Override
        public String toString()
        {
            return StringUtils.join(header, "::");
        }

    }

    private static final String VAL1 = "val1";

    private static final String VAL2 = "val2";

    @Test
    public void testGetPropertyValuePropertyNotDefined() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        boolean exceptionThrown = false;
        try
        {
            HeaderUtils.getPropertyValue(property, map, true);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("BiID not defined", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetPropertyValuePropertyDefinedButEmpty() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        map.put(property, set);
        boolean exceptionThrown = false;
        try
        {
            HeaderUtils.getPropertyValue(property, map, true);
        } catch (UserFailureException ex)
        {
            exceptionThrown = true;
            assertEquals("BiID not defined", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetPropertyValuePropertyDefinedOnce() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        set.add(VAL1);
        map.put(property, set);
        assertEquals(VAL1, HeaderUtils.getPropertyValue(property, map, true));
    }

    @Test
    public void testGetPropertyValuePropertyDefinedMoreThanOnce() throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        set.add(VAL1);
        set.add(VAL2);
        map.put(property, set);
        String result = HeaderUtils.getPropertyValue(property, map, true);
        assertTrue((VAL1 + ", " + VAL2).equals(result) || (VAL2 + ", " + VAL1).equals(result));
    }

    @Test
    public void testGetPropertyValuePropertyDefinedMoreThanOnceButOnlyOneExpected()
            throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.BiID;
        HashSet<String> set = new HashSet<String>();
        set.add(VAL1);
        set.add(VAL2);
        map.put(property, set);
        boolean exceptionThrown = false;
        try
        {
            HeaderUtils.getPropertyValue(property, map, false);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
            assertEquals("Inconsistent header values of 'BiID'. "
                    + "Expected the same value in all the columns, found: [val1, val2].", e
                    .getMessage());
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testGetPropertyValuePropertyDefinedMoreThanOnceOneExpectedQuantPeptides()
            throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.DataSetType;
        HashSet<String> set = new HashSet<String>();
        set.add(PROTEIN_LCMS_RATIO);
        set.add(QUANTIFIED_PEPTIDES);
        map.put(property, set);
        assertEquals(PROTEIN_LCMS_RATIO, HeaderUtils.getPropertyValue(property, map,
                false, true));
    }

    @Test
    public void testGetPropertyValuePropertyDefinedMoreThanOnceOneExpectedQuantPeptidesTurnedOff()
            throws Exception
    {
        HashMap<DataHeaderProperty, Set<String>> map =
                new HashMap<DataHeaderProperty, Set<String>>();
        DataHeaderProperty property = DataHeaderProperty.DataSetType;
        HashSet<String> set = new HashSet<String>();
        set.add(QUANTIFIED_PEPTIDES);
        set.add(PROTEIN_LCMS_RATIO);
        map.put(property, set);
        boolean exceptionThrown = false;
        try
        {
            HeaderUtils.getPropertyValue(property, map, false, false);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
            assertEquals(
                    "Inconsistent header values of 'DataSetType'. "
                            + "Expected the same value in all the columns, found: [ProteinLcmsRatio, QuantifiedPeptides].",
                    e.getMessage());
        }
        assertTrue(exceptionThrown);
    }

}
