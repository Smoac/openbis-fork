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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForProteinResults.DEFAULT_EXPERIMENT_TYPE_CODE;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForProteinResults.EXPERIMENT_IDENTIFIER_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForProteinResults.EXPERIMENT_PROPERTIES_FILE_NAME_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForProteinResults.EXPERIMENT_TYPE_CODE_KEY;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForProteinResults.PARENT_DATA_SET_CODES;

import java.io.File;
import java.util.Arrays;
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
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=DataSetInfoExtractorForProteinResults.class)
public class DataSetInfoExtractorForProteinResultsTest extends AbstractFileSystemTestCase
{
    private static final String PARENT_DATA_SET_CODES_KEY =
            DataSetInfoExtractorForProteinResults.PARENT_DATA_SET_CODES.toUpperCase();

    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private File dataSet;
    
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dataSet = new File(workingDirectory, "space1&project1");
        dataSet.mkdirs();
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
    public void testRegistrationWithOneMandatoryProperty()
    {
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
                            public boolean matches(Object item)
                            {
                                if (item instanceof NewExperiment)
                                {
                                    NewExperiment experiment = (NewExperiment) item;
                                    assertEquals(DEFAULT_EXPERIMENT_TYPE_CODE, experiment
                                            .getExperimentTypeCode());
                                    IEntityProperty[] properties = experiment.getProperties();
                                    assertEquals(1, properties.length);
                                    assertEquals("answer", properties[0].getPropertyType()
                                            .getCode());
                                    assertEquals("42", properties[0].tryGetAsString());
                                    return true;
                                }
                                return false;
                            }

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
    public void testRegistrationWithMissingMandatoryProperty()
    {
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
    public void testWithParentDataSetsDefinedByBaseExperiment()
    {
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
                    one(service).tryToGetExperiment(
                            new ExperimentIdentifier(new ProjectIdentifier("TEST", "PROJECT"),
                                    "EXP1"));
                    Experiment experiment = new ExperimentBuilder().id(123789L).getExperiment();
                    will(returnValue(experiment));
                    
                    one(service).listDataSetsByExperimentID(experiment.getId());
                    ExternalData ds1 = new DataSetBuilder().code("ds1").getDataSet();
                    ExternalData ds2 = new DataSetBuilder().code("ds2").getDataSet();
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    
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
    public void testWithUnkownBaseExperiment()
    {
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
                    one(service).tryToGetExperiment(
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
        context.checking(new Expectations()
            {
                {
                    one(service).drawANewUniqueID();
                    will(returnValue(4711L));
                    
                    one(service).getExperimentType(experimentType);
                    ExperimentType type = new ExperimentType();
                    ExperimentTypePropertyType etpt = new ExperimentTypePropertyType();
                    PropertyType propertyType = new PropertyType();
                    propertyType.setCode("answer");
                    etpt.setPropertyType(propertyType);
                    etpt.setMandatory(true);
                    type.setExperimentTypePropertyTypes(Arrays.asList(etpt));
                    will(returnValue(type));
                }
            });
    }
    
    private void prepareGetDataSet(final String dataSetCode)
    {
        prepareGetDataSet(dataSetCode, new DataSetBuilder().code(dataSetCode).getDataSet());
    }
    
    private void prepareGetDataSet(final String dataSetCode, final ExternalData data)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(dataSetCode);
                    will(returnValue(data));
                }
            });
    }
    
    private IDataSetInfoExtractor createExtractor(Properties properties)
    {
        return new DataSetInfoExtractorForProteinResults(properties, service);
    }
}
