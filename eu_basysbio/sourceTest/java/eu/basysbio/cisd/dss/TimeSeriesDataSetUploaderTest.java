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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=TimeSeriesDataSetUploader.class)
public class TimeSeriesDataSetUploaderTest extends UploaderTestCase
{
    private static final String E_MAIL_ADDRESS = "e-mail";

    private static final String DATA_SET_CODE = "DS1";

    private static final long DATA_SET_SPECIAL_ID = 4711;
    
    private static final long VALUE_GROUP_ID1 = 42;
    private static final long VALUE_GROUP_ID2 = 43;


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
        properties.setProperty(TimeSeriesDataSetUploaderParameters.DATA_SET_TYPE_PATTERN_FOR_DEFAULT_HANDLING, ".*");
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
        final Experiment experiment = createExperiment("GM_BR_B1");
        prepareGetOrCreateDataSet(experiment, true);
        final String v1 =
                "TimeSeriesValue{identifier=CHEBI:15721,humanReadable=sedoheptulose 7-phosphate,"
                        + "bsbId=<null>,confidenceLevel=<null>,controlledGene=<null>,numberOfReplicates=<null>,"
                        + "value=0.125,rowIndex=0,columnIndex=2,valueGroupId=42,"
                        + "descriptor=TimeSeriesColumnDescriptor{"
                        + "valueGroupDescriptor=ValueGroupDescriptor{experimentType=GM,cultivationMethod=BR,"
                        + "biologicalReplicates=B1,timePoint=200,timePointType=EX,technicalReplicates=T1,"
                        + "cellLocation=CE,dataSetType=MetaboliteLCMS,biId=NB,controlledGene=NC,"
                        + "growthPhase=<null>,genotype=<null>},"
                        + "valueType=Value,unit=mM,scale=Log10}}";
        final String v2 =
                "TimeSeriesValue{identifier=CHEBI:18211,humanReadable=citrulline,"
                        + "bsbId=<null>,confidenceLevel=<null>,controlledGene=<null>,numberOfReplicates=<null>,"
                        + "value=0.625,rowIndex=1,columnIndex=2,valueGroupId=42,"
                        + "descriptor=TimeSeriesColumnDescriptor{"
                        + "valueGroupDescriptor=ValueGroupDescriptor{experimentType=GM,cultivationMethod=BR,"
                        + "biologicalReplicates=B1,timePoint=200,timePointType=EX,technicalReplicates=T1,"
                        + "cellLocation=CE,dataSetType=MetaboliteLCMS,biId=NB,controlledGene=NC,"
                        + "growthPhase=<null>,genotype=<null>},"
                        + "valueType=Value,unit=mM,scale=Log10}}";
        final String v3 =
                "TimeSeriesValue{identifier=CHEBI:15721,humanReadable=sedoheptulose 7-phosphate,"
                        + "bsbId=<null>,confidenceLevel=<null>,controlledGene=<null>,numberOfReplicates=<null>,"
                        + "value=0.75,rowIndex=0,columnIndex=3,valueGroupId=43,"
                        + "descriptor=TimeSeriesColumnDescriptor{"
                        + "valueGroupDescriptor=ValueGroupDescriptor{experimentType=GM,cultivationMethod=BR,"
                        + "biologicalReplicates=B1,timePoint=7200,timePointType=EX,technicalReplicates=T2,"
                        + "cellLocation=CE,dataSetType=b,biId=NB,controlledGene=NC,"
                        + "growthPhase=<null>,genotype=<null>},"
                        + "valueType=Value,unit=mM,scale=LIN}}";
        final String v4 =
                "TimeSeriesValue{identifier=CHEBI:18211,humanReadable=citrulline,"
                        + "bsbId=<null>,confidenceLevel=<null>,controlledGene=<null>,numberOfReplicates=<null>,"
                        + "value=1.25,rowIndex=1,columnIndex=3,valueGroupId=43,"
                        + "descriptor=TimeSeriesColumnDescriptor{"
                        + "valueGroupDescriptor=ValueGroupDescriptor{experimentType=GM,cultivationMethod=BR,"
                        + "biologicalReplicates=B1,timePoint=7200,timePointType=EX,technicalReplicates=T2,"
                        + "cellLocation=CE,dataSetType=b,biId=NB,controlledGene=NC,"
                        + "growthPhase=<null>,genotype=<null>},"
                        + "valueType=Value,unit=mM,scale=LIN}}";
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
                            with(new BaseMatcher<List<TimeSeriesValue>>()
                                {
                                    @SuppressWarnings("unchecked")
                                    public boolean matches(Object item)
                                    {
                                        List<TimeSeriesValue> values = (List<TimeSeriesValue>) item;
                                        assertEquals(v1, values.get(0).toString());
                                        assertEquals(v2, values.get(1).toString());
                                        assertEquals(v3, values.get(2).toString());
                                        assertEquals(v4, values.get(3).toString());
                                        assertEquals(4, values.size());
                                        return true;
                                    }

                                    public void describeTo(Description description)
                                    {
                                    }
                                }));
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


    private File createDataExample()
    {
        TableBuilder builder =
                new TableBuilder("ID", "HumanReadable",
                        "GM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC",
                        "GM::BR::B1::+7200::EX::T2::CE::b::Value[mM]::LIN::NB::NC");
        builder.addRow("CHEBI:15721", "sedoheptulose 7-phosphate", "0.125", "0.75");
        builder.addRow("CHEBI:18211", "citrulline", "0.625", "1.25");
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
