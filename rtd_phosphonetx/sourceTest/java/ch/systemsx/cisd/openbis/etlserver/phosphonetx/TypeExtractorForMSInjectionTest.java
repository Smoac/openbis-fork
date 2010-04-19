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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForMSInjection.DATA_SET_PROPERTIES_FILE;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypeExtractorForMSInjectionTest extends AbstractFileSystemTestCase
{
    @Test
    public void testMissingDataSetPropertiesFile()
    {
        TypeExtractorForMSInjection extractor = new TypeExtractorForMSInjection(new Properties());
        try
        {
            extractor.getDataSetType(workingDirectory);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Missing properties file '" + DATA_SET_PROPERTIES_FILE + "'.", ex.getMessage());
        }
    }
    
    @Test
    public void testGetDataSetType()
    {
        FileUtilities.writeToFile(new File(workingDirectory, DATA_SET_PROPERTIES_FILE), "DATA_SET_TYPE = RAW");
        TypeExtractorForMSInjection extractor = new TypeExtractorForMSInjection(new Properties());
        DataSetType dataSetType = extractor.getDataSetType(workingDirectory);
        assertEquals("RAW", dataSetType.getCode());
    }
    
    @Test
    public void testGetProcessorType()
    {
        FileUtilities.writeToFile(new File(workingDirectory, DATA_SET_PROPERTIES_FILE), "DATA_SET_TYPE = RAW");
        TypeExtractorForMSInjection extractor = new TypeExtractorForMSInjection(new Properties());
        assertEquals(null, extractor.getProcessorType(workingDirectory));
    }
    
    @Test
    public void testGetFileFormatType()
    {
        FileUtilities.writeToFile(new File(workingDirectory, DATA_SET_PROPERTIES_FILE), "FILE_TYPE = XML");
        TypeExtractorForMSInjection extractor = new TypeExtractorForMSInjection(new Properties());
        assertEquals("XML", extractor.getFileFormatType(workingDirectory).getCode());
    }
    
    @Test
    public void testGetLocatorType()
    {
        FileUtilities.writeToFile(new File(workingDirectory, DATA_SET_PROPERTIES_FILE), "FILE_TYPE = XML");
        TypeExtractorForMSInjection extractor = new TypeExtractorForMSInjection(new Properties());
        assertEquals(LocatorType.DEFAULT_LOCATOR_TYPE_CODE, extractor.getLocatorType(workingDirectory).getCode());
    }
}
