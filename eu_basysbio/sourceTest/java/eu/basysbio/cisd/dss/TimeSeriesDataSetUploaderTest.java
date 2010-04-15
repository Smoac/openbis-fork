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
import java.util.ArrayList;
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
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=TimeSeriesDataSetUploader.class)
public class TimeSeriesDataSetUploaderTest extends AbstractFileSystemTestCase
{
    private final class MockDropBoxFeeder implements IDropBoxFeeder
    {
        private List<String> userEmails = new ArrayList<String>();
        private List<String> sampleCodes = new ArrayList<String>();
        private List<List<Column>> commonColumns = new ArrayList<List<Column>>();
        private List<Column> dataColumns = new ArrayList<Column>();

        public void feed(String email, String code, List<Column> columns, Column column)
        {
            userEmails.add(email);
            sampleCodes.add(code);
            commonColumns.add(columns);
            dataColumns.add(column);
        }
    }

    private static final String SAMPLE_EX_200 = "GM_BR_B1_EX_200";

    private static final String SAMPLE_EX_7200 = "GM_BR_B1_EX_7200";

    private static final String DATA_SET_CODE = "DS1";

    private static final long DATA_SET_SPECIAL_ID = 4711;

    private static final String GROUP_CODE = "/G1";

    private static final long EXP_ID = 42L;

    private static final String EXP_PERM_ID = "perm-" + EXP_ID;

    private static final long EXP_SPECIAL_ID = 2 * EXP_ID;

    private static final String PROJECT_CODE = "P1";

    private static final long ROW1_ID = 1;

    private static final long ROW2_ID = 2;
    
    private Mockery context;
    private ITimeSeriesDAO dao;
    private IEncapsulatedOpenBISService service;
    private IDataSetUploader uploader;
    private MockDropBoxFeeder feeder;
    private File dropBox;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(ITimeSeriesDAO.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        feeder = new MockDropBoxFeeder();
        dropBox = new File(workingDirectory, "drop-box");
        dropBox.mkdirs();
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox.toString());
        uploader =
                new TimeSeriesDataSetUploader(dao, service,
                        new TimeSeriesDataSetUploaderParameters(properties, true), false);
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
            uploader.upload(new File(workingDirectory, "example.txt"), info, feeder);
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
            uploader.upload(file, dataSetInformation, feeder);
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

                    one(service).listSamples(with(any(ListSampleCriteria.class)));
                    Sample sample200 = createSample(SAMPLE_EX_200);
                    will(returnValue(Arrays.<Sample> asList(sample200)));

                    one(service).listDataSetsBySampleID(sample200.getId(), true);
                    will(returnValue(Arrays
                            .asList(createData("ds0", "T0"), createData("ds1", "T1"))));
                }

            });

        DataSetInformation dataSetInformation =
                createDataSetInformation(DataSetHandler.TIME_SERIES);
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE,
                "GM_BR_B1"));
        try
        {
            uploader.upload(file, dataSetInformation, feeder);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "For data column 'GM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC' "
                            + "the data set 'ds1' has already been registered.", ex.getMessage());
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
        final Sample sample200 = createSample(SAMPLE_EX_200);
        long sample200SpecialID = prepareGetOrCreateSample(sample200, false);
        prepareCreateDataColumn("GM::BR::B1::200::EX::T1::CE::"
                + "MetaboliteLCMS::Value[mM]::Log10::NB::NC", sample200SpecialID, 0.34, 0.87);
        final Sample sample7200 = createSample(SAMPLE_EX_7200);
        long sample7200SpecialID = prepareGetOrCreateSample(sample7200, true);
        prepareCreateDataColumn("GM::BR::B1::+7200::EX::T2::CE::" + "b::Value[mM]::LIN::NB::NC",
                sample7200SpecialID, 0.799920281, 1.203723714);
        context.checking(new Expectations()
            {
                {
                    one(service).tryToGetExperiment(
                            new ExperimentIdentifier(PROJECT_CODE, "GM_BR_B1"));
                    will(returnValue(createExperiment("GM_BR_B1")));

                    exactly(2).of(service).listSamples(with(new BaseMatcher<ListSampleCriteria>()
                        {
                            private TechId experimentId;

                            public void describeTo(Description description)
                            {
                                description.appendText("Experiment ID: expected: " + EXP_ID
                                        + ", actual: " + experimentId);
                            }

                            public boolean matches(Object item)
                            {
                                if (item instanceof ListSampleCriteria)
                                {
                                    experimentId = ((ListSampleCriteria) item).getExperimentId();
                                    return experimentId.getId().equals(EXP_ID);
                                }
                                return false;
                            }
                        }));
                    will(returnValue(Arrays.<Sample> asList(sample200)));

                    one(service).listDataSetsBySampleID(sample200.getId(), true);
                    will(returnValue(Arrays.asList()));

                    final NewSample sample = createNewSample(SAMPLE_EX_7200);
                    one(service).registerSample(with(new BaseMatcher<NewSample>()
                        {
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewSample)
                                {
                                    NewSample actualSample = (NewSample) item;
                                    assertEquals(sample.getIdentifier(), actualSample
                                            .getIdentifier());
                                    IEntityProperty[] p = actualSample.getProperties();
                                    assertEquals(1, p.length);
                                    assertEquals("7200", p[0].getValue());
                                    assertEquals("TIME_POINT", p[0].getPropertyType().getCode());
                                    return true;
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                                description.appendValue(sample);
                            }
                        }), with((String) null));
                    will(returnValue(sample7200.getId()));

                    one(service).listDataSetsBySampleID(sample7200.getId(), true);
                    will(returnValue(Arrays.asList()));
                }
            });

        DataSetInformation dataSetInformation =
                createDataSetInformation(DataSetHandler.TIME_SERIES);
        dataSetInformation.setUploadingUserEmail("ab@c.de");
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE,
                "GM_BR_B1"));
        uploader.upload(file, dataSetInformation, feeder);
        
        assertEquals("[ab@c.de, ab@c.de]", feeder.userEmails.toString());
        assertEquals(SAMPLE_EX_200, feeder.sampleCodes.get(0));
        List<Column> commonColumns = feeder.commonColumns.get(0);
        assertEquals(2, commonColumns.size());
        assertEquals("ID", commonColumns.get(0).getHeader());
        assertEquals("[CHEBI:15721, CHEBI:18211]", commonColumns.get(0).getValues().toString());
        assertEquals("HumanReadable", commonColumns.get(1).getHeader());
        assertEquals("[sedoheptulose 7-phosphate, citrulline]", commonColumns.get(1).getValues().toString());
        Column dataColumn = feeder.dataColumns.get(0);
        assertEquals("GM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC", dataColumn.getHeader());
        assertEquals("[0.34, 0.87]", dataColumn.getValues().toString());
        
        assertEquals(SAMPLE_EX_7200, feeder.sampleCodes.get(1));
        commonColumns = feeder.commonColumns.get(1);
        assertEquals(2, commonColumns.size());
        assertEquals("ID", commonColumns.get(0).getHeader());
        assertEquals("[CHEBI:15721, CHEBI:18211]", commonColumns.get(0).getValues().toString());
        assertEquals("HumanReadable", commonColumns.get(1).getHeader());
        assertEquals("[sedoheptulose 7-phosphate, citrulline]", commonColumns.get(1).getValues().toString());
        dataColumn = feeder.dataColumns.get(1);
        assertEquals("GM::BR::B1::+7200::EX::T2::CE::b::Value[mM]::LIN::NB::NC", dataColumn.getHeader());
        assertEquals("[0.799920281, 1.203723714]", dataColumn.getValues().toString());

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
    
    private long prepareGetOrCreateSample(final Sample sample, final boolean get)
    {
        final long id = sample.getPermId().length();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetSampleWithExperiment(
                            SampleIdentifierFactory.parse(sample.getIdentifier()));
                    will(returnValue(sample));
    
                    one(dao).tryToGetSampleIDByPermID(sample.getPermId());
                    if (get)
                    {
                        will(returnValue(id));
                    } else
                    {
                        will(returnValue(null));
                        one(dao).createSample(sample.getPermId());
                        will(returnValue(id));
                    }
                }
            });
        return id;
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
    
    private void prepareCreateDataColumn(final String columnHeader, final long sampleID,
            final Double dataOfRow1, final Double dataOfRow2)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).createDataColumn(new DataColumnHeader(columnHeader),
                            DATA_SET_SPECIAL_ID, sampleID);
                    long id = columnHeader.hashCode();
                    will(returnValue(id));
    
                    one(dao).createDataValue(id, ROW1_ID, dataOfRow1);
                    one(dao).createDataValue(id, ROW2_ID, dataOfRow2);
                }
            });
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

    private NewSample createNewSample(String sampleCode)
    {
        NewSample sample = new NewSample();
        sample.setContainerIdentifier(sampleCode);
        sample.setIdentifier(GROUP_CODE + "/" + sampleCode);
        return sample;
    }

    private Sample createSample(String sampleCode)
    {
        Sample sample = new Sample();
        sample.setCode(sampleCode);
        sample.setPermId("perm-" + sampleCode);
        sample.setId(new Long(sampleCode.length()));
        sample.setIdentifier(GROUP_CODE + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR
                + sampleCode);
        return sample;
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

    private ExternalData createData(String code, String technicalReplicateCode)
    {
        ExternalData externalData = new ExternalData();
        externalData.setCode(code);
        EntityProperty tr =
                createProperty(TimePointPropertyType.TECHNICAL_REPLICATE_CODE,
                        technicalReplicateCode);
        EntityProperty biID = createProperty(TimePointPropertyType.BI_ID, "NB");
        EntityProperty cg = createProperty(TimePointPropertyType.CG, "NC");
        EntityProperty vt = createProperty(TimePointPropertyType.VALUE_TYPE, "Value[mM]");
        EntityProperty scale = createProperty(TimePointPropertyType.SCALE, "LOG10");
        EntityProperty celLoc = createProperty(TimePointPropertyType.CEL_LOC, "CE");
        EntityProperty t =
                createProperty(TimePointPropertyType.TIME_SERIES_DATA_SET_TYPE, "MetaboliteLCMS");
        externalData.setDataSetProperties(Arrays.<IEntityProperty> asList(tr, biID, cg, vt, scale,
                celLoc, t));
        return externalData;
    }

    private EntityProperty createProperty(TimePointPropertyType type, String value)
    {
        return createProperty(type.toString(), value);
    }

    private EntityProperty createProperty(String code, String value)
    {
        EntityProperty entityProperty = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        entityProperty.setPropertyType(propertyType);
        entityProperty.setValue(value);
        return entityProperty;
    }
}
