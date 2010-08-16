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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
    public static final BeanFactory APPLICATION_CONTEXT =
            new ClassPathXmlApplicationContext(new String[]
                { "applicationContext.xml" }, true);

    public static ICommonServer getCommonServer()
    {
        return (ICommonServer) APPLICATION_CONTEXT.getBean("common-server");
    }

    public static IDAOFactory getDAOFactory()
    {
        return (IDAOFactory) APPLICATION_CONTEXT.getBean("dao-factory");
    }

    public static ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return (ICommonBusinessObjectFactory) APPLICATION_CONTEXT
                .getBean("common-business-object-factory");
    }

    private CommonServiceProvider()
    {
    }
}
