/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IndexCreationUtil;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexerRunnable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;

/**
 * @author Franz-Josef Elmer
 */
public class TestInitializer
{
    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TestInitializer.class);

    private static final String LUCENE_INDEX_TEMPLATE_PATH = "targets/tempLuceneIndices";

    private static final String LUCENE_INDEX_PATH = "targets/lucene/indices";

    private static final String DB_KIND_PROPERTY_NAME = "dbKind";

    private static final String SCRIPT_FOLDER_TEST_DB_PROPERTY_NAME = "scriptFolderTestDB";

    private static final String SCRIPT_FOLDER_EMPTY_DB_PROPERTY_NAME = "scriptFolderEmptyDB";

    private static final String DEFAULT_DB_KIND = "test";

    private static final String DEFAULT_SCRIPT_FOLDER_TEST_DB = "../openbis/sourceTest";

    private static final String DEFAULT_SCRIPT_FOLDER_EMPTY_DB = "../openbis/source";

    public static void init()
    {
        initWithoutIndex();
    }

    public static void initWithoutIndex()
    {
        init(IndexMode.NO_INDEX, getScriptFolderTestDB());
    }

    public static void initWithIndex()
    {
        init(IndexMode.SKIP_IF_MARKER_FOUND, getScriptFolderTestDB());
    }

    public static void initEmptyDbNoIndex()
    {
        init(IndexMode.NO_INDEX, getScriptFolderEmptyDB());
    }

    public static void initEmptyDbWithIndex()
    {
        init(IndexMode.SKIP_IF_MARKER_FOUND, getScriptFolderEmptyDB());
    }

    private static boolean firstTry = true;

    private static void init(IndexMode hibernateIndexMode, String scriptFolder)
    {
        LogInitializer.init();

        if (firstTry && System.getProperty("rebuild-index", "true").equals("true"))
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
            String timestamp = dateFormat.format(new Date());
            String databaseKind = "indexing_" + timestamp;
            try
            {
                File temporaryFile = new File(LUCENE_INDEX_TEMPLATE_PATH);
                FileUtilities.deleteRecursively(temporaryFile);
                temporaryFile.mkdirs();

                System.setProperty("script-folder", scriptFolder);

                IndexCreationUtil.main(databaseKind, temporaryFile.getAbsolutePath(), "true");
            } catch (Exception ex)
            {
                operationLog.error(ex);
                CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                String psql = DumpPreparator.getPSQLExecutable();
                final String databaseName = "openbis_" + databaseKind;
                String sql = "drop database if exists " + databaseName;
                final List<String> cmd = Arrays.asList(psql, "-U", "postgres", "-c", sql);
                boolean result = ProcessExecutionHelper.runAndLog(cmd, operationLog, operationLog);
                if (result == false)
                {
                    operationLog.error("Couldn't drop database created for indexing: "
                            + databaseName);
                    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                operationLog.info("Try to drop indexing database " + databaseName);
                                boolean ok =
                                        ProcessExecutionHelper.runAndLog(cmd, operationLog,
                                                operationLog);
                                operationLog.info("Dropping indexing database " + databaseName
                                        + (ok ? "was" : "wasn't") + " successful.");
                            }
                        }, "dropping-indexing-database-shutdown"));
                }
            }
            firstTry = false;
        }

        // make sure the search index is up-to-date
        // and in the right place when we run tests
        restoreSearchIndex();

        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", getDBKind());
        System.setProperty("script-folder", scriptFolder);
        System.setProperty("hibernate.search.index-mode", hibernateIndexMode.name());
        System.setProperty("hibernate.search.index-base", LUCENE_INDEX_PATH);
        System.setProperty("hibernate.search.worker.execution", "sync");

    }

    // create a fresh copy of the Lucene index
    public static void restoreSearchIndex()
    {
        File targetPath = new File(TestInitializer.LUCENE_INDEX_PATH).getAbsoluteFile();
        FileUtilities.deleteRecursively(targetPath);
        targetPath.mkdirs();
        File srcPath = new File(LUCENE_INDEX_TEMPLATE_PATH).getAbsoluteFile();
        try
        {
            FileUtils.copyDirectory(srcPath, targetPath, new FileFilter()
                {
                    @Override
                    public boolean accept(File path)
                    {
                        return false == path.getName().equalsIgnoreCase(".svn");
                    }
                });
            new File(srcPath, FullTextIndexerRunnable.FULL_TEXT_INDEX_MARKER_FILENAME)
                    .createNewFile();
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    public static String getDBKindPropertyName()
    {
        return TestInitializer.class.getName() + "." + DB_KIND_PROPERTY_NAME;
    }

    public static String getScriptFolderTestDBPropertyName()
    {
        return TestInitializer.class.getName() + "." + SCRIPT_FOLDER_TEST_DB_PROPERTY_NAME;
    }

    public static String getScriptFolderEmptyDBPropertyName()
    {
        return TestInitializer.class.getName() + "." + SCRIPT_FOLDER_EMPTY_DB_PROPERTY_NAME;
    }

    private static String getDBKind()
    {
        return getSystemProperty(getDBKindPropertyName(), DEFAULT_DB_KIND);
    }

    private static String getScriptFolderTestDB()
    {
        return getSystemProperty(getScriptFolderTestDBPropertyName(), DEFAULT_SCRIPT_FOLDER_TEST_DB);
    }

    private static String getScriptFolderEmptyDB()
    {
        return getSystemProperty(getScriptFolderEmptyDBPropertyName(), DEFAULT_SCRIPT_FOLDER_EMPTY_DB);
    }

    private static String getSystemProperty(String propertyName, String defaultValue)
    {
        String propertyValue = System.getProperty(propertyName);

        if (propertyValue != null && false == propertyValue.trim().isEmpty())
        {
            return propertyValue.trim();
        } else
        {
            return defaultValue;
        }
    }

}
