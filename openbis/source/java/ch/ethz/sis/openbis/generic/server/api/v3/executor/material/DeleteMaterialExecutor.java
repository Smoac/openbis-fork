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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractDeleteEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.material.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author Jakub Straszewski
 */
@Component
public class DeleteMaterialExecutor extends AbstractDeleteEntityExecutor<Void, IMaterialId, MaterialPE, MaterialDeletionOptions> implements
        IDeleteMaterialExecutor
{

    @Autowired
    IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Override
    protected Map<IMaterialId, MaterialPE> map(IOperationContext context, List<? extends IMaterialId> entityIds)
    {
        return mapMaterialByIdExecutor.map(context, entityIds);
    }

    @Override
    protected void checkAccess(IOperationContext context, IMaterialId entityId, MaterialPE entity)
    {
        // nothing to do
    }

    @Override
    protected void updateModificationDateAndModifier(IOperationContext context, MaterialPE entity)
    {
        // nothing to do
    }

    @Override
    protected Void delete(IOperationContext context, Collection<MaterialPE> materials, MaterialDeletionOptions deletionOptions)
    {
        IMaterialBO materialBO = businessObjectFactory.createMaterialBO(context.getSession());
        for (MaterialPE material : materials)
        {
            materialBO.deleteByTechId(new TechId(material.getId()), deletionOptions.getReason());
        }
        return null;
    }

}
