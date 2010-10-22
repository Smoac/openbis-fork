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
 *     &lt;densityRatio&gt;3556528488&lt;/densityRatio&gt;
 *     &lt;yield&gt;3556528488&lt;/yield&gt;
 *   &lt;/ChipResultsSummary&gt;
 *   &lt;Software&gt;CASAVA-1.7.0&lt;/Software&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */

class ChipResultsSummary
{
    private Long clusterCountPF = 0L;

    private Long clusterCountRaw = 0L;

    private Long yield = 0L;

    private Double densityRatio = 0.0;

    @XmlElement(name = "clusterCountPF")
    public Long getClusterCountPF()
    {
        return clusterCountPF;
    }

    public void setClusterCountPF(Long clusterCountPF)
    {
        this.clusterCountPF = clusterCountPF;
    }

    @XmlElement(name = "clusterCountRaw")
    public Long getClusterCountRaw()
    {
        return clusterCountRaw;
    }

    public void setClusterCountRaw(Long clusterCountRaw)
    {
        this.clusterCountRaw = clusterCountRaw;
    }

    @XmlElement(name = "yield")
    public Long getYield()
    {
        return yield;
    }

    public void setYield(Long yield)
    {
        this.yield = yield;
    }

    @XmlElement(name = "densityRatio")
    public Double getDensityRatio()
    {
        return densityRatio;
    }

    public void setDensityRatio(Double densityRatio)
    {
        this.densityRatio = densityRatio;
    }

}
