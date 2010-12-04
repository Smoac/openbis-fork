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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
public final class DataProviderAdapter<T extends ISerializable> implements
        IOriginalDataProvider<TableModelRowWithObject<T>>
{
    private final ITableModelProvider<T> provider;

    public DataProviderAdapter(ITableModelProvider<T> provider)
    {
        this.provider = provider;
    }

    public List<TableModelRowWithObject<T>> getOriginalData()
    {
        return provider.getTableModel().getRows();
    }

    public List<TableModelColumnHeader> getHeaders()
    {
        return provider.getTableModel().getHeader();
    }
}
