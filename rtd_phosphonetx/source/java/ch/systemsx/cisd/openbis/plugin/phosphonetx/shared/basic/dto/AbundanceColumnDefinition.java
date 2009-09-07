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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbundanceColumnDefinition implements Serializable, IsSerializable, Comparable<AbundanceColumnDefinition>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    private long sampleID;
    
    private String sampleCode;
    
    private List<Treatment> treatments;
    
    public final long getSampleID()
    {
        return sampleID;
    }

    public final void setSampleID(long sampleID)
    {
        this.sampleID = sampleID;
    }

    public final String getSampleCode()
    {
        return sampleCode;
    }

    public final void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public final List<Treatment> getTreatments()
    {
        return treatments;
    }

    public final void setTreatments(List<Treatment> treatments)
    {
        this.treatments = treatments;
    }

    public int compareTo(AbundanceColumnDefinition that)
    {
        if (this.treatments != null && that.treatments != null)
        {
            int sizeDiff = this.treatments.size() - that.treatments.size();
            if (sizeDiff != 0)
            {
                return sizeDiff;
            }
            Collections.sort(this.treatments);
            Collections.sort(that.treatments);
            for (int i = 0, n = treatments.size(); i < n; i++)
            {
                Treatment thisTreatment = this.treatments.get(i);
                Treatment thatTreatment = that.treatments.get(i);
                int diff = thisTreatment.compareTo(thatTreatment);
                if (diff != 0)
                {
                    return diff;
                }
            }
        }
        if (this.sampleCode != null && that.sampleCode != null)
        {
            return this.sampleCode.compareTo(that.sampleCode);
        }
        return 0;
    }
}
