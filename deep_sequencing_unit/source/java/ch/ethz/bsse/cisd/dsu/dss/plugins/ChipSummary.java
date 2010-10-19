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

/**
 * <pre>
 *   &lt;ChipSummary&gt;
 *     &lt;ChipID&gt;708KLAAXX&lt;/ChipID&gt;
 *     &lt;Machine&gt;BS-DSU_ELLAC&lt;/Machine&gt;
 *     &lt;RunFolder&gt;101006_708KLAAXX&lt;/RunFolder&gt;
 *   &lt;/ChipSummary&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */
class ChipSummary
{
    private String chipID;

    private String machine;

    private String runFolder;

    @XmlElement (name = "ChipID")
    public String getChipID()
    {
        return chipID;
    }

    public void setChipID(String chipID)
    {
        this.chipID = chipID;
    }

    @XmlElement (name = "Machine")
    public String getMachine()
    {
        return machine;
    }

    public void setMachine(String machine)
    {
        this.machine = machine;
    }

    @XmlElement (name = "RunFolder")
    public String getRunFolder()
    {
        return runFolder;
    }

    public void setRunFolder(String runFolder)
    {
        this.runFolder = runFolder;
    }
}