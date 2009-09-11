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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Test cases for the {@link IterativeTableModelBuilder}.
 * 
 * @author Bernd Rinn
 */
public class IterativeTableModelBuilderTest
{
    @BeforeTest
    public void setUp()
    {
        LogInitializer.init();
    }
    
    @Test
    public void testHappyCaseOneFile()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description =
                new DatasetDescription("code", "location", "sampleCode", "groupCode");
        final DatasetFileLines lines =
                new DatasetFileLines(new File("doesn't matter"), description, Arrays.asList(
                        "id\tval", "a\tA", "b\tB", "c\tC"));
        builder.addFile(lines);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(2, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<String> row0 = rows.get(0).getValues();
        assertEquals(2, row0.size());
        assertEquals("a", row0.get(0));
        assertEquals("A", row0.get(1));
        final List<String> row1 = rows.get(1).getValues();
        assertEquals(2, row1.size());
        assertEquals("b", row1.get(0));
        assertEquals("B", row1.get(1));
        final List<String> row2 = rows.get(2).getValues();
        assertEquals(2, row2.size());
        assertEquals("c", row2.get(0));
        assertEquals("C", row2.get(1));
    }

    @Test
    public void testHappyCaseTwoFiles()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description =
                new DatasetDescription("code", "location", "sampleCode", "groupCode");
        final DatasetFileLines lines =
                new DatasetFileLines(new File("doesn't matter"), description, Arrays.asList(
                        "id\tval", "a\tA", "b\tB", "c\tC"));
        builder.addFile(lines);
        final DatasetFileLines lines2 =
            new DatasetFileLines(new File("doesn't matter"), description, Arrays.asList(
                    "id\tval2", "a\tD", "b\tE", "c\tF"));
        builder.addFile(lines2);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(3, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        assertEquals("val2", headers.get(2).getTitle());
        assertEquals(2, headers.get(2).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<String> row0 = rows.get(0).getValues();
        assertEquals(3, row0.size());
        assertEquals("a", row0.get(0));
        assertEquals("A", row0.get(1));
        assertEquals("D", row0.get(2));
        final List<String> row1 = rows.get(1).getValues();
        assertEquals(3, row1.size());
        assertEquals("b", row1.get(0));
        assertEquals("B", row1.get(1));
        assertEquals("E", row1.get(2));
        final List<String> row2 = rows.get(2).getValues();
        assertEquals(3, row2.size());
        assertEquals("c", row2.get(0));
        assertEquals("C", row2.get(1));
        assertEquals("F", row2.get(2));
    }

    @Test
    public void testHappyCaseTwoFilesDifferentIds()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description =
                new DatasetDescription("code", "location", "sampleCode", "groupCode");
        final DatasetFileLines lines =
                new DatasetFileLines(new File("doesn't matter"), description, Arrays.asList(
                        "id\tval", "a\tA", "b\tB", "c\tC"));
        builder.addFile(lines);
        final DatasetFileLines lines2 =
            new DatasetFileLines(new File("doesn't matter"), description, Arrays.asList(
                    "id\tval2", "d\tD", "b\tE", "f\tF"));
        builder.addFile(lines2);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(3, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        assertEquals("val2", headers.get(2).getTitle());
        assertEquals(2, headers.get(2).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(5, rows.size());
        final List<String> row0 = rows.get(0).getValues();
        assertEquals(3, row0.size());
        assertEquals("a", row0.get(0));
        assertEquals("A", row0.get(1));
        assertEquals("", row0.get(2));
        final List<String> row1 = rows.get(1).getValues();
        assertEquals(3, row1.size());
        assertEquals("b", row1.get(0));
        assertEquals("B", row1.get(1));
        assertEquals("E", row1.get(2));
        final List<String> row2 = rows.get(2).getValues();
        assertEquals(3, row2.size());
        assertEquals("c", row2.get(0));
        assertEquals("C", row2.get(1));
        assertEquals("", row2.get(2));
        final List<String> row3 = rows.get(3).getValues();
        assertEquals(3, row3.size());
        assertEquals("d", row3.get(0));
        assertEquals("", row3.get(1));
        assertEquals("D", row3.get(2));
        final List<String> row4 = rows.get(4).getValues();
        assertEquals(3, row4.size());
        assertEquals("f", row4.get(0));
        assertEquals("", row4.get(1));
        assertEquals("F", row4.get(2));
    }

    @Test
    public void testFileTwice()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description =
                new DatasetDescription("code", "location", "sampleCode", "groupCode");
        final DatasetFileLines lines =
                new DatasetFileLines(new File("doesn't matter"), description, Arrays.asList(
                        "id\tval", "a\tA", "b\tB", "c\tC"));
        builder.addFile(lines);
        builder.addFile(lines);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(3, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        assertEquals("valX", headers.get(2).getTitle());
        assertEquals(2, headers.get(2).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<String> row0 = rows.get(0).getValues();
        assertEquals(3, row0.size());
        assertEquals("a", row0.get(0));
        assertEquals("A", row0.get(1));
        assertEquals("A", row0.get(2));
        final List<String> row1 = rows.get(1).getValues();
        assertEquals(3, row1.size());
        assertEquals("b", row1.get(0));
        assertEquals("B", row1.get(1));
        assertEquals("B", row1.get(2));
        final List<String> row2 = rows.get(2).getValues();
        assertEquals(3, row2.size());
        assertEquals("c", row2.get(0));
        assertEquals("C", row2.get(1));
        assertEquals("C", row2.get(2));
    }
    
    @Test
    public void testNoIdColumn()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description =
                new DatasetDescription("code", "location", "sampleCode", "groupCode");
        final DatasetFileLines lines =
                new DatasetFileLines(new File("bad file - no id"), description, Arrays.asList(
                        "ID\tval", "a\tA", "b\tB", "c\tC"));
        builder.addFile(lines);
        final TableModel model = builder.getTableModel();
        assertEquals(0, model.getHeader().size());
        assertEquals(0, model.getRows().size());
    }

    @Test
    public void testTwoFilesNoIdColumnInOneFile()
    {
        final IterativeTableModelBuilder builder = new IterativeTableModelBuilder("id");
        final DatasetDescription description =
                new DatasetDescription("code", "location", "sampleCode", "groupCode");
        final DatasetFileLines lines =
                new DatasetFileLines(new File("bad file - no id (2)"), description, Arrays.asList(
                        "ID\tval", "a\tA", "b\tB", "c\tC"));
        builder.addFile(lines);
        final DatasetFileLines lines2 =
            new DatasetFileLines(new File("doesn't matter"), description, Arrays.asList(
                    "id\tval", "a\tA", "b\tB", "c\tC"));
        builder.addFile(lines2);
        final TableModel model = builder.getTableModel();
        final List<TableModelColumnHeader> headers = model.getHeader();
        assertEquals(2, headers.size());
        assertEquals("id", headers.get(0).getTitle());
        assertEquals(0, headers.get(0).getIndex());
        assertEquals("val", headers.get(1).getTitle());
        assertEquals(1, headers.get(1).getIndex());
        final List<TableModelRow> rows = model.getRows();
        assertEquals(3, rows.size());
        final List<String> row0 = rows.get(0).getValues();
        assertEquals(2, row0.size());
        assertEquals("a", row0.get(0));
        assertEquals("A", row0.get(1));
        final List<String> row1 = rows.get(1).getValues();
        assertEquals(2, row1.size());
        assertEquals("b", row1.get(0));
        assertEquals("B", row1.get(1));
        final List<String> row2 = rows.get(2).getValues();
        assertEquals(2, row2.size());
        assertEquals("c", row2.get(0));
        assertEquals("C", row2.get(1));
    }
}
