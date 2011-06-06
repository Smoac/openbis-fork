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

package eu.basynthec.cisd.dss;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class TimeSeriesDataExcelTest extends AssertJUnit
{

    @Test
    public void testReadingMetaboliteData()
    {
        TimeSeriesDataExcel data =
                TimeSeriesDataExcel
                        .createTimeSeriesDataExcel("sourceTest/examples/Metabolomics-Example.xlsx");
        List<String[]> metadataLines = data.getRawMetadataLines();
        assertTrue("Metadata lines should not be empty", metadataLines.size() > 0);
        String[][] expectedMetadata =
            {
                { "Property", "Value" },
                { "Experiment", "BLANK" },
                { "Strain", "strain1" },
                { "Timepoint Type", "EX" },
                { "Cell Location", "CE" },
                { "Value Type", "Std" },
                { "Value Unit", "RatioCs" },
                { "Scale", "Lin" } };
        assertLinesAreEqual(metadataLines, expectedMetadata);

        List<String[]> dataLines = data.getRawDataLines();
        assertTrue("Data lines should not be empty", dataLines.size() > 0);
        String[][] expectedData =
            {
                { "CompoundID", "HumanReadable", "-703.0", "-603.0" },
                { "CHEBI:15521", "phosphate1", "0.095157063", "0.083137933" },
                { "CHEBI:18311", "phosphate2", "0.059749697", "0.044605606" } };
        assertLinesAreEqual(dataLines, expectedData);
    }

    /**
     * Check that the lines in expected show up in actual in the same order. Actual may have
     * additional columns, though -- these are ignored.
     */
    private void assertLinesAreEqual(List<String[]> actual, String[][] expected)
    {
        assertTrue("Number of lines does not match: " + expected.length + " vs. " + actual.size(),
                actual.size() == expected.length);
        for (int i = 0; i < expected.length; ++i)
        {
            String[] actualLine = actual.get(i);
            String[] expectedLine = expected[i];
            for (int j = 0; j < expectedLine.length; ++j)
            {
                assertEquals(expectedLine[j], actualLine[j]);
            }
        }
    }
}
