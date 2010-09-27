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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.Arrays;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleProviderTest extends AssertJUnit
{
    private static final class MatcherOfSampleCriteriaByChildID extends
            BaseMatcher<ListOrSearchSampleCriteria>
    {
        private final Long childID;

        private MatcherOfSampleCriteriaByChildID(Long childID)
        {
            this.childID = childID;
        }

        public boolean matches(Object item)
        {
            if (item instanceof ListOrSearchSampleCriteria)
            {
                ListOrSearchSampleCriteria criteria = (ListOrSearchSampleCriteria) item;
                assertEquals(true, criteria.isEnrichDependentSamplesWithProperties());
                assertEquals(childID, criteria.getChildSampleId().getId());
                return true;
            }
            return false;
        }

        public void describeTo(Description description)
        {
        }
    }

    private static final Principal PRINCIPAL =
            new Principal(CommonTestUtils.USER_ID, "john", "doe", "j@d");
    private static final String SESSION_TOKEN = "session-token";
    private static final Session SESSION =
            new Session(CommonTestUtils.USER_ID, SESSION_TOKEN, PRINCIPAL, "remote-host", 1);
    private static final TechId EXPERIMENT_ID = new TechId(4711);

    private Mockery context;
    private IBusinessObjectFactory boFactory;
    private ISampleProvider sampleProvider;
    private ISampleLister sampleLister;
    
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        boFactory = context.mock(IBusinessObjectFactory.class);
        sampleLister = context.mock(ISampleLister.class);
        sampleProvider = new SampleProvider(SESSION, boFactory);
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        final Sample s1 = createSample("abc");
        final Sample s2 = createSample("123");
        final Sample s3 = createSample("parent-of-123");
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));
                    
                    one(sampleLister).list(with(new BaseMatcher<ListOrSearchSampleCriteria>()
                        {
                            public boolean matches(Object item)
                            {
                                if (item instanceof ListOrSearchSampleCriteria)
                                {
                                    ListOrSearchSampleCriteria criteria = (ListOrSearchSampleCriteria) item;
                                    assertEquals(true, criteria.isEnrichDependentSamplesWithProperties());
                                    assertEquals(EXPERIMENT_ID, criteria.getExperimentId());
                                    return true;
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                            }
                        }));
                    will(returnValue(Arrays.asList(s1, s2)));
                    
                    one(sampleLister).list(with(new MatcherOfSampleCriteriaByChildID(s1.getId())));
                    will(returnValue(Arrays.asList()));
                    
                    one(sampleLister).list(with(new MatcherOfSampleCriteriaByChildID(s2.getId())));
                    will(returnValue(Arrays.asList(s3)));
                    
                    one(sampleLister).list(with(new MatcherOfSampleCriteriaByChildID(s3.getId())));
                    will(returnValue(Arrays.asList()));
                }
            });
        
        sampleProvider.loadByExperimentID(EXPERIMENT_ID);
        assertSame(s1, sampleProvider.getSample(s1.getPermId()));
        assertSame(s2, sampleProvider.getSample(s2.getPermId()));
        assertSame(s3, sampleProvider.getSample(s3.getPermId()));
        try
        {
            sampleProvider.getSample("42");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("No sample with following perm ID registered in openBIS: 42", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    private Sample createSample(String samplePermID){
        Sample sample = new Sample();
        sample.setId((long) samplePermID.hashCode());
        sample.setPermId(samplePermID);
        return sample;
    }
}
