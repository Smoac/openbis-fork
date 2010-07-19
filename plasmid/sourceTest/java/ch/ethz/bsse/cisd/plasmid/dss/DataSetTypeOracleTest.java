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
import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.bsse.cisd.plasmid.dss.DataSetTypeOracle.DataSetTypeInfo;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

/**
 * @author Piotr Buczek
 */
public class DataSetTypeOracleTest extends AbstractFileSystemTestCase
{
    private static final String MAPPING = "SEQ_FILE: gb fasta xdna, RAW_DATA: ab1";

    @DataProvider(name = "files")
    protected Object[][] files()
    {
        return new Object[][]
            {
                { "PRS316.gb", DataSetTypeInfo.SEQ_FILE },
                { "PRS316.fasta", DataSetTypeInfo.SEQ_FILE },
                { "PRS316.xdna", DataSetTypeInfo.SEQ_FILE },
                { "PRS316-1.ab1", DataSetTypeInfo.RAW_DATA },
                { "PRS316-2.ab1", DataSetTypeInfo.RAW_DATA },
                { "PRS316-3.ab1", DataSetTypeInfo.RAW_DATA },
                { "PRS316.png", DataSetTypeInfo.VERIFICATION },
                { "PRS316.tiff", DataSetTypeInfo.VERIFICATION }

            };
    }

    private static Properties prepareProperties(String typeMapping)
    {
        Properties result = new Properties();
        result.put(DataSetTypeOracle.DATASET_TYPES_NAME, typeMapping);
        return result;
    }

    @Test(dataProvider = "files")
    public void testFile(String filename, DataSetTypeInfo expectedInfo)
    {
        File file = new File(workingDirectory, filename);
        DataSetTypeOracle.initializeMapping(prepareProperties(MAPPING));

        DataSetTypeInfo info = DataSetTypeOracle.extractDataSetTypeInfo(file);
        assertEquals(expectedInfo, info);
        assertEquals(info != DataSetTypeInfo.SEQ_FILE, info.isMeasured());
    }

    public void testDirectory()
    {
        DataSetTypeInfo info = DataSetTypeOracle.extractDataSetTypeInfo(workingDirectory);
        assertEquals(DataSetTypeInfo.UNKNOWN, info);
    }

}
