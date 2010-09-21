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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypedTableModel<T extends IsSerializable>
{
    private final List<TableModelColumnHeader> header;
    private final List<TableModelRowWithObject<T>> rows;

    public TypedTableModel(List<TableModelColumnHeader> header, List<TableModelRowWithObject<T>> rows)
    {
        this.header = header;
        this.rows = rows;
    }

    public final List<TableModelColumnHeader> getHeader()
    {
        return header;
    }

    public final List<TableModelRowWithObject<T>> getRows()
    {
        return rows;
    }

}
