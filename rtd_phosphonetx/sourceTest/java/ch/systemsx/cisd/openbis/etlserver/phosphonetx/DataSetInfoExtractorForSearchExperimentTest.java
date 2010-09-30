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

import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForSearchExperiment.EXPERIMENT_TYPE_CODE;
import static ch.systemsx.cisd.openbis.etlserver.phosphonetx.DataSetInfoExtractorForSearchExperiment.PARENT_DATA_SET_CODES;

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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=DataSetInfoExtractorForSearchExperiment.class)
public class DataSetInfoExtractorForSearchExperimentTest extends AbstractFileSystemTestCase
{
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
    public void testRegistrationWithOneMandatoryProperty()
    {
        FileUtilities.writeToFile(new File(dataSet,
                DataSetInfoExtractorForSearchExperiment.SEARCH_PROPERTIES),
                "answer=42\nblabla=blub\n" + PARENT_DATA_SET_CODES + "=1 2  3   4\n");
        prepare();

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
                                    assertEquals(EXPERIMENT_TYPE_CODE, experiment
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
        prepare();
        
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
    
    private void prepare()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).drawANewUniqueID();
                    will(returnValue(4711L));
                    
                    one(service).getExperimentType(EXPERIMENT_TYPE_CODE);
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
    
    private IDataSetInfoExtractor createExtractor(Properties properties)
    {
        return new DataSetInfoExtractorForSearchExperiment(properties, service);
    }
}
