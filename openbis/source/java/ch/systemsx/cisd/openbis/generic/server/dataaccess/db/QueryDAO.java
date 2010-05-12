/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author Franz-Josef Elmer
 */
public class QueryDAO extends AbstractGenericEntityDAO<QueryPE> implements IQueryDAO
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, QueryDAO.class);

    public QueryDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, QueryPE.class);
    }

    public List<QueryPE> listQueries(QueryType queryType)
    {
        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("databaseInstance", getDatabaseInstance()));
        if (queryType != QueryType.UNSPECIFIED)
        {
            criteria.add(Restrictions.eq("queryType", queryType));
        }
        final List<QueryPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d queries have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    public void createQuery(QueryPE query)
    {
        assert query != null : "Unspecified query";
        assert query.getDatabaseInstance() == null;
        query.setDatabaseInstance(getDatabaseInstance());

        persist(query);
    }

}
