/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentTypeAuthorizationExecutor implements IExperimentTypeAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("CREATE_EXPERIMENT_TYPE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT_TYPE)
    public void canCreate(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_EXPERIMENT_TYPE")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN })
    @Capability("UPDATE_EXPERIMENT_TYPE")
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT_TYPE)
    public void canUpdate(IOperationContext context)
    {
    }
    
    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN })
    @Capability("DELETE_EXPERIMENT_TYPE")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT_TYPE)
    public void canDelete(IOperationContext context)
    {
    }

}
