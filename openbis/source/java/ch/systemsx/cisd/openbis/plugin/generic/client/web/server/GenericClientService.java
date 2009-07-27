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
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AttachmentRegistrationHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
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

    public final SampleGeneration getSampleGenerationInfo(final TechId sampleId, String baseIndexURL)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final SampleGenerationDTO sampleGenerationDTO =
                    genericServer.getSampleInfo(sessionToken, sampleId);
            return SampleTranslator.translate(sampleGenerationDTO, baseIndexURL);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Sample getSampleInfo(final TechId sampleId, String baseIndexURL)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return getSampleGenerationInfo(sampleId, baseIndexURL).getGenerator();
    }

    public final void registerSample(final String sessionKey, final NewSample newSample)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(List<AttachmentPE> attachments)
                {
                    genericServer.registerSample(sessionToken, newSample, attachments);
                }
            }.process(sessionKey, getHttpSession(), newSample.getAttachments());
    }

    public final List<BatchRegistrationResult> registerSamples(final SampleType sampleType,
            final String sessionKey, String defaultGroupIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        SampleExtractor info =
                new SampleExtractor().prepareSamples(sampleType, sessionKey,
                        defaultGroupIdentifier, defaultGroupIdentifier != null);
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerSamples(sessionToken, sampleType, info.getSamples());
            return info.getResultList();
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    private class SampleExtractor
    {
        private List<NewSample> samples;

        private List<BatchRegistrationResult> resultList;

        String[] sampleCodes;

        public List<NewSample> getSamples()
        {
            return samples;
        }

        public List<BatchRegistrationResult> getResultList()
        {
            return resultList;
        }

        public String[] getCodes()
        {
            return sampleCodes;
        }

        public SampleExtractor prepareSamples(final SampleType sampleType, final String sessionKey,
                String defaultGroupIdentifier, final boolean isAutoGenerateCodes)
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
                        createSampleLoader(sampleType, isAutoGenerateCodes);
                final List<NewSample> newSamples = new ArrayList<NewSample>();
                final List<BatchRegistrationResult> results =
                        loadSamplesFromFiles(uploadedFiles, tabFileLoader, newSamples);
                generateIdentifiers(defaultGroupIdentifier, isAutoGenerateCodes, sessionToken,
                        newSamples);
                fillTheBean(newSamples, results);
                return this;
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

        private void fillTheBean(final List<NewSample> newSamples,
                final List<BatchRegistrationResult> results)
        {
            resultList = results;
            samples = newSamples;
            sampleCodes = new String[samples.size()];
            for (int i = 0; i < samples.size(); i++)
            {
                sampleCodes[i] =
                        SampleIdentifierFactory.parse(samples.get(i).getIdentifier())
                                .getSampleCode();
            }
        }

        private BisTabFileLoader<NewSample> createSampleLoader(final SampleType sampleType,
                final boolean isAutoGenerateCodes)
        {
            final BisTabFileLoader<NewSample> tabFileLoader =
                    new BisTabFileLoader<NewSample>(new IParserObjectFactoryFactory<NewSample>()
                        {
                            public final IParserObjectFactory<NewSample> createFactory(
                                    final IPropertyMapper propertyMapper) throws ParserException
                            {
                                return new NewSampleParserObjectFactory(sampleType, propertyMapper,
                                        isAutoGenerateCodes == false);
                            }
                        });
            return tabFileLoader;
        }

        private List<BatchRegistrationResult> loadSamplesFromFiles(UploadedFilesBean uploadedFiles,
                final BisTabFileLoader<NewSample> tabFileLoader, final List<NewSample> newSamples)
        {
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
            return results;
        }

        private void generateIdentifiers(String defaultGroupIdentifier,
                final boolean isAutoGenerateCodes, final String sessionToken,
                final List<NewSample> newSamples)
        {
            if (isAutoGenerateCodes)
            {
                List<String> codes =
                        genericServer.generateCodes(sessionToken, "S", newSamples.size());
                for (int i = 0; i < newSamples.size(); i++)
                {
                    newSamples.get(i).setIdentifier(defaultGroupIdentifier + "/" + codes.get(i));
                }
            }
        }

    }

    public final Experiment getExperimentInfo(final String experimentIdentifier, String baseIndexURL)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experimentIdentifier).createIdentifier();
            final ExperimentPE experiment =
                    genericServer.getExperimentInfo(sessionToken, identifier);
            return ExperimentTranslator.translate(experiment, baseIndexURL,
                    ExperimentTranslator.LoadableFields.PROPERTIES,
                    ExperimentTranslator.LoadableFields.ATTACHMENTS);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Experiment getExperimentInfo(final TechId experimentId, String baseIndexURL)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExperimentPE experiment =
                    genericServer.getExperimentInfo(sessionToken, experimentId);
            return ExperimentTranslator.translate(experiment, baseIndexURL,
                    ExperimentTranslator.LoadableFields.PROPERTIES,
                    ExperimentTranslator.LoadableFields.ATTACHMENTS);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Material getMaterialInfo(final TechId materialId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final MaterialPE material = genericServer.getMaterialInfo(sessionToken, materialId);
            return MaterialTranslator.translate(material, true);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final ExternalData getDataSetInfo(final TechId datasetId, final String baseIndexURL)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExternalDataPE dataset = genericServer.getDataSetInfo(sessionToken, datasetId);
            return ExternalDataTranslator.translate(dataset, getDataStoreBaseURL(), baseIndexURL,
                    false);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public void registerExperiment(final String attachmentsSessionKey,
            final String samplesSessionKey, final NewExperiment experiment)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        if (experiment.isRegisterSamples())
        {
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier();
            SampleExtractor extractor =
                    new SampleExtractor().prepareSamples(experiment.getSampleType(),
                            samplesSessionKey, new GroupIdentifier(identifier
                                    .getDatabaseInstanceCode(), identifier.getGroupCode())
                                    .toString(), experiment.isGenerateCodes());
            experiment.setNewSamples(extractor.getSamples());
            experiment.setSamples(extractor.getCodes());
        }
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(List<AttachmentPE> attachments)
                {
                    genericServer.registerExperiment(sessionToken, experiment, attachments);
                }
            }.process(attachmentsSessionKey, getHttpSession(), experiment.getAttachments());
    }

    public final List<BatchRegistrationResult> registerMaterials(final MaterialType materialType,
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
            final BisTabFileLoader<NewMaterial> tabFileLoader =
                    new BisTabFileLoader<NewMaterial>(
                            new IParserObjectFactoryFactory<NewMaterial>()
                                {
                                    public final IParserObjectFactory<NewMaterial> createFactory(
                                            final IPropertyMapper propertyMapper)
                                            throws ParserException
                                    {
                                        return new NewMaterialParserObjectFactory(materialType,
                                                propertyMapper);
                                    }
                                });
            final List<NewMaterial> newMaterials = new ArrayList<NewMaterial>();
            final List<BatchRegistrationResult> results =
                    new ArrayList<BatchRegistrationResult>(uploadedFiles.size());
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                final StringReader stringReader =
                        new StringReader(new String(multipartFile.getBytes()));
                final List<NewMaterial> loadedMaterials =
                        tabFileLoader.load(new DelegatedReader(stringReader, multipartFile
                                .getOriginalFilename()));
                newMaterials.addAll(loadedMaterials);
                results.add(new BatchRegistrationResult(multipartFile.getOriginalFilename(), String
                        .format("%d material(s) found and registered.", loadedMaterials.size())));
            }
            genericServer.registerMaterials(sessionToken, materialType.getCode(), newMaterials);
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

    public Date updateSample(final SampleUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Date modificationDate = new Date();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(List<AttachmentPE> attachments)
                {
                    ExperimentIdentifier convExperimentIdentifierOrNull = null;
                    SampleIdentifier sampleOwner = null;
                    if (updates.getExperimentIdentifierOrNull() != null)
                    {
                        convExperimentIdentifierOrNull =
                                new ExperimentIdentifierFactory(updates
                                        .getExperimentIdentifierOrNull().getIdentifier())
                                        .createIdentifier();
                    }
                    if (StringUtils.isBlank(updates.getSampleIdentifier()) == false)
                    {
                        sampleOwner =
                                new SampleIdentifierFactory(updates.getSampleIdentifier())
                                        .createIdentifier();
                    }
                    Date date =
                            genericServer.updateSample(sessionToken, new SampleUpdatesDTO(updates
                                    .getSampleId(), updates.getProperties(),
                                    convExperimentIdentifierOrNull, attachments, updates
                                            .getVersion(), sampleOwner, updates
                                            .getParentIdentifierOrNull(), updates
                                            .getContainerIdentifierOrNull()));
                    modificationDate.setTime(date.getTime());
                }
            }.process(updates.getSessionKey(), getHttpSession(), updates.getAttachments());
        return modificationDate;
    }

    public Date updateMaterial(TechId materialId, List<MaterialProperty> properties, Date version)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return genericServer.updateMaterial(sessionToken, materialId, properties, version);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ExperimentUpdateResult updateExperiment(final ExperimentUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final ExperimentUpdateResult result = new ExperimentUpdateResult();
        if (updates.isRegisterSamples())
        {
            final ProjectIdentifier newProject =
                    new ProjectIdentifierFactory(updates.getProjectIdentifier()).createIdentifier();
            SampleExtractor extractor =
                    new SampleExtractor().prepareSamples(updates.getSampleType(), updates
                            .getSamplesSessionKey(), new GroupIdentifier(newProject
                            .getDatabaseInstanceCode(), newProject.getGroupCode()).toString(),
                            updates.isGenerateCodes());
            updates.setNewSamples(extractor.getSamples());
            updates.setSampleCodes(extractor.getCodes());
        }
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(List<AttachmentPE> attachments)
                {
                    ExperimentUpdatesDTO updatesDTO =
                            createExperimentUpdatesDTO(updates, attachments);
                    BeanUtils.fillBean(ExperimentUpdateResult.class, result, genericServer
                            .updateExperiment(sessionToken, updatesDTO));
                }
            }
                .process(updates.getAttachmentSessionKey(), getHttpSession(), updates
                        .getAttachments());
        return result;
    }

    public Date updateDataSet(TechId datasetId, String sampleIdentifier,
            List<DataSetProperty> properties, Date version)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();

            SampleIdentifier convSampleIdentifier = SampleIdentifierFactory.parse(sampleIdentifier);
            return genericServer.updateDataSet(sessionToken, datasetId, convSampleIdentifier,
                    properties, version);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private static ExperimentUpdatesDTO createExperimentUpdatesDTO(ExperimentUpdates updates,
            List<AttachmentPE> attachments)
    {
        ExperimentUpdatesDTO updatesDTO = new ExperimentUpdatesDTO();

        updatesDTO.setExperimentId(updates.getExperimentId());

        final ProjectIdentifier project =
                new ProjectIdentifierFactory(updates.getProjectIdentifier()).createIdentifier();
        updatesDTO.setProjectIdentifier(project);
        updatesDTO.setAttachments(attachments);
        updatesDTO.setProperties(updates.getProperties());
        updatesDTO.setSampleCodes(updates.getSampleCodes());
        updatesDTO.setVersion(updates.getVersion());
        updatesDTO.setRegisterSamples(updates.isRegisterSamples());
        updatesDTO.setNewSamples(updates.getNewSamples());
        updatesDTO.setSampleType(updates.getSampleType());
        return updatesDTO;
    }
}
