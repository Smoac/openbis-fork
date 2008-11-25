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

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * An interface that contains all data access operations on {@link DatabaseInstancePE}s.
 * 
 * @author Christian Ribeaud
 */
public interface IDatabaseInstanceDAO
{

    /**
     * Returns the home database instance - the only database instance which has its
     * <code>is_original_source</code> flag set to <code>true</code>.
     * 
     * @throws EmptyResultDataAccessException if no original source database instance has been
     *             found.
     * @throws IncorrectResultSizeDataAccessException if more than one original source database
     *             instance has been found.
     */
    public DatabaseInstancePE getHomeInstance() throws DataAccessException;

    /**
     * Updates given <code>databaseInstanceDTO</code>.
     * <p>
     * Note that to do so, {@link DatabaseInstancePE#getId()} must not be <code>null</code>.
     * </p>
     */
    public void updateDatabaseInstancePE(final DatabaseInstancePE databaseInstancePE)
            throws DataAccessException;

    /**
     * Returns a list of all available {@link DatabaseInstancePE} on this installation.
     */
    public List<DatabaseInstancePE> listDatabaseInstances();

    /**
     * Tries to find the database instance of specified code.
     * 
     * @return <code>null</code> if not found.
     */
    public DatabaseInstancePE tryFindDatabaseInstanceByCode(final String databaseInstanceCode)
            throws DataAccessException;

    /**
     * Tries to find the database instance of specified <i>UUID</i>.
     * 
     * @return <code>null</code> if not found.
     */
    public DatabaseInstancePE tryFindDatabaseInstanceByUUID(final String databaseInstanceUUID)
            throws DataAccessException;

    /**
     * Returns the database instance found for given <var>id</var>.
     */
    public DatabaseInstancePE getDatabaseInstanceById(final long id) throws DataAccessException;

    /**
     * Creates a new database instance. Only {@link DatabaseInstancePE#setCode(String)} has to be
     * set. UUID will be set by the implementation.
     */
    public void createDatabaseInstance(final DatabaseInstancePE databaseInstance)
            throws DataAccessException;
}
