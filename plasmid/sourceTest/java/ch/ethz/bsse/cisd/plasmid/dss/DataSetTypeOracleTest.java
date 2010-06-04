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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.bsse.cisd.plasmid.dss.DataSetTypeOracle.DataSetTypeInfo;

/**
 * @author Piotr Buczek
 */
public class DataSetTypeOracleTest extends AssertJUnit
{
    private static final String EXAMPLE_DIR = "resource/example/BOX_1:FRP_1/";

    @DataProvider(name = "files")
    protected Object[][] files()
    {
        return new Object[][]
            {
                { "PRS316.gb", DataSetTypeInfo.GB },
                { "PRS316-1.ab1", DataSetTypeInfo.SEQUENCING },
                { "PRS316-2.ab1", DataSetTypeInfo.SEQUENCING },
                { "PRS316-3.ab1", DataSetTypeInfo.SEQUENCING },
                { "PRS316.png", DataSetTypeInfo.VERIFICATION },
                { "PRS316.tiff", DataSetTypeInfo.VERIFICATION }

            };
    }

    @Test(dataProvider = "files")
    public void testFile(String filename, DataSetTypeInfo expectedInfo)
    {
        File file = new File(EXAMPLE_DIR + filename);
        DataSetTypeInfo info = DataSetTypeOracle.extractDataSetTypeInfo(file);
        assertEquals(expectedInfo, info);
        assertEquals(info != DataSetTypeInfo.GB, info.isMeasured());
    }

    public void testDirectory()
    {
        File dir = new File(EXAMPLE_DIR);
        DataSetTypeOracle.extractDataSetTypeInfo(dir);
        DataSetTypeInfo info = DataSetTypeOracle.extractDataSetTypeInfo(dir);
        assertEquals(DataSetTypeInfo.UNKNOWN, info);
    }
}
