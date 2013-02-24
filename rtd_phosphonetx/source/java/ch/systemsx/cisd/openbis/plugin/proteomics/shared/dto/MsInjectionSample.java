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

package ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MsInjectionSample
{
    private final Sample sample;
    private final Map<String, AbstractExternalData> latestDataSets = new LinkedHashMap<String, AbstractExternalData>();
    private final List<AbstractExternalData> dataSets;

    public MsInjectionSample(Sample sample, List<AbstractExternalData> dataSets)
    {
        this.sample = sample;
        this.dataSets = dataSets;
        add(dataSets);
    }

    private void add(Collection<AbstractExternalData> datasets)
    {
        if (datasets != null)
        {
            for (AbstractExternalData dataSet : datasets)
            {
                addLatestDataSet(dataSet);
                add(dataSet.getChildren());
            }
        }
    }

    public Sample getSample()
    {
        return sample;
    }

    public final List<AbstractExternalData> getDataSets()
    {
        return dataSets;
    }

    public Map<String, AbstractExternalData> getLatestDataSets()
    {
        return latestDataSets;
    }

    private void addLatestDataSet(AbstractExternalData dataSet)
    {
        String dataSetTypeCode = dataSet.getDataSetType().getCode();
        Date registrationDate = dataSet.getRegistrationDate();
        AbstractExternalData latestDataSet = latestDataSets.get(dataSetTypeCode);
        if (latestDataSet == null
                || latestDataSet.getRegistrationDate().getTime() < registrationDate.getTime())
        {
            latestDataSets.put(dataSetTypeCode, dataSet);
        }
    }

    @Override
    public String toString()
    {
        return sample.getIdentifier()+":"+latestDataSets;
    }
    
    
        
}
