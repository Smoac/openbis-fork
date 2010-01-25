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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Definition of a column header for {@link GenericTableRow} data. 
 * A column had has
 * <ul><li>an index (needed to access a cell in a {@link GenericTableRow} object),
 * <li>a code which has to be unique among all other headers,
 * <li>a data type
 * </ul>
 * The header title is optional. If not specified the code will be the title. Usually it will be
 * set in the client code by using a translation mechanism (like {@link IMessageProvider}).
 *
 * @author Franz-Josef Elmer
 */
public class GenericTableColumnHeader implements Serializable, IsSerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;
    
    /**
     * Creates a header without a title.
     */
    public static GenericTableColumnHeader untitledStringHeader(int index, String code)
    {
        GenericTableColumnHeader header = new GenericTableColumnHeader();
        header.setIndex(index);
        header.setCode(code);
        header.setType(DataTypeCode.VARCHAR);
        return header;
    }

    /**
     * Creates a header without a title and <code>linkable</code> flag set <code>true</code>..
     */
    public static GenericTableColumnHeader untitledLinkableStringHeader(int index, String code)
    {
        GenericTableColumnHeader header = untitledStringHeader(index, code);
        header.setLinkable(true);
        return header;
    }
    
    private String title;
    
    private int index;
    
    private String code;
    
    private boolean linkable;
    
    private DataTypeCode type;

    public String getTitle()
    {
        return title == null ? code : title;
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

    public void setLinkable(boolean linkable)
    {
        this.linkable = linkable;
    }

    public boolean isLinkable()
    {
        return linkable;
    }
    
}
