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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFilterDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;

/**
 * Hibernate-based implementation of {@link IFilterDAO}.
 * 
 * @author Izabela Adamczyk
 */
public class FilterDAO extends AbstractGenericEntityDAO<FilterPE> implements IFilterDAO
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FilterDAO.class);

    public FilterDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, FilterPE.class);
    }

    public void createFilter(FilterPE filter)
    {
        assert filter != null : "Unspecified filter";
        assert filter.getDatabaseInstance() == null;
        filter.setDatabaseInstance(getDatabaseInstance());

        persist(filter);
    }

    public List<FilterPE> listFilters(String gridId)
    {
        assert gridId != null : "Unspecified grid ID.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        criteria.add(Restrictions.eq("databaseInstance", getDatabaseInstance()));
        criteria.add(Restrictions.eq("gridId", gridId));
        final List<FilterPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): %d filters(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), gridId, list.size()));
        }
        return list;
    }

}
