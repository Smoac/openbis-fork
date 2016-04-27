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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import static ch.systemsx.cisd.openbis.etlserver.proteomics.DataSetInfoExtractorForProteinResults.DEFAULT_EXPERIMENT_TYPE_CODE;
import static ch.systemsx.cisd.openbis.etlserver.proteomics.DataSetInfoExtractorForProteinResults.EXPERIMENT_IDENTIFIER_KEY;
import static ch.systemsx.cisd.openbis.etlserver.proteomics.DataSetInfoExtractorForProteinResults.EXPERIMENT_PROPERTIES_FILE_NAME_KEY;
import static ch.systemsx.cisd.openbis.etlserver.proteomics.DataSetInfoExtractorForProteinResults.EXPERIMENT_TYPE_CODE_KEY;
import static ch.systemsx.cisd.openbis.etlserver.proteomics.DataSetInfoExtractorForProteinResults.PARENT_DATA_SET_CODES;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataSetInfoExtractorForProteinResults.class)
public class DataSetInfoExtractorForProteinResultsTest extends AbstractFileSystemTestCase
{
    private static final String PARENT_DATA_SET_CODES_KEY =
            DataSetInfoExtractorForProteinResults.PARENT_DATA_SET_CODES.toUpperCase();

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private File dataSet;

