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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GenericTableColumnHeader implements Serializable, IsSerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    public static GenericTableColumnHeader untitledStringHeader(int identifier, String code)
    {
        GenericTableColumnHeader header = new GenericTableColumnHeader();
        header.setIndex(identifier);
        header.setCode(code);
        header.setType(DataTypeCode.VARCHAR);
        return header;
    }

    private String title;
    
    private int index;
    
    private String code;
    
    private DataTypeCode type;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
    
    public void setCode(String code)
    {
        this.code = code;
    }
    
    public String getCode()
    {
        return code;
    }

    public DataTypeCode getType()
    {
        return type;
    }

    public void setType(DataTypeCode type)
    {
        this.type = type;
    }
    
    
}
