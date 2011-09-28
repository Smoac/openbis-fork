/*
 * Copyright 2011 ETH Zuerich, CISD
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
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ICorePluginDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CorePluginPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Hibernate-based implementation of {@link ICorePluginDAO}.
 * 
 * @author Kaloyan Enimanev
 */
public class CorePluginDAO extends AbstractDAO implements ICorePluginDAO
{
    private final static Class<CorePluginPE> ENTITY_CLASS = CorePluginPE.class;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CorePluginDAO.class);

    public CorePluginDAO(SessionFactory sessionFactory, DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    public void createOrUpdateDataStore(DataStorePE dataStore)
    {
        assert dataStore != null : "Unspecified data store";

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("SAVE/UPDATE: data store '%s'.", dataStore));
        }
    }

    public void createCorePlugins(List<CorePluginPE> corePlugins)
    {
        HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdateAll(corePlugins);
        template.flush();
    }

    public List<CorePluginPE> listCorePluginsByName(String name)
    {
        final Criteria criteria = getSession().createCriteria(ENTITY_CLASS);
        criteria.add(Restrictions.eq("name", name));
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return cast(criteria.list());
    }

}
