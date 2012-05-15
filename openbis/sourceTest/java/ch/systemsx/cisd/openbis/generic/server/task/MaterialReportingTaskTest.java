/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.task.MaterialReportingTask.MappingInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.VocabularyTermBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
    { MaterialReportingTask.class, MappingInfo.class })
public class MaterialReportingTaskTest extends AbstractFileSystemTestCase
{
    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private ICommonServerForInternalUse server;

    private DatabaseConfigurationContext dbConfigContext;

    private MaterialReportingTask materialReportingTask;

    private String databaseName;

    private File mappingFile;

    private Properties properties;

    @BeforeMethod
    public void setUpMocks() throws Exception
    {
        context = new Mockery();
        server = context.mock(ICommonServerForInternalUse.class);
        materialReportingTask = new MaterialReportingTask(server);

        dbConfigContext = new DatabaseConfigurationContext();
        dbConfigContext.setDatabaseEngineCode("postgresql");
        dbConfigContext.setBasicDatabaseName("material_reporting_task");
        dbConfigContext.setDatabaseKind("test");
        dbConfigContext.setOwner(null);
        databaseName = dbConfigContext.getDatabaseName();
        dropTestDatabase();
        createTestDatabase();
        createTables(
                "create table report1 (id bigint, code varchar(20), description varchar(200))",
                "create table report2 (code varchar(20), rank integer, greetings varchar(200), "
                        + "size double precision, organism varchar(100), material varchar(30), timestamp timestamp)");
        dbConfigContext.closeConnections();
        mappingFile = new File(workingDirectory, "mapping-file.txt");
        properties = new Properties();
        properties.setProperty("database-driver", "org.postgresql.Driver");
        properties.setProperty("database-url", "jdbc:postgresql://localhost/" + databaseName);
        properties.setProperty("database-username", "postgres");
        properties.setProperty(MaterialReportingTask.MAPPING_FILE_KEY, mappingFile.getPath());
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        context.assertIsSatisfied();
        materialReportingTask.closeDatabaseConnections();
        dbConfigContext.closeConnections();
    }

    @Test
    public void testReadValidMappingFile() throws SQLException
    {
        FileUtilities.writeToFile(mappingFile, "# my mapping\n[ T1 :  TABLE1 , CODE ]\n\n"
                + "[T2: TABLE2, code]\n  P2 : Prop2   \n P1 :  PROP1  ");

        Map<String, MappingInfo> mapping =
                MaterialReportingTask.readMappingFile(mappingFile.getPath());

        MappingInfo mappingInfo1 = mapping.get("T1");
        assertEquals("insert into table1 (code) values(?)", mappingInfo1.createInsertStatement());
        MappingInfo mappingInfo2 = mapping.get("T2");
        assertEquals("insert into table2 (code, prop2, prop1) values(?, ?, ?)",
                mappingInfo2.createInsertStatement());
        assertEquals(2, mapping.size());
    }

