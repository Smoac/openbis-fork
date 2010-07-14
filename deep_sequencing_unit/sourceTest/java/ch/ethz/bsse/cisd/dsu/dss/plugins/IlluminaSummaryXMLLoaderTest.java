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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import java.io.File;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class IlluminaSummaryXMLLoaderTest extends AbstractFileSystemTestCase
{
    @Test
    public void test()
    {
        File file = new File(workingDirectory, "test.xml");
        FileUtilities.writeToFile(file, EXAMPLE);
        IlluminaSummary summary =
                IlluminaSummaryReportingPlugin.IlluminaSummaryXMLLoader.readSummaryXML(file);

        ChipResultsSummary chipResultsSummary = summary.getChipResultsSummary();
        assertEquals(new Long(98792458L), chipResultsSummary.getClusterCountPF());
        assertEquals(new Long(158466917L), chipResultsSummary.getClusterCountRaw());
        assertEquals(new Long(3556528488L), chipResultsSummary.getYield());
        if (summary.getSoftware() == null)
        {
            assertEquals(null, summary.getSoftware());
        } else
        {
            assertEquals("CASAVA-1.6.0", summary.getSoftware());
        }
    }

    protected static final String EXAMPLE = "<?xml version='1.0' ?>\n               "
            + "<?xml-stylesheet type='text/xsl' href='Summary.xsl' ?>\n             "
            + "<Summary>                                                            "
            + "  <ChipResultsSummary>                                               "
            + "    <clusterCountPF>98792458</clusterCountPF>                        "
            + "    <clusterCountRaw>158466917</clusterCountRaw>                     "
            + "    <yield>3556528488</yield>                                        "
            + "  </ChipResultsSummary>                                              "
            + "  <ChipSummary>                                                      "
            + "    <ChipID>unknown</ChipID>                                         "
            + "    <Machine>HWI-EAS264</Machine>                                    "
            + "    <RunFolder>090916_42R0CAAXX</RunFolder>                          "
            + "  </ChipSummary>                                                     "
            + "  <Date>Tue Oct 20 18:15:43 2009</Date>                              "
            + "  <ExpandedLaneSummary/>                                             "
            + "  <LaneParameterSummary/>                                            "
            + "  <LaneResultsSummary/>                                              "
            + "  <TileErrorsByLane/>                                                "
            // + "  <Software>CASAVA-1.6.0</Software>                                  "
            + "</Summary>";
}
