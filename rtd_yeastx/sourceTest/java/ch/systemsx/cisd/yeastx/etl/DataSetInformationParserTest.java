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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.etlserver.Constants;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * @author Tomasz Pylak
 */
public class DataSetInformationParserTest extends AbstractFileSystemTestCase
{
    private static final String MANDATORY_HEADER_SAMPLE = "file_name sample group\n";

    private static final String HEADER =
    // "# user@gmail.com\n+"+
            "file_name sample parent group experiment project conversion dataset_property_1 dataset_property_2\n";

    private static final String TAB = "\t";

    @Test
    public void testLoadIndexFile()
    {
        File indexFile =
                writeMappingFile(HEADER
                        + "data.txt sample1 parentCode group1 experiment1 project1 fiaML v1 v2");
        List<DataSetMappingInformation> list = tryParse(indexFile);
        AssertJUnit.assertEquals(1, list.size());
        DataSetMappingInformation elem = list.get(0);
        AssertJUnit.assertEquals("group1", elem.getSpaceOrGroupCode());
        AssertJUnit.assertEquals("parentCode", elem.getParentDataSetCodes());
        AssertJUnit.assertEquals("sample1", elem.getSampleCodeOrLabel());
        AssertJUnit.assertEquals("data.txt", elem.getFileName());
        AssertJUnit.assertEquals("experiment1", elem.getExperimentName());
        AssertJUnit.assertEquals("project1", elem.getProjectCode());
        AssertJUnit.assertEquals(2, elem.getProperties().size());
        NewProperty prop1 = elem.getProperties().get(0);
        AssertJUnit.assertEquals("v1", prop1.getValue());
        AssertJUnit.assertEquals("dataset_property_1", prop1.getPropertyCode());
    }

    @Test
    public void testLoadIndexFileMandatoryColumnsOnly()
    {
        File indexFile = writeMappingFile(MANDATORY_HEADER_SAMPLE + "data2.txt sample2 group2");
        List<DataSetMappingInformation> list = tryParse(indexFile);
        AssertJUnit.assertEquals(1, list.size());
        DataSetMappingInformation elem = list.get(0);
        AssertJUnit.assertEquals("group2", elem.getSpaceOrGroupCode());
        AssertJUnit.assertEquals("sample2", elem.getSampleCodeOrLabel());
        AssertJUnit.assertEquals("data2.txt", elem.getFileName());
    }

    @Test
    public void testLoadIndexFileWithMissingFieldValueFails() throws FileNotFoundException,
            IOException
    {
        File indexFile = writeMappingFile(HEADER + TAB + TAB + TAB + TAB + TAB + TAB + TAB + TAB);
        List<DataSetMappingInformation> result = tryParse(indexFile);
        AssertJUnit.assertNull("error during parsing expected", result);
        List<String> logLines = readLogFile();
        System.out.println(logLines);
        AssertJUnit.assertEquals(3, logLines.size());
        AssertionUtil.assertContains("Missing value for the mandatory column", logLines.get(2));
    }

    @Test
    public void testLoadIndexFileWithMissingFieldHeaderFails() throws FileNotFoundException,
            IOException
    {
        File indexFile = writeMappingFile("xxx");
        List<DataSetMappingInformation> result = tryParse(indexFile);
        AssertJUnit.assertNull("error during parsing expected", result);
        List<String> logLines = readLogFile();
        AssertJUnit.assertEquals(2, logLines.size());
        AssertionUtil
                .assertContains("Mandatory column(s) 'file_name' are missing", logLines.get(1));
    }

    private List<DataSetMappingInformation> tryParse(File indexFile)
    {
        LogUtils log = new LogUtils(indexFile.getParentFile());
        return DataSetMappingInformationParser.tryParse(indexFile, log);
    }

    private List<String> readLogFile() throws IOException, FileNotFoundException
    {
        File log = new File(workingDirectory, Constants.USER_LOG_FILE);
        List<String> logLines = readLines(log);
        return logLines;
    }

    @SuppressWarnings("unchecked")
    private static List<String> readLines(File file) throws IOException, FileNotFoundException
    {
        return IOUtils.readLines(new FileInputStream(file));
    }

    private File writeMappingFile(String content)
    {
        return writeFile("index.tsv", content);
    }

    private File writeFile(String fileName, String content)
    {

        String contentWithTabs = spacesToTabs(content);
        File indexFile = new File(workingDirectory, fileName);
        FileUtilities.writeToFile(indexFile, contentWithTabs);
        return indexFile;
    }

    private static String spacesToTabs(String text)
    {
        return text.replaceAll(" ", TAB);
    }
}
