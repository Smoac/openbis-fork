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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.ISampleTypeSlaveServerPlugin;
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

    private ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin;

    private final IGenericServer createServer()
    {
        return new GenericServer(sessionManager, daoFactory, genericBusinessObjectFactory,
                sampleTypeSlaveServerPlugin);
    }

    private final NewSample createNewSample(final String identifier)
    {
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(identifier);
        return newSample;
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
        sampleTypeSlaveServerPlugin = context.mock(ISampleTypeSlaveServerPlugin.class);
    }

    @Test
    public final void testGetSampleInfo()
    {
        final Session session = prepareGetSession();
        final SampleIdentifier sampleIdentifier = CommonTestUtils.createSampleIdentifier();
        final SamplePE samplePE = CommonTestUtils.createSample();
        final SampleGenerationDTO sampleGenerationDTO = new SampleGenerationDTO();
        sampleGenerationDTO.setGenerator(samplePE);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier);

                    one(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(sampleTypeSlaveServerPlugin).getSampleInfo(session, samplePE);
                    will(returnValue(sampleGenerationDTO));
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

    @Test
    public void testGetExperimentInfo() throws Exception
    {
        final Session session = prepareGetSession();
        final ExperimentIdentifier experimentIdentifier =
                CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE experimentPE = CommonTestUtils.createExperiment(experimentIdentifier);
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).loadByExperimentIdentifier(experimentIdentifier);
                    one(experimentBO).enrichWithProperties();
                    one(experimentBO).enrichWithAttachments();

                    one(experimentBO).getExperiment();
                    will(returnValue(experimentPE));
                }
            });
        assertEquals(experimentPE, createServer().getExperimentInfo(SESSION_TOKEN,
                experimentIdentifier));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetExperimentFileAttachment() throws Exception
    {
        final Session session = prepareGetSession();
        final ExperimentIdentifier experimentIdentifier =
                CommonTestUtils.createExperimentIdentifier();
        final AttachmentPE attachmentPE = CommonTestUtils.createAttachment();
        context.checking(new Expectations()
            {
                {
                    one(genericBusinessObjectFactory).createExperimentBO(session);
                    will(returnValue(experimentBO));

                    one(experimentBO).loadByExperimentIdentifier(experimentIdentifier);

                    one(experimentBO).getExperimentFileAttachment(attachmentPE.getFileName(),
                            attachmentPE.getVersion());
                    will(returnValue(attachmentPE));

                }
            });
        assertEquals(attachmentPE, createServer().getExperimentFileAttachment(
                session.getSessionToken(), experimentIdentifier, attachmentPE.getFileName(),
                attachmentPE.getVersion()));
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamplesWithoutExpectations()
    {
        prepareGetSession();
        final IGenericServer server = createServer();
        // Null values
        boolean fail = true;
        try
        {
            server.registerSamples(null, null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        // Empty collection
        server
                .registerSamples(SESSION_TOKEN, new SampleType(), Collections
                        .<NewSample> emptyList());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamplesWithDuplicatedNewSamples()
    {
        prepareGetSession();
        final IGenericServer server = createServer();
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("same"));
        newSamples.add(createNewSample("same"));
        try
        {
            server.registerSamples(SESSION_TOKEN, new SampleType(), newSamples);
            fail(String.format("'%s' expected.", UserFailureException.class));
        } catch (final UserFailureException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterSamples()
    {
        final Session session = prepareGetSession();
        final SampleTypePE sampleTypePE = CommonTestUtils.createSampleType();
        final SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypePE.getCode());
        final List<NewSample> newSamples = new ArrayList<NewSample>();
        newSamples.add(createNewSample("one"));
        newSamples.add(createNewSample("two"));
        context.checking(new Expectations()
            {
                {
                    one(sampleTypeDAO).tryFindSampleTypeByCode(sampleTypePE.getCode());
                    will(returnValue(sampleTypePE));

                    one(sampleTypeSlaveServerPlugin).registerSamples(session, newSamples);
                }
            });
        createServer().registerSamples(session.getSessionToken(), sampleType, newSamples);
        context.assertIsSatisfied();
    }
}
