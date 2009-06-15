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

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.demo.shared.IDemoServer;
import ch.systemsx.cisd.openbis.plugin.demo.shared.ResourceNames;

/**
 * The concrete {@link IDemoServer} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.DEMO_PLUGIN_SERVER)
public final class DemoServer extends AbstractServer<IDemoServer> implements IDemoServer
{
    @Resource(name = ResourceNames.DEMO_BUSINESS_OBJECT_FACTORY)
    private IDemoBusinessObjectFactory businessObjectFactory;

    public DemoServer()
    {
    }

    @Private
    DemoServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IDemoBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // AbstractServerWithLogger
    //

    @Override
    protected final Class<IDemoServer> getProxyInterface()
    {
        return IDemoServer.class;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final IDemoServer createLogger(final boolean invocationSuccessful)
    {
        return new DemoServerLogger(getSessionManager(), invocationSuccessful);
    }

    //
    // IDemoServer
    //

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        final SamplePE sample = sampleBO.getSample();
        return getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample);
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken, final TechId sampleId)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        final SamplePE sample = sampleBO.getSample();
        return getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample);
    }

    public final void registerSample(final String sessionToken, final NewSample newSample,
            List<AttachmentPE> attachments)
    {
        throw new NotImplementedException();
    }

    public int getNumberOfExperiments(String sessionToken)
    {
        return getDAOFactory().getExperimentDAO().listExperiments().size();
    }

}
