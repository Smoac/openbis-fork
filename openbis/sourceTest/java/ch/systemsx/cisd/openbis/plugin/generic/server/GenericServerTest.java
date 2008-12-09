/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Test cases for corresponding {@link GenericServer} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = GenericServer.class)
public final class GenericServerTest extends AbstractServerTestCase
{
    private IGenericBusinessObjectFactory genericBusinessObjectFactory;

    private final IGenericServer createServer()
    {
        return new GenericServer(sessionManager, daoFactory, genericBusinessObjectFactory);
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        genericBusinessObjectFactory = context.mock(IGenericBusinessObjectFactory.class);
    }

    @Test
    public final void testGetSampleInfo()
    {
        final Session session = prepareGetSession();
        final SampleIdentifier sampleIdentifier = CommonTestUtils.createSampleIdentifier();
        final SamplePE samplePE = CommonTestUtils.createSample();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier);

                    one(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(daoFactory).getSampleDAO();
                    will(returnValue(sampleDAO));

                    one(sampleDAO).listSamplesByGeneratedFrom(samplePE);
                    will(returnValue(SamplePE.EMPTY_LIST));
                }
            });

        final SampleGenerationDTO sampleGeneration =
                createServer().getSampleInfo(SESSION_TOKEN, sampleIdentifier);
        assertEquals(samplePE, sampleGeneration.getGenerator());
        assertEquals(0, sampleGeneration.getGenerated().length);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSample()
    {
        final Session session = prepareGetSession();
        final NewSample newSample = new NewSample();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(newSample);
                    one(sampleBO).save();

                }
            });
        createServer().registerSample(SESSION_TOKEN, newSample);
        context.assertIsSatisfied();
    }

}
