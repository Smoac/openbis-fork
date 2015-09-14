/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IMapProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.sql.IProjectSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class MapProjectSqlMethodExecutor extends AbstractMapMethodExecutor<IProjectId, Long, Project, ProjectFetchOptions> implements
        IMapProjectMethodExecutor
{

    @Autowired
    private IMapProjectByIdExecutor mapExecutor;

    @Autowired
    private IProjectSqlTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IProjectId, Long> getMapExecutor()
    {
        // TODO replace with IProjectId -> Long mapExecutor once there is one
        return new IMapObjectByIdExecutor<IProjectId, Long>()
            {
                @Override
                public Map<IProjectId, Long> map(IOperationContext context, Collection<? extends IProjectId> ids)
                {
                    Map<IProjectId, ProjectPE> peMap = mapExecutor.map(context, ids);
                    Map<IProjectId, Long> idMap = new LinkedHashMap<IProjectId, Long>();

                    for (Map.Entry<IProjectId, ProjectPE> peEntry : peMap.entrySet())
                    {
                        idMap.put(peEntry.getKey(), peEntry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

    @Override
    protected ITranslator<Long, Project, ProjectFetchOptions> getTranslator()
    {
        return translator;
    }

}
