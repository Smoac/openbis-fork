/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.helper.dataset;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
public class ListDataSetByPermId implements IListObjectById<DataSetPermId, DataPE>
{

    private IDataDAO dataDAO;

    public ListDataSetByPermId(IDataDAO dataDAO)
    {
        this.dataDAO = dataDAO;
    }

    @Override
    public Class<DataSetPermId> getIdClass()
    {
        return DataSetPermId.class;
    }

    @Override
    public DataSetPermId createId(DataPE dataSet)
    {
        return new DataSetPermId(dataSet.getCode());
    }

    @Override
    public List<DataPE> listByIds(List<DataSetPermId> ids)
    {
        Set<String> codes = new HashSet<String>();

        for (DataSetPermId id : ids)
        {
            codes.add(id.getPermId());
        }

        return dataDAO.listByCode(codes);
    }

}
