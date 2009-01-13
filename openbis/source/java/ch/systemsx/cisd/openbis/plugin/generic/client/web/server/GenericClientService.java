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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.shared.NewSample;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.AbstractClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The {@link IGenericClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.GENERIC_PLUGIN_SERVICE)
public final class GenericClientService extends AbstractClientService implements
        IGenericClientService
{

    @Resource(name = ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    public GenericClientService()
    {
    }

    @Private
    GenericClientService(final IGenericServer genericServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.genericServer = genericServer;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return genericServer;
    }

    //
    // IGenericClientService
    //

    public final SampleGeneration getSampleInfo(final String sampleIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
            final SampleGenerationDTO sampleGeneration =
                    genericServer.getSampleInfo(sessionToken, identifier);
            return BeanUtils.createBean(SampleGeneration.class, sampleGeneration, DtoConverters
                    .getSampleConverter());
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final void registerSample(final NewSample newSample)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerSample(sessionToken, newSample);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final List<BatchRegistrationResult> registerSamples(final SampleType sampleType,
            final String sessionKey)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        HttpSession session = null;
        UploadedFilesBean uploadedFiles = null;
        try
        {
            final String sessionToken = getSessionToken();
            session = getHttpSession();
            assert session.getAttribute(sessionKey) != null
                    && session.getAttribute(sessionKey) instanceof UploadedFilesBean : String
                    .format("No UploadedFilesBean object as session attribute '%s' found.",
                            sessionKey);
            uploadedFiles = (UploadedFilesBean) session.getAttribute(sessionKey);
            final BisTabFileLoader<NewSample> tabFileLoader =
                    new BisTabFileLoader<NewSample>(new IParserObjectFactoryFactory<NewSample>()
                        {
                            //
                            // IParserObjectFactoryFactory
                            //

                            public final IParserObjectFactory<NewSample> createFactory(
                                    final IPropertyMapper propertyMapper) throws ParserException
                            {
                                return new NewSampleParserObjectFactory(sampleType, propertyMapper);
                            }
                        });
            final List<NewSample> newSamples = new ArrayList<NewSample>();
            final List<BatchRegistrationResult> results =
                    new ArrayList<BatchRegistrationResult>(uploadedFiles.size());
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                final StringReader stringReader =
                        new StringReader(new String(multipartFile.getBytes()));
                final List<NewSample> loadedSamples =
                        tabFileLoader.load(new DelegatedReader(stringReader, multipartFile
                                .getOriginalFilename()));
                newSamples.addAll(loadedSamples);
                results.add(new BatchRegistrationResult(multipartFile.getOriginalFilename(), String
                        .format("%d sample(s) found and registered.", loadedSamples.size())));
            }
            genericServer.registerSamples(sessionToken, sampleType, newSamples);
            return results;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            if (uploadedFiles != null)
            {
                uploadedFiles.deleteTransferredFiles();
            }
            if (session != null)
            {
                session.removeAttribute(sessionKey);
            }
        }
    }

    public final Experiment getExperimentInfo(final String experimentIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experimentIdentifier).createIdentifier();
            final ExperimentPE experiment =
                    genericServer.getExperimentInfo(sessionToken, identifier);
            return ExperimentTranslator.translate(experiment,
                    ExperimentTranslator.LoadableFields.PROPERTIES,
                    ExperimentTranslator.LoadableFields.ATTACHMENTS);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

}
