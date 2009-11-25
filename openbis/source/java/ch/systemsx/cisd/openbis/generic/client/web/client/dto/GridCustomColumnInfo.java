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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Basic metadata about a grid custom column: code (column identifier), label and description.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomColumnInfo implements IsSerializable
{
    private String code;

    private String label;

    private String description;

    public GridCustomColumnInfo(String code, String label, String description)
    {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    @SuppressWarnings("unused")
    // GWT only
    private GridCustomColumnInfo()
    {
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof GridCustomColumnInfo == false)
        {
            return false;
        }
        GridCustomColumnInfo that = (GridCustomColumnInfo) obj;
        return code.equals(that.code) && label.equals(that.label);
    }

    @Override
    public int hashCode()
    {
        return ((17 * 59) + code.hashCode()) * 59 + label.hashCode(); 
    }
}
