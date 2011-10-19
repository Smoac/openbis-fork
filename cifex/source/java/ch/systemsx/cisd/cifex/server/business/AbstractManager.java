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

package ch.systemsx.cisd.cifex.server.business;

import ch.systemsx.cisd.cifex.server.business.bo.IBusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Abstract super class of all Managers.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractManager
{
    protected final IDAOFactory daoFactory;

    protected final IBusinessObjectFactory boFactory;

    protected final IBusinessContext businessContext;

    protected final ITimeProvider timeProvider;

    AbstractManager(final IDAOFactory daoFactory, IBusinessObjectFactory boFactory,
            IBusinessContext businessContext, ITimeProvider timeProvider)
    {
        this.boFactory = boFactory;
        this.businessContext = businessContext;
        assert daoFactory != null : "Undefined DAO Factory.";
        this.daoFactory = daoFactory;
        this.timeProvider = timeProvider;
    }

}
