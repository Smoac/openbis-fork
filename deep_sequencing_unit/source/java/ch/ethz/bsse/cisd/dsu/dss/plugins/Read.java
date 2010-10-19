/* Copyright 2010 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * <pre>
 *  &lt;Read&gt;
 *  &lt;readNumber&gt;1&lt;/readNumber&gt;
 *  &lt;Lane&gt;
 *  ...
 *  &lt;/Lane&gt;
 *  ...
 *  &lt;/Read&gt;
 * </pre>
 * 
 * @author Manuel Kohler
 */

class Read
{
    private List<Integer> readNumber = new ArrayList<Integer>();
    
    private List<Lane> lanes;
    
    @XmlElement (name = "readNumber")
    public List<Integer> getReadNumbers()
    {
        return readNumber;
    }

    public void setReadNumbers(List<Integer> readNumber)
    {
        this.readNumber = readNumber;
    }

    @XmlElement (name = "Lane")
    public List<Lane> getLanes()
    {
        return lanes;
    }

    public void setLanes(List<Lane> lanes)
    {
        this.lanes = lanes;
    }
    

}
    