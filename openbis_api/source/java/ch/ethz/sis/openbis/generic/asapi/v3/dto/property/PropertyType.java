/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.property;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.property.PropertyType")
public class PropertyType implements ICodeHolder, Serializable
{
    private static final long serialVersionUID = 1L;

    private String code;
    
    private String label;
    
    private String description;
    
    private DataTypeCode dataTypeCode;
    
    private boolean internalNameSpace;

    @Override
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public DataTypeCode getDataTypeCode()
    {
        return dataTypeCode;
    }

    public void setDataTypeCode(DataTypeCode dataTypeCode)
    {
        this.dataTypeCode = dataTypeCode;
    }

    public boolean isInternalNameSpace()
    {
        return internalNameSpace;
    }

    public void setInternalNameSpace(boolean internalNameSpace)
    {
        this.internalNameSpace = internalNameSpace;
    }

}
