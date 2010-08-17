/*
 * Copyright 2010 ETH Zuerich, CISD
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
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=TimeSeriesDataSetUploader.class)
public class TimeSeriesDataSetUploaderTest extends UploaderTestCase
{
    private static final String DATA_SET_CODE = "DS1";

    private static final long DATA_SET_SPECIAL_ID = 4711;

    private static final long EXP_SPECIAL_ID = 2 * EXP_ID;

    private static final long ROW1_ID = 1;

    private static final long ROW2_ID = 2;
    
    private Mockery context;
    private ITimeSeriesDAO dao;
    private IEncapsulatedOpenBISService service;
    private IDataSetUploader uploader;
    private File dropBox;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(ITimeSeriesDAO.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        dropBox = new File(workingDirectory, "drop-box");
        dropBox.mkdirs();
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox.toString());
        uploader =
                new TimeSeriesDataSetUploader(dao, service,
                        new TimeSeriesDataSetUploaderParameters(properties));
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testUploadNotForExperiment()
    {
        DataSetInformation info = new DataSetInformation();
        try
        {
            uploader.upload(new File(workingDirectory, "example.txt"), info);
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
        File file = createDataExample();
        DataSetInformation dataSetInformation =
                createDataSetInformation(DataSetHandler.TIME_SERIES);
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE, "exp1"));

        try
        {
            uploader.upload(file, dataSetInformation);
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
        File file = createDataExample();
        prepareGetOrCreateDataSet(true);
        prepareCreateRows();
        prepareCreateColumn("ID", "CHEBI:15721", "CHEBI:18211");
        prepareCreateColumn("HumanReadable", "sedoheptulose 7-phosphate", "citrulline");
        context.checking(new Expectations()
            {
                {
                    one(service).tryToGetExperiment(
                            new ExperimentIdentifier(PROJECT_CODE, "GM_BR_B1"));
                    will(returnValue(createExperiment("GM_BR_B1")));

                    one(dao).listDataSetsByDataColumnHeader(
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
            uploader.upload(file, dataSetInformation);
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
    public void test()
    {
        File file = createDataExample();
        prepareGetOrCreateDataSet(false);
        prepareCreateRows();
        prepareCreateColumn("ID", "CHEBI:15721", "CHEBI:18211");
        prepareCreateColumn("HumanReadable", "sedoheptulose 7-phosphate", "citrulline");
        prepareCreateDataColumn("GM::BR::B1::200::EX::T1::CE::"
                + "MetaboliteLCMS::Value[mM]::Log10::NB::NC", 0.34, 0.87);
        prepareCreateDataColumn("GM::BR::B1::+7200::EX::T2::CE::" + "b::Value[mM]::LIN::NB::NC",
                0.799920281, 1.203723714);
        context.checking(new Expectations()
            {
                {
                    one(service).tryToGetExperiment(
                            new ExperimentIdentifier(PROJECT_CODE, "GM_BR_B1"));
                    will(returnValue(createExperiment("GM_BR_B1")));

                }
            });

        DataSetInformation dataSetInformation =
                createDataSetInformation(DataSetHandler.TIME_SERIES);
        dataSetInformation.setUploadingUserEmail("ab@c.de");
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE,
                "GM_BR_B1"));
        uploader.upload(file, dataSetInformation);
        
        context.assertIsSatisfied();
    }

    private void prepareCreateRows()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).createRow();
                    will(returnValue(ROW1_ID));
                    one(dao).createRow();
                    will(returnValue(ROW2_ID));
                }
            });
    }
    
    private void prepareGetOrCreateDataSet(final boolean get)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetExperimentIDByPermID(EXP_PERM_ID);
                    if (get)
                    {
                        will(returnValue(EXP_SPECIAL_ID));
                    } else
                    {
                        will(returnValue(null));
    
                        one(dao).createExperiment(EXP_PERM_ID);
                        will(returnValue(EXP_SPECIAL_ID));
                    }
    
                    one(dao).tryToGetDataSetIDByPermID(DATA_SET_CODE);
                    if (get)
                    {
                        will(returnValue(DATA_SET_SPECIAL_ID));
                    } else
                    {
                        will(returnValue(null));
    
                        one(dao).createDataSet(DATA_SET_CODE, EXP_SPECIAL_ID);
                        will(returnValue(DATA_SET_SPECIAL_ID));
                    }
                }
            });
    }
    
    private void prepareCreateColumn(final String columnHeader, final String dataOfRow1,
            final String dataOfRow2)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).createColumn(columnHeader, DATA_SET_SPECIAL_ID);
                    long id = columnHeader.hashCode();
                    will(returnValue(id));
    
                    one(dao).createValue(id, ROW1_ID, dataOfRow1);
                    one(dao).createValue(id, ROW2_ID, dataOfRow2);
                }
            });
    }
    
    private void prepareCreateDataColumn(final String columnHeader, final Double dataOfRow1,
            final Double dataOfRow2)
    {
        context.checking(new Expectations()
            {
                {
                    DataColumnHeader dataColumnHeader = new DataColumnHeader(columnHeader);
                    one(dao).listDataSetsByDataColumnHeader(dataColumnHeader);
                    will(returnValue(new MockDataSet<String>()));
                    
                    one(dao).createDataColumn(dataColumnHeader,
                            DATA_SET_SPECIAL_ID, null);
                    long id = columnHeader.hashCode();
                    will(returnValue(id));
    
                    one(dao).createDataValue(id, ROW1_ID, dataOfRow1);
                    one(dao).createDataValue(id, ROW2_ID, dataOfRow2);
                }
            });
    }
    
    private DataSetInformation createDataSetInformation(String dataSetTypeCode)
    {
        DataSetInformation dataSetInformation = new DataSetInformation();
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(dataSetTypeCode);
        dataSetInformation.setDataSetType(dataSetType);
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        return dataSetInformation;
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

}
