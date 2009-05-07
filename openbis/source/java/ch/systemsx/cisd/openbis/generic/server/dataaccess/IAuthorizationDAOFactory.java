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

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * Factory definition for all Data Access Objects which are needed for managing authorization.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthorizationDAOFactory
{

    /**
     * Returns the current {@link DatabaseInstancePE}.
     */
    public DatabaseInstancePE getHomeDatabaseInstance();

    /**
     * Returns the <code>IPersonDAO</code> implementation.
     */
    public IPersonDAO getPersonDAO();

    /**
     * @return The implementation of the {@link IGroupDAO}.
     */
    public IGroupDAO getGroupDAO();

    /**
     * @return The implementation of the {@link IDatabaseInstanceDAO}.
     */
    public IDatabaseInstanceDAO getDatabaseInstanceDAO();

    /**
     * @return The implementation of the {@link IRoleAssignmentDAO}.
     */
    public IRoleAssignmentDAO getRoleAssignmentDAO();

    /**
     * Returns the {@link IExternalDataDAO} implementation.
     */
    public IExternalDataDAO getExternalDataDAO();

    /**
     * Returns the implementation of {@link IExperimentDAO}.
     */
    public IExperimentDAO getExperimentDAO();

    /**
     * Returns the implementation of {@link IProjectDAO}.
     */
    public IProjectDAO getProjectDAO();
}