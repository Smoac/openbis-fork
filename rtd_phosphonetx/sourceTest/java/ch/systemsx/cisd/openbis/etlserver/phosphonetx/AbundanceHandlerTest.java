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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Parameter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AbundanceHandlerTest extends AssertJUnit
{
    private static final GroupIdentifier GROUP_IDENTIFIER = new GroupIdentifier("MY-DB", "G1");
    private static final String PARAMETER_VALUE = "1234.5";
    private static final double ABUNDANCE = 1234.5;
    private static final String PARAMETER_NAME = "abc12";
    private static final SampleIdentifier SAMPLE_IDENTIFER =
            new SampleIdentifier(GROUP_IDENTIFIER, PARAMETER_NAME);
    private static final String SAMPLE_PERM_ID = "s12-34";
    private static final String EXPERIMENT_PERM_ID = "e12345-42";
    private static final long EXPERIMENT_ID = 42;
    private static final long SAMPLE_ID = 43;
    private static final long PROTEIN_ID = 4711;
    private static final String PROTEIN_NAME = "my protein";
    private static final ListSamplesByPropertyCriteria CRITERIA =
            new ListSamplesByPropertyCriteria(AbundanceHandler.MZXML_FILENAME, PARAMETER_NAME,
                    GROUP_IDENTIFIER.getSpaceCode(), null);

    private Mockery context;

    private IProtDAO dao;

    private IEncapsulatedOpenBISService service;

    private Experiment experiment;

    private AbundanceHandler handler;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(IProtDAO.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        experiment = new Experiment();
        experiment.setPermID(EXPERIMENT_PERM_ID);
        experiment.setId(EXPERIMENT_ID);
        handler = new AbundanceHandler(service, dao, GROUP_IDENTIFIER, experiment);
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testAddAbundanceValueWhichIsNotANumber()
    {
        prepareCreateSampleIdentifiedByCode();

        try
        {
            handler.addAbundancesToDatabase(createParameter("blabla"), PROTEIN_ID, PROTEIN_NAME);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Abundance of sample '" + PARAMETER_NAME + "' of protein '" + PROTEIN_NAME
                    + "' is not a number: blabla", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testAddTwoAbundanceValuesForASampleIdentifiedByCode()
    {
        prepareCreateSampleIdentifiedByCode();
        context.checking(new Expectations()
            {
                {
                    one(dao).createAbundance(PROTEIN_ID, SAMPLE_ID, ABUNDANCE);
                    one(dao).createAbundance(PROTEIN_ID, SAMPLE_ID, 1.5);
                }
            });

        handler.addAbundancesToDatabase(createParameter(PARAMETER_VALUE), PROTEIN_ID, PROTEIN_NAME);
        handler.addAbundancesToDatabase(createParameter("1.5"), PROTEIN_ID, PROTEIN_NAME);

        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddTwoAbundanceValuesForASampleIdentifiedByPropertyButNoSampleFound()
    {
        prepareCreateSampleIdentifiedByProperty(Arrays.<Sample>asList());

        try
        {
            handler.addAbundancesToDatabase(createParameter(PARAMETER_VALUE), PROTEIN_ID, PROTEIN_NAME);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Protein '" + PROTEIN_NAME
                    + "' has an abundance value for an unidentified sample: " + PARAMETER_NAME, e
                    .getMessage());
        }
        
        try
        {
            handler.addAbundancesToDatabase(createParameter(PARAMETER_VALUE), PROTEIN_ID, PROTEIN_NAME);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Protein '" + PROTEIN_NAME
                    + "' has an abundance value for an unidentified sample: " + PARAMETER_NAME, e
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testAddAbundanceValuesForASampleIdentifiedByPropertyButTwoSamplesFound()
    {
        Sample sample = new Sample();
        sample.setPermId(SAMPLE_PERM_ID);
        prepareCreateSampleIdentifiedByProperty(Arrays.<Sample> asList(sample, sample));

        try
        {
            handler.addAbundancesToDatabase(createParameter(PARAMETER_VALUE), PROTEIN_ID,
                    PROTEIN_NAME);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Protein '" + PROTEIN_NAME
                    + "' has an abundance value for a not uniquely specified sample "
                    + "(2 samples are found): " + PARAMETER_NAME, e.getMessage());
        }

        context.assertIsSatisfied();
    }
    
    @Test
    public void testAddAbundanceValuesForASampleIdentifiedByProperty()
    {
        Sample sample = new Sample();
        sample.setPermId(SAMPLE_PERM_ID);
        prepareCreateSampleIdentifiedByProperty(Arrays.<Sample>asList(sample));
        prepareCreateSample();
        context.checking(new Expectations()
        {
            {
                one(dao).createAbundance(PROTEIN_ID, SAMPLE_ID, ABUNDANCE);
            }
        });
        
        handler.addAbundancesToDatabase(createParameter(PARAMETER_VALUE), PROTEIN_ID, PROTEIN_NAME);
        
        context.assertIsSatisfied();
    }

    private Parameter createParameter(String value)
    {
        Parameter parameter = new Parameter();
        parameter.setName(PARAMETER_NAME);
        parameter.setValue(value);
        return parameter;
    }

    private void prepareCreateSampleIdentifiedByCode()
    {
        Sample sample = new Sample();
        sample.setPermId(SAMPLE_PERM_ID);
        prepareGetSample(sample);
        prepareCreateSample();
    }

    private void prepareCreateSample()
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).tryToGetSampleByPermID(SAMPLE_PERM_ID);
                    will(returnValue(null));

                    one(dao).createSample(EXPERIMENT_ID, SAMPLE_PERM_ID);
                    will(returnValue(SAMPLE_ID));
                }
            });
    }
    
    private void prepareCreateSampleIdentifiedByProperty(final List<Sample> samples)
    {
        prepareGetSample(null);
        context.checking(new Expectations()
            {
                {
                    one(service).listSamplesByCriteria(
                            with(new BaseMatcher<ListSamplesByPropertyCriteria>()
                                {
                                    public boolean matches(Object item)
                                    {
                                        return CRITERIA.toString().equals(item.toString());
                                    }

                                    public void describeTo(Description description)
                                    {
                                        description.appendValue(CRITERIA);

                                    }
                                }));
                    will(returnValue(samples));
                }
            });
    }
    
    private void prepareGetSample(final Sample sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetSampleWithExperiment(
                            with(new BaseMatcher<SampleIdentifier>()
                                {

                                    public boolean matches(Object item)
                                    {
                                        return SAMPLE_IDENTIFER.toString().equals(item.toString());
                                    }

                                    public void describeTo(Description description)
                                    {
                                        description.appendValue(SAMPLE_IDENTIFER);

                                    }
                                }));
                    will(returnValue(sample));
                }
            });
    }
}
