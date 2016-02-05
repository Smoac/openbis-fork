/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IOperationExecutionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;

/**
 * <i>Data Access Object</i> implementation for {@link OperationExecutionPE}.
 * 
 * @author pkupczyk
 */
final class OperationExecutionDAO extends AbstractGenericEntityDAO<OperationExecutionPE> implements IOperationExecutionDAO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, OperationExecutionDAO.class);

    OperationExecutionDAO(final SessionFactory sessionFactory, EntityHistoryCreator historyCreator)
    {
        super(sessionFactory, OperationExecutionPE.class, historyCreator);
    }

    @Override
    public void createOrUpdate(OperationExecutionPE execution)
    {
        validatePE(execution);
        final HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(execution);
        template.flush();

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Created or updated operation execution '%s'.", execution));
        }
    }

    @Override
    public OperationExecutionPE tryFindByCode(String code)
    {
        final Criteria criteria = currentSession().createCriteria(OperationExecutionPE.class);
        criteria.add(Restrictions.eq("code", code));
        OperationExecutionPE execution = (OperationExecutionPE) criteria.uniqueResult();

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Found operation execution '%s'.", execution));
        }

        return execution;
    }

}
