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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Factory definition for all Data Access Objects which are needed for managing authorization.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthorizationDAOFactory
{
    /** Returns the persistency resources used to create DAO's. */
    public PersistencyResources getPersistencyResources();

    /**
     * Returns the current {@link DatabaseInstancePE}.
     */
    public DatabaseInstancePE getHomeDatabaseInstance();

    /**
     * Disables interaction with the second level cache for the current Hibernate session.
     */
    public void disableSecondLevelCacheForSession();

    /**
     * Returns the Hibernate session factory.
     */
    public SessionFactory getSessionFactory();

    public IPersonDAO getPersonDAO();

    public IGroupDAO getGroupDAO();

    public IDatabaseInstanceDAO getDatabaseInstanceDAO();

    public IRoleAssignmentDAO getRoleAssignmentDAO();

    public IExternalDataDAO getExternalDataDAO();

    public IExperimentDAO getExperimentDAO();

    public IProjectDAO getProjectDAO();

    public ISampleDAO getSampleDAO();

    public IGridCustomFilterDAO getGridCustomFilterDAO();

    public IGridCustomColumnDAO getGridCustomColumnDAO();
}
