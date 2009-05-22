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
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGenericDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Abstract super class of DAOs using generic interface.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractGenericEntityDAO<T extends IIdHolder> extends AbstractDAO implements
        IGenericDAO<T>
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractGenericEntityDAO.class);

    private final Class<T> entityClass;

    protected AbstractGenericEntityDAO(final SessionFactory sessionFactory,
            final DatabaseInstancePE databaseInstance, final Class<T> entityClass)
    {
        super(sessionFactory, databaseInstance);
        this.entityClass = entityClass;
    }

    protected Class<T> getEntityClass()
    {
        return entityClass;
    }

    public final T getByTechId(final TechId techId) throws DataAccessException
    {
        assert techId != null : "Technical identifier unspecified.";
        final T result = getEntity(getHibernateTemplate().load(getEntityClass(), techId.getId()));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%d): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), techId, result));
        }
        return result;
    }

    // TODO 2009-05-22, Tomasz Pylak: remove connections, it forces BOs to use strings with field
    // paths
    public final T tryGetByTechId(final TechId techId, String... connections)
            throws DataAccessException
    {
        assert techId != null : "Technical identifier unspecified.";
        final Criteria criteria = getSession().createCriteria(getEntityClass());
        criteria.add(Restrictions.eq("id", techId.getId()));
        for (String connection : connections)
        {
            criteria.setFetchMode(connection, FetchMode.JOIN);
        }
        final T result = tryGetEntity(criteria.uniqueResult());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%d): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), techId, result));
        }
        return result;
    }
}
