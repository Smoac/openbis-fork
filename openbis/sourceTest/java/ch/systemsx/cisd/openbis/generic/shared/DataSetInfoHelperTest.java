/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.DataSetUploadInfo.DataSetUploadInfoHelper;

/**
 * @author Izabela Adamczyk
 */
public class DataSetInfoHelperTest extends AssertJUnit
{

    @Test
    public void testDataSetUploadInfoContructor()
    {
        String sample = "sampleA";
        String dataSetType = "dataSetTypeB";
        String fileType = "fileTypeC";
        DataSetUploadInfo info = new DataSetUploadInfo(sample, dataSetType, fileType);
        assertEquals(info.getSample(), sample);
        assertEquals(info.getFileType(), fileType);
        assertEquals(info.getDataSetType(), dataSetType);
    }

    @Test
    public void testExtractFromComment() throws Exception
    {
        DataSetUploadInfo info = DataSetUploadInfoHelper.extractFromCifexComment("sA,dsB,ftC");
        assertEquals("sA", info.getSample());
        assertEquals("ftC", info.getFileType());
        assertEquals("dsB", info.getDataSetType());
    }

    @Test
    public void testEncodeAsComment()
    {
        String sample = "sA";
        String dataSetType = "dsB";
        String fileType = "ftC";
        DataSetUploadInfo info = new DataSetUploadInfo(sample, dataSetType, fileType);
        assertEquals("sA,dsB,ftC", DataSetUploadInfoHelper.encodeAsCifexComment(info));

    }
}
