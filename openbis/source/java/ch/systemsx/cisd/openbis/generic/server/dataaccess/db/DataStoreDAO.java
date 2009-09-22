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
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Hibernate-based implementation of {@link IDataStoreDAO}.
 * 
 * @author     Franz-Josef Elmer
 */
public class DataStoreDAO extends AbstractDAO implements IDataStoreDAO
{
    private final static Class<DataStorePE> ENTITY_CLASS = DataStorePE.class;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataStoreDAO.class);

    public DataStoreDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    public void createOrUpdateDataStore(DataStorePE dataStore)
    {
        assert dataStore != null : "Unspecified data store";

        HibernateTemplate template = getHibernateTemplate();

        dataStore.setCode(CodeConverter.tryToDatabase(dataStore.getCode()));
        template.saveOrUpdate(dataStore);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE/UPDATE: data store '%s'.", dataStore));
        }
    }

    public DataStorePE tryToFindDataStoreByCode(String dataStoreCode)
    {
        assert dataStoreCode != null : "Unspecified data store code.";

        final Criteria criteria = getSession().createCriteria(DataStorePE.class);
        criteria.add(Restrictions.eq("code", CodeConverter.tryToDatabase(dataStoreCode)));
        return (DataStorePE) criteria.uniqueResult();
    }

    public List<DataStorePE> listDataStores()
    {
        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("databaseInstance", getDatabaseInstance()));
        criteria.setFetchMode("servicesInternal", FetchMode.JOIN);
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        final List<DataStorePE> list = cast(criteria.list());
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%d data stores have been found.", list.size()));
        }
        return list;
    }

}