    @Test
    public void testReadMappingFileWithMissingInitialTableDefinition()
    {
        FileUtilities.writeToFile(mappingFile, "P2: prop2\nP1:prop1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 'P2: prop2': "
                    + "Missing first material type table definition of form "
                    + "'[<material type tode>: <table name>, <code column name>]'", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingFinishingBracket()
    {
        FileUtilities.writeToFile(mappingFile, "[T1: TABLE, CODE\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1: TABLE, CODE': Missing ']'", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingColon()
    {
        FileUtilities.writeToFile(mappingFile, "[T1]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1]': 2 items separated by ':' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionMissingCodeColumnName()
    {
        FileUtilities.writeToFile(mappingFile, "[T1: table]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T1: table]': 2 items separated by ',' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionEmptyMaterialTypeCode()
    {
        FileUtilities.writeToFile(mappingFile, "[ : table, code]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[ : table, code]': Missing material type code.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionEmptyTableName()
    {
        FileUtilities.writeToFile(mappingFile, "[T : , code]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T : , code]': Missing table name.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidTableDefinitionCodeColumnName()
    {
        FileUtilities.writeToFile(mappingFile, "[T : t, ]\nP1: p1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 1 "
                    + "'[T : t, ]': Missing code column name.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidPropertyMappingMissingColon()
    {
        FileUtilities.writeToFile(mappingFile, "[ T : t , c]\nP1");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 2 "
                    + "'P1': 2 items separated by ':' expected.", ex.getMessage());
        }
    }

    @Test
    public void testReadMappingFileWithInvalidPropertyMappingMissungPropertyTypeCode()
    {
        FileUtilities.writeToFile(mappingFile, "[ T : t , c]\n : p");

        try
        {
            MaterialReportingTask.readMappingFile(mappingFile.getPath());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Error in mapping file '" + mappingFile + "' at line 2 "
                    + "' : p': Missing property type code.", ex.getMessage());
        }
    }

    @Test
    public void testDatabaseMetaDataMissingCodeColumn() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT1,C]");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing column 'c' in table 'report1' of report database.",
                    ex.getMessage());
        }
    }

    @Test
    public void testDatabaseMetaDataMissingTable() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT3,C]");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing table 'report3' in report database.", ex.getMessage());
        }
    }

    @Test
    public void testDatabaseMetaDataCodeColumnOfWrongType() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT1,ID]");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Column 'id' of table 'report1' is not of type VARCHAR.", ex.getMessage());
        }
    }

    @Test
    public void testDatabaseMetaDataMissingPropertyColumn() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT2,CODE]\nP1:my_prop");
        try
        {
            materialReportingTask.setUp("", properties);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing column 'my_prop' in table 'report2' of report database.",
                    ex.getMessage());
        }
    }

    @Test
    public void testInsert() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "# my mapping\n[T1:REPORT1,CODE]\n\n"
                + "[T2: REPORT2, code]\nM:MATERIAL\nS:size\nP2: GREETINGS\nP1:RANK\nORG:ORGANISM\n"
                + "T:timestamp");
        materialReportingTask.setUp("", properties);
        final Material m1 =
                new MaterialBuilder().code("M1").type("T1").property("P1", "42").getMaterial();
        MaterialBuilder mb2 = new MaterialBuilder().code("M2").type("T2");
        mb2.property("P1").type(DataTypeCode.INTEGER).value(42);
        mb2.property("S").type(DataTypeCode.REAL).value(1e7 + 0.5);
        mb2.property("ORG").type(DataTypeCode.CONTROLLEDVOCABULARY)
                .value(new VocabularyTermBuilder("FLY").getTerm());
        mb2.property("M").type(DataTypeCode.MATERIAL).value(m1);
        mb2.property("T").type(DataTypeCode.TIMESTAMP).value(new Date(24 * 3600L * 1000L * 33));
        final Material m2 = mb2.getMaterial();
        final Material m3 =
                new MaterialBuilder().code("M3").type("T2").property("P2", "hello").getMaterial();
        final RecordingMatcher<DetailedSearchCriteria> criteriaRecorder =
                new RecordingMatcher<DetailedSearchCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(m1, m2, m3)));
                }
            });

        materialReportingTask.execute();

        List<?> result = loadTable("report1");
        assertEquals("[{code=M1, description=null, id=null}]", result.toString());
        result = loadTable("report2");
        assertEquals("[{code=M2, greetings=null, material=M1, organism=FLY, rank=42, "
                + "size=1.00000005E7, timestamp=1970-02-03 01:00:00.0}, "
                + "{code=M3, greetings=hello, material=null, organism=null, rank=null, "
                + "size=null, timestamp=null}]", result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testInsertUpdate() throws Exception
    {
        FileUtilities.writeToFile(mappingFile, "[T1:REPORT1,CODE]\n"
                + "[T2: REPORT2, code]\nM:MATERIAL\nS:size\nP1:RANK\nORG:ORGANISM\n"
                + "P2: GREETINGS\nT:timestamp");
        materialReportingTask.setUp("", properties);
        final Material m1 =
                new MaterialBuilder().code("M1").type("T1").property("P1", "42").getMaterial();
        MaterialBuilder mb2 = new MaterialBuilder().code("M2").type("T2");
        mb2.property("P1").type(DataTypeCode.INTEGER).value(42);
        final Material m2 = mb2.getMaterial();
        final Material m3 =
                new MaterialBuilder().code("M3").type("T2").property("P2", "hello").getMaterial();
        MaterialBuilder mb2v2 = new MaterialBuilder().code("M2").type("T2");
        mb2v2.property("P1").type(DataTypeCode.INTEGER).value(137);
        mb2v2.property("ORG").type(DataTypeCode.CONTROLLEDVOCABULARY)
                .value(new VocabularyTermBuilder("TIGER").label("Tiger").getTerm());
        final Material m2v2 = mb2v2.property("P2", "blabla").getMaterial();
        final Material m4 =
                new MaterialBuilder().code("M4").type("T2").property("P2", "hi").getMaterial();
        final RecordingMatcher<DetailedSearchCriteria> criteriaRecorder =
                new RecordingMatcher<DetailedSearchCriteria>();
        final Sequence sequence = context.sequence("materials");
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(server).tryToAuthenticateAsSystem();
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(m1, m2, m3)));
                    inSequence(sequence);

                    one(server).searchForMaterials(with(SESSION_TOKEN), with(criteriaRecorder));
                    will(returnValue(Arrays.asList(m1, m2v2, m4)));
                    inSequence(sequence);
                }
            });

        materialReportingTask.execute();
        materialReportingTask.execute();

        List<?> result = loadTable("report1");
        assertEquals("[{code=M1, description=null, id=null}]", result.toString());
        result = loadTable("report2");
        assertEquals("[{code=M2, greetings=blabla, material=null, organism=Tiger, "
                + "rank=137, size=null, timestamp=null}, "
                + "{code=M3, greetings=hello, material=null, organism=null, "
                + "rank=null, size=null, timestamp=null}, "
                + "{code=M4, greetings=hi, material=null, organism=null, "
                + "rank=null, size=null, timestamp=null}]", result.toString());
        context.assertIsSatisfied();
    }

    private List<?> loadTable(String tableName)
    {
        return new JdbcTemplate(dbConfigContext.getDataSource()).query("select * from " + tableName
                + " order by code", new ColumnMapRowMapper()
            {
                @SuppressWarnings("rawtypes")
                @Override
                protected Map createColumnMap(int columnCount)
                {
                    return new TreeMap();
                }

                @Override
                protected String getColumnKey(String columnName)
                {
                    return columnName.toLowerCase();
                }
            });
    }

    private void createTables(String... creationStatements) throws Exception
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            for (String creationStatement : creationStatements)
            {
                statement.execute(creationStatement);
            }
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    private void createTestDatabase() throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getAdminDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            statement.execute("create database " + dbConfigContext.getDatabaseName()
                    + " with owner = \"" + dbConfigContext.getOwner() + "\"");
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

    private void dropTestDatabase() throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            connection = dbConfigContext.getAdminDataSource().getConnection();
            connection.setAutoCommit(true);
            statement = connection.createStatement();
            statement.execute("drop database " + dbConfigContext.getDatabaseName());
        } catch (SQLException ex)
        {
            if (ex.getMessage().indexOf("does not exist") < 0)
            {
                throw ex;
            }
        } finally
        {
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
    }

}
