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
import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FlowLineFeederTest extends AbstractFileSystemTestCase
{
    private static final String DROP_BOX_PREFIX = "drop-box-";
    private FlowLineFeeder flowLineFeeder;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        assertEquals(true, new File(workingDirectory, DROP_BOX_PREFIX + "1").mkdirs());
        assertEquals(true, new File(workingDirectory, DROP_BOX_PREFIX + "2").mkdirs());
        Properties properties = new Properties();
        properties.setProperty(FlowLineFeeder.FLOW_LINE_DROP_BOX_TEMPLATE, new File(
                workingDirectory, DROP_BOX_PREFIX).getAbsolutePath()
                + "{0}");
        flowLineFeeder = new FlowLineFeeder(properties);
    }
    
    @Test
    public void testMissingProperty()
    {
        try
        {
            new FlowLineFeeder(new Properties());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + FlowLineFeeder.FLOW_LINE_DROP_BOX_TEMPLATE
                    + "' not found in properties '[]'", ex.getMessage());
        }
    }
    
    @Test 
    void testMissingDropBox()
    {
        File flowCell = new File(workingDirectory, "abc");
        assertEquals(true, flowCell.mkdir());
        FileUtilities.writeToFile(new File(flowCell, "s_3.srf"), "hello flow line 3");
        
        try
        {
            flowLineFeeder.handle(flowCell, null);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            AssertionUtil.assertContains(DROP_BOX_PREFIX + "3", ex.getMessage());
        }
    }
    
    @Test
    public void testHappyCase()
    {
        File flowCell = new File(workingDirectory, "abc");
        assertEquals(true, flowCell.mkdir());
        File logs = new File(flowCell, "logs");
        assertEquals(true, logs.mkdir());
        FileUtilities.writeToFile(new File(logs, "basic.log"), "hello log");
        File srfFolder = new File(flowCell, "SRF");
        assertEquals(true, srfFolder.mkdir());
        File originalFlowLine1 = new File(srfFolder, "s_1.srf");
        FileUtilities.writeToFile(originalFlowLine1, "hello flow line 1");
        File originalFlowLine2 = new File(srfFolder, "2.srf");
        FileUtilities.writeToFile(originalFlowLine2, "hello flow line 2");

        flowLineFeeder.handle(flowCell, null);
        
        checkFlowLineDataSet(originalFlowLine1, "1");
        checkFlowLineDataSet(originalFlowLine2, "2");
    }
    
    @Test
    public void testUndoLastOperation()
    {
        testHappyCase();
        assertEquals(1, new File(workingDirectory, DROP_BOX_PREFIX + "1").list().length);
        assertEquals(1, new File(workingDirectory, DROP_BOX_PREFIX + "2").list().length);
        
        flowLineFeeder.undoLastOperation();
        
        assertEquals(0, new File(workingDirectory, DROP_BOX_PREFIX + "1").list().length);
        assertEquals(0, new File(workingDirectory, DROP_BOX_PREFIX + "2").list().length);
    }

    private void checkFlowLineDataSet(File originalFlowLine, String flowLineNumber)
    {
        File dropBox = new File(workingDirectory, DROP_BOX_PREFIX + flowLineNumber);
        File ds = new File(dropBox, "abc_" + flowLineNumber);
        assertEquals(true, ds.isDirectory());

        File flowLine = new File(ds, originalFlowLine.getName());
        assertEquals(true, flowLine.isFile());
        assertEquals(FileUtilities.loadToString(originalFlowLine), FileUtilities
                .loadToString(flowLine));
        // check hard-link copy by changing last-modified date of one file should change
        // last-modified date of the other file.
        originalFlowLine.setLastModified(4711000);
        assertEquals(4711000, flowLine.lastModified());
    }
}
