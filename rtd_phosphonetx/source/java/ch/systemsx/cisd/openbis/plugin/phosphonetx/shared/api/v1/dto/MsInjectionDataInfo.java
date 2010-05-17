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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MsInjectionDataInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long msInjectionSampleID;
    
    private String msInjectionSampleCode;
    
    private Date msInjectionSampleRegistrationDate;
    
    private Map<PropertyKey, Serializable> msInjectionSampleProperties;
    
    private long biologicalSampleID;
    
    private String biologicalSampleIdentifier;
    
    private Map<PropertyKey, Serializable> biologicalSampleProperties;
    
    private Map<String, Date> latestDataSetRegistrationDates;

    public long getMsInjectionSampleID()
    {
        return msInjectionSampleID;
    }

    public void setMsInjectionSampleID(long msInjectionSampleID)
    {
        this.msInjectionSampleID = msInjectionSampleID;
    }

    public String getMsInjectionSampleCode()
    {
        return msInjectionSampleCode;
    }

    public void setMsInjectionSampleCode(String msInjectionSampleCode)
    {
        this.msInjectionSampleCode = msInjectionSampleCode;
    }

    public Date getMsInjectionSampleRegistrationDate()
    {
        return msInjectionSampleRegistrationDate;
    }

    public void setMsInjectionSampleRegistrationDate(Date msInjectionSampleRegistrationDate)
    {
        this.msInjectionSampleRegistrationDate = msInjectionSampleRegistrationDate;
    }

    public Map<PropertyKey, Serializable> getMsInjectionSampleProperties()
    {
        return msInjectionSampleProperties;
    }

    public void setMsInjectionSampleProperties(
            Map<PropertyKey, Serializable> msInjectionSampleProperties)
    {
        this.msInjectionSampleProperties = msInjectionSampleProperties;
    }

    public long getBiologicalSampleID()
    {
        return biologicalSampleID;
    }

    public void setBiologicalSampleID(long biologicalSampleID)
    {
        this.biologicalSampleID = biologicalSampleID;
    }

    public String getBiologicalSampleIdentifier()
    {
        return biologicalSampleIdentifier;
    }

    public void setBiologicalSampleIdentifier(String biologicalSampleIdentifier)
    {
        this.biologicalSampleIdentifier = biologicalSampleIdentifier;
    }

    public Map<PropertyKey, Serializable> getBiologicalSampleProperties()
    {
        return biologicalSampleProperties;
    }

    public void setBiologicalSampleProperties(Map<PropertyKey, Serializable> biologicalSampleProperties)
    {
        this.biologicalSampleProperties = biologicalSampleProperties;
    }

    public Map<String, Date> getLatestDataSetRegistrationDates()
    {
        return latestDataSetRegistrationDates;
    }

    public void setLatestDataSetRegistrationDates(Map<String, Date> latestDataSetRegistrationDates)
    {
        this.latestDataSetRegistrationDates = latestDataSetRegistrationDates;
    }
}
