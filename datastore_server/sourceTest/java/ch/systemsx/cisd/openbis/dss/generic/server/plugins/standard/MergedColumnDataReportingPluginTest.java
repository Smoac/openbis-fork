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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Test cases for the {@link MergedColumnDataReportingPlugin}.
 * 
 * @author Bernd Rinn
 */
public class MergedColumnDataReportingPluginTest
{

    private final static File dir =
            new File("targets/unit-test-wd/MergedColumnDataReportingPluginTest");

    @Test
    public void testMerge()
    {
        final File dirA = new File(dir, "a");
        final File dirB = new File(dir, "b");
        final File dirC = new File(dir, "c");
        dirA.mkdirs();
        dirB.mkdirs();
        dirC.mkdirs();
        final File f1 = new File(dirA, "results.txt");
        f1.deleteOnExit();
        final File f2 = new File(dirB, "results.txt");
        f2.deleteOnExit();
        final File f3 = new File(dirC, "results.txt");
        f3.deleteOnExit();
        FileUtilities.writeToFile(f1, "key\tval1\n" + "one\t1\n" + "two\t2.2\n" + "three\tCC\n");
        FileUtilities.writeToFile(f2, "val2\tkey\n" + "17\ttwo\n" + "42\tthree\n"
                + "105\tfour\n");
        FileUtilities.writeToFile(f3, "key\tval3\n" + "one\t0\n" + "three\t-8.2\n"
                + "two\t1.9e+5\n");
        Properties props = new Properties();
        props.put("row-id-column-header", "key");
        props.put("sub-directory-name", "");
        final IReportingPluginTask plugin = new MergedColumnDataReportingPlugin(props, dir);
        final DatasetDescription dsd1 = new DatasetDescription("", "a", "", "");
        final DatasetDescription dsd2 = new DatasetDescription("", "b", "", "");
        final DatasetDescription dsd3 = new DatasetDescription("", "c", "", "");
        final TableModel model = plugin.createReport(Arrays.asList(dsd1, dsd2, dsd3));
        assertEquals(4, model.getHeader().size());
        assertEquals("key", model.getHeader().get(0).getTitle());
        assertEquals(TableModelColumnType.TEXT, model.getHeader().get(0).getType());
        assertEquals("val1", model.getHeader().get(1).getTitle());
        assertEquals(TableModelColumnType.TEXT, model.getHeader().get(1).getType());
        assertEquals("val2", model.getHeader().get(2).getTitle());
        assertEquals(TableModelColumnType.INTEGER, model.getHeader().get(2).getType());
        assertEquals("val3", model.getHeader().get(3).getTitle());
        assertEquals(TableModelColumnType.REAL, model.getHeader().get(3).getType());
        assertEquals(4, model.getRows().size());
        assertEquals("one\t1\t\t0", StringUtils.join(model.getRows().get(0).getValues(), '\t'));
        assertEquals("two\t2.2\t17\t1.9e+5", StringUtils.join(model.getRows().get(1).getValues(), '\t'));
        assertEquals("three\tCC\t42\t-8.2", StringUtils.join(model.getRows().get(2).getValues(), '\t'));
        assertEquals("four\t\t105\t", StringUtils.join(model.getRows().get(3).getValues(), '\t'));
    }

}
