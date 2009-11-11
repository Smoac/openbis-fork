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

package ch.ethz.bsse.cisd.dsu.dss;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorTest extends AbstractFileSystemTestCase
{
    private BufferedAppender appender;

    @BeforeMethod
    public final void beforeMethod()
    {
        LogInitializer.init();
        appender = new BufferedAppender();
        appender.resetLogContent();
    }

    @Test
    public void testWithSingleQuotes()
    {
        DataSetInfoExtractor extractor = new DataSetInfoExtractor(new Properties());
        File configFile = new File(workingDirectory, DataSetInfoExtractor.DEFAULT_PATH_TO_CONFIG_FILE);
        configFile.getParentFile().mkdirs();
        FileUtilities.writeToFile(configFile, "<abc>\n  <Software Name='RTA' Version='1.4.15.0'/>\n</abc>\n");
        
        DataSetInformation info = extractor.getDataSetInformation(workingDirectory, null);
        
        List<NewProperty> properties = info.getDataSetProperties();
        assertEquals(1, properties.size());
        assertEquals(DataSetInfoExtractor.VERSION_KEY, properties.get(0).getPropertyCode());
        assertEquals("1.4.15.0", properties.get(0).getValue());
        // checks delegation to DefaultDataSetInfoExtractor
        assertEquals("DataSetInfoExtractorTest", info.getSampleCode()); 
    }
    
    @Test
    public void testWithDoubleQuotes()
    {
        DataSetInfoExtractor extractor = new DataSetInfoExtractor(new Properties());
        File configFile = new File(workingDirectory, DataSetInfoExtractor.DEFAULT_PATH_TO_CONFIG_FILE);
        configFile.getParentFile().mkdirs();
        FileUtilities.writeToFile(configFile, "<abc>\n  <Software Name=\"RTA\" Version=\"1.4.15.0\"/>\n</abc>\n");
        
        DataSetInformation info = extractor.getDataSetInformation(workingDirectory, null);
        
        List<NewProperty> properties = info.getDataSetProperties();
        assertEquals(1, properties.size());
        assertEquals(DataSetInfoExtractor.VERSION_KEY, properties.get(0).getPropertyCode());
        assertEquals("1.4.15.0", properties.get(0).getValue());
    }
    
    @Test
    public void testWithMissingVersion()
    {
        DataSetInfoExtractor extractor = new DataSetInfoExtractor(new Properties());
        File configFile = new File(workingDirectory, DataSetInfoExtractor.DEFAULT_PATH_TO_CONFIG_FILE);
        configFile.getParentFile().mkdirs();
        FileUtilities.writeToFile(configFile, "<abc>\n  </abc>\n");
        
        DataSetInformation info = extractor.getDataSetInformation(workingDirectory, null);
        
        List<NewProperty> properties = info.getDataSetProperties();
        assertEquals(0, properties.size());
        String logContent = appender.getLogContent();
        assertEquals("No version found in config file 'Data/Intensities/config.xml'.", logContent);
    }
    
    @Test
    public void testWithMissingConfigFile()
    {
        DataSetInfoExtractor extractor = new DataSetInfoExtractor(new Properties());
        
        DataSetInformation info = extractor.getDataSetInformation(workingDirectory, null);
        
        List<NewProperty> properties = info.getDataSetProperties();
        assertEquals(0, properties.size());
        String logContent = appender.getLogContent();
        assertEquals(
                "Config file 'Data/Intensities/config.xml' does not exists or is a directory.",
                logContent);
    }
}
