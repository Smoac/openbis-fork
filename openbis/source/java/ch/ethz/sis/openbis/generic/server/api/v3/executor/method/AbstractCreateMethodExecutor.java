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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.ICreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.utils.ExceptionUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public abstract class AbstractCreateMethodExecutor<PERM_ID, CREATION> extends AbstractMethodExecutor implements
        ICreateMethodExecutor<PERM_ID, CREATION>
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public List<PERM_ID> create(String sessionToken, List<CREATION> creations)
    {
        Session session = getSession(sessionToken);
        OperationContext context = new OperationContext(session);

        try
        {
            List<PERM_ID> permIds = getCreateExecutor().create(context, creations);
            daoFactory.getSessionFactory().getCurrentSession().flush();
            return permIds;
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        } finally
        {
            daoFactory.getSessionFactory().getCurrentSession().clear();
        }
    }

    protected abstract ICreateEntityExecutor<CREATION, PERM_ID> getCreateExecutor();

}
