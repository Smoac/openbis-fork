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
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.parser.MandatoryPropertyMissingException;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * @author Tomasz Pylak
 */
public class DataSetInformationParserTest extends AbstractFileSystemTestCase
{
    private static final String HEADER =
            "file_name sample experiment conversion dataset_property_1 dataset_property_2\n";

    private static final String TAB = "\t";

    @Test
    public void testLoadIndexFile()
    {
        File indexFile = writeFile(HEADER + "data.txt sample1 experiment1 conversion1 v1 v2");
        List<DataSetMappingInformation> list = DataSetMappingInformationParser.tryParse(indexFile);
        AssertJUnit.assertEquals(1, list.size());
        DataSetMappingInformation elem = list.get(0);
        AssertJUnit.assertEquals("data.txt", elem.getFileName());
        AssertJUnit.assertEquals(2, elem.getProperties().size());
        NewProperty prop1 = elem.getProperties().get(0);
        AssertJUnit.assertEquals("v1", prop1.getValue());
        AssertJUnit.assertEquals("dataset_property_1", prop1.getPropertyCode());
    }

    // TODO 2009-05-25, Tomasz Pylak: remove from broken after LMS-914 is fixed
    @Test(expectedExceptions = MandatoryPropertyMissingException.class, groups = "broken")
    public void testLoadIndexFileWithMissingFieldValueFails()
    {
        File indexFile = writeFile(HEADER + TAB + TAB + TAB + TAB + TAB);
        DataSetMappingInformationParser.tryParse(indexFile);
    }

    @Test(expectedExceptions = MandatoryPropertyMissingException.class)
    public void testLoadIndexFileWithMissingFieldHeaderFails()
    {
        File indexFile = writeFile("xxx");
        DataSetMappingInformationParser.tryParse(indexFile);
    }

    private File writeFile(String content)
    {
        String contentWithTabs = spacesToTabs(content);
        File indexFile = new File(workingDirectory, "index.tsv");
        FileUtilities.writeToFile(indexFile, contentWithTabs);
        return indexFile;
    }

    private static String spacesToTabs(String text)
    {
        return text.replaceAll(" ", TAB);
    }
}
