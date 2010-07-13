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
 *   &lt;ChipResultsSummary&gt;
 *     &lt;clusterCountPF&gt;98792458&lt;/clusterCountPF&gt;
 *     &lt;clusterCountRaw&gt;158466917&lt;/clusterCountRaw&gt;
 *     &lt;yield&gt;3556528488&lt;/yield&gt;
 *   &lt;/ChipResultsSummary&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */
class ChipResultsSummary
{
    private Long clusterCountPF;

    private Long clusterCountRaw;

    private Long yield;

    @XmlElement
    public Long getClusterCountPF()
    {
        return clusterCountPF;
    }

    public void setClusterCountPF(Long clusterCountPF)
    {
        this.clusterCountPF = clusterCountPF;
    }

    @XmlElement
    public Long getClusterCountRaw()
    {
        return clusterCountRaw;
    }

    public void setClusterCountRaw(Long clusterCountRaw)
    {
        this.clusterCountRaw = clusterCountRaw;
    }

    @XmlElement
    public Long getYield()
    {
        return yield;
    }

    public void setYield(Long yield)
    {
        this.yield = yield;
    }

}
