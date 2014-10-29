/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.dataset.ListDataSetByPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;

/**
 * @author pkupczyk
 */
@Component
public class MapDataSetByIdExecutor extends AbstractMapObjectByIdExecutor<IDataSetId, DataPE> implements IMapDataSetByIdExecutor
{

    private IDataDAO dataDAO;

    @SuppressWarnings("unused")
    private MapDataSetByIdExecutor()
    {
    }

    public MapDataSetByIdExecutor(IDataDAO dataDAO)
    {
        this.dataDAO = dataDAO;
    }

    @Override
    protected List<IListObjectById<? extends IDataSetId, DataPE>> createListers(IOperationContext context)
    {
        List<IListObjectById<? extends IDataSetId, DataPE>> listers =
                new LinkedList<IListObjectById<? extends IDataSetId, DataPE>>();
        listers.add(new ListDataSetByPermId(dataDAO));
        return listers;
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        dataDAO = daoFactory.getDataDAO();
    }

}
