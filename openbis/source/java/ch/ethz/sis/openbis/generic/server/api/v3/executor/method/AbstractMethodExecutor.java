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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.utils.ExceptionUtils;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class AbstractMethodExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    protected <T> T executeInContext(String sessionToken, IMethodAction<T> action)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            checkSession(session);
            T result = action.execute(context);
            flushCurrentSession();
            return result;
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            clearCurrentSession();
        }
    }

    protected void clearCurrentSession()
    {
        daoFactory.getSessionFactory().getCurrentSession().clear();
    }

    protected void flushCurrentSession()
    {
        daoFactory.getSessionFactory().getCurrentSession().flush();
    }

    protected Session getSession(String sessionToken)
    {
        return sessionManager.getSession(sessionToken);
    }

    protected void checkSession(Session session)
    {
    }

    public interface IMethodAction<T>
    {

        public T execute(IOperationContext context);

    }

}
