/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class ExcelRowTokenizerTest extends AssertJUnit
{
    @Test
    public void testIntegerValuesParsedCorrectly() throws Exception
    {
        List<Row> row = getRows();

        String[] line1 = ExcelRowTokenizer.tokenizeRow(row.get(0));
        assertEquals("4980", line1[0]);
        assertEquals("TEST_STRING", line1[1]);
        assertEquals("yes", line1[2]);

        String[] line2 = ExcelRowTokenizer.tokenizeRow(row.get(1));
        assertEquals("12.3", line2[0]);
        assertEquals("Meh blah & so on", line2[1]);
        assertEquals("no", line2[2]);

    }

    private List<Row> getRows() throws Exception
    {
        final InputStream stream = getClass().getResourceAsStream("excel-row-tokenizer-test.xls");
        try
        {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(stream);
            HSSFWorkbook workbook = new HSSFWorkbook(poifsFileSystem);
            final HSSFSheet sheet = workbook.getSheetAt(0);
            return Arrays.<Row> asList(sheet.getRow(0), sheet.getRow(1));
        } finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

}
