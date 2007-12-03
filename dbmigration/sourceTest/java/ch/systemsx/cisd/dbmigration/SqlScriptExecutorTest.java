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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Tests for {@link SqlScriptExecutor}.
 *
 * @author Franz-Josef Elmer
 */
public class SqlScriptExecutorTest
{
    private static final String TEMPORARY_DATA_SCRIPT_FOLDER_NAME = "temporaryDataScriptFolder";
    private static final String TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME = "temporarySchemaScriptFolder";
    private static final String TEMPORARY_MASS_DATA_UPLOAD_FOLDER_NAME = "temporaryMassDataUploadFolder";
    private static final String TEMPORARY_INTERNAL_SCRIPT_FOLDER_NAME = "temporaryInternalScriptFolder";
    private static final File TEMP_SCHEMA_SCRIPT_FOLDER = new File(TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME);
    private static final File TEMP_DATA_SCRIPT_FOLDER = new File(TEMPORARY_DATA_SCRIPT_FOLDER_NAME);
    private static final File TEMP_MASS_DATA_UPLOAD_FOLDER = new File(TEMPORARY_MASS_DATA_UPLOAD_FOLDER_NAME);
    private static final File TEMP_INTERNAL_SCRIPT_FOLDER = new File(TEMPORARY_INTERNAL_SCRIPT_FOLDER_NAME);
    private static final String MIGRATION = "migration";
    private static final String VERSION = "042";
    private static final String VERSION2 = "049";
    
    private SqlScriptProvider sqlScriptProvider;

    @BeforeClass
    public void setUpTestFiles() throws IOException
    {
        File schemaVersionFolder = new File(TEMP_SCHEMA_SCRIPT_FOLDER, VERSION);
        schemaVersionFolder.mkdirs();
        write(new File(schemaVersionFolder, "schema-" + VERSION + ".sql"), "code: schema");
        File migrationFolder = new File(TEMP_SCHEMA_SCRIPT_FOLDER, MIGRATION);
        migrationFolder.mkdir();
        write(new File(migrationFolder, "migration-" + VERSION + "-" + VERSION2 + ".sql"), "code: migration");
        File dataVersionFolder = new File(TEMP_DATA_SCRIPT_FOLDER, VERSION);
        dataVersionFolder.mkdirs();
        write(new File(dataVersionFolder, "data-" + VERSION + ".sql"), "code: data");
        TEMP_INTERNAL_SCRIPT_FOLDER.mkdir();
        write(new File(TEMP_INTERNAL_SCRIPT_FOLDER, "hello.script"), "hello world!");
        File massUploaadVersionFolder = new File(TEMP_MASS_DATA_UPLOAD_FOLDER, VERSION);
        massUploaadVersionFolder.mkdirs();
        write(new File(massUploaadVersionFolder, "1=test.tsv"), "1\tbla");
        sqlScriptProvider = new SqlScriptProvider(TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME, 
                                                  TEMPORARY_DATA_SCRIPT_FOLDER_NAME,
                                                  TEMPORARY_MASS_DATA_UPLOAD_FOLDER_NAME,
                                                  TEMPORARY_INTERNAL_SCRIPT_FOLDER_NAME);
    }

    private void write(File file, String content) throws IOException
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(file);
            new PrintWriter(fileWriter).print(content);
        } finally
        {
            if (fileWriter != null)
            {
                fileWriter.close();
            }
        }
    }
    
    @AfterClass
    public void deleteTestFiles()
    {
        delete(TEMP_SCHEMA_SCRIPT_FOLDER);
        delete(TEMP_DATA_SCRIPT_FOLDER);
        delete(TEMP_INTERNAL_SCRIPT_FOLDER);
        delete(TEMP_MASS_DATA_UPLOAD_FOLDER);
    }

    private void delete(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (File child : files)
            {
                delete(child);
            }
        }
        file.delete();
    }
    
    @Test
    public void testGetSchemaScript()
    {
        Script script = sqlScriptProvider.getSchemaScript(VERSION);
        assertEquals(TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME + "/" + VERSION + "/schema-" + VERSION + ".sql", script.getName());
        assertEquals("code: schema", script.getCode().trim());
    }

    @Test
    public void testGetNonExistingSchemaScript()
    {
        assertEquals(null, sqlScriptProvider.getSchemaScript("000"));
    }
    
    @Test
    public void testGetDataScript()
    {
        Script script = sqlScriptProvider.getDataScript(VERSION);
        assertEquals(TEMPORARY_DATA_SCRIPT_FOLDER_NAME + "/" + VERSION + "/data-" + VERSION + ".sql", script.getName());
        assertEquals("code: data", script.getCode().trim());
    }

    @Test void testGetMassUploadFiles()
    {
        final File[] massUploadFiles = sqlScriptProvider.getMassUploadFiles(VERSION);
        assertEquals(1, massUploadFiles.length);
        assertEquals("1=test.tsv", massUploadFiles[0].getName());
        assertTrue(massUploadFiles[0].exists());
    }
    
    @Test
    public void testGetNonExistingDataScript()
    {
        assertEquals(null, sqlScriptProvider.getDataScript("000"));
    }
    
    @Test
    public void testGetMigrationScript()
    {
        Script script = sqlScriptProvider.getMigrationScript(VERSION, VERSION2);
        assertEquals(TEMPORARY_SCHEMA_SCRIPT_FOLDER_NAME + "/" + MIGRATION + "/migration-" + VERSION + "-" + VERSION2
                     + ".sql", script.getName());
        assertEquals("code: migration", script.getCode().trim());
    }
    
    @Test
    public void testGetNonExistingMigrationScript()
    {
        assertEquals(null, sqlScriptProvider.getMigrationScript("000", "001"));
    }
    
    @Test
    public void testGetScript()
    {
        Script script = sqlScriptProvider.getScript("hello.script");
        assertEquals(TEMPORARY_INTERNAL_SCRIPT_FOLDER_NAME + "/hello.script", script.getName());
        assertEquals("hello world!", script.getCode().trim());
    }

    @Test
    public void testGetNonExistingScript()
    {
        assertEquals(null, sqlScriptProvider.getScript("blabla.sql"));
    }
    
}
