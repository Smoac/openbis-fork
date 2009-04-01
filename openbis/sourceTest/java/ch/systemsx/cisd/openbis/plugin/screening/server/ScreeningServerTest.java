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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;

/**
 * Test cases for corresponding {@link ScreeningServer} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = ScreeningServer.class)
public final class ScreeningServerTest extends AbstractServerTestCase
{
    private IScreeningBusinessObjectFactory screeningBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;
    
    private final IScreeningServer createServer()
    {
        return new ScreeningServer(sessionManager, daoFactory, screeningBusinessObjectFactory,
                sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
    }

    //
    // AbstractServerTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
        screeningBusinessObjectFactory = context.mock(IScreeningBusinessObjectFactory.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
    }

    @Test
    public final void testGetSampleInfo()
    {
        prepareGetSession();
        final SampleIdentifier sampleIdentifier = CommonTestUtils.createSampleIdentifier();
        final SamplePE samplePE = CommonTestUtils.createSample();
        final SampleGenerationDTO sampleGenerationDTO = new SampleGenerationDTO();
        sampleGenerationDTO.setGenerator(samplePE);
        context.checking(new Expectations()
            {
                {
                    one(screeningBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier);

                    one(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(sampleTypeSlaveServerPlugin).getSampleInfo(SESSION, samplePE);
                    will(returnValue(sampleGenerationDTO));
                }
            });

        final SampleGenerationDTO sampleGeneration =
                createServer().getSampleInfo(SESSION_TOKEN, sampleIdentifier);
        assertEquals(samplePE, sampleGeneration.getGenerator());
        assertEquals(0, sampleGeneration.getGenerated().length);
        context.assertIsSatisfied();
    }
}
