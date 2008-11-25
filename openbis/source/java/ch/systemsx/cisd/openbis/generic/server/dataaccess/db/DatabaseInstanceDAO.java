/*
 * Copyright 2008 ETH Zuerich, CISD
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Implementation of {@link IDatabaseInstanceDAO} for data bases.
 * 
 * @author Christian Ribeaud
 */
final class DatabaseInstanceDAO extends AbstractDAO implements IDatabaseInstanceDAO
{

    private final static Class<DatabaseInstancePE> ENTITY_CLASS = DatabaseInstancePE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     * </p>
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatabaseInstanceDAO.class);

    DatabaseInstanceDAO(final SessionFactory sessionFactory)
    {
        super(sessionFactory, null);
    }

    private final DatabaseInstancePE tryFindDatabaseInstanceByCode(
            final String databaseInstanceCode, final boolean isUUID) throws DataAccessException
    {
        final List<DatabaseInstancePE> databaseInstances =
                listDatabaseInstances(databaseInstanceCode, isUUID);
        final DatabaseInstancePE databaseInstance =
                tryFindEntity(databaseInstances, "database instance");
        if (operationLog.isDebugEnabled())
        {
            final String foundInstanceDesc =
                    databaseInstance == null ? "not found." : databaseInstance.toString();
            final String methodSuffix = isUUID ? "UUID" : "Code";
            operationLog.debug("tryToFindDatabaseInstanceBy" + methodSuffix + "("
                    + databaseInstanceCode + "): " + foundInstanceDesc);
        }
        return databaseInstance;
    }

    private final List<DatabaseInstancePE> listDatabaseInstances(final String databaseInstanceCode,
            final boolean isUUID)
    {
        assert databaseInstanceCode != null : "Unspecified DatabaseInstance code";

        final String code = CodeConverter.tryToDatabase(databaseInstanceCode);
        final String columnName = isUUID ? "uuid" : "code";
        final List<DatabaseInstancePE> databaseInstances =
                cast(getHibernateTemplate().find(
                        String.format("from %s d where d." + columnName + " = ? ", TABLE_NAME),
                        toArray(code)));
        return databaseInstances;
    }

    //
    // IDatabaseInstancesDAO
    //

    public final DatabaseInstancePE getHomeInstance() throws DataAccessException
    {
        final List<DatabaseInstancePE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s d where d.originalSource = ?", TABLE_NAME),
                        toArray(true)));
        return getEntity(list);
    }

    public final DatabaseInstancePE tryFindDatabaseInstanceByUUID(final String databaseInstanceUUID)
            throws DataAccessException
    {
        return tryFindDatabaseInstanceByCode(databaseInstanceUUID, true);
    }

    public final DatabaseInstancePE tryFindDatabaseInstanceByCode(final String databaseInstanceCode)
            throws DataAccessException
    {
        return tryFindDatabaseInstanceByCode(databaseInstanceCode, false);
    }

    public final void updateDatabaseInstancePE(final DatabaseInstancePE databaseInstancePE)
            throws DataAccessException
    {
        assert databaseInstancePE != null : "Unspecified database instance";
        validatePE(databaseInstancePE);

        databaseInstancePE.setCode(CodeConverter.tryToDatabase(databaseInstancePE.getCode()));
        databaseInstancePE.setUuid(CodeConverter.tryToDatabase(databaseInstancePE.getUuid()));
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.update(databaseInstancePE);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: database instance '%s'.", databaseInstancePE));
        }
    }

    public final List<DatabaseInstancePE> listDatabaseInstances()
    {
        final List<DatabaseInstancePE> list = cast(getHibernateTemplate().loadAll(ENTITY_CLASS));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listDatabaseInstances(): " + list.size()
                    + " database instance(s) have been found.");
        }
        return list;

    }

    public final DatabaseInstancePE getDatabaseInstanceById(final long databaseInstanceId)
            throws DataAccessException
    {
        final DatabaseInstancePE databaseInstance =
                (DatabaseInstancePE) getHibernateTemplate().load(ENTITY_CLASS, databaseInstanceId);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("getDatabaseInstanceById(" + databaseInstanceId + "): '"
                    + databaseInstance + "'.");
        }
        return databaseInstance;
    }

    public final void createDatabaseInstance(final DatabaseInstancePE databaseInstance)
            throws DataAccessException
    {
        assert databaseInstance != null : "Unspecified database instance";
        databaseInstance.setUuid(UuidUtil.generateUUID());
        validatePE(databaseInstance);

        databaseInstance.setCode(CodeConverter.tryToDatabase(databaseInstance.getCode()));
        final HibernateTemplate template = getHibernateTemplate();
        template.save(databaseInstance);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: Database instance '%s'.", databaseInstance));
        }

    }
}
