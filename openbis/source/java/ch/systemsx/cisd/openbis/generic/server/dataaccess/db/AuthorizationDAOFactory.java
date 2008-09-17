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
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.ISqlScriptProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.util.UuidUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Super class of all DAO factories which extend {@link IAuthorizationDAOFactory}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AuthorizationDAOFactory implements IAuthorizationDAOFactory
{
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, AuthorizationDAOFactory.class);
    
    private final IDatabaseInstanceDAO databaseInstancesDAO;

    private final IRoleAssignmentDAO roleAssignmentDAO;

    private final IGroupDAO groupDAO;

    private final IPersonDAO personDAO;
    
    private final DatabaseInstancePE homeDatabaseInstance;
   
    protected AuthorizationDAOFactory(final DatabaseConfigurationContext context,
            final SessionFactory sessionFactory, final ISqlScriptProvider sqlScriptProvider)
    {
        databaseInstancesDAO = new DatabaseInstanceDAO(sessionFactory);
        homeDatabaseInstance = getDatabaseInstanceId(context.getDatabaseInstance());
        personDAO = new PersonDAO(sessionFactory, homeDatabaseInstance);
        groupDAO = new GroupDAO(sessionFactory, homeDatabaseInstance);
        roleAssignmentDAO = new RoleAssignmentDAO(sessionFactory, homeDatabaseInstance);
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

    // TODO 2008-07-10, Franz-Josef Elmer: Remove method if no longer needed
    public DatabaseInstancePE getHomeDatabaseInstance()
    {
        return homeDatabaseInstance;
    }
    
    public IDatabaseInstanceDAO getDatabaseInstancesDAO()
    {
        return databaseInstancesDAO;
    }

    public IGroupDAO getGroupDAO()
    {
        return groupDAO;
    }

    public IPersonDAO getPersonDAO()
    {
        return personDAO;
    }

    public IRoleAssignmentDAO getRoleAssignmentDAO()
    {
        return roleAssignmentDAO;
    }

}
