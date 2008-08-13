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

package ch.systemsx.cisd.dbmigration.postgresql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseMetaData;
import ch.systemsx.cisd.dbmigration.SimpleTableMetaData;

/**
 * Helper application which creates 'mass upload' files to be used to setup the database from a
 * PostgreSQL dump.
 * <p>
 * <b>Important:</b> When creating the dump file with <code>pg_dump</code> use the option
 * <b>--no-owner</b>.
 * <p>
 * Note, that this is a stand alone class which only uses J2EE library classes.
 * 
 * @author Franz-Josef Elmer
 */
public class DumpPreparator
{
    private static final String MAC_POSTGRESQL_PATH = "/opt/local/lib/postgresql83/bin/";

    private static final String DUMP_EXEC = "pg_dump";

    private static final Set<String> FILTERED_SCHEMA_LINES =
            new LinkedHashSet<String>(Arrays.asList("SET client_encoding = 'UTF8';",
                    "COMMENT ON SCHEMA public IS 'Standard public schema';",
                    "CREATE PROCEDURAL LANGUAGE plpgsql;"));

    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.out
                    .println("Usage: java ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator "
                            + "<main folder> [<dump file>]");
            System.exit(1);
        }
        File destination = new File(args[0]);
        File dumpFile;
        if (args.length == 1)
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(System.getProperty("user.dir")));
            int response = fileChooser.showOpenDialog(null);
            if (response != JFileChooser.APPROVE_OPTION)
            {
                System.exit(0);
            }
            dumpFile = fileChooser.getSelectedFile();
        } else
        {
            dumpFile = new File(args[1]);
        }
        System.out.println("Parsing PostgreSQL dump file " + dumpFile.getAbsolutePath());
        createUploadFiles(dumpFile, destination, false);
        System.out.println("Dump preparation successfully finished.");
    }

    /**
     * Creates a dump of the specified database and stores it into the specified file.
     */
    public final static boolean createDatabaseDump(final String dataBaseName, final File dumpFile)
    {
        final String dumpExec = getDumpExecutable();
        final String dumpFilePath = dumpFile.getAbsolutePath();
        final List<String> command =
                Arrays.asList(dumpExec, "-U", "postgres", "--no-owner", "-f", dumpFilePath,
                        dataBaseName);
        final Logger rootLogger = Logger.getRootLogger();
        final boolean ok = ProcessExecutionHelper.runAndLog(command, rootLogger, rootLogger);
        return ok;
    }

    private final static String getDumpExecutable()
    {
        final Set<String> paths = OSUtilities.getSafeOSPath();
        paths.add(MAC_POSTGRESQL_PATH);
        final File dumbExec = OSUtilities.findExecutable(DUMP_EXEC, paths);
        if (dumbExec == null)
        {
            throw new EnvironmentFailureException("Cannot locate executable file: " + DUMP_EXEC);
        }
        return dumbExec.getAbsolutePath();
    }

    /**
     * Creates all files necessary to setup a database from the specified PostgreSQL dump file and
     * returns meta data which allows to access the tab-separated files.
     * 
     * @param writeEmptyTabFiles If <code>true</code> tab files are created also for empty tables.
     * @param destination Destination folder in which the folder with the files will be created. The
     *            folder will be named after the database version extracted from the dump.
     */
    public static SimpleDatabaseMetaData createUploadFiles(File dumpFile, File destination,
            boolean writeEmptyTabFiles) throws IOException
    {
        Reader reader = new FileReader(dumpFile);
        try
        {
            return createUploadFiles(reader, destination, writeEmptyTabFiles);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    static SimpleDatabaseMetaData createUploadFiles(Reader dumpReader, File destinationFolder,
            boolean writeEmptyTabFiles) throws IOException
    {
        BufferedReader reader = new BufferedReader(dumpReader);
        String line;
        State state = State.SCHEMA;
        UploadFileManager uploadFileManager =
                new UploadFileManager(destinationFolder, FILTERED_SCHEMA_LINES, writeEmptyTabFiles);
        while ((line = reader.readLine()) != null)
        {
            if (line.length() != 0 && line.startsWith("--") == false)
            {
                state = state.processLine(line, uploadFileManager);
            }
        }
        return uploadFileManager.save();
    }

    private static enum State
    {
        SCHEMA()
        {
            @Override
            State processLine(String line, UploadFileManager manager)
            {
                if (COPY_PATTERN.matcher(line).matches())
                {
                    return COPY_LINE.processLine(line, manager);
                }
                manager.addSchemaLine(line);
                return this;
            }
        },
        COPY_LINE
        {
            @Override
            State processLine(String line, UploadFileManager manager)
            {
                Matcher matcher = COPY_PATTERN.matcher(line);
                if (matcher.matches())
                {
                    manager.createUploadFile(matcher.group(1), matcher.group(2));
                } else
                {
                    throw new IllegalArgumentException(
                            "Couldn't extract table name from the following line: " + line);
                }
                return IN_COPY;
            }
        },
        IN_COPY
        {
            @Override
            State processLine(String line, UploadFileManager manager)
            {
                return manager.addTableLine(line) ? COPY : this;
            }
        },
        COPY
        {
            @Override
            State processLine(String line, UploadFileManager manager)
            {
                if (COPY_PATTERN.matcher(line).matches())
                {
                    return COPY_LINE.processLine(line, manager);
                }
                return FIX.processLine(line, manager);
            }
        },
        FIX
        {
            @Override
            State processLine(String line, UploadFileManager manager)
            {
                manager.addFinishLine(line);
                return this;
            }
        };

        private static final Pattern COPY_PATTERN = Pattern.compile("COPY (\\w*) \\((.*)\\).*");

        State processLine(String line, UploadFileManager manager)
        {
            return this;
        }

    }

    private static class UploadFileManager
    {
        private static final String VERSION_LOGS_TABLE = "database_version_logs";

        private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d{3})\\t.*");

        private final File destinationFolder;

        private final Set<String> filteredSchemaLines;

        private final StringWriter schemaScript = new StringWriter();

        private final PrintWriter schemaPrinter = new PrintWriter(schemaScript, true);

        private final StringWriter finishScript = new StringWriter();

        private final PrintWriter finishPrinter = new PrintWriter(finishScript, true);

        private final Map<String, Table> tables = new LinkedHashMap<String, Table>();

        private Table currentTable;

        private final boolean writeEmptyTabFiles;

        UploadFileManager(File destinationFolder, Set<String> filteredSchemaLines, boolean writeEmptyTabFiles)
        {
            this.destinationFolder = destinationFolder;
            this.filteredSchemaLines = filteredSchemaLines;
            this.writeEmptyTabFiles = writeEmptyTabFiles;
        }

        void addSchemaLine(String line)
        {
            if (filteredSchemaLines.contains(line) == false)
            {
                schemaPrinter.println(line);
            }
        }

        void addFinishLine(String line)
        {
            finishPrinter.println(line);
        }

        void createUploadFile(String tableName, String columns)
        {
            currentTable = new Table(tableName, extractColumnNames(columns));
            tables.put(tableName, currentTable);
        }
        
        private List<String> extractColumnNames(String columns)
        {
            List<String> list = new ArrayList<String>();
            StringTokenizer tokenizer = new StringTokenizer(columns, ",");
            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken().trim();
                if (token.startsWith("\"") && token.endsWith("\""))
                {
                    token = token.substring(1, Math.max(1, token.length() - 1));
                }
                list.add(token);
            }
            return list;
        }

        boolean addTableLine(String line)
        {
            if (currentTable == null)
            {
                throw new IllegalStateException("No table created to add the following line: "
                        + line);
            }
            if (line.startsWith("\\."))
            {
                return true;
            }
            currentTable.addRow(line);
            return false;
        }

        SimpleDatabaseMetaData save() throws IOException
        {
            String databaseVersion = getDatabaseVersion();
            File folder = createDestinationFolder();
            writeTo(folder, "schema-" + databaseVersion + ".sql", Arrays.asList(schemaScript
                    .toString()));
            ArrayList<SimpleTableMetaData> metaData = new ArrayList<SimpleTableMetaData>();
            for (Table table : tables.values())
            {
                SimpleTableMetaData tableMetaData = table.getTableMetaData();
                metaData.add(tableMetaData);
                List<String> rows = table.getRows();
                if (writeEmptyTabFiles || rows.size() > 0)
                {
                    writeTo(folder, tableMetaData.getTableFileName(), rows);
                }
            }
            writeTo(folder, "finish-" + databaseVersion + ".sql", Arrays.asList(finishScript
                    .toString()));
            return new SimpleDatabaseMetaData(databaseVersion, metaData);
        }

        private void writeTo(File folder, String fileName, List<String> lines) throws IOException
        {
            PrintWriter writer = null;
            try
            {
                final File file = new File(folder, fileName);
                writer = new PrintWriter(new FileWriter(file), true);
                for (String line : lines)
                {
                    writer.println(line);
                }
            } finally
            {
                IOUtils.closeQuietly(writer);
            }
        }

        private File createDestinationFolder()
        {
            File folder = destinationFolder;
            if (folder.exists())
            {
                if (folder.isDirectory())
                {
                    File[] files = folder.listFiles(new FileFilter()
                        {
                            public boolean accept(File pathname)
                            {
                                if (pathname.isDirectory())
                                {
                                    return false;
                                }
                                String name = pathname.getName();
                                return name.endsWith(".tsv") || name.endsWith(".sql");
                            }
                        });
                    for (File file : files)
                    {
                        if (file.delete() == false)
                        {
                            throw new IllegalStateException("Couldn't delete file '"
                                    + file.getAbsolutePath() + "' for some unknown reasons.");
                        }
                    }
                } else
                {
                    throw new IllegalStateException("Is not a directory: "
                            + folder.getAbsolutePath());
                }
            } else
            {
                if (folder.mkdirs() == false)
                {
                    throw new IllegalStateException("Couldn't create folder '"
                            + folder.getAbsolutePath() + "' for some unknown reason.");
                }
            }
            return folder;
        }

        private String getDatabaseVersion()
        {
            Table logTable = tables.get(VERSION_LOGS_TABLE);
            if (logTable == null)
            {
                throw new IllegalStateException("Table '" + VERSION_LOGS_TABLE + "' missing.");
            }
            List<String> rows = logTable.getRows();
            String result = "000";
            for (String row : rows)
            {
                Matcher matcher = VERSION_PATTERN.matcher(row);
                if (matcher.matches() == false)
                {
                    throw new IllegalArgumentException("Row does not start with a version number: "
                            + row);
                }
                String v = matcher.group(1);
                if (v.compareTo(result) > 0)
                {
                    result = v;
                }
            }
            return result;
        }
    }

    private static class Table
    {
        private static int counter;

        private final List<String> rows = new ArrayList<String>();

        private final SimpleTableMetaData tableMetaData;

        Table(String tableName, List<String> columnNames)
        {
            String uploadFileName = String.format("%03d=%s.tsv", ++counter, tableName);
            tableMetaData = new SimpleTableMetaData(tableName, uploadFileName, columnNames);
        }

        void addRow(String line)
        {
            rows.add(line);
        }

        public final SimpleTableMetaData getTableMetaData()
        {
            return tableMetaData;
        }

        public final List<String> getRows()
        {
            return rows;
        }

    }

}
