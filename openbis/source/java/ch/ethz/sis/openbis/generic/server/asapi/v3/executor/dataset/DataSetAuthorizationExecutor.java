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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Set;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author pkupczyk
 */
@Component
public class DataSetAuthorizationExecutor implements IDataSetAuthorizationExecutor
{

    @Override
    @Capability("CREATE_DATASET")
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void canCreate(IOperationContext context)
    {
        // We are checking the creator of the session here instead of the actual user. The creator of the session has to always be ETL_SERVER. The
        // actual user does not have to be ETL_SERVER. This will happen when data store executes the registration on behalf of a different user for
        // the user to become the registrator.

        Set<RoleAssignmentPE> roles = context.getSession().tryGetCreatorPerson().getAllPersonRoles();
        boolean isEtlServer = false;

        for (RoleAssignmentPE role : roles)
        {
            if (RoleCode.ETL_SERVER.equals(role.getRole()))
            {
                isEtlServer = true;
                break;
            }
        }

        if (false == isEtlServer)
        {
            throw new UserFailureException("Data set creation can be only executed by a user with " + RoleCode.ETL_SERVER + " role.");
        }
    }

    @Override
    public void canCreate(IOperationContext context, DataPE dataSet)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(dataSet.getRegistrator(), dataSet))
        {
            throw new UnauthorizedObjectAccessException(new DataSetPermId(dataSet.getPermId()));
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_DATASET")
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void canUpdate(IOperationContext context)
    {
    }

    @Override
    public void canUpdate(IOperationContext context, IDataSetId id, DataPE dataSet)
    {
        boolean isStorageConfirmed;
        if (dataSet instanceof ExternalDataPE)
        {
            isStorageConfirmed = ((ExternalDataPE) dataSet).isStorageConfirmation();
        } else
        {
            isStorageConfirmed = true;
        }

        if (isStorageConfirmed
                && false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), dataSet))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    @DatabaseCreateOrDeleteModification(value = { ObjectKind.DATA_SET, ObjectKind.DELETION })
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("DELETE_DATASET")
    public void canDelete(IOperationContext context)
    {
    }

    @Override
    public void canDelete(IOperationContext context, IDataSetId id, DataPE dataSet)
    {
        canUpdate(context, id, dataSet);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("GET_DATASET")
    public void canGet(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_DATASET")
    public void canSearch(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("SEARCH_DATASET_TYPE")
    public void canSearchType(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("ARCHIVE_DATASET")
    public void canArchive(IOperationContext context)
    {
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UNARCHIVE_DATASET")
    public void canUnarchive(IOperationContext context)
    {
    }

}
