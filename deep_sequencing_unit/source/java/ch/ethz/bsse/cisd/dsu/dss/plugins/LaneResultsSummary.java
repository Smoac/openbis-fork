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

/**
 * <pre>
 * &lt;LaneResultsSummary&gt;
 *  &lt;Read&gt;
 *  &lt;readNumber&gt;1&lt;/readNumber&gt;
 *  &lt;Lane&gt;
 *  &lt;laneNumber&gt;1&lt;/laneNumber&gt;
 *  &lt;clusterCountPF&gt;
 *  &lt;mean&gt;251107&lt;/mean&gt;
 *  &lt;stdev&gt;9934&lt;/stdev&gt;
 *  &lt;sumsq&gt;11841370000&lt;/sumsq&gt;
 *  &lt;/clusterCountPF&gt;
 *  &lt;clusterCountRaw&gt;
 *  &lt;mean&gt;290967&lt;/mean&gt;
 *  &lt;stdev&gt;12357&lt;/stdev&gt;
 *  &lt;sumsq&gt;18322720000&lt;/sumsq&gt;
 *  &lt;/clusterCountRaw&gt;
 *  &lt;laneYield&gt;2290093&lt;/laneYield&gt;
 * &lt;oneSig&gt;
 *  &lt;mean&gt;497&lt;/mean&gt;
 *  &lt;stdev&gt;61&lt;/stdev&gt;
 *  &lt;sumsq&gt;447654&lt;/sumsq&gt;
 *  &lt;/oneSig&gt;
 *  &lt;percentClustersPF&gt;
 *  &lt;mean&gt;86.32&lt;/mean&gt;
 *  &lt;stdev&gt;1.66&lt;/stdev&gt;
 *  &lt;sumsq&gt;331.22&lt;/sumsq&gt;
 *  &lt;/percentClustersPF&gt;
 *  &lt;signal20AsPctOf1&gt;
 *  &lt;mean&gt;81.60&lt;/mean&gt;
 *  &lt;stdev&gt;6.26&lt;/stdev&gt;
 *  &lt;sumsq&gt;4700.89&lt;/sumsq&gt;
 *  &lt;/signal20AsPctOf1&gt;
 *  &lt;/Lane&gt;
 *  ...
 *  &lt;/Read&gt;
 *  &lt;/LaneResultsSummary&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */

public class LaneResultsSummary
{
    private String Read;

    private Long clusterCountPF;

    private Long clusterCountRaw;

    private Long laneyield;

    public String getRead()
    {
        return Read;
    }
    
    public void setRead(String read)
    {
        Read = read;
    }
    public Long getClusterCountPF()
    {
        return clusterCountPF;
    }

    public void setClusterCountPF(Long clusterCountPF)
    {
        this.clusterCountPF = clusterCountPF;
    }

    public Long getClusterCountRaw()
    {
        return clusterCountRaw;
    }

    public void setClusterCountRaw(Long clusterCountRaw)
    {
        this.clusterCountRaw = clusterCountRaw;
    }

    public Long getLaneyield()
    {
        return laneyield;
    }

    public void setLaneyield(Long laneyield)
    {
        this.laneyield = laneyield;
    }
}