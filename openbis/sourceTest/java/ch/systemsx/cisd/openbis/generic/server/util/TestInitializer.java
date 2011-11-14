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

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.FullTextIndexerRunnable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexMode;

/**
 * @author Franz-Josef Elmer
 */
public class TestInitializer
{
    public static final String LUCENE_INDEX_TEMPLATE_PATH = "../openbis/sourceTest/lucene/indices";

    public static final String LUCENE_INDEX_PATH = "../openbis/targets/lucene/indices";

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

    private static void init(IndexMode hibernateIndexMode, String scriptFolder)
    {
        LogInitializer.init();
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", "test");
        System.setProperty("script-folder", scriptFolder);
        System.setProperty("hibernate.search.index-mode", hibernateIndexMode.name());
        System.setProperty("hibernate.search.index-base", LUCENE_INDEX_PATH);
        System.setProperty("hibernate.search.worker.execution", "sync");

        // make sure the search index is up-to-date
        // and in the right place when we run tests
        restoreSearchIndex();
    }

    // create a fresh copy of the Lucene index
    public static void restoreSearchIndex()
    {
        File targetPath = new File(TestInitializer.LUCENE_INDEX_PATH);
        FileUtilities.deleteRecursively(targetPath);
        targetPath.mkdirs();
        File srcPath = new File(LUCENE_INDEX_TEMPLATE_PATH);
        try
        {
            FileUtils.copyDirectory(srcPath, targetPath, new FileFilter()
                {
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
