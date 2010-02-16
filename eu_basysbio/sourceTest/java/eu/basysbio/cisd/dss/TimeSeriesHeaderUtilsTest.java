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

import org.apache.commons.lang.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for {@link TimeSeriesHeaderUtils}.
 * 
 * @author Izabela Adamczyk
 */
public class TimeSeriesHeaderUtilsTest extends AssertJUnit
{
    private static final String CG_NEW1 = "CG1";

    private static final String TR_NEW1 = "TR1";

    private static final String TR_NEW2 = "TR2";

    private static final String INCONSISTENT_HEADERS_MESSAGE = "Inconsistent data column headers";

    private static final String DEFAULT_HEADER = "0::1::2::3::4::5::6::7::8::9::10::11";

    @Test
    public void testConsistentOneHeaderNoRequirement() throws Exception
    {
        String header = new Header().toString();
        assertEquals(DEFAULT_HEADER, header);
        Collection<DataColumnHeader> headers = Arrays.asList(new DataColumnHeader(header));
        TimeSeriesHeaderUtils
                .assertMetadataConsistent(headers, new ArrayList<DataHeaderProperty>());
    }

    @Test
    public void testConsistentOneHeaderOneRequirement() throws Exception
    {
        String header = new Header().toString();
        assertEquals(DEFAULT_HEADER, header);
        Collection<DataColumnHeader> headers = Arrays.asList(new DataColumnHeader(header));
        TimeSeriesHeaderUtils.assertMetadataConsistent(headers, Arrays
                .asList(DataHeaderProperty.BiologicalReplicatateCode));
    }

    @Test
    public void testConsistentOneHeaderTwoRequirements() throws Exception
    {
        String header = new Header().toString();
        assertEquals(DEFAULT_HEADER, header);
        Collection<DataColumnHeader> headers = Arrays.asList(new DataColumnHeader(header));
        TimeSeriesHeaderUtils.assertMetadataConsistent(headers, Arrays.asList(
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
        TimeSeriesHeaderUtils.assertMetadataConsistent(headers, Arrays
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
        TimeSeriesHeaderUtils.assertMetadataConsistent(headers, Arrays
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
            TimeSeriesHeaderUtils.assertMetadataConsistent(headers, Arrays.asList(property));
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
            TimeSeriesHeaderUtils.assertMetadataConsistent(headers, Arrays.asList(property));
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
            TimeSeriesHeaderUtils.assertMetadataConsistent(headers, Arrays.asList(firstRequirement,
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

}
