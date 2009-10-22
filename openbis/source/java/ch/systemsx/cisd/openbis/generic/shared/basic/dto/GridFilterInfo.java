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

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * The specification of the value filter: the column schema and the filter pattern.
 * 
 * @author Tomasz Pylak
 */
public class GridFilterInfo<T> implements IsSerializable
{

    // allows to fetch the value from the row model
    private IColumnDefinition<T> filteredField;

    // the value has to match to this pattern, If null filter should not be applied.
    private String filterTextOrNull;

    // GWT only
    @SuppressWarnings("unused")
    private GridFilterInfo()
    {
    }

    public GridFilterInfo(IColumnDefinition<T> filteredField, String filterTextOrNull)
    {
        this.filteredField = filteredField;
        this.filterTextOrNull = filterTextOrNull;
    }

    public IColumnDefinition<T> getFilteredField()
    {
        return filteredField;
    }

    public String tryGetFilterPattern()
    {
        return filterTextOrNull;
    }

}
