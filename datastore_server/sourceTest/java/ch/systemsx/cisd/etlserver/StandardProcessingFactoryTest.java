/*
 * Copyright 2008 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.fail;

import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Test cases for the {@link StandardProcessorFactory}.
 * 
 * @author Bernd Rinn
 */
public class StandardProcessingFactoryTest
{

    @Test
    public void testCreateStandardProcessingFactoryWithMissingInputDataSetFormat()
    {
        try
        {
            final Properties props = new Properties();
            props.put("parameters-file", "parameters.dat");
            props.put("finished-file-template", ".finished_{0}");
            StandardProcessorFactory.create(props);
            fail("Missing property not detected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'input-storage-format' not found in properties"
                    + " '[parameters-file, finished-file-template]'", ex.getMessage());
        }
    }

    @Test
    public void testCreateStandardProcessingFactoryWithIllegalInputDataSetFormat()
    {
        try
        {
            final Properties props = new Properties();
            props.put("input-storage-format", "ILLEGAL");
            props.put("parameters-file", "parameters.dat");
            props.put("finished-file-template", ".finished_{0}");
            StandardProcessorFactory.create(props);
            fail("Illegal property value not detected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("input-storage-format property has illegal value 'ILLEGAL'.", ex
                    .getMessage());
        }
    }

    @Test
    public void testCreateStandardProcessingFactoryWithInputDataSetFormatProprietary()
    {
        final Properties props = new Properties();
        props.put("input-storage-format", "PROPRIETARY");
        props.put("parameters-file", "parameters.dat");
        props.put("finished-file-template", ".finished_{0}");
        props.put("data-set-code-prefix-glue", "_");
        StandardProcessorFactory.create(props);
    }

    @Test
    public void testCreateStandardProcessingFactoryWithInputDataSetFormatBdsDirectory()
    {
        final Properties props = new Properties();
        props.put("input-storage-format", "BDS_DIRECTORY");
        props.put("parameters-file", "parameters.dat");
        props.put("finished-file-template", ".finished_{0}");
        props.put("data-set-code-prefix-glue", "_");
        StandardProcessorFactory.create(props);
    }

    @Test
    public void testCreateStandardProcessingFactoryWithMissingParametersFileProperty()
    {
        try
        {
            final Properties props = new Properties();
            props.put("input-storage-format", "BDS_DIRECTORY");
            props.put("finished-file-template", ".finished_{0}");
            StandardProcessorFactory.create(props);
            fail("Missing property not detected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'parameters-file' not found in properties"
                    + " '[input-storage-format, finished-file-template]'", ex.getMessage());
        }
    }

    @Test
    public void testCreateStandardProcessingFactoryWithMissingFinishedFileTemplateProperty()
    {
        try
        {
            final Properties props = new Properties();
            props.put("input-storage-format", "BDS_DIRECTORY");
            props.put("parameters-file", "parameters.dat");
            StandardProcessorFactory.create(props);
            fail("Missing property not detected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key 'finished-file-template' not found in properties"
                    + " '[parameters-file, input-storage-format]'", ex.getMessage());
        }
    }

}
