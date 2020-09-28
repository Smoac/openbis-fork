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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import java.util.Set;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class VocabularyTermAuthorizationExecutor implements IVocabularyTermAuthorizationExecutor
{

    @Override
    // @RolesAllowed and @Capability are checked later depending whether an official or unofficial term is created
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    @Capability("CREATE_VOCABULARY_TERM")
    public void canCreate(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("CREATE_OFFICIAL_VOCABULARY_TERM")
    public void canCreateOfficial(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_UNOFFICIAL_VOCABULARY_TERM")
    public void canCreateUnofficial(IOperationContext context)
    {
    }

    @Override
    // @RolesAllowed and @Capability are checked later depending whether an official or unofficial term is updated
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY_TERM)
    @Capability("UPDATE_VOCABULARY_TERM")
    public void canUpdate(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("UPDATE_OFFICIAL_VOCABULARY_TERM")
    public void canUpdateOfficial(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("UPDATE_UNOFFICIAL_VOCABULARY_TERM")
    public void canUpdateUnofficial(IOperationContext context)
    {
    }

    @Override
    public boolean canUpdateInternallyManaged(IOperationContext context)
    {
        Set<RoleAssignmentPE> roles = context.getSession().tryGetCreatorPerson().getAllPersonRoles();

        for (RoleAssignmentPE role : roles)
        {
            if ((RoleCode.ETL_SERVER.equals(role.getRole()) || RoleCode.ADMIN.equals(role.getRole()))
                    && role.getRoleWithHierarchy().isInstanceLevel())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.VOCABULARY_TERM, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("DELETE_VOCABULARY_TERM")
    public void canDelete(IOperationContext context, IVocabularyTermId id, VocabularyTermPE term)
    {
        // nothing to do
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_VOCABULARY_TERM")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_VOCABULARY_TERM")
    public void canSearch(IOperationContext context)
    {
    }

}
