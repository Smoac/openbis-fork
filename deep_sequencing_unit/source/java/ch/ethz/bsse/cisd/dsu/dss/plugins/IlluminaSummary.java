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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Note: Not all XML Elements are read in
 * 
 * <pre>
 *   &lt;Summary&gt;
 *     &lt;ChipResultsSummary&gt;
 *     ...
 *     &lt;/ChipResultsSummary&gt;
 *     ...
 *     &lt;ChipSummary&gt;
 *     ...
 *     &lt;/ChipSummary&gt;
 *     ...
 *     &lt;Date&gt;
 *     ...
 *     &lt;/Date&gt;
 *     ...
 *     &lt;ExpandedLaneSummary&gt;
 *     ...
 *     &lt;/ExpandedLaneSummary&gt;
 *     ...
 *     &lt;LaneParameterSummary&gt;
 *     ...
 *     &lt;/LaneParameterSummary&gt;
 *     ...
 *     &lt;LaneResultsSummary&gt;
 *     ...
 *     &lt;/LaneResultsSummary&gt;
 *     ...
 *     &lt;Software&gt;CASAVA-1.7.0&lt;/Software&gt;
 *     ...
 *     &lt;TileErrorsByLane&gt;
 *     ...
 *     &lt;/TileErrorsByLane&gt;
 *     ...
 *     &lt;TileResultsByLane&gt;
 *     ...
 *     &lt;/TileResultsByLane&gt;
 *     ...
 *   &lt;Summary&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */

@XmlRootElement(name = "Summary")
class IlluminaSummary
{
    private ChipResultsSummary chipResultsSummary;

    private LaneResultsSummary LaneResultsSummary;

    private ChipSummary chipSummary;

    private String Software = "";

    private String Date = "";

    @XmlElement(name = "ChipResultsSummary")
    public ChipResultsSummary getChipResultsSummary()
    {
        return chipResultsSummary;
    }

    public void setChipResultsSummary(ChipResultsSummary chipResultsSummary)
    {
        this.chipResultsSummary = chipResultsSummary;
    }

    @XmlElement(name = "LaneResultsSummary")
    public LaneResultsSummary getLaneResultsSummary()
    {
        return LaneResultsSummary;
    }

    public void setLaneResultsSummary(LaneResultsSummary laneResultsSummary)
    {
        this.LaneResultsSummary = laneResultsSummary;
    }

    @XmlElement(name = "ChipSummary")
    public ChipSummary getChipSummary()
    {
        return chipSummary;
    }

    public void setChipSummary(ChipSummary chipSummary)
    {
        this.chipSummary = chipSummary;
    }

    @XmlElement(name = "Software")
    public String getSoftware()
    {
        return Software;
    }

    public void setSoftware(String software)
    {
        this.Software = software;
    }

    @XmlElement(name = "Date")
    public String getDate()
    {
        return Date;
    }

    public void setDate(String date)
    {
        this.Date = date;
    }
}
