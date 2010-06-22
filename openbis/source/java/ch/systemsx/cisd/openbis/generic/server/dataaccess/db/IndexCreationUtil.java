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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexerRunnable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.HibernateSearchContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdater;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;

/**
 * Utility methods around database indexing with <i>Hibernate</i>.
 * 
 * @author Christian Ribeaud
 */
public final class IndexCreationUtil
{
    static final String DATABASE_NAME_PREFIX = "openbis_";

    private static final Template DROP_DATABASE_TEMPLATE =
            new Template("drop database if exists ${duplicated-database}");

    private static final Template CREATE_DATABASE_TEMPLATE =
            new Template(
                    "create database ${duplicated-database} with owner ${owner} template ${database}");

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, IndexCreationUtil.class);

    private static HibernateSearchContext hibernateSearchContext;

    private static BeanFactory beanFactory;

    private IndexCreationUtil()
    {
        // Can not be instantiated.
    }

    private final static BeanFactory getBeanFactory()
    {
        if (beanFactory == null)
        {
            final AbstractApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[]
                        { "applicationContext.xml" }, true);
            IndexCreationUtil.beanFactory = applicationContext;
        }
        return beanFactory;
    }

    /**
     * Performs a full text index because the test database has been migrated.
     */
    private final static void performFullTextIndex() throws Exception
    {
        final BeanFactory factory = getBeanFactory();
        final IFullTextIndexUpdater updater = createDummyUpdater();
        final FullTextIndexerRunnable fullTextIndexer =
                new FullTextIndexerRunnable((SessionFactory) factory
                        .getBean("hibernate-session-factory"), hibernateSearchContext, updater);
        fullTextIndexer.run();
    }

    /**
     * Creates a dummy {@link IFullTextIndexUpdater} that does nothing.
     */
    private final static IFullTextIndexUpdater createDummyUpdater()
    {
        return new IFullTextIndexUpdater()
            {

                public void clear()
                {
                }

                public void start()
                {
                }

                public void scheduleUpdate(IndexUpdateOperation entities)
                {
                }
            };
    }

    /**
     * Creates a freshly new {@link HibernateSearchContext} overriding the one loaded by
     * <i>Spring</i>.
     */
    private final static HibernateSearchContext createHibernateSearchContext(String indexFolder)
    {
        final HibernateSearchContext context = new HibernateSearchContext();
        context.setIndexBase(indexFolder);
        context.setIndexMode(IndexMode.INDEX_FROM_SCRATCH);
        return context;
    }

    //
    // Main method
    //

    public static void main(final String[] args) throws Exception
    {
        Parameters parameters = null;
        try
        {
            parameters = new Parameters(args);
        } catch (IllegalArgumentException e)
        {
            System.out.println(Parameters.getUsage());
            System.exit(1);
            return; // for Eclipse
        }
        LogInitializer.init();
        String databaseKind = parameters.getDatabaseKind();
        String duplicatedDatabaseKind = parameters.getDuplicatedDatabaseKind();
        String indexFolder = parameters.getIndexFolder();
        if (duplicatedDatabaseKind != null)
        {

            String databaseName = DATABASE_NAME_PREFIX + databaseKind;
            String duplicatedDatabaseName = DATABASE_NAME_PREFIX + duplicatedDatabaseKind;
            boolean ok = duplicateDatabase(duplicatedDatabaseName, databaseName);
            if (ok == false)
            {
                System.exit(1);
            }
            File dumpFile = parameters.getDumpFile();
            operationLog.info("Dump '" + duplicatedDatabaseName + "' into '" + dumpFile + "'.");
            DumpPreparator.createDatabaseDump(duplicatedDatabaseName, dumpFile);
            databaseKind = duplicatedDatabaseKind;
            FileUtilities.deleteRecursively(new File(indexFolder));
        }
        System.setProperty("database.kind", databaseKind);
        // Deactivate the indexing in the application context loaded by Spring.
        System.setProperty("hibernate.search.index-mode", "NO_INDEX");
        System.setProperty("hibernate.search.index-base", indexFolder);
        System.setProperty("database.create-from-scratch", "false");
        hibernateSearchContext = createHibernateSearchContext(indexFolder);
        hibernateSearchContext.afterPropertiesSet();
        operationLog.info("=========== Start indexing ===========");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        performFullTextIndex();
        stopWatch.stop();
        operationLog.info("Index of database '" + DATABASE_NAME_PREFIX + databaseKind
                + "' successfully built in '" + indexFolder + "' after "
                + ((stopWatch.getTime() + 30000) / 60000) + " minutes.");
        System.exit(0);
    }

    static boolean duplicateDatabase(String destinationDatabase, String sourceDatabase)
    {
        operationLog.info("Duplicate database '" + sourceDatabase + "' as '" + destinationDatabase
                + "'.");
        Template dropCmd = DROP_DATABASE_TEMPLATE.createFreshCopy();
        dropCmd.bind("duplicated-database", destinationDatabase);

        boolean ok = execute(dropCmd);
        if (ok == false)
        {
            return false;
        }
        Template createCmd = CREATE_DATABASE_TEMPLATE.createFreshCopy();
        createCmd.bind("database", sourceDatabase);
        createCmd.bind("duplicated-database", destinationDatabase);
        createCmd.bind("owner", System.getProperty("user.name"));
        return execute(createCmd);
    }

    private static boolean execute(Template template)
    {
        String sql = template.createText();
        String psql = DumpPreparator.getPSQLExecutable();
        List<String> cmd = Arrays.asList(psql, "-U", "postgres", "-c", sql);
        boolean ok = ProcessExecutionHelper.runAndLog(cmd, operationLog, operationLog);
        if (ok == false)
        {
            operationLog.error("Sql command execution failed: " + template.createText());
        }
        return ok;
    }

    private static class Parameters
    {
        static String getUsage()
        {
            return "Usage: java "
                    + IndexCreationUtil.class.getName()
                    + " [-d <duplicated database kind> <dump file>] <database kind> [<index folder>]";
        }

        private String duplicatedDatabaseKind;

        private File dumpFile;

        private String databaseKind;

        private String indexFolder = "sourceTest/lucene/indices";

        Parameters(String[] args)
        {
            List<String> arguments = new ArrayList<String>(Arrays.asList(args));
            if (arguments.size() > 0 && arguments.get(0).equals("-d"))
            {
                arguments.remove(0);
                throwExceptionIfEmpty(arguments, "duplicated database kind");
                duplicatedDatabaseKind = arguments.remove(0);
                throwExceptionIfEmpty(arguments, "dump file");
                dumpFile = new File(arguments.remove(0));
            }
            throwExceptionIfEmpty(arguments, "database kind");
            databaseKind = arguments.remove(0);
            if (arguments.size() > 0)
            {
                indexFolder = arguments.get(0);
            }
        }

        private void throwExceptionIfEmpty(List<String> arguments, String entityType)
        {
            if (arguments.size() < 1)
            {
                throw new IllegalArgumentException("Missing argument <" + entityType + ">.");
            }
        }

        final File getDumpFile()
        {
            return dumpFile;
        }

        final String getDuplicatedDatabaseKind()
        {
            return duplicatedDatabaseKind;
        }

        final String getDatabaseKind()
        {
            return databaseKind;
        }

        final String getIndexFolder()
        {
            return indexFolder;
        }

    }
}
