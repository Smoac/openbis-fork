/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author   Franz-Josef Elmer
 */
public abstract class AbstractExternalDataBusinessObject extends
        AbstractSampleIdentifierBusinessObject
{

    /**
     * @param daoFactory
     * @param session
     */
    public AbstractExternalDataBusinessObject(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
    }

    protected void enrichWithParentsAndExperiment(ExternalDataPE externalDataPE)
    {
        HibernateUtils.initialize(externalDataPE.getParents());
        HibernateUtils.initialize(externalDataPE.getExperiment());
    }

    protected void enrichWithChildren(ExternalDataPE externalDataPE)
    {
        HibernateUtils.initialize(externalDataPE.getChildren());
    }

}
