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

package ch.systemsx.cisd.cifex.server.business.dataaccess.db;

import javax.sql.DataSource;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IFileDAO;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IUserDAO;
import ch.systemsx.cisd.common.db.ISequencerHandler;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * <code>IDAOFactory</code> implementation for data bases.
 * 
 * @author Franz-Josef Elmer
 */
public class DAOFactory implements IDAOFactory
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "007";

    private final IUserDAO userDAO;

    private final IFileDAO fileDAO;

    public DAOFactory(final DatabaseConfigurationContext context)
    {
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, DATABASE_VERSION);

        final DataSource dataSource = context.getDataSource();
        final ISequencerHandler sequencerHandler = context.getSequencerHandler();

        userDAO = new UserDAO(dataSource, sequencerHandler);
        fileDAO = new FileDAO(dataSource, sequencerHandler);

    }

    public final IUserDAO getUserDAO()
    {
        return userDAO;
    }

    public IFileDAO getFileDAO()
    {
        return fileDAO;
    }
}
