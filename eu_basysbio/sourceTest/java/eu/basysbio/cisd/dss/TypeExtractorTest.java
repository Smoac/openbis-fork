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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypeExtractorTest extends AbstractFileSystemTestCase
{
    private TypeExtractor extractor;
    private File dataSet;

    @BeforeMethod
    public void beforeMethod()
    {
        extractor = new TypeExtractor(new Properties());
        dataSet = new File(workingDirectory, "data-set");
    }
    
    @Test
    public void testNonExistentDataSet()
    {
        try
        {
            extractor.getDataSetType(dataSet);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data set should be a folder: " + dataSet.getAbsolutePath(), ex
                    .getMessage());
        }
    }
    
    @Test
    public void testMissingDataFile()
    {
        dataSet.mkdirs();
        try
        {
            extractor.getDataSetType(dataSet);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Exactly one file of type '.data.txt' expected instead of 0.", ex
                    .getMessage());
        }
    }
    
    
    @Test
    public void testMoreThanOneDataFile() throws IOException
    {
        dataSet.mkdirs();
        new File(dataSet, "MY_TYPE" + TypeExtractor.DATA_TYPE).createNewFile();
        new File(dataSet, "MY_TYPE2" + TypeExtractor.DATA_TYPE).createNewFile();
        try
        {
            extractor.getDataSetType(dataSet);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Exactly one file of type '.data.txt' expected instead of 2.", ex
                    .getMessage());
        }
    }
    
    @Test
    public void testExtractDataSetType() throws IOException
    {
        dataSet.mkdirs();
        new File(dataSet, "MY_TYPE" + TypeExtractor.DATA_TYPE).createNewFile();
        assertEquals("MY_TYPE", extractor.getDataSetType(dataSet).getCode());
    }
    
}
