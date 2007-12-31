/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import ch.systemsx.cisd.common.db.ISequencerHandler;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.db.PostgreSQLSequencerHandler;
import ch.systemsx.cisd.dbmigration.postgresql.PostgreSQLDAOFactory;

/**
 * An enumeration to keep the configuration setting for different database engines.
 * 
 * @author Bernd Rinn
 */
public enum DatabaseEngine
{
    POSTGRESQL("postgresql", org.postgresql.Driver.class, PostgreSQLDAOFactory.class, new DefaultLobHandler(),
            new PostgreSQLSequencerHandler(), "jdbc:postgresql:{0}", "jdbc:postgresql:{0}{1}", "//localhost/",
            "postgres");

//    H2("h2", org.h2.Driver.class, H2DAOFactory.class, new DefaultLobHandler(),
//            new PostgreSQLSequencerHandler(), "jdbc:h2:{0}{1};DB_CLOSE_DELAY=-1",
//            "jdbc:h2:{0}{1};DB_CLOSE_DELAY=-1", "mem:", "");

    private static Map<String, DatabaseEngine> engines = initEngineMap();

    private final String code;

    private final Class<java.sql.Driver> driverClass;

    private final Class<ch.systemsx.cisd.dbmigration.IDAOFactory> daoFactoryClass;

    private final Constructor<ch.systemsx.cisd.dbmigration.IDAOFactory> daoFactoryConstructor;

    private final LobHandler lobHandler;

    private final ISequencerHandler sequenceHandler;

    private final String adminUrlTemplate;

    private final String urlTemplate;

    private final String defaultURLHostPart;

    private final String defaultAdminUser;

    @SuppressWarnings("unchecked")
    DatabaseEngine(String code, Class<?> driver, Class<?> daoFactoryClass, LobHandler lobHandler,
            ISequencerHandler sequenceHandler, String adminUrlTemplate, String urlTemplate, String defaultURLHostPart,
            String defaultAdminUser)
    {
        assert code != null;
        assert driver != null;
        assert daoFactoryClass != null;
        assert lobHandler != null;
        assert sequenceHandler != null;
        assert adminUrlTemplate != null;
        assert urlTemplate != null;
        assert defaultURLHostPart != null;
        assert defaultAdminUser != null;

        this.code = code.toLowerCase();
        this.adminUrlTemplate = adminUrlTemplate;
        this.urlTemplate = urlTemplate;
        this.defaultURLHostPart = defaultURLHostPart;
        this.defaultAdminUser = defaultAdminUser;
        this.driverClass = (Class<java.sql.Driver>) driver;
        this.lobHandler = lobHandler;
        this.sequenceHandler = sequenceHandler;
        this.daoFactoryClass = (Class<ch.systemsx.cisd.dbmigration.IDAOFactory>) daoFactoryClass;
        try
        {
            this.daoFactoryConstructor = this.daoFactoryClass.getConstructor(DatabaseConfigurationContext.class);
        } catch (NoSuchMethodException ex)
        {
            throw new Error("No constructor", ex);
        }
    }

    /**
     * @return The code of this engine.
     */
    public final String getCode()
    {
        return code;
    }

    /**
     * Creates a new {@link IDAOFactory} for this engine
     */
    public IDAOFactory createDAOFactory(DatabaseConfigurationContext context)
    {
        try
        {
            return daoFactoryConstructor.newInstance(context);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * @return the class of the driver.
     */
    public final Class<java.sql.Driver> getDriverClass()
    {
        return driverClass;
    }

    public final LobHandler getLobHandler()
    {
        return lobHandler;
    }

    public final ISequencerHandler getSequenceHandler()
    {
        return sequenceHandler;
    }

    /**
     * @param urlHostPartOrNull The host part of the URL, or <code>null</code>, if the default host part should be
     *            used.
     * @param databaseName The name of the database (may be ignored for the admin URL, depending on the database engine)
     * @return The admin URL of the db.
     */
    public final String getAdminURL(String urlHostPartOrNull, String databaseName)
    {
        final String hostPartUrl = (urlHostPartOrNull == null) ? defaultURLHostPart : urlHostPartOrNull;
        final String url = MessageFormat.format(adminUrlTemplate, hostPartUrl, databaseName);
        return url;
    }

    public final String getDefaultAdminUser()
    {
        return defaultAdminUser;
    }

    /**
     * @param urlHostPartOrNull The host part of the URL, or <code>null</code>, if the default host part should be
     *            used.
     * @param databaseName The name of the database.
     * @return The URL of the db.
     */
    public final String getURL(String urlHostPartOrNull, String databaseName)
    {
        final String hostPartUrl = (urlHostPartOrNull == null) ? defaultURLHostPart : urlHostPartOrNull;
        final String url = MessageFormat.format(urlTemplate, hostPartUrl, databaseName);
        return url;
    }

    /**
     * @return The database engine for <var>code</var>, or <code>null</code>
     * @throws ConfigurationFailureException If no engine is found for this code.
     */
    public static DatabaseEngine getEngineForCode(String code) throws ConfigurationFailureException
    {
        final DatabaseEngine engine = engines.get(code.toLowerCase());
        if (engine == null)
        {
            throw new ConfigurationFailureException("No database engine '" + code + "' found.");
        }
        return engine;
    }

    private static Map<String, DatabaseEngine> initEngineMap()
    {
        final Map<String, DatabaseEngine> map = new HashMap<String, DatabaseEngine>();
        for (DatabaseEngine engine : values())
        {
            map.put(engine.code, engine);
        }
        return map;
    }

}
