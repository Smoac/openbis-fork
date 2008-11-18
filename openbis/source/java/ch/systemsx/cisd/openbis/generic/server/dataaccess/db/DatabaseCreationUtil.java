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

import org.apache.commons.lang.StringUtils;
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
    private static final String LUCENE_INDICES = "sourceTest/lucene/indices";

    private static BeanFactory beanFactory;

    private static HibernateSearchContext hibernateSearchContext;

    static
    {
        LogInitializer.init();
        // Deactivate the indexing in the application context loaded by Spring.
        System.setProperty("hibernate.search.index-mode", "NO_INDEX");
        System.setProperty("hibernate.search.index-base", LUCENE_INDICES);
    }

    public static void main(final String[] args) throws Exception
    {
        if (args.length == 0 || args.length > 2)
        {
            System.out.println("Usage: java " + DatabaseCreationUtil.class.getName()
                    + "[--reindex[=true|false]] <database version>");
            System.exit(1);
        }
        final String databaseVersion;
        boolean reindex = false;
        if (args.length == 1)
        {
            reindex = false;
            databaseVersion = args[0];
        } else
        {
            if (args[0].startsWith("--reindex"))
            {
                if (args[0].indexOf('=') > -1)
                {
                    reindex = Boolean.parseBoolean(StringUtils.split(args[0], '=')[1]);
                } else
                {
                    reindex = true;
                }
            }
            databaseVersion = args[1];
        }
        // If reindexing is asked, creating a HibernateSearchContext should be done before the
        // Spring application context gets loaded because HibernateSearchContext removes the whole
        // indices directory.
        if (reindex)
        {
            hibernateSearchContext = createHibernateSearchContext();
            hibernateSearchContext.afterPropertiesSet();
        }
        createFilesFromADumpOfAMigratedDatabase(databaseVersion, reindex);
    }

    private final static BeanFactory getBeanFactory()
    {
        if (beanFactory == null)
        {
            final AbstractApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[]
                        { "dbConfigurationContext.xml" }, true);
            DatabaseCreationUtil.beanFactory = applicationContext;
        }
        return beanFactory;
    }

    /**
     * Creates all files in <code>sourceTest/sql/postgresql</code> necessary to set up a database
     * of the current version by dumping a database migrated from the specified version.
     * 
     * @param reindex
     */
    private final static void createFilesFromADumpOfAMigratedDatabase(final String databaseVersion,
            final boolean reindex) throws Exception
    {
        final String databaseKind = "migration_dump";
        final DatabaseConfigurationContext context =
                createDatabaseConfigurationContext(databaseKind);
        context.setCreateFromScratch(true);
        final ISqlScriptProvider scriptProvider =
                DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context,
                        databaseVersion);
        context.setCreateFromScratch(false);
        context.setScriptFolder("source/sql");
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context,
                DatabaseVersionHolder.getDatabaseVersion());
        createDumpForJava(databaseKind, scriptProvider.getDumpFolder(DatabaseVersionHolder
                .getDatabaseVersion()));
        scriptProvider.markAsDumpRestorable(DatabaseVersionHolder.getDatabaseVersion());
        if (reindex)
        {
            performFullTextIndex(databaseKind);
        }
    }

    /**
     * Performs a full text index because the test database has been migrated.
     */
    private final static void performFullTextIndex(final String databaseKind) throws Exception
    {
        final BeanFactory factory = getBeanFactory();
        final FullTextIndexerRunnable fullTextIndexer =
                new FullTextIndexerRunnable((SessionFactory) factory
                        .getBean("hibernate-session-factory"), hibernateSearchContext);
        fullTextIndexer.run();
    }

    /**
     * Creates a freshly new {@link HibernateSearchContext} overriding the one loaded by <i>Spring</i>.
     */
    private final static HibernateSearchContext createHibernateSearchContext()
    {
        final HibernateSearchContext context = new HibernateSearchContext();
        context.setIndexBase(LUCENE_INDICES);
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
        final BeanFactory factory = getBeanFactory();
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