    private File protXmlFile;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dataSet = new File(workingDirectory, "space1&project1");
        dataSet.mkdirs();
        protXmlFile = new File(dataSet, "prot.xml");
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testWithNonDefaultExperimentTypeAndPropertiesFileName()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        String propertiesFile = "my.properties";
        FileUtilities.writeToFile(new File(dataSet, propertiesFile), "answer=42\nblabla=blub\n"
                + EXPERIMENT_IDENTIFIER_KEY + "= /TEST/PROJECT/EXP_TO_BE_IGNORED\n"
                + PARENT_DATA_SET_CODES + "=1 2  3   4\n");
        Properties properties = new Properties();
        String experimentType = "MY_EXPERIMENT";
        properties.setProperty(EXPERIMENT_TYPE_CODE_KEY, experimentType);
        properties.setProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY, propertiesFile);
        prepareGetDataSet("1");
        prepareGetDataSet("2");
        prepareGetDataSet("3");
        prepareGetDataSet("4");
        prepare(experimentType);
        context.checking(new Expectations()
            {
                {
                    one(service).registerExperiment(with(any(NewExperiment.class)));
                }
            });

        IDataSetInfoExtractor extractor = createExtractor(properties);

        DataSetInformation info = extractor.getDataSetInformation(dataSet, service);

        assertEquals("/SPACE1/PROJECT1/E4711", info.getExperimentIdentifier().toString());
        assertEquals("[1, 2, 3, 4]", info.getParentDataSetCodes().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithProvidedExperimentCode()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        String propertiesFile = "my.properties";
        FileUtilities.writeToFile(new File(dataSet, propertiesFile), "answer=42\nblabla=blub\n"
                + DataSetInfoExtractorForProteinResults.EXPERIMENT_CODE_KEY + "= MY_EXP1\n");
        Properties properties = new Properties();
        String experimentType = "MY_EXPERIMENT";
        properties.setProperty(EXPERIMENT_TYPE_CODE_KEY, experimentType);
        properties.setProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY, propertiesFile);
        prepare(experimentType, false);
        context.checking(new Expectations()
            {
                {
                    one(service).registerExperiment(with(any(NewExperiment.class)));
                }
            });

        IDataSetInfoExtractor extractor = createExtractor(properties);

        DataSetInformation info = extractor.getDataSetInformation(dataSet, service);

        assertEquals("/SPACE1/PROJECT1/MY_EXP1", info.getExperimentIdentifier().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegistrationWithOneMandatoryProperty()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        FileUtilities.writeToFile(new File(dataSet,
                DataSetInfoExtractorForProteinResults.DEFAULT_EXPERIMENT_PROPERTIES_FILE_NAME),
                "answer=42\nblabla=blub\n" + PARENT_DATA_SET_CODES + "=1 2  3   4\n");
        prepare(DEFAULT_EXPERIMENT_TYPE_CODE);
        prepareGetDataSet("1");
        prepareGetDataSet("2");
        prepareGetDataSet("3");
        prepareGetDataSet("4");

        context.checking(new Expectations()
            {
                {
                    one(service).registerExperiment(with(new BaseMatcher<NewExperiment>()
                        {
                            @Override
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewExperiment)
                                {
                                    NewExperiment experiment = (NewExperiment) item;
                                    assertEquals(DEFAULT_EXPERIMENT_TYPE_CODE,
                                            experiment.getExperimentTypeCode());
                                    IEntityProperty[] properties = experiment.getProperties();
                                    assertEquals(1, properties.length);
                                    assertEquals("answer", properties[0].getPropertyType()
                                            .getCode());
                                    assertEquals("42", properties[0].tryGetAsString());
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public void describeTo(Description description)
                            {
                            }
                        }));
                }
            });

        IDataSetInfoExtractor extractor = createExtractor(new Properties());
        DataSetInformation info = extractor.getDataSetInformation(dataSet, service);

        assertEquals("/SPACE1/PROJECT1/E4711", info.getExperimentIdentifier().toString());
        assertEquals("[1, 2, 3, 4]", info.getParentDataSetCodes().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegistrationWithMissingProtXmlFile()
    {
        prepare(DEFAULT_EXPERIMENT_TYPE_CODE);

        IDataSetInfoExtractor extractor = createExtractor(new Properties());
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No *prot.xml file found in data set '" + dataSet + "'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegistrationWithMissingMandatoryProperty()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        prepare(DEFAULT_EXPERIMENT_TYPE_CODE);

        IDataSetInfoExtractor extractor = createExtractor(new Properties());
        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("The following mandatory properties are missed: [answer]", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testWithParentDataSetsDefinedByBaseExperimentAndProtXmlFileTooLarge()
    {
        FileUtilities.writeToFile(protXmlFile, "abc");
        String propertiesFile = "my.properties";
        FileUtilities.writeToFile(new File(dataSet, propertiesFile), "answer=42\nblabla=blub\n"
                + EXPERIMENT_IDENTIFIER_KEY + "= /TEST/PROJECT/EXP1\n");
        Properties properties = new Properties();
        String experimentType = "MY_EXPERIMENT";
        properties.setProperty(EXPERIMENT_TYPE_CODE_KEY, experimentType);
        properties.setProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY, propertiesFile);
        properties.setProperty(DataSetInfoExtractorForProteinResults.PROT_XML_SIZE_THRESHOLD, "0");
        prepare(experimentType);
        final RecordingMatcher<NewExperiment> experimentMatcher = new RecordingMatcher<NewExperiment>();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetExperiment(
                            new ExperimentIdentifier(new ProjectIdentifier("TEST", "PROJECT"),
                                    "EXP1"));
                    Experiment experiment = new ExperimentBuilder().id(123789L).getExperiment();
                    will(returnValue(experiment));

                    one(service).listDataSetsByExperimentID(experiment.getId());
                    AbstractExternalData ds1 = new DataSetBuilder().code("ds1").getDataSet();
                    AbstractExternalData ds2 = new DataSetBuilder().code("ds2").getDataSet();
                    will(returnValue(Arrays.asList(ds1, ds2)));

                    one(service).registerExperiment(with(experimentMatcher));
                }
            });

        IDataSetInfoExtractor extractor = createExtractor(properties);

        DataSetInformation info = extractor.getDataSetInformation(dataSet, service);

        assertEquals("/SPACE1/PROJECT1/E4711", info.getExperimentIdentifier().toString());
        assertEquals("[ds1, ds2]", info.getParentDataSetCodes().toString());
        IEntityProperty[] expProps = experimentMatcher.recordedObject().getProperties();
        assertEquals("Size of prot.xml file prot.xml is with 3 bytes too large. Maximum size is 0 bytes",
                asMap(expProps).get(DataSetInfoExtractorForProteinResults.NOT_PROCESSED_PROPERTY).getValue());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithUnkownBaseExperiment()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        String propertiesFile = "my.properties";
        FileUtilities.writeToFile(new File(dataSet, propertiesFile), "answer=42\nblabla=blub\n"
                + EXPERIMENT_IDENTIFIER_KEY + "= /TEST/PROJECT/EXP1\n");
        Properties properties = new Properties();
        String experimentType = "MY_EXPERIMENT";
        properties.setProperty(EXPERIMENT_TYPE_CODE_KEY, experimentType);
        properties.setProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY, propertiesFile);
        prepare(experimentType);
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetExperiment(
                            new ExperimentIdentifier(new ProjectIdentifier("TEST", "PROJECT"),
                                    "EXP1"));
                    will(returnValue(null));

                }
            });

        IDataSetInfoExtractor extractor = createExtractor(properties);

        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Unkown experiment /TEST/PROJECT/EXP1", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testWithUnkownParentDataSets()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        String propertiesFile = "my.properties";
        FileUtilities.writeToFile(new File(dataSet, propertiesFile), "answer=42\nblabla=blub\n"
                + EXPERIMENT_IDENTIFIER_KEY + "= /TEST/PROJECT/EXP1\n" + PARENT_DATA_SET_CODES_KEY
                + " = ds1 ds2, ds3");
        Properties properties = new Properties();
        String experimentType = "MY_EXPERIMENT";
        properties.setProperty(EXPERIMENT_TYPE_CODE_KEY, experimentType);
        properties.setProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY, propertiesFile);
        prepare(experimentType);
        prepareGetDataSet("ds1");
        prepareGetDataSet("ds2", null);
        prepareGetDataSet("ds3", null);

        IDataSetInfoExtractor extractor = createExtractor(properties);

        try
        {
            extractor.getDataSetInformation(dataSet, service);
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown data sets: ds2, ds3", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testWithParentDataSetsSeparatedBySpaces()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        String propertiesFile = "my.properties";
        FileUtilities.writeToFile(new File(dataSet, propertiesFile), "answer=42\nblabla=blub\n"
                + EXPERIMENT_IDENTIFIER_KEY + "= /TEST/PROJECT/EXP1\n" + PARENT_DATA_SET_CODES_KEY
                + " = ds1     ds2");
        Properties properties = new Properties();
        String experimentType = "MY_EXPERIMENT";
        properties.setProperty(EXPERIMENT_TYPE_CODE_KEY, experimentType);
        properties.setProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY, propertiesFile);
        prepare(experimentType);
        prepareGetDataSet("ds1");
        prepareGetDataSet("ds2");
        context.checking(new Expectations()
            {
                {
                    one(service).registerExperiment(with(any(NewExperiment.class)));
                }
            });

        IDataSetInfoExtractor extractor = createExtractor(properties);

        DataSetInformation info = extractor.getDataSetInformation(dataSet, service);

        assertEquals("/SPACE1/PROJECT1/E4711", info.getExperimentIdentifier().toString());
        assertEquals("[ds1, ds2]", info.getParentDataSetCodes().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithParentDataSetsSeparatedByComma()
    {
        FileUtilities.writeToFile(protXmlFile, "");
        String propertiesFile = "my.properties";
        FileUtilities.writeToFile(new File(dataSet, propertiesFile), "answer=42\nblabla=blub\n"
                + PARENT_DATA_SET_CODES_KEY + " = ds1,ds2");
        Properties properties = new Properties();
        String experimentType = "MY_EXPERIMENT";
        properties.setProperty(EXPERIMENT_TYPE_CODE_KEY, experimentType);
        properties.setProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY, propertiesFile);
        prepare(experimentType);
        prepareGetDataSet("ds1");
        prepareGetDataSet("ds2");
        context.checking(new Expectations()
            {
                {
                    one(service).registerExperiment(with(any(NewExperiment.class)));
                }
            });

        IDataSetInfoExtractor extractor = createExtractor(properties);

        DataSetInformation info = extractor.getDataSetInformation(dataSet, service);

        assertEquals("/SPACE1/PROJECT1/E4711", info.getExperimentIdentifier().toString());
        assertEquals("[ds1, ds2]", info.getParentDataSetCodes().toString());
        context.assertIsSatisfied();
    }

    private void prepare(final String experimentType)
    {
        prepare(experimentType, true);
    }

    private void prepare(final String experimentType, final boolean experimentCodeGenerated)
    {
        context.checking(new Expectations()
            {
                {
                    if (experimentCodeGenerated)
                    {
                        one(service).generateCodes("E", EntityKind.EXPERIMENT, 1);
                        will(returnValue(Collections.singletonList("E4711")));
                    }

                    one(service).getExperimentType(experimentType);
                    ExperimentType type = new ExperimentType();
                    ExperimentTypePropertyType etpt1 = new ExperimentTypePropertyType();
                    PropertyType propertyType1 = new PropertyType();
                    propertyType1.setCode("answer");
                    etpt1.setPropertyType(propertyType1);
                    etpt1.setMandatory(true);
                    ExperimentTypePropertyType etpt2 = new ExperimentTypePropertyType();
                    PropertyType propertyType2 = new PropertyType();
                    propertyType2.setCode(DataSetInfoExtractorForProteinResults.NOT_PROCESSED_PROPERTY);
                    etpt2.setPropertyType(propertyType2);
                    type.setExperimentTypePropertyTypes(Arrays.asList(etpt1, etpt2));
                    will(returnValue(type));
                }
            });
    }

    private void prepareGetDataSet(final String dataSetCode)
    {
        prepareGetDataSet(dataSetCode, new DataSetBuilder().code(dataSetCode).getDataSet());
    }

    private void prepareGetDataSet(final String dataSetCode, final AbstractExternalData data)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(dataSetCode);
                    will(returnValue(data));
                }
            });
    }

    private Map<String, IEntityProperty> asMap(IEntityProperty[] properties)
    {
        Map<String, IEntityProperty> result = new HashMap<String, IEntityProperty>();
        for (IEntityProperty property : properties)
        {
            result.put(property.getPropertyType().getCode(), property);
        }
        return result;
    }

    private IDataSetInfoExtractor createExtractor(Properties properties)
    {
        return new DataSetInfoExtractorForProteinResults(properties, service);
    }
}
