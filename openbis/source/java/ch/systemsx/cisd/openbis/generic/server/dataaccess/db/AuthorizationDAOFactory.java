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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomColumnDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomFilterDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Super class of all DAO factories which extend {@link IAuthorizationDAOFactory}.
 * 
 * @author Franz-Josef Elmer
 */
public class AuthorizationDAOFactory implements IAuthorizationDAOFactory
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AuthorizationDAOFactory.class);

    private final IDatabaseInstanceDAO databaseInstancesDAO;

    private final IRoleAssignmentDAO roleAssignmentDAO;

    private final IGroupDAO groupDAO;

    private final IPersonDAO personDAO;

    private final IExternalDataDAO externalDataDAO;

    private final IExperimentDAO experimentDAO;

    private final DatabaseInstancePE homeDatabaseInstance;

    private final IProjectDAO projectDAO;

    private final ISampleDAO sampleDAO;

    private final IGridCustomFilterDAO gridCustomFilterDAO;

    private final IGridCustomColumnDAO gridCustomColumnDAO;

    private final QueryDAO queryDAO;

    private final PersistencyResources persistencyResources;

    private final IRelationshipTypeDAO relationshipTypeDAO;

    public AuthorizationDAOFactory(final DatabaseConfigurationContext context,
            final SessionFactory sessionFactory,
            final IFullTextIndexUpdateScheduler indexUpdateScheduler)
    {
        persistencyResources = new PersistencyResources(context, sessionFactory);
        databaseInstancesDAO = new DatabaseInstanceDAO(sessionFactory);
        homeDatabaseInstance = getDatabaseInstanceId(context.getDatabaseInstance());
        personDAO = new PersonDAO(sessionFactory, homeDatabaseInstance);
        groupDAO = new GroupDAO(sessionFactory, homeDatabaseInstance);
        roleAssignmentDAO = new RoleAssignmentDAO(sessionFactory, homeDatabaseInstance);
        externalDataDAO = new ExternalDataDAO(sessionFactory, homeDatabaseInstance);
        experimentDAO = new ExperimentDAO(sessionFactory, homeDatabaseInstance);
        projectDAO = new ProjectDAO(sessionFactory, homeDatabaseInstance);
        sampleDAO = new SampleDAO(sessionFactory, homeDatabaseInstance, indexUpdateScheduler);
        gridCustomFilterDAO = new GridCustomFilterDAO(sessionFactory, homeDatabaseInstance);
        gridCustomColumnDAO = new GridCustomColumnDAO(sessionFactory, homeDatabaseInstance);
        queryDAO = new QueryDAO(sessionFactory, homeDatabaseInstance);
        relationshipTypeDAO = new RelationshipTypeDAO(sessionFactory, homeDatabaseInstance);
    }

    public final PersistencyResources getPersistencyResources()
    {
        return persistencyResources;
    }

    public SessionFactory getSessionFactory()
    {
        return persistencyResources.getSessionFactoryOrNull();
    }

    private final DatabaseInstancePE getDatabaseInstanceId(final String databaseInstanceCode)
    {
        assert databaseInstanceCode != null : "Unspecified database instance";
        try
        {
            final DatabaseInstancePE originalSource = databaseInstancesDAO.getHomeInstance();
            final String origCode = originalSource.getCode();
            if (originalSource.isSystemDefault())
            {
                if (StringUtils.isNotEmpty(databaseInstanceCode)
                        && DatabaseInstancePE.isSystemDefault(databaseInstanceCode) == false)
                {
                    updateDatabaseInstanceCode(originalSource, databaseInstanceCode, origCode);
                } else
                {
                    throw new ConfigurationFailureException(String.format(
                            "Invalid database instance '%s'.", databaseInstanceCode));
                }
            } else
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format("Original source database instance: '%s'.",
                            origCode));
                }
            }
            return originalSource;
        } catch (final DataAccessException ex)
        {
            throw new ConfigurationFailureException(
                    "A problem has occurred while getting or setting "
                            + "the original source database instance.", ex);
        }
    }

    private void updateDatabaseInstanceCode(final DatabaseInstancePE databaseInstancePE,
            final String newCode, final String oldCode) throws UserFailureException
    {
        if (UuidUtil.isValidUUID(newCode))
        {
            throw UserFailureException.fromTemplate("The new database instance code '%s' "
                    + "has an UUID format and should not.", newCode);
        }
        final String uuid = UuidUtil.generateUUID();
        databaseInstancePE.setUuid(uuid);
        databaseInstancePE.setCode(newCode);
        databaseInstancesDAO.updateDatabaseInstancePE(databaseInstancePE);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Original source database instance renamed from '%s' to '%s'. "
                            + "UUID = '%s'.", oldCode, newCode, uuid));
        }
    }

    //
    // IAuthorizationDAOFactory
    //

    public final DatabaseInstancePE getHomeDatabaseInstance()
    {
        return homeDatabaseInstance;
    }

    public final IDatabaseInstanceDAO getDatabaseInstanceDAO()
    {
        return databaseInstancesDAO;
    }

    public final IGroupDAO getGroupDAO()
    {
        return groupDAO;
    }

    public final IPersonDAO getPersonDAO()
    {
        return personDAO;
    }

    public final IRoleAssignmentDAO getRoleAssignmentDAO()
    {
        return roleAssignmentDAO;
    }

    public final IExternalDataDAO getExternalDataDAO()
    {
        return externalDataDAO;
    }

    public final IExperimentDAO getExperimentDAO()
    {
        return experimentDAO;
    }

    public final IProjectDAO getProjectDAO()
    {
        return projectDAO;
    }

    public final ISampleDAO getSampleDAO()
    {
        return sampleDAO;
    }

    public IGridCustomFilterDAO getGridCustomFilterDAO()
    {
        return gridCustomFilterDAO;
    }

    public IGridCustomColumnDAO getGridCustomColumnDAO()
    {
        return gridCustomColumnDAO;
    }

    public IQueryDAO getQueryDAO()
    {
        return queryDAO;
    }

    public IRelationshipTypeDAO getRelationshipTypeDAO()
    {
        return relationshipTypeDAO;
    }

    /**
     * @param batchMode if true the second level cache will be switched off and the hibernate
     *            session will be synchronized with the database only at the end of the transaction.
     *            Note that 1) this cause that the stale data will be fetched from database,
     *            ignoring the changes in hibernate layer 2) if you have many write operations
     *            interleaved with read operations in one block, switching synchronization off
     *            greatly improves the performance.
     */
    public void setBatchUpdateMode(boolean batchMode)
    {
        SessionFactory sessionFactory = persistencyResources.getSessionFactoryOrNull();
        Session currentSession = sessionFactory.getCurrentSession();

        CacheMode cacheMode = (batchMode ? CacheMode.IGNORE : CacheMode.NORMAL);
        currentSession.setCacheMode(cacheMode);

        FlushMode mode = (batchMode ? FlushMode.COMMIT : FlushMode.AUTO);
        currentSession.setFlushMode(mode);
    }

}
