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
public class Experiment implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private long id;
    
    private String spaceCode;
    
    private String projectCode;
    
    private String code;
    
    private Date registrationDate;
    
    private Map<PropertyKey, Serializable> properties;

    public final long getId()
    {
        return id;
    }

    public final void setId(long id)
    {
        this.id = id;
    }

    public final String getSpaceCode()
    {
        return spaceCode;
    }

    public final void setSpaceCode(String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    public final String getProjectCode()
    {
        return projectCode;
    }

    public final void setProjectCode(String projectCode)
    {
        this.projectCode = projectCode;
    }

    public final String getCode()
    {
        return code;
    }

    public final void setCode(String code)
    {
        this.code = code;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final Map<PropertyKey, Serializable> getProperties()
    {
        return properties;
    }

    public final void setProperties(Map<PropertyKey, Serializable> properties)
    {
        this.properties = properties;
    }


}
