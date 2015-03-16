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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.material;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.material.ListMaterialsByPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author pkupczyk
 */
@Component
public class MapMaterialByIdExecutor extends AbstractMapObjectByIdExecutor<IMaterialId, MaterialPE> implements IMapMaterialByIdExecutor
{

    private IMaterialDAO materialDAO;

    @SuppressWarnings("unused")
    private MapMaterialByIdExecutor()
    {
    }

    public MapMaterialByIdExecutor(IMaterialDAO materialDAO)
    {
        this.materialDAO = materialDAO;
    }

    @Override
    protected List<IListObjectById<? extends IMaterialId, MaterialPE>> createListers(IOperationContext context)
    {
        List<IListObjectById<? extends IMaterialId, MaterialPE>> listers =
                new LinkedList<IListObjectById<? extends IMaterialId, MaterialPE>>();
        listers.add(new ListMaterialsByPermId(materialDAO));
        return listers;
    }

    @Autowired
    private void setDAOFactory(IDAOFactory daoFactory)
    {
        materialDAO = daoFactory.getMaterialDAO();
    }

}
