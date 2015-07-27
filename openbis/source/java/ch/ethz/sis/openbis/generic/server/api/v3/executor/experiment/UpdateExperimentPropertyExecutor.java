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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExperimentPropertyExecutor implements IUpdateExperimentPropertyExecutor
{

    @Autowired
    private IUpdateEntityPropertyExecutor executor;

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_EXPERIMENT_PROPERTY")
    public void update(IOperationContext context, Map<IEntityPropertiesHolder, Map<String, String>> entityToPropertiesMap)
    {
        executor.update(context, entityToPropertiesMap);
    }

}
