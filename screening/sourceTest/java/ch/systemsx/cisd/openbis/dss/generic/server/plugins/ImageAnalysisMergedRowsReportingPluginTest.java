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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Test cases for the {@link ImageAnalysisMergedRowsReportingPlugin}.
 * 
 * @author Tomasz Pylak
 */
public class ImageAnalysisMergedRowsReportingPluginTest extends AssertJUnit
{

    private final static File dir =
            new File("targets/unit-test-wd/ImageAnalysisMergedRowsReportingPluginTest");

    @Test
    public void testMerge()
    {
        String separator = ",";
        Properties props = createProperties(separator, true);
        TableModel model = createReport(separator, props);

        assertEquals(4, model.getHeader().size());
        assertEquals("Data Set Code#Plate#key#val", StringUtils.join(model.getHeader(), '#'));

        List<TableModelRow> rows = model.getRows();
        assertEquals(2, rows.size());
        assertEquals("datasetCode-a#fa.txt#one, and the only#1", StringUtils.join(rows.get(0)
                .getValues(), '#'));
        assertEquals("datasetCode-b#fb.txt#two#2", StringUtils.join(rows.get(1).getValues(), '#'));
    }

    @Test
    // do not ignore comments - should fail when comment is treated as a header
    public void testMergeNotIgnoringCommentsFailed()
    {

        boolean failedBecauseOfComment = false;
        try
        {
            String separator = ";";
            Properties props = createProperties(separator, false);
            createReport(separator, props);
        } catch (UserFailureException ex)
        {
            AssertionUtil.assertContains("Number of columns in header", ex.getMessage());
            failedBecauseOfComment = true;
        }
        assertTrue("should fail when comment is treated as a header in file a.txt",
                failedBecauseOfComment);
    }

    @Test
    // the same test, but now treat ';' as separator - two columns should be merged
    public void testMergeCheckSeparator()
    {
        Properties props = createProperties(";", true);
        TableModel model = createReport(",", props);

        assertEquals(3, model.getHeader().size());
        assertEquals(2, model.getRows().size());
    }

    private TableModel createReport(String separator, Properties props)
    {
        final File dirA = new File(dir, "a");
        final File dirB = new File(dir, "b");
        dirA.mkdirs();
        dirB.mkdirs();
        final File f1 = new File(dirA, "fa.txt");
        f1.deleteOnExit();
        final File f2 = new File(dirB, "fb.txt");
        f2.deleteOnExit();
        FileUtilities.writeToFile(f2, "\n"); // empty line - should be ignored
        String keyColumnValue = "\"one" + separator + " and the only\""; // escaped separator
        FileUtilities.writeToFile(f1, "# any comment\n" + "key" + separator + "val\n"
                + keyColumnValue + separator + "1\n");
        FileUtilities.writeToFile(f2, "key" + separator + "val\n" + "\"two\"" + separator + "2\n");

        IReportingPluginTask plugin = new ImageAnalysisMergedRowsReportingPlugin(props, dir);
        List<DatasetDescription> datasets =
                Arrays.asList(createDatasetDescription(dirA.getName()),
                        createDatasetDescription(dirB.getName()));
        TableModel model = plugin.createReport(datasets);
        return model;
    }

    private Properties createProperties(String separator, boolean ignoreComments)
    {
        Properties props = new Properties();
        props.put("separator", separator);
        props.put("sub-directory-name", "");
        props.put("ignore-comments", "" + ignoreComments);
        return props;
    }

    private DatasetDescription createDatasetDescription(String location)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDatasetCode("datasetCode-" + location);
        description.setDataSetLocation(location);
        return description;
    }

}
