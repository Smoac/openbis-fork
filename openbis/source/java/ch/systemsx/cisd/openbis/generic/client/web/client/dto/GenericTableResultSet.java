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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.GenericTableRowColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;

/**
 * Class for the {@link ResultSet} of {@link GenericTableRow}. It also contains the
 * {@link GenericTableRowColumnDefinition}s.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericTableResultSet implements IsSerializable
{
    private ResultSet<GenericTableRow> resultSet;
    private List<GenericTableColumnHeader> headers;

    public GenericTableResultSet(ResultSet<GenericTableRow> resultSet, List<GenericTableColumnHeader> headers)
    {
        this.resultSet = resultSet;
        this.headers = headers;
    }
    
    // GWT only
    @SuppressWarnings("unused")
    private GenericTableResultSet()
    {
    }

    public ResultSet<GenericTableRow> getResultSet()
    {
        return resultSet;
    }

    public List<GenericTableColumnHeader> getHeaders()
    {
        return headers;
    }


}
