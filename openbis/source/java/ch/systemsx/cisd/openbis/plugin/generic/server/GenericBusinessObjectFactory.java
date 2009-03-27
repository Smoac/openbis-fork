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

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProcedureBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.AbstractPluginBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The unique {@link IGenericBusinessObjectFactory} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.GENERIC_BUSINESS_OBJECT_FACTORY)
public final class GenericBusinessObjectFactory extends AbstractPluginBusinessObjectFactory
        implements IGenericBusinessObjectFactory
{

    //
    // IGenericBusinessObjectFactory
    //

    public final ISampleBO createSampleBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createSampleBO(session);
    }

    public IExperimentBO createExperimentBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createExperimentBO(session);
    }

    public IMaterialBO createMaterialBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialBO(session);
    }

    public final ISampleTable createSampleTable(final Session session)
    {
        return getCommonBusinessObjectFactory().createSampleTable(session);
    }

    public IProcedureBO createProcedureBO(Session session)
    {
        return getCommonBusinessObjectFactory().createProcedureBO(session);
    }

    public IMaterialTable createMaterialTable(Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialTable(session);
    }
}
