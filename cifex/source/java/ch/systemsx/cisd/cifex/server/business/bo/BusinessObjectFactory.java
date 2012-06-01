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

package ch.systemsx.cisd.cifex.server.business.bo;

import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * @author Franz-Josef Elmer
 */
public class BusinessObjectFactory implements IBusinessObjectFactory
{
    private final IDAOFactory daoFactory;

    private final IBusinessContext businessContext;

    private final ITimeProvider timeProvider;

    public BusinessObjectFactory(IDAOFactory daoFactory, IBusinessContext businessContext)
    {
        this(daoFactory, businessContext, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }
    
    public BusinessObjectFactory(IDAOFactory daoFactory, IBusinessContext businessContext,
            ITimeProvider timeProvider)
    {
        this.daoFactory = daoFactory;
        this.businessContext = businessContext;
        this.timeProvider = timeProvider;
    }

    @Override
    public IUserBO createUserBO()
    {
        return new UserBO(daoFactory, businessContext, timeProvider);
    }

}
