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
 *   &lt;mean&gt;277736&lt;/mean&gt;
 *   &lt;stdev&gt;11981&lt;/stdev&gt;
 *   &lt;sumsq&gt;17224950000&lt;/sumsq&gt;
 * </pre>
 * 
 * @author kohleman
 */

public class stats
{
    private Long mean = 0L;

    private Long stdev = 0L;

    private Long sumsq = 0L;

    @XmlElement(name = "mean")
    public Long getMean()
    {
        return mean;
    }

    public void setMean(Long mean)
    {
        this.mean = mean;
    }

    @XmlElement(name = "stdev")
    public Long getStdev()
    {
        return stdev;
    }

    public void setStdev(Long stdev)
    {
        this.stdev = stdev;
    }

    @XmlElement(name = "sumsq")
    public Long getSumsq()
    {
        return sumsq;
    }

    public void setSumsq(Long sumsq)
    {
        this.sumsq = sumsq;
    }
}
