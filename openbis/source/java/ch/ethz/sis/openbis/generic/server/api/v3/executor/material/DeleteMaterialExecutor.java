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

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.material.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author Jakub Straszewski
 */
@Component
public class DeleteMaterialExecutor implements IDeleteMaterialExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    IMapMaterialByIdExecutor mapMaterialByIdExecutor;

    @Override
    public void delete(IOperationContext context, List<? extends IMaterialId> materialIds, MaterialDeletionOptions deletionOptions)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (materialIds == null)
        {
            throw new IllegalArgumentException("Space ids cannot be null");
        }
        if (deletionOptions == null)
        {
            throw new IllegalArgumentException("Deletion options cannot be null");
        }
        if (deletionOptions.getReason() == null)
        {
            throw new IllegalArgumentException("Deletion reason cannot be null");
        }

        IMaterialBO materialBO = businessObjectFactory.createMaterialBO(context.getSession());
        Map<IMaterialId, MaterialPE> materialMap = mapMaterialByIdExecutor.map(context, materialIds);

        for (Map.Entry<IMaterialId, MaterialPE> entry : materialMap.entrySet())
        {
            MaterialPE material = entry.getValue();

            materialBO.deleteByTechId(new TechId(material.getId()), deletionOptions.getReason());
        }
    }

}
