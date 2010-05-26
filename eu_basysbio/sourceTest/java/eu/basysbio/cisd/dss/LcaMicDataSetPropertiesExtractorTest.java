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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class LcaMicDataSetPropertiesExtractorTest extends AbstractFileSystemTestCase
{
    static final String EXAMPLE =
            "# Ma::MS::B1::NT::EX::T1::NC::GrowthRate::Value[h^(-1)]::LIN::BBA9001#A_S20090325-2::NC\t0.68\n"
                    + "Time (s)\t"
                    + "Ma::MS::B1::NT::EX::T1::NC::LcaMicCfd::Value[um]::LIN::BBA9001#A_S20090325-2::NC\t"
                    + "Ma::MS::B1::NT::EX::T1::NC::LcaMicAbsFl::Mean[Au]::LIN::BBA9001#A_S20090325-2::NC\t"
                    + "Ma::MS::B1::NT::EX::T1::NC::LcaMicAbsFl::Std[Au]::LIN::BBA9001#A_S20090325-2::NC\n";

    @Test
    public void test() throws IOException
    {
        File ds = new File(workingDirectory, "ds");
        ds.mkdirs();
        File file = new File(ds, "t.txt");
        FileUtilities.writeToFile(file, EXAMPLE);
        IDataSetPropertiesExtractor extractor =
                new LcaMicDataSetPropertiesExtractor(new Properties());
        List<NewProperty> props = extractor.extractDataSetProperties(ds);
        assertEquals(13, props.size());
        Map<String, String> map = new HashMap<String, String>();
        for (NewProperty property : props)
        {
            map.put(property.getPropertyCode(), property.getValue());
        }
        assertEquals("BBA9001#A_S20090325-2", map.get(TimeSeriesPropertyType.BI_ID.toString()));
        assertEquals("B1", map.get(TimeSeriesPropertyType.BIOLOGICAL_REPLICATE_CODE.toString()));
        assertEquals("NC", map.get(TimeSeriesPropertyType.CEL_LOC.toString()));
        assertEquals("NC", map.get(TimeSeriesPropertyType.CG_LIST.toString()));
        assertEquals("MS", map.get(TimeSeriesPropertyType.CULTIVATION_METHOD_EXPERIMENT_CODE.toString()));
        assertEquals("Ma", map.get(TimeSeriesPropertyType.EXPERIMENT_CODE.toString()));
        assertEquals("LIN", map.get(TimeSeriesPropertyType.SCALE_LIST.toString()));
        assertEquals("T1", map.get(TimeSeriesPropertyType.TECHNICAL_REPLICATE_CODE_LIST.toString()));
        assertEquals("0", map.get(TimeSeriesPropertyType.TIME_POINT_LIST.toString()));
        assertEquals("EX", map.get(TimeSeriesPropertyType.TIME_POINT_TYPE.toString()));
        assertEquals("LcaMicAbsFl, LcaMicCfd", map.get(TimeSeriesPropertyType.TIME_SERIES_DATA_SET_TYPE.toString()));
        assertEquals(null, map.get(TimeSeriesPropertyType.UPLOADER_EMAIL.toString()));
        assertEquals("Mean[Au], Std[Au], Value[um]", map.get(TimeSeriesPropertyType.VALUE_TYPE_LIST.toString()));
        assertEquals("0.68", map.get(LcaMicDataSetPropertiesExtractor.GROWTH_RATE));
    }
    
}
