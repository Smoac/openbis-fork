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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;

/**
 * @author Franz-Josef Elmer
 */
public class SampleIDProviderTest extends AbstractServerTestCase
{
    private static final String PERM_ID = "abc-1";

    private static final Long ID = 42L;

    private SampleIDProvider sampleIDProvider;

    @BeforeMethod
    @Override
    public void setUp()
    {
        super.setUp();
        sampleIDProvider = new SampleIDProvider(sampleDAO);
    }

    @Test
    public void testGetUnkownSample()
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID(PERM_ID);
                    will(returnValue(null));
                }
            });

        try
        {
            sampleIDProvider.getSampleIDOrParentSampleID(PERM_ID);
            fail("UserFailureException expected.");
        } catch (UserFailureException e)
        {
            assertEquals("No sample found for permID " + PERM_ID, e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetSampleIDTwice()
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID(PERM_ID);
                    SamplePE sample = new SamplePE();
                    sample.setId(ID);
                    will(returnValue(sample));
                }
            });

        long s1 = sampleIDProvider.getSampleIDOrParentSampleID(PERM_ID);
        long s2 = sampleIDProvider.getSampleIDOrParentSampleID(PERM_ID);
        assertEquals(ID.longValue(), s1);
        assertEquals(ID.longValue(), s2);

        context.assertIsSatisfied();
    }

    @Test
    public void testGetParentSampleIDTwice()
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID(PERM_ID);
                    SamplePE parentSample = new SamplePE();
                    parentSample.setCode("s1");
                    parentSample.setId(ID);
                    SamplePE sample = new SamplePE();
                    sample.setCode("s1");
                    sample.setId(2 * ID);
                    RelationshipTypePE relationship = new RelationshipTypePE();
                    relationship.setCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
                    sample.addParentRelationship(new SampleRelationshipPE(parentSample, sample,
                            relationship));
                    will(returnValue(sample));
                }
            });

        long s1 = sampleIDProvider.getSampleIDOrParentSampleID(PERM_ID);
        long s2 = sampleIDProvider.getSampleIDOrParentSampleID(PERM_ID);
        assertEquals(ID.longValue(), s1);
        assertEquals(ID.longValue(), s2);

        context.assertIsSatisfied();
    }
}
