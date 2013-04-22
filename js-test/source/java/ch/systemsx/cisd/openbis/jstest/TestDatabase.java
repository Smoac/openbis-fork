/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.jstest;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;

/**
 * @author pkupczyk
 */
public class TestDatabase
{

    private static final Logger logger = Logger.getLogger(TestDatabase.class);

    private static final Template DROP_DATABASE_TEMPLATE = new Template(
            "drop database if exists ${database-name}");

    private static final Template CREATE_DATABASE_TEMPLATE = new Template(
            "create database ${database-name} with owner ${database-owner}");

    public static void restoreDumps(String dumpFolderPathOrNull)
    {
        if (dumpFolderPathOrNull == null)
        {
            return;
        }

        File dumpFolder = new File(dumpFolderPathOrNull);

        if (dumpFolder.exists() && dumpFolder.isDirectory())
        {
            File[] dumpFiles = dumpFolder.listFiles();

            for (File dumpFile : dumpFiles)
            {
                if (dumpFile.isFile() && dumpFile.getName().endsWith(".sql"))
                {
                    String databaseOwner = System.getProperty("user.name");
                    String databaseName = FilenameUtils.getBaseName(dumpFile.getName());

                    dropDatabase(databaseName);
                    createEmptyDatabase(databaseOwner, databaseName);
                    restoreDatabaseDump(databaseOwner, databaseName, dumpFile);
                }
            }
        }
    }

    private static void dropDatabase(String databaseName)
    {
        System.out.println("Droping database " + databaseName);

        Template template = DROP_DATABASE_TEMPLATE.createFreshCopy();
        template.bind("database-name", databaseName);
        executeSql("postgres", template.createText());
    }

    private static void createEmptyDatabase(String databaseOwner, String databaseName)
    {
        System.out.println("Creating database " + databaseName);

        Template template = CREATE_DATABASE_TEMPLATE.createFreshCopy();
        template.bind("database-name", databaseName);
        template.bind("database-owner", databaseOwner);
        executeSql("postgres", template.createText());
    }

    private static void restoreDatabaseDump(String databaseOwner, String databaseName,
            File databaseDump)
    {
        System.out.println("Restoring database " + databaseName + " from dump "
                + databaseDump.getAbsolutePath());

        String psql = DumpPreparator.getPSQLExecutable();
        List<String> command =
                Arrays.asList(psql, "-U", databaseOwner, "-d", databaseName, "-f",
                        databaseDump.getAbsolutePath());
        executeCommand(command);
    }

    private static void executeSql(String userName, String sql)
    {
        String psql = DumpPreparator.getPSQLExecutable();
        List<String> command = Arrays.asList(psql, "-U", userName, "-c", sql);
        executeCommand(command);
    }

    private static void executeCommand(List<String> command)
    {
        ProcessResult run = ProcessExecutionHelper.run(command, logger, logger);
        if (run.isOK() == false)
        {
            throw new IllegalArgumentException("Couldn't execute a command: " + command
                    + " because: " + run.getOutput());
        }
    }

}
