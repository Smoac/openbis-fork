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

    public static final String LUCENE_INDEX_TEMPLATE_PATH = "targets/tempLuceneIndices";

    public static final String LUCENE_INDEX_PATH = "targets/lucene/indices";

    public static final String SCRIPT_FOLDER_TEST_DB = "../openbis/sourceTest";

    public static final String SCRIPT_FOLDER_EMPTY_DB = "../openbis/source";

    public static void init()
    {
        initWithoutIndex();
    }

    public static void initWithoutIndex()
    {
        init(IndexMode.NO_INDEX, SCRIPT_FOLDER_TEST_DB);
    }

    public static void initWithIndex()
    {
        init(IndexMode.SKIP_IF_MARKER_FOUND, SCRIPT_FOLDER_TEST_DB);
    }

    public static void initEmptyDbNoIndex()
    {
        init(IndexMode.NO_INDEX, SCRIPT_FOLDER_EMPTY_DB);
    }

    public static void initEmptyDbWithIndex()
    {
        init(IndexMode.SKIP_IF_MARKER_FOUND, SCRIPT_FOLDER_EMPTY_DB);
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

                System.setProperty("script-folder", SCRIPT_FOLDER_TEST_DB);

                IndexCreationUtil.main(databaseKind, temporaryFile.getAbsolutePath(), "true");
            } catch (Exception ex)
            {
                operationLog.error(ex);
                CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                String psql = DumpPreparator.getPSQLExecutable();
                String databaseName = "openbis_" + databaseKind;
                String sql = "drop database if exists " + databaseName;
                List<String> cmd = Arrays.asList(psql, "-U", "postgres", "-c", sql);
                for (int i = 0, n = 10; i < n; i++)
                {
                    boolean result =
                            ProcessExecutionHelper.runAndLog(cmd, operationLog, operationLog);
                    if (result == false)
                    {
                        operationLog.error("Couldn't drop database created for indexing: "
                                + databaseName);
                        if (i < n - 1)
                        {
                            operationLog.info("Try it again after some waiting time");
                            try
                            {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex)
                            {
                                // ignored silently
                            }
                        }
                    } else
                    {
                        operationLog.info("Database for indexing ropped: " + databaseName);
                        break;
                    }
                }
            }
            firstTry = false;
        }

        // make sure the search index is up-to-date
        // and in the right place when we run tests
        restoreSearchIndex();

        String projectName = System.getProperty("ant.project.name", "");

        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", projectName.isEmpty() ? "test" : "test_" + projectName);
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

}
