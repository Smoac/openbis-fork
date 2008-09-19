/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Tomasz Pylak
 */
public class GenericBusinessObjectFactory implements IGenericBusinessObjectFactory
{
    private final IAuthorizationDAOFactory daoFactory;
    
    public GenericBusinessObjectFactory(IAuthorizationDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    public final IPersonBO createPersonBO(final Session session)
    {
        return new PersonBO(daoFactory, session);
    }

    public final IGroupBO createGroupBO(final Session session)
    {
        return new GroupBO(daoFactory, session);
    }

    public final IGroupTable createGroupTable(final Session session)
    {
        return new GroupTable(daoFactory, session);
    }

    public final IRoleAssignmentTable createRoleAssignmentTable(final Session session)
    {
        return new RoleAssignmentTable(daoFactory, session);
    }
}
