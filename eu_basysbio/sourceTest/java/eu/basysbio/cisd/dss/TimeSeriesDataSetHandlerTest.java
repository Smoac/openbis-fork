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
import static eu.basysbio.cisd.dss.TimeSeriesDataSetHandler.DATA_FILE_TYPE;
import static eu.basysbio.cisd.dss.TimeSeriesDataSetHandler.DATA_SET_PROPERTIES_FILE_NAME_KEY;
import static eu.basysbio.cisd.dss.TimeSeriesDataSetHandler.TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY;
import static eu.basysbio.cisd.dss.TimeSeriesDataSetHandler.TRANSLATION_KEY;

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

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.Constants;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TimeSeriesDataSetHandlerTest extends AbstractFileSystemTestCase
{

    private static final String DATA_SET_PROPERTIES_FILE = "data-set-properties.txt";
    private static final String SAMPLE_EX_200 = "EX_200";
    private static final String SAMPLE_EX_7200 = "EX_7200";
    private static final String GROUP_CODE = "G1";
    private static final long EXP_ID = 42L;
    private static final String PROJECT_CODE = "p1";
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private File dropBox;


    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        super.setUp();
        LogInitializer.init();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
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
    public void testMissingDropBoxProperty()
    {
        try
        {
            new TimeSeriesDataSetHandler(new Properties(), service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY
                    + "' not found in properties '[]'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingDataSetProertiesFileNameProperty()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY, dropBox.getAbsolutePath());
            new TimeSeriesDataSetHandler(properties, service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals(
                    "Given key '" + DATA_SET_PROPERTIES_FILE_NAME_KEY
                            + "' not found in properties '["
                            + TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY + "]'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingDataSetTypesProperty()
    {
        try
        {
            Properties properties = new Properties();
            properties.setProperty(TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY, dropBox.getAbsolutePath());
            properties.setProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY, "p.txt");
            new TimeSeriesDataSetHandler(properties, service);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            AssertionUtil.assertContains("Given key '" + DATA_SET_TYPES_KEY + "'", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingExperiment() throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty(TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY, dropBox.getAbsolutePath());
        properties.setProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY, DATA_SET_PROPERTIES_FILE);
        properties.setProperty(TRANSLATION_KEY + DATA_SET_TYPES_KEY, "a, b");
        properties.setProperty(TRANSLATION_KEY + "a", "Alpha");
        TimeSeriesDataSetHandler handler = new TimeSeriesDataSetHandler(properties, service);
        
        File file = createDataExample();

        try
        {
            handler.handle(file, new DataSetInformation());
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Data set should be registered for an experiment and not for a sample.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void test()
    {
        Properties properties = new Properties();
        properties.setProperty(TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY, dropBox.getAbsolutePath());
        properties.setProperty(DATA_SET_PROPERTIES_FILE_NAME_KEY, DATA_SET_PROPERTIES_FILE);
        properties.setProperty(TRANSLATION_KEY + DATA_SET_TYPES_KEY, "MetaboliteLCMS, b");
        properties.setProperty(TRANSLATION_KEY + "MetaboliteLCMS", "METABOLITE_LCMS");
        TimeSeriesDataSetHandler handler = new TimeSeriesDataSetHandler(properties, service);
        File file = createDataExample();
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(service).tryToGetExperiment(new ExperimentIdentifier(PROJECT_CODE, "GM_BR_B1"));
                    will(returnValue(createExperiment("GM_BR_B1")));
                    
                    exactly(2).of(service).listSamples(with(new BaseMatcher<ListSampleCriteria>()
                        {
                            private TechId experimentId;

                            public void describeTo(Description description)
                            {
                                description.appendText("Experiment ID: expected: " + EXP_ID + ", actual: " + experimentId);
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
                    will(returnValue(Arrays.<Sample>asList(createSample(SAMPLE_EX_200))));
                    
                    NewSample sample = createNewSample(SAMPLE_EX_7200);
                    one(service).registerSample(sample);
                }
            });
        
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier(PROJECT_CODE, "exp1"));
        handler.handle(file, dataSetInformation);
        
        File dataSet = new File(dropBox, SAMPLE_EX_200);
        assertEquals(true, dataSet.isDirectory());
        File dataFile = new File(dataSet, "METABOLITE_LCMS" + DATA_FILE_TYPE);
        List<String> data = FileUtilities.loadToStringList(dataFile);
        assertEquals("ID\tHumanReadable\tGM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC", data.get(0));
        assertEquals("CHEBI:15721\tsedoheptulose 7-phosphate\t0.34", data.get(1));
        assertEquals("CHEBI:18211\tcitrulline\t0.87", data.get(2));
        assertEquals(3, data.size());
        File dataSetPropertiesFile = new File(dataSet, DATA_SET_PROPERTIES_FILE);
        List<String> dataSetProperties = FileUtilities.loadToStringList(dataSetPropertiesFile);
        assertEquals("property\tvalue", dataSetProperties.get(0));
        assertEquals("TECHNICAL_REPLICATE_CODE\tT1", dataSetProperties.get(1));
        assertEquals("CEL_LOC\tCE", dataSetProperties.get(2));
        assertEquals("VALUE_TYPE\tValue[mM]", dataSetProperties.get(3));
        assertEquals("SCALE\tLog10", dataSetProperties.get(4));
        assertEquals("BI_ID\tNB", dataSetProperties.get(5));
        assertEquals("CG\tNC", dataSetProperties.get(6));
        assertEquals(7, dataSetProperties.size());
        File markerFile = new File(dropBox, Constants.IS_FINISHED_PREFIX + SAMPLE_EX_200);
        assertEquals(true, markerFile.exists());
        
        dataSet = new File(dropBox, SAMPLE_EX_7200);
        assertEquals(true, dataSet.isDirectory());
        dataFile = new File(dataSet, "B" + DATA_FILE_TYPE);
        data = FileUtilities.loadToStringList(dataFile);
        assertEquals("ID\tHumanReadable\tGM::BR::B1::7200::EX::T2::CE::b::Value[mM]::LIN::NB::NC", data.get(0));
        assertEquals("CHEBI:15721\tsedoheptulose 7-phosphate\t0.799920281", data.get(1));
        assertEquals("CHEBI:18211\tcitrulline\t1.203723714", data.get(2));
        assertEquals(3, data.size());
        dataSetPropertiesFile = new File(dataSet, DATA_SET_PROPERTIES_FILE);
        dataSetProperties = FileUtilities.loadToStringList(dataSetPropertiesFile);
        assertEquals("property\tvalue", dataSetProperties.get(0));
        assertEquals("TECHNICAL_REPLICATE_CODE\tT2", dataSetProperties.get(1));
        assertEquals("CEL_LOC\tCE", dataSetProperties.get(2));
        assertEquals("VALUE_TYPE\tValue[mM]", dataSetProperties.get(3));
        assertEquals("SCALE\tLIN", dataSetProperties.get(4));
        assertEquals("BI_ID\tNB", dataSetProperties.get(5));
        assertEquals("CG\tNC", dataSetProperties.get(6));
        assertEquals(7, dataSetProperties.size());
        markerFile = new File(dropBox, Constants.IS_FINISHED_PREFIX + SAMPLE_EX_7200);
        assertEquals(true, markerFile.exists());
        
        context.assertIsSatisfied();
    }

    private File createDataExample()
    {
        TableBuilder builder =
                new TableBuilder("ID", "HumanReadable",
                        "GM::BR::B1::200::EX::T1::CE::MetaboliteLCMS::Value[mM]::Log10::NB::NC",
                        "GM::BR::B1::7200::EX::T2::CE::b::Value[mM]::LIN::NB::NC");
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
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        Group group = new Group();
        group.setIdentifier(GROUP_CODE);
        project.setGroup(group);
        experiment.setProject(project);
        return experiment;
    }

    private NewSample createNewSample(String sampleCode)
    {
        NewSample sample = new NewSample();
        sample.setIdentifier(GROUP_CODE + "/" + sampleCode);
        return sample;
    }
    
    private Sample createSample(String sampleCode)
    {
        Sample sample = new Sample();
        sample.setCode(sampleCode);
        return sample;
    }
}
