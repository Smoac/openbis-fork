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
 *  &lt;Lane&gt;
 *  &lt;laneNumber&gt;1&lt;/laneNumber&gt;
 *  &lt;averageAlignScorePF&gt;
 *  ...
 *  &lt;/averageAlignScorePF&gt;
 *  &lt;clusterCountPF&gt;
 *  ...
 *  &lt;/clusterCountPF&gt;
 *  &lt;clusterCountRaw&gt;
 *  ...
 *  &lt;/clusterCountRaw&gt;
 *  &lt;errorPF&gt;
 *  ...
 *  &lt;/errorPF&gt;
 *  &lt;laneYield&gt;2290093&lt;/laneYield&gt;
 *  &lt;oneSig&gt;
 *  ...
 *  &lt;/oneSig&gt;
 *  &lt;percentClustersPF&gt;
 *  ...
 *  &lt;/percentClustersPF&gt;
 *  &lt;percentUniquelyAlignedPF&gt;
 *  ...
 *  &lt;/percentUniquelyAlignedPF&gt;
 *  &lt;signal20AsPctOf1&gt;
 *  ...
 *  &lt;/signal20AsPctOf1&gt;
 *  &lt;/Lane&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */

class Lane
{
    private Integer laneNumber;

    // since Casava 1.7 available
    private doubleStats averageAlignScorePF;

    private stats clusterCountPF;

    private stats clusterCountRaw;

    // since Casava 1.7 available
    private doubleStats errorPF;

    private Integer laneYield;

    private stats oneSig;

    private doubleStats percentClustersPF;

    // since Casava 1.7 available
    private doubleStats percentUniquelyAlignedPF;

    private doubleStats signal20AsPctOf1;

    @XmlElement(name = "laneNumber")
    public Integer getLaneNumber()
    {
        return laneNumber;
    }

    public void setLaneNumber(Integer laneNumber)
    {
        this.laneNumber = laneNumber;
    }

    @XmlElement(name = "averageAlignScorePF")
    public doubleStats getAverageAlignScorePF()
    {
        return averageAlignScorePF;
    }

    public void setAverageAlignScorePF(doubleStats averageAlignScorePF)
    {
        this.averageAlignScorePF = averageAlignScorePF;
    }

    @XmlElement(name = "clusterCountPF")
    public stats getClusterCountPF()
    {
        return clusterCountPF;
    }

    public void setClusterCountPF(stats clusterCountPF)
    {
        this.clusterCountPF = clusterCountPF;
    }

    @XmlElement(name = "clusterCountRaw")
    public stats getClusterCountRaw()
    {
        return clusterCountRaw;
    }

    public void setClusterCountRaw(stats clusterCountRaw)
    {
        this.clusterCountRaw = clusterCountRaw;
    }

    @XmlElement(name = "errorPF")
    public doubleStats getErrorPF()
    {
        return errorPF;
    }

    public void setErrorPF(doubleStats errorPF)
    {
        this.errorPF = errorPF;
    }

    @XmlElement(name = "laneYield")
    public Integer getLaneYield()
    {
        return laneYield;
    }

    public void setLaneYield(Integer laneYield)
    {
        this.laneYield = laneYield;
    }

    @XmlElement(name = "oneSig")
    public stats getOneSig()
    {
        return oneSig;
    }

    public void setOneSig(stats oneSig)
    {
        this.oneSig = oneSig;
    }

    @XmlElement(name = "percentClustersPF")
    public doubleStats getPercentClustersPF()
    {
        return percentClustersPF;
    }

    public void setPercentClustersPF(doubleStats percentClustersPF)
    {
        this.percentClustersPF = percentClustersPF;
    }

    @XmlElement(name = "percentUniquelyAlignedPF")
    public doubleStats getPercentUniquelyAlignedPF()
    {
        return percentUniquelyAlignedPF;
    }

    public void setPercentUniquelyAlignedPF(doubleStats percentUniquelyAlignedPF)
    {
        this.percentUniquelyAlignedPF = percentUniquelyAlignedPF;
    }

    @XmlElement(name = "signal20AsPctOf1")
    public doubleStats getSignal20AsPctOf1()
    {
        return signal20AsPctOf1;
    }

    public void setSignal20AsPctOf1(doubleStats signal20AsPctOf1)
    {
        this.signal20AsPctOf1 = signal20AsPctOf1;
    }

}
