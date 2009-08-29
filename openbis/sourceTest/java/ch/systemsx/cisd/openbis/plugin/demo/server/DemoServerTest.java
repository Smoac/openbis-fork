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

package ch.systemsx.cisd.openbis.plugin.demo.server;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleParentWithDerivedDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.demo.shared.IDemoServer;

/**
 * Test cases for corresponding {@link DemoServer} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = DemoServer.class)
public final class DemoServerTest extends AbstractServerTestCase
{
    private IDemoBusinessObjectFactory demoBusinessObjectFactory;

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin;
    
    private final IDemoServer createServer()
    {
        return new DemoServer(sessionManager, daoFactory, demoBusinessObjectFactory,
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
        demoBusinessObjectFactory = context.mock(IDemoBusinessObjectFactory.class);
        dataSetTypeSlaveServerPlugin = context.mock(IDataSetTypeSlaveServerPlugin.class);
    }

    @Test
    public final void testGetSampleInfo()
    {
        prepareGetSession();
        final SampleIdentifier sampleIdentifier = CommonTestUtils.createSampleIdentifier();
        final SamplePE samplePE = CommonTestUtils.createSample();
        final SampleParentWithDerivedDTO sampleGenerationDTO = new SampleParentWithDerivedDTO();
        sampleGenerationDTO.setParent(samplePE);
        context.checking(new Expectations()
            {
                {
                    one(demoBusinessObjectFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier);

                    one(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(sampleTypeSlaveServerPlugin).getSampleInfo(SESSION, samplePE);
                    will(returnValue(sampleGenerationDTO));
                }
            });

        final SampleParentWithDerived sampleGeneration =
                createServer().getSampleInfo(SESSION_TOKEN, sampleIdentifier);
        assertEquals(samplePE.getCode(), sampleGeneration.getParent().getCode());
        assertEquals(0, sampleGeneration.getDerived().length);
        context.assertIsSatisfied();
    }
}
