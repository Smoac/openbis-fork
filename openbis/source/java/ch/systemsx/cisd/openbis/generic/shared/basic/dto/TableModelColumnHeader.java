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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Column header for {@link TableModel}.
 * 
 * @author Tomasz Pylak
 */
public class TableModelColumnHeader implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String title;

    private String id;

    // allows to fetch the value for this column from the row content
    private int index;

    private DataTypeCode dataType;

    // if column values contain permIds of certain entities entity kind is stored here
    private EntityKind entityKindOrNull;
    
    private Map<String, String> properties;

    private int defaultColumnWidth = 150;

    public TableModelColumnHeader()
    {
    }

    public TableModelColumnHeader(String title, String id, int index)
    {
        setId(id);
        setTitle(title);
        setIndex(index);
    }

    public void setDataType(DataTypeCode dataType)
    {
        this.dataType = dataType;
    }

    public DataTypeCode getDataType()
    {
        return dataType;
    }

    public String getTitle()
    {
        return title;
    }

    public void setId(String id)
    {
        // NOTE: id shouldn't contain spaces or some features of columns (e.g. links) will not work
        this.id = id.replaceAll(" ", "_");
    }
    
    public final String getId()
    {
        return id;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
    
    public int getIndex()
    {
        return index;
    }

    public void setDefaultColumnWidth(int width)
    {
        this.defaultColumnWidth = width;
    }

    public int getDefaultColumnWidth()
    {
        return defaultColumnWidth;
    }

    public boolean isNumeric()
    {
        return dataType == DataTypeCode.REAL || dataType == DataTypeCode.INTEGER;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public EntityKind tryGetEntityKind()
    {
        return entityKindOrNull;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKindOrNull = entityKind;
    }
    
    public void setProperty(String key, String value)
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
        }
        properties.put(key, value);
    }
    
    public String tryToGetProperty(String key)
    {
        return properties == null ? null : properties.get(key);
    }

    @Override
    public String toString()
    {
        return title;
    }
}