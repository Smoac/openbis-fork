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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.server;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AttachmentRegistrationHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.IDemoClientService;
import ch.systemsx.cisd.openbis.plugin.demo.shared.IDemoServer;
import ch.systemsx.cisd.openbis.plugin.demo.shared.ResourceNames;

/**
 * The {@link IDemoClientService} implementation.
 * 
 * @author Christian Ribeaud
 */
@Component(value = ResourceNames.DEMO_PLUGIN_SERVICE)
public final class DemoClientService extends AbstractClientService implements
        IDemoClientService
{

    @Resource(name = ResourceNames.DEMO_PLUGIN_SERVER)
    private IDemoServer demoServer;

    public DemoClientService()
    {
    }

    @Private
    DemoClientService(final IDemoServer demoServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.demoServer = demoServer;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return demoServer;
    }

    //
    // IDemoClientService
    //

    public final SampleGeneration getSampleGenerationInfo(final TechId sampleId, String baseIndexURL)
            throws UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
          final SampleGenerationDTO sampleGenerationDTO =
                    demoServer.getSampleInfo(sessionToken, sampleId);
            return SampleTranslator.translate(sampleGenerationDTO, baseIndexURL);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerSample(final String sessionKey, final NewSample sample)
            throws UserFailureException
    {

        final String sessionToken = getSessionToken();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(List<AttachmentPE> attachments)
                {
                    demoServer.registerSample(sessionToken, sample, attachments);
                }
            }.process(sessionKey, getHttpSession());
    }
}
