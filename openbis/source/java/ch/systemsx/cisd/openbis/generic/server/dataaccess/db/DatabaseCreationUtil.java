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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.io.File;
import java.io.IOException;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.ISqlScriptProvider;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexerRunnable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.HibernateSearchContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;

/**
 * Utility methods around database creation.
 * 
 * @author Franz-Josef Elmer
 */
public final class DatabaseCreationUtil
{
    private static BeanFactory beanFactory;

    public static void main(final String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java " + DatabaseCreationUtil.class.getName()
                    + " <database version>");
            System.exit(1);
        }
        final String databaseVersion = args[0];
        createFilesFromADumpOfAMigratedDatabase(databaseVersion);
    }

    private final static BeanFactory getBeanFactory(final String xmlConfigurationContextName)
    {
        if (beanFactory == null)
        {
            final AbstractApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[]
                        { xmlConfigurationContextName }, true);
            DatabaseCreationUtil.beanFactory = applicationContext;
        }
        return beanFactory;
    }

    /**
     * Creates all files in <code>sourceTest/sql/postgresql</code> necessary to set up a database
     * of the current version by dumping a database migrated from the specified version.
     */
    private final static void createFilesFromADumpOfAMigratedDatabase(final String databaseVersion)
            throws IOException
    {
        LogInitializer.init();
        final String databaseKind = "migration_dump";
        final DatabaseConfigurationContext context =
                createDatabaseConfigurationContext(databaseKind);
        context.setCreateFromScratch(true);
        final ISqlScriptProvider scriptProvider =
                DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context,
                        databaseVersion);
        context.setCreateFromScratch(false);
        context.setScriptFolder("../openbis/source/sql");
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context,
                DatabaseVersionHolder.getDatabaseVersion());
        createDumpForJava(databaseKind, scriptProvider.getDumpFolder(DatabaseVersionHolder
                .getDatabaseVersion()));
        scriptProvider.markAsDumpRestorable(DatabaseVersionHolder.getDatabaseVersion());
        performFullTextIndex(databaseKind);
    }

    /**
     * Performs a full text index because the test database has been migrated.
     */
    private final static void performFullTextIndex(final String databaseKind)
    {
        final BeanFactory factory = getBeanFactory("commonContext.xml");
        final FullTextIndexerRunnable fullTextIndexer =
                new FullTextIndexerRunnable((SessionFactory) factory
                        .getBean("hibernate-session-factory"),
                        createHibernateSearchContext(factory));
        fullTextIndexer.run();
    }

    /**
     * Adapts the {@link HibernateSearchContext} laoded by <i>Spring</i>.
     * 
     * @param factory
     */
    private final static HibernateSearchContext createHibernateSearchContext(
            final BeanFactory factory)
    {
        final HibernateSearchContext context = new HibernateSearchContext();
        context.setIndexBase("sourceTest/lucene/indices");
        context.setIndexMode(IndexMode.INDEX_FROM_SCRATCH);
        return context;
    }

    /**
     * Creates all files in <var>destinationDir</var> necessary to set up the database
     * <var>databaseKind</var> in its current state.
     */
    private static final void createDumpForJava(final String databaseKind, final File destinationDir)
            throws IOException
    {
        LogInitializer.init();
        final String dataBaseName = "lims_" + databaseKind;
        final File dumpFile = new File("targets/dump.sql");
        final boolean ok = DumpPreparator.createDatabaseDump(dataBaseName, dumpFile);
        if (ok == false)
        {
            throw new EnvironmentFailureException("Database dump failed.");
        }
        DumpPreparator.createUploadFiles(dumpFile, destinationDir, false);
    }

    /**
     * Creates a database configuration based on <code>dbConfigurationContext.xml</code>.
     */
    public final static DatabaseConfigurationContext createDatabaseConfigurationContext(
            final String databaseKind)
    {
        final BeanFactory factory = getBeanFactory("dbConfigurationContext.xml");
        final DatabaseConfigurationContext configurationContext =
                (DatabaseConfigurationContext) factory.getBean("db-configuration-context");
        configurationContext.setDatabaseKind(databaseKind);
        configurationContext.setScriptFolder("sourceTest/sql");
        return configurationContext;
    }

    private DatabaseCreationUtil()
    {
        // This class can not be instantiated.
    }
}
