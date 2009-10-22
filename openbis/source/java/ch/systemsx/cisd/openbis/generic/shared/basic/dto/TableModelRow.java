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
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The content of one row, without the header specification.
 * 
 * @author Tomasz Pylak
 */
public class TableModelRow implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // values in each column from left to right
    private List<ISerializableComparable> values;

    public TableModelRow(List<ISerializableComparable> values)
    {
        this.values = values;
    }

    public List<ISerializableComparable> getValues()
    {
        return values;
    }

    // ---------------------------

    // GWT only
    @SuppressWarnings("unused")
    private TableModelRow()
    {
    }
}
