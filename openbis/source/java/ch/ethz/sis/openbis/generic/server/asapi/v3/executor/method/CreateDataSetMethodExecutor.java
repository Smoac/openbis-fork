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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.ICreateDataSetExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.ICreateEntityExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
@Component
public class CreateDataSetMethodExecutor extends AbstractCreateMethodExecutor<DataSetPermId, DataSetCreation> implements
        ICreateDataSetMethodExecutor
{

    @Autowired
    private ICreateDataSetExecutor createExecutor;

    @Override
    protected void checkSession(Session session)
    {
        // We are checking the creator of the session here instead of the actual user. The creator of the session has to always be ETL_SERVER. The
        // actual user does not have to be ETL_SERVER. This will happen when data store executes the registration on behalf of a different user for
        // the user to become the registrator.

        Set<RoleAssignmentPE> roles = session.tryGetCreatorPerson().getAllPersonRoles();

        for (RoleAssignmentPE role : roles)
        {
            if (RoleCode.ETL_SERVER.equals(role.getRole()))
            {
                return;
            }
        }

        throw new UserFailureException("Data set creation can be only executed by a user with " + RoleCode.ETL_SERVER + " role.");
    }

    @Override
    protected ICreateEntityExecutor<DataSetCreation, DataSetPermId> getCreateExecutor()
    {
        return createExecutor;
    }

}
