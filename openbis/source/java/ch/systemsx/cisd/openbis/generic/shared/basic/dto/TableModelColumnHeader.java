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

    // allows to fetch the value for this column from the row content
    private int index;

    private boolean numeric;
    
    public TableModelColumnHeader(String title, int index)
    {
        this(title, index, false);
    }
    
    public TableModelColumnHeader(String title, int index, boolean numeric)
    {
        this.title = title;
        this.index = index;
        this.numeric = numeric;
    }

    public String getTitle()
    {
        return title;
    }

    public int getIndex()
    {
        return index;
    }

    public boolean isNumeric()
    {
        return numeric;
    }
    
    // GWT only
    @SuppressWarnings("unused")
    private TableModelColumnHeader()
    {
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setTitle(String title)
    {
        this.title = title;
    }

    // GWT only
    @SuppressWarnings("unused")
    private void setIndex(int index)
    {
        this.index = index;
    }
    
    // GWT only
    @SuppressWarnings("unused")
    private void setNumeric(boolean numeric)
    {
        this.numeric = numeric;
    }
}