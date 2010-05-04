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

import static eu.basysbio.cisd.dss.DataSetTypeTranslator.DATA_SET_TYPES_KEY;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

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
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyTypeWithVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataSetHandler.class)
public class TimeSeriesDataSetHandlerTest extends AbstractFileSystemTestCase
{

    private static final String DATA_SET_PROPERTIES_FILE = "data-set-properties.txt";

    private static final String SAMPLE_EX_200 = "GM_BR_B1_EX_200";

    private static final String DATA_SET_EX_200 =
            SAMPLE_EX_200 + ".T1.CE.MetaboliteLCMS.Value[mM].Log10.NB.NC";

    private static final String SAMPLE_EX_7200 = "GM_BR_B1_EX_7200";

    private static final String DATA_SET_EX_7200 = SAMPLE_EX_7200 + ".T2.CE.b.Value[mM].LIN.NB.NC";

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
    public void testMissingTimeSeriesDropBoxProperty()
    {
        try
        {
            Properties properties = new Properties();
            createHandler(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '"
                    + TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH
                    + "' not found in properties '[]'", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingTimePointDropBoxProperty()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(
                    TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                    .getAbsolutePath());
            createHandler(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '"
                    + TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY
                    + "' not found in properties '["
                    + TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH
                    + "]'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingDataSetPropertiesFileNameProperty()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(
                    TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                            .getAbsolutePath());
            properties.setProperty(
                    TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY, dropBox
                            .getAbsolutePath());
            createHandler(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            AssertionUtil.assertContains("Given key '"
                    + TimePointDataDropBoxFeeder.DATA_SET_PROPERTIES_FILE_NAME_KEY
                    + "' not found in properties", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingDataSetTypesProperty()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(
                    TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY, dropBox
                            .getAbsolutePath());
            properties.setProperty(
                    TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                            .getAbsolutePath());
            properties.setProperty(TimePointDataDropBoxFeeder.DATA_SET_PROPERTIES_FILE_NAME_KEY,
                    "p.txt");
            createHandler(properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            AssertionUtil.assertContains("Given key '" + DATA_SET_TYPES_KEY + "'", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testWrongDataSetType() throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                        .getAbsolutePath());
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                        .getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY,
                dropBox.getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.DATA_SET_PROPERTIES_FILE_NAME_KEY,
                DATA_SET_PROPERTIES_FILE);
        properties.setProperty(Util.TRANSLATION_KEY + DATA_SET_TYPES_KEY, "a, b");
        properties.setProperty(Util.TRANSLATION_KEY + "a", "Alpha");
        prepareDataSetPropertiesValidator("Alpha", "B");
        DataSetHandler handler = createHandler(properties);
        File file = createDataExample();
        DataSetInformation dataSetInformation = createDataSetInformation("BLABLA");
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE, "exp1"));

        try
        {
            handler.handle(file, dataSetInformation, null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data has to be uploaded for data set type "
                    + "[TIME_SERIES, LCA_MTP_PCAV_TIME_SERIES, LCA_MTP_TIME_SERIES, LCA_MIC_TIME_SERIES, LCA_MIC] "
                    + "instead of BLABLA.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingExperiment() throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                        .getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY,
                dropBox.getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.DATA_SET_PROPERTIES_FILE_NAME_KEY,
                DATA_SET_PROPERTIES_FILE);
        properties.setProperty(Util.TRANSLATION_KEY + DATA_SET_TYPES_KEY, "a, b");
        properties.setProperty(Util.TRANSLATION_KEY + "a", "Alpha");
        prepareDataSetPropertiesValidator("Alpha", "B");
        DataSetHandler handler = createHandler(properties);

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
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                        .getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY,
                dropBox.getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.DATA_SET_PROPERTIES_FILE_NAME_KEY,
                DATA_SET_PROPERTIES_FILE);
        properties.setProperty(Util.TRANSLATION_KEY + DATA_SET_TYPES_KEY, "MetaboliteLCMS");
        properties.setProperty(Util.TRANSLATION_KEY + "MetaboliteLCMS", "METABOLITE_LCMS");
        prepareDataSetPropertiesValidator("METABOLITE_LCMS");
        DataSetHandler handler = createHandler(properties);
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
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                        .getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY,
                dropBox.getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.DATA_SET_PROPERTIES_FILE_NAME_KEY,
                DATA_SET_PROPERTIES_FILE);
        properties.setProperty(Util.TRANSLATION_KEY + DATA_SET_TYPES_KEY, "b");
        prepareDataSetPropertiesValidator("B");
        DataSetHandler handler = createHandler(properties);
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
            handler.handle(file, dataSetInformation, null);
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
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                        .getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY,
                dropBox.getAbsolutePath());
        properties.setProperty(TimePointDataDropBoxFeeder.DATA_SET_PROPERTIES_FILE_NAME_KEY,
                DATA_SET_PROPERTIES_FILE);
        properties.setProperty(Util.TRANSLATION_KEY + DATA_SET_TYPES_KEY, "MetaboliteLCMS, b");
        properties.setProperty(Util.TRANSLATION_KEY + "MetaboliteLCMS", "METABOLITE_LCMS");
        prepareDataSetPropertiesValidator("METABOLITE_LCMS", "B");
        DataSetHandler handler = createHandler(properties);
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
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE,
                "GM_BR_B1"));
        handler.handle(file, dataSetInformation, null);

        File dataSet = new File(dropBox, DATA_SET_EX_200);
        assertEquals(true, dataSet.isDirectory());
        File dataFile =
                new File(dataSet, "METABOLITE_LCMS" + TimePointDataDropBoxFeeder.DATA_FILE_TYPE);
        List<String> data = FileUtilities.loadToStringList(dataFile);
        assertEquals(
                "ID\tHumanReadable\tGM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC",
                data.get(0));
        assertEquals("CHEBI:15721\tsedoheptulose 7-phosphate\t0.34", data.get(1));
        assertEquals("CHEBI:18211\tcitrulline\t0.87", data.get(2));
        assertEquals(3, data.size());
        File dataSetPropertiesFile = new File(dataSet, DATA_SET_PROPERTIES_FILE);
        List<String> dataSetProperties = FileUtilities.loadToStringList(dataSetPropertiesFile);
        Collections.sort(dataSetProperties);
        assertEquals(
                "[BI_ID\tNB, CEL_LOC\tCE, CG\tNC, SCALE\tLOG10, TECHNICAL_REPLICATE_CODE\tT1, "
                        + "TIME_SERIES_DATA_SET_TYPE\tMetaboliteLCMS, VALUE_TYPE\tValue[mM], "
                        + "property\tvalue]", dataSetProperties.toString());

        dataSet = new File(dropBox, DATA_SET_EX_7200);
        assertEquals(true, dataSet.isDirectory());
        dataFile = new File(dataSet, "B" + TimePointDataDropBoxFeeder.DATA_FILE_TYPE);
        data = FileUtilities.loadToStringList(dataFile);
        assertEquals("ID\tHumanReadable\tGM::BR::B1::+7200::EX::T2::CE::b::Value[mM]::LIN::NB::NC",
                data.get(0));
        assertEquals("CHEBI:15721\tsedoheptulose 7-phosphate\t0.799920281", data.get(1));
        assertEquals("CHEBI:18211\tcitrulline\t1.203723714", data.get(2));
        assertEquals(3, data.size());
        dataSetPropertiesFile = new File(dataSet, DATA_SET_PROPERTIES_FILE);
        dataSetProperties = FileUtilities.loadToStringList(dataSetPropertiesFile);
        Collections.sort(dataSetProperties);
        assertEquals("[BI_ID\tNB, CEL_LOC\tCE, CG\tNC, SCALE\tLIN, TECHNICAL_REPLICATE_CODE\tT2, "
                + "TIME_SERIES_DATA_SET_TYPE\tb, VALUE_TYPE\tValue[mM], " + "property\tvalue]",
                dataSetProperties.toString());

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

    private void prepareDataSetPropertiesValidator(final String... dataSetTypes)
    {
        context.checking(new Expectations()
            {
                {
                    for (String dataSetTypeCode : dataSetTypes)
                    {
                        one(service).getDataSetType(dataSetTypeCode);
                        DataSetTypeWithVocabularyTerms type = new DataSetTypeWithVocabularyTerms();
                        type.setDataSetType(new DataSetType(dataSetTypeCode));
                        for (TimePointPropertyType timePointPropertyType : TimePointPropertyType
                                .values())
                        {
                            PropertyTypeWithVocabulary propertyType =
                                    new PropertyTypeWithVocabulary();
                            propertyType.setCode(timePointPropertyType.toString());
                            if (timePointPropertyType.isVocabulary())
                            {
                                propertyType.setTerms(createTerms("CE", "LIN", "LOG10"));

                            }
                            type.addPropertyType(propertyType);

                        }
                        will(returnValue(type));
                    }
                }

                private Set<VocabularyTerm> createTerms(String... terms)
                {
                    LinkedHashSet<VocabularyTerm> set = new LinkedHashSet<VocabularyTerm>();
                    for (String term : terms)
                    {
                        set.add(createTerm(term));
                    }
                    return set;
                }

                private VocabularyTerm createTerm(String code)
                {
                    VocabularyTerm vocabularyTerm = new VocabularyTerm();
                    vocabularyTerm.setCode(code);
                    return vocabularyTerm;
                }
            });
    }

    private DataSetHandler createHandler(final Properties properties)
    {
        return new DataSetHandler(properties, dataSource, service)
            {
                @Override
                IDataSetUploader createUploader(DataSetInformation dataSetInformation)
                {
                    return factory.create(dataSetInformation, dao, service,
                            new TimeSeriesDataSetUploaderParameters(properties, true));
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
