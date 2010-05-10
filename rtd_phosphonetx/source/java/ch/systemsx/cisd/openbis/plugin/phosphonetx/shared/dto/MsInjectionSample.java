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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MsInjectionSample
{
    private final Sample sample;
    private final Map<String, Date> latestRegistrationDates = new LinkedHashMap<String, Date>();

    public MsInjectionSample(Sample sample)
    {
        this.sample = sample;
    }

    public Sample getSample()
    {
        return sample;
    }

    public Map<String, Date> getLatestRegistrationDates()
    {
        return latestRegistrationDates;
    }

    public void addLatestDataSet(String dataSetTypeCode, Date registrationDate)
    {
        Date date = latestRegistrationDates.get(dataSetTypeCode);
        if (date == null || date.getTime() < registrationDate.getTime())
        {
            latestRegistrationDates.put(dataSetTypeCode, registrationDate);
        }
    }

    @Override
    public String toString()
    {
        return sample.getIdentifier()+":"+latestRegistrationDates;
    }
    
    
        
}
