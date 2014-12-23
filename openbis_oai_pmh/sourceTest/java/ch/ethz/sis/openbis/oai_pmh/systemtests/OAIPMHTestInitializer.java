/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.oai_pmh.systemtests;

import java.io.File;

import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.test.server.TestDatabase;

/**
 * @author pkupczyk
 */
public class OAIPMHTestInitializer
{

    private static String dbKind = "test_oai_pmh";

    private static String getDBKind()
    {
        return dbKind;
    }

    private static String getDBKindForIndexing()
    {
        return getDBKind() + "_indexing";
    }

    private static String getDBName()
    {
        return "openbis_" + getDBKind();
    }

    private static String getDBNameForIndexing()
    {
        return "openbis_" + getDBKindForIndexing();
    }

    public static void init()
    {
        File sqlDumpFile = new File("resource/test-db/" + getDBName() + ".sql");
        File binaryDumpFile = new File("resource/test-db/" + getDBName() + ".dmp");

        File dumpFile = null;
        if (sqlDumpFile.exists())
        {
            dumpFile = sqlDumpFile;
        } else if (binaryDumpFile.exists())
        {
            dumpFile = binaryDumpFile;
        } else
        {
            throw new IllegalArgumentException("No dump file found.");
        }

        TestDatabase.restoreDump(dumpFile, getDBName());
        TestDatabase.restoreDump(dumpFile, getDBNameForIndexing());

        System.setProperty("jetty.home", "../openbis/targets/www");
        TestInitializer.setScriptFolderForEmptyDB("../openbis/source");
        TestInitializer.setScriptFolderForTestDB("../openbis/sourceTest");
        TestInitializer.setDBKind(getDBKind());
        TestInitializer.setDBKindForIndexing(getDBKindForIndexing());
        TestInitializer.setCreateDBFromScratch(false);
    }

}
