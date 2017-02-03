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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentAuthorizationExecutor implements IExperimentAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_EXPERIMENT")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public void canCreate(IOperationContext context)
    {
    }

    @Override
    public void canCreate(IOperationContext context, ExperimentPE experiment)
    {
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
        {
            throw new UnauthorizedObjectAccessException(new ExperimentIdentifier(experiment.getIdentifier()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_EXPERIMENT")
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void canUpdate(IOperationContext context, IExperimentId id, @AuthorizationGuard(guardClass = ExperimentPEPredicate.class) ExperimentPE experiment)
    {
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.EXPERIMENT, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_EXPERIMENT")
    public void canDelete(IOperationContext context, IExperimentId id,
            @AuthorizationGuard(guardClass = ExperimentPEPredicate.class) ExperimentPE experiment)
    {
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_EXPERIMENT")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_EXPERIMENT")
    public void canSearch(IOperationContext context)
    {
    }

}
