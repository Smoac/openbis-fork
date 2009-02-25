/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Test cases for the {@link DefaultDataSetInfoExtractor}.
 * 
 * @author Bernd Rinn
 */
public final class DefaultDataSetInfoExtractorTest extends CodeExtractortTestCase
{

    @Test
    public void testHappyCaseWithDefaultProperties()
    {
        final String barcode = "XYZ123";
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(new Properties());

        final DataSetInformation dsInfo =
                extractor.getDataSetInformation(new File("bla.bla." + barcode));

        assertNull(dsInfo.getExperimentIdentifier());
        assertEquals(barcode, dsInfo.getSampleIdentifier().getSampleCode());
        assertEquals(null, dsInfo.getParentDataSetCode());
        assertEquals(null, dsInfo.getProducerCode());
        assertEquals(null, dsInfo.getProductionDate());
    }

    @Test
    public void testHappyCaseWithProducerCodeAndProductionDate()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_DATA_PRODUCER_CODE, "-2");
        properties.setProperty(INDEX_OF_DATA_PRODUCTION_DATE, "0");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        final String producerCode = "M1";
        final String productionDate = "20070903181312";
        final String barcode = "XYZ123";

        final DataSetInformation dsInfo =
                extractor.getDataSetInformation(new File(productionDate + ".A.B." + producerCode
                        + "." + barcode));

        assertEquals(barcode, dsInfo.getSampleIdentifier().getSampleCode());
        assertEquals(producerCode, dsInfo.getProducerCode());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        assertEquals(productionDate, dateFormat.format(dsInfo.getProductionDate()));
    }

    @Test
    public void testHappyCaseWithAllPropertiesSet()
    {
        final Properties properties = new Properties();
        final String separator = "=";
        properties.setProperty(ENTITY_SEPARATOR, separator);
        properties.setProperty(INDEX_OF_SAMPLE_CODE, "0");
        properties.setProperty(INDEX_OF_PARENT_DATA_SET_CODE, "1");
        properties.setProperty(INDEX_OF_DATA_PRODUCER_CODE, "-2");
        properties.setProperty(INDEX_OF_DATA_PRODUCTION_DATE, "-1");
        final String format = "yyyy-MM-dd HH:mm:ss";
        properties.setProperty(DATA_PRODUCTION_DATE_FORMAT, format);
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        final String producerCode = "M1";
        final String parentDataSetCode = "1234-8";
        final String productionDate = "2007-09-03 18:03:12";
        final String barcode = "XYZ-123";
        final DataSetInformation dsInfo =
                extractor.getDataSetInformation(new File(barcode + separator + parentDataSetCode
                        + separator + "A" + separator + producerCode + separator + productionDate));
        assertEquals(barcode, dsInfo.getSampleIdentifier().getSampleCode());
        assertEquals(parentDataSetCode, dsInfo.getParentDataSetCode());
        assertEquals(producerCode, dsInfo.getProducerCode());
        final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        assertEquals(productionDate, dateFormat.format(dsInfo.getProductionDate()));
    }

    @Test
    public void testWrongProductionDateFormat()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_DATA_PRODUCTION_DATE, "0");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        try
        {
            extractor.getDataSetInformation(new File("blabla.XYZ-123"));
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Could not parse data production date 'blabla' "
                    + "because it violates the following format: yyyyMMddHHmmss", e.getMessage());
        }
    }

    @Test
    public void testIndexTooLarge()
    {
        final Properties properties = new Properties();
        properties.setProperty(INDEX_OF_SAMPLE_CODE, "1");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        try
        {
            extractor.getDataSetInformation(new File("XYZ-123"));
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Invalid data set name 'XYZ-123'. We need 2 entities, separated by '.', "
                    + "but got only 1.", e.getMessage());
        }
    }

    @Test
    public void testIndexTooSmall()
    {
        final Properties properties = new Properties();
        properties.setProperty(ENTITY_SEPARATOR, "-");
        properties.setProperty(INDEX_OF_SAMPLE_CODE, "-3");
        final IDataSetInfoExtractor extractor = new DefaultDataSetInfoExtractor(properties);
        try
        {
            extractor.getDataSetInformation(new File("XYZ-123"));
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Invalid data set name 'XYZ-123'. We need 3 entities, separated by '-', "
                    + "but got only 2.", e.getMessage());
        }
    }

}
