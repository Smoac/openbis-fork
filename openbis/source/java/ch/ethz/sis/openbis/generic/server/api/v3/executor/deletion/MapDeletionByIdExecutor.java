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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.deletion;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.deletion.ListDeletionByTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;

/**
 * @author pkupczyk
 */
@Component
public class MapDeletionByIdExecutor extends AbstractMapObjectByIdExecutor<IDeletionId, DeletionPE> implements IMapDeletionByIdExecutor
{

    private IDeletionDAO deletionDAO;

    @SuppressWarnings("unused")
    private MapDeletionByIdExecutor()
    {
    }

    public MapDeletionByIdExecutor(IDeletionDAO deletionDAO)
    {
        this.deletionDAO = deletionDAO;
    }

    @Override
    protected List<IListObjectById<? extends IDeletionId, DeletionPE>> createListers(IOperationContext context)
    {
        List<IListObjectById<? extends IDeletionId, DeletionPE>> listers =
                new LinkedList<IListObjectById<? extends IDeletionId, DeletionPE>>();
        listers.add(new ListDeletionByTechId(deletionDAO));
        return listers;
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        deletionDAO = daoFactory.getDeletionDAO();
    }

}
