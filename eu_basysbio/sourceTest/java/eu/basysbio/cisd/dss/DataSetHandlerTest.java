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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataSetHandler.class)
public class DataSetHandlerTest extends AbstractFileSystemTestCase
{
    private static final String E_MAIL_ADDRESS = "e-mail";
    
    private static final String DATA_SET_CODE = "DS1";

    private static final long DATA_SET_SPECIAL_ID = 4711;

    private static final String GROUP_CODE = "/G1";

    private static final long EXP_ID = 42L;

    private static final String EXP_PERM_ID = "perm-" + EXP_ID;

    private static final long VALUE_GROUP_ID1 = 42;
    private static final long VALUE_GROUP_ID2 = 43;

    private static final String PROJECT_CODE = "P1";


    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private File dropBox;

    private DataSource dataSource;

    private ITimeSeriesDAO dao;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        super.setUp();
        LogInitializer.init();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dataSource = context.mock(DataSource.class);
        dao = context.mock(ITimeSeriesDAO.class);
        dropBox = new File(workingDirectory, "drop-box");
        dropBox.mkdirs();
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testMissingExperiment() throws IOException
    {
        DataSetHandler handler = createHandler();

        File file = createDataExample();

        try
        {
            handler.handle(file, createDataSetInformation(DataSetHandler.TIME_SERIES), null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data set should be registered for an experiment and not for a sample.",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testWrongExperiment() throws IOException
    {
        DataSetHandler handler = createHandler();
        File file = createDataExample();
        DataSetInformation dataSetInformation =
                createDataSetInformation(DataSetHandler.TIME_SERIES);
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE, "exp1"));

        try
        {
            handler.handle(file, dataSetInformation, null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data should be uploaded for experiment 'GM_BR_B1' instead of 'EXP1'.", ex
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetAlreadyExists()
    {
        DataSetHandler handler = createHandler();
        File file = createDataExample();
        final Experiment experiment = createExperiment("GM_BR_B1");
        prepareGetOrCreateDataSet(experiment, true);
        context.checking(new Expectations()
            {
                {
                    one(service).tryToGetExperiment(
                            new ExperimentIdentifier(PROJECT_CODE, "GM_BR_B1"));
                    will(returnValue(experiment));

                    one(dao).listDataSetsByTimeSeriesDataColumnHeader(
                            new DataColumnHeader("GM::BR::B1::200::EX::T1::CE::"
                                    + "MetaboliteLCMS::Value[mM]::Log10::NB::NC"));
                    MockDataSet<String> dataSets = new MockDataSet<String>();
                    dataSets.add("ds1");
                    will(returnValue(dataSets));
                }

            });

        DataSetInformation dataSetInformation =
                createDataSetInformation(DataSetHandler.TIME_SERIES);
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE,
                "GM_BR_B1"));
        try
        {
            handler.handle(file, dataSetInformation, null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "For data column 'GM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC' "
                            + "following data sets have already been registered: [ds1]", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testTimeSeries()
    {
        DataSetHandler handler = createHandler();
        File file = createDataExample();
        final Experiment experiment = createExperiment("GM_BR_B1");
        prepareGetOrCreateDataSet(experiment, false);
        context.checking(new Expectations()
            {
                {
                    one(service).tryToGetExperiment(
                            new ExperimentIdentifier(PROJECT_CODE, "GM_BR_B1"));
                    will(returnValue(experiment));
                    one(dao).listDataSetsByTimeSeriesDataColumnHeader(
                            new DataColumnHeader("GM::BR::B1::200::EX::T1::CE::"
                                    + "MetaboliteLCMS::Value[mM]::Log10::NB::NC"));
                    MockDataSet<String> dataSets = new MockDataSet<String>();
                    will(returnValue(dataSets));
                    
                    one(dao).listDataSetsByTimeSeriesDataColumnHeader(
                            new DataColumnHeader("GM::BR::B1::+7200::EX::T2::CE::"
                                    + "b::Value[mM]::LIN::NB::NC"));
                    will(returnValue(dataSets));
                    
                    one(dao).getNextValueGroupId();
                    will(returnValue(VALUE_GROUP_ID1));
                    
                    one(dao).getNextValueGroupId();
                    will(returnValue(VALUE_GROUP_ID2));
                    
                    one(dao).insertTimeSeriesValues(with(DATA_SET_SPECIAL_ID), with("ID"),
                            with(Expectations.<List<TimeSeriesValue>> anything()));
                }
            });

        DataSetInformation dataSetInformation =
                createDataSetInformation(DataSetHandler.TIME_SERIES);
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE,
                "GM_BR_B1"));
        handler.handle(file, dataSetInformation, null);

        context.assertIsSatisfied();
    }

    
    @Test
    public void testGeneric()
    {
        DataSetHandler handler = createHandler();
        File file = createDataExample();
        final Experiment experiment = createExperiment("GM_BR_B1");
        prepareGetOrCreateDataSet(experiment, false);
        context.checking(new Expectations()
            {
                {
                    one(service).tryToGetExperiment(
                            new ExperimentIdentifier(PROJECT_CODE, "GM_BR_B1"));
                    will(returnValue(experiment));
                    one(dao).listDataSetsByTimeSeriesDataColumnHeader(
                            new DataColumnHeader("GM::BR::B1::200::EX::T1::CE::"
                                    + "MetaboliteLCMS::Value[mM]::Log10::NB::NC"));
                    MockDataSet<String> dataSets = new MockDataSet<String>();
                    will(returnValue(dataSets));
                    
                    one(dao).listDataSetsByTimeSeriesDataColumnHeader(
                            new DataColumnHeader("GM::BR::B1::+7200::EX::T2::CE::"
                                    + "b::Value[mM]::LIN::NB::NC"));
                    will(returnValue(dataSets));
                    
                    one(dao).getNextValueGroupId();
                    will(returnValue(VALUE_GROUP_ID1));
                    
                    one(dao).getNextValueGroupId();
                    will(returnValue(VALUE_GROUP_ID2));
                    
                    one(dao).insertTimeSeriesValues(with(DATA_SET_SPECIAL_ID), with("ID"),
                            with(Expectations.<List<TimeSeriesValue>> anything()));
                }
            });
        
        DataSetInformation dataSetInformation =
            createDataSetInformation("GENERIC");
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE,
                "GM_BR_B1"));
        handler.handle(file, dataSetInformation, null);
        
        context.assertIsSatisfied();
    }
    
    private void prepareGetOrCreateDataSet(final Experiment experiment, final boolean get)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetDataSetIDByPermID(DATA_SET_CODE);
                    if (get)
                    {
                        will(returnValue(DATA_SET_SPECIAL_ID));
                    } else
                    {
                        will(returnValue(null));
    
                        one(dao).createDataSet(DATA_SET_CODE, E_MAIL_ADDRESS, experiment);
                        will(returnValue(DATA_SET_SPECIAL_ID));
                    }
                }
            });
    }
    
    private DataSetHandler createHandler()
    {
        final Properties properties = new Properties();
        properties.setProperty(TimeSeriesDataSetUploaderParameters.DATA_SET_TYPE_PATTERN_FOR_DEFAULT_HANDLING, ".*");
        return new DataSetHandler(properties, dataSource, service)
            {
                @Override
                IDataSetUploader createUploader(DataSetInformation dataSetInformation)
                {
                    return factory.create(dataSetInformation, dao, service,
                            new TimeSeriesDataSetUploaderParameters(properties));
                }
            };
    }

    private File createDataExample()
    {
        TableBuilder builder =
                new TableBuilder("ID", "HumanReadable",
                        "GM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC",
                        "GM::BR::B1::+7200::EX::T2::CE::b::Value[mM]::LIN::NB::NC");
        builder.addRow("CHEBI:15721", "sedoheptulose 7-phosphate", "0.34", "0.799920281");
        builder.addRow("CHEBI:18211", "citrulline", "0.87", "1.203723714");
        File file = new File(workingDirectory, "data.txt");
        write(builder, file);
        return file;
    }

    private void write(TableBuilder builder, File file)
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(file);
            List<Column> columns = builder.getColumns();
            String delim = "";
            for (Column column : columns)
            {
                writer.print(delim);
                writer.print(column.getHeader());
                delim = "\t";
            }
            writer.println();
            int numberOfRows = columns.get(0).getValues().size();
            for (int i = 0; i < numberOfRows; i++)
            {
                delim = "";
                for (Column column : columns)
                {
                    writer.print(delim);
                    writer.print(column.getValues().get(i));
                    delim = "\t";
                }
                writer.println();
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private Experiment createExperiment(String code)
    {
        Experiment experiment = new Experiment();
        experiment.setId(EXP_ID);
        experiment.setCode(code);
        experiment.setPermId(EXP_PERM_ID);
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        Space space = new Space();
        space.setIdentifier(GROUP_CODE);
        project.setSpace(space);
        experiment.setProject(project);
        return experiment;
    }

    private DataSetInformation createDataSetInformation(String dataSetTypeCode)
    {
        DataSetInformation dataSetInformation = new DataSetInformation();
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(dataSetTypeCode);
        dataSetInformation.setDataSetType(dataSetType);
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        dataSetInformation.setDataSetProperties(Arrays.asList(new NewProperty(
                DatabaseFeeder.UPLOADER_EMAIL_KEY, E_MAIL_ADDRESS)));
        return dataSetInformation;
    }

}
