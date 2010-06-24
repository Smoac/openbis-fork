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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TimeSeriesPropertiesReportingPluginTest extends AbstractFileSystemTestCase
{
    @Test
    public void test()
    {
        DatasetDescription ds1 =
                createDataSet("ds1", "1::2::3::4::5::6::7::8::9::10::11::12\t"
                                   + "1::2::3::44::5::66::7::8::99::101::11::121");
        DatasetDescription ds2 =
                createDataSet("ds2", "1::2::3::4::5::6::7::8::9::10::11::12::13::14");
        IReportingPluginTask plugin =
                new TimeSeriesPropertiesReportingPlugin(new Properties(), workingDirectory);

        TableModel table = plugin.createReport(Arrays.asList(ds1, ds2));
        assertEquals("[CODE, TECHNICAL_REPLICATE_CODE_LIST, BIOLOGICAL_REPLICATE_CODE, "
                + "TIME_SERIES_DATA_SET_TYPE, CEL_LOC, CG_LIST, "
                + "CULTIVATION_METHOD_EXPERIMENT_CODE, EXPERIMENT_CODE, GENOTYPE, "
                + "GROWTH_PHASE, SCALE_LIST, TIME_POINT_LIST, "
                + "TIME_POINT_TYPE, BI_ID, VALUE_TYPE_LIST]", table.getHeader().toString());
        List<TableModelRow> rows = table.getRows();
        Collections.sort(rows, new Comparator<TableModelRow>()
            {
                public int compare(TableModelRow r1, TableModelRow r2)
                {
                    return r1.getValues().toString().compareTo(r2.getValues().toString());
                }
            });
        assertEquals("[ds1, 6, 66, 3, 8, 7, 12, 121, 2, 1, , , 10, 101, 4, 44, 5, 11, 9, 99]", rows
                .get(0).getValues().toString());
        assertEquals("[ds2, 6, 3, 8, 7, 12, 2, 1, 14, 13, 10, 4, 5, 11, 9]", rows.get(1)
                .getValues().toString());
        assertEquals(2, rows.size());
    }

    private DatasetDescription createDataSet(String dataSetCode, String header)
    {
        File dir = new File(workingDirectory, dataSetCode + "/original");
        dir.mkdirs();
        File file = new File(dir, "data.tsv");
        FileUtilities.writeToFile(file, header);
        DatasetDescription datasetDescription = new DatasetDescription();
        datasetDescription.setDataSetLocation(dataSetCode);
        datasetDescription.setDatasetCode(dataSetCode);
        return datasetDescription;
    }
}
