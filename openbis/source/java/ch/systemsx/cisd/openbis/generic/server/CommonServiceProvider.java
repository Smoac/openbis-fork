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

package ch.systemsx.cisd.openbis.generic.server;

import org.springframework.context.ApplicationContext;

import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;

/**
 * Provider of common openBIS server services.
 * 
 * @author Piotr Buczek
 */
public class CommonServiceProvider
{
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context)
    {
        applicationContext = context;
    }
    
    public static ICommonServer getCommonServer()
    {
        return (ICommonServer) applicationContext.getBean("common-server");
    }

    public static IDAOFactory getDAOFactory()
    {
        return (IDAOFactory) applicationContext.getBean("dao-factory");
    }

    public static ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return (ICommonBusinessObjectFactory) applicationContext
                .getBean("common-business-object-factory");
    }

    private CommonServiceProvider()
    {
    }

}
