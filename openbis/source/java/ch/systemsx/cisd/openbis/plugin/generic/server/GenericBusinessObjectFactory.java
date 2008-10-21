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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.business.bo.AbstractBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.RoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The unique {@link IGenericBusinessObjectFactory} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
public class GenericBusinessObjectFactory extends AbstractBusinessObjectFactory implements
        IGenericBusinessObjectFactory
{
    private GenericBusinessObjectFactory()
    {
    }

    //
    // IGenericBusinessObjectFactory
    //

    public final IGroupBO createGroupBO(final Session session)
    {
        return new GroupBO(getDaoFactory(), session);
    }

    public final IRoleAssignmentTable createRoleAssignmentTable(final Session session)
    {
        return new RoleAssignmentTable(getDaoFactory(), session);
    }

    public final ISampleTable createSampleTable(final Session session)
    {
        return new SampleTable(getDaoFactory(), session);
    }

    public final ISampleBO createSampleBO(final Session session)
    {
        return new SampleBO(getDaoFactory(), session);
    }
}
