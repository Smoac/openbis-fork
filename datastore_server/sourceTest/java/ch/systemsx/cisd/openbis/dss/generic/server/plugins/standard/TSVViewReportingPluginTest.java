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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractFileTableReportingPlugin.IGNORE_COMMENTS_PROPERTY_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractFileTableReportingPlugin.IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TSVViewReportingPluginTest extends AbstractFileSystemTestCase
{
    private static final String TEST_FILE = "test.txt";
    
    private File store;

    private File dataSetInStore;

    private DatasetDescription datasetDescription;
    
    @BeforeMethod
    public void beforeMethod()
    {
        store = new File(workingDirectory, "store");
        store.mkdirs();
        dataSetInStore = new File(store, "dataset");
        dataSetInStore.mkdirs();
        datasetDescription = new DatasetDescription();
        datasetDescription.setDatasetCode("ds1");
        datasetDescription.setMainDataSetPattern(".*");
        datasetDescription.setDataSetLocation(dataSetInStore.getName());
    }

    @Test
    public void testCreateReport()
    {
        FileUtilities.writeToFile(new File(dataSetInStore, TEST_FILE), "a\t<42>b\n1\t2\n\t4");
        TSVViewReportingPlugin plugin = new TSVViewReportingPlugin(new Properties(), store);
        TableModel tableModel = plugin.createReport(Arrays.asList(datasetDescription));
        
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        assertEquals("a", headers.get(0).getTitle());
        assertEquals("A", headers.get(0).getId());
        assertEquals("b", headers.get(1).getTitle());
        assertEquals("42", headers.get(1).getId());
        assertEquals(2, headers.size());
        List<TableModelRow> rows = tableModel.getRows();
        assertEquals("[1, 2]", rows.get(0).getValues().toString());
        assertEquals("[, 4]", rows.get(1).getValues().toString());
        assertEquals(2, rows.size());
    }
    
    @Test
    public void testCreateReportFailingBecauseOfInvalidNumberOfCells()
    {
        FileUtilities.writeToFile(new File(dataSetInStore, TEST_FILE), "a\tb\n1\t2\t3\t\n");
        Properties properties = new Properties();
        properties.setProperty(IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY, "true");
        TSVViewReportingPlugin plugin = new TSVViewReportingPlugin(properties, store);
        
        try
        {
            plugin.createReport(Arrays.asList(datasetDescription));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Number of columns in header (2) does not match number of columns in "
                    + "1. data row (3) in Data Set 'ds1' file.", ex.getMessage());
        }
    }
    
    @Test
    public void testIgnoringCommentAndTrailingEmptyCells()
    {
        FileUtilities.writeToFile(new File(dataSetInStore, TEST_FILE), 
                "a\tb\n" +
                "#comment\n" +
                "1\ta\t\t\n" +
                "2\tb\t\n" +
                "3\tc\n" +
        "4\t\n");
        Properties properties = new Properties();
        properties.setProperty(IGNORE_COMMENTS_PROPERTY_KEY, "true");
        properties.setProperty(IGNORE_TRAILING_EMPTY_CELLS_PROPERTY_KEY, "true");
        TSVViewReportingPlugin plugin = new TSVViewReportingPlugin(properties, store);
        TableModel tableModel = plugin.createReport(Arrays.asList(datasetDescription));
        
        assertEquals("[a, b]", tableModel.getHeader().toString());
        List<TableModelRow> rows = tableModel.getRows();
        assertEquals("[1, a]", rows.get(0).getValues().toString());
        assertEquals("[2, b]", rows.get(1).getValues().toString());
        assertEquals("[3, c]", rows.get(2).getValues().toString());
        assertEquals("[4, ]", rows.get(3).getValues().toString());
        assertEquals(4, rows.size());
    }
}
