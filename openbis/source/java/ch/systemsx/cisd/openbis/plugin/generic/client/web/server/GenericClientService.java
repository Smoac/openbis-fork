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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AttachmentRegistrationHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.NewMaterialParserObjectFactory;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.SampleUploadSectionsParser;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.SampleUploadSectionsParser.BatchSamplesOperation;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.SampleUploadSectionsParser.SampleCodeGenerator;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * The {@link IGenericClientService} implementation.
 * 
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.GENERIC_PLUGIN_SERVICE)
public class GenericClientService extends AbstractClientService implements IGenericClientService
{

    @Resource(name = ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    public GenericClientService()
    {
    }

    @Private
    protected GenericClientService(final IGenericServer genericServer,
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

    public final SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final SampleParentWithDerived sampleParentWithDerived =
                    genericServer.getSampleInfo(sessionToken, sampleId);
            return sampleParentWithDerived;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Sample getSampleInfo(final TechId sampleId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return getSampleGenerationInfo(sampleId).getParent();
    }

    public final void registerSample(final String sessionKey, final NewSample newSample)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    genericServer.registerSample(sessionToken, newSample, attachments);
                }
            }.process(sessionKey, getHttpSession(), newSample.getAttachments());
    }

    public final List<BatchRegistrationResult> registerSamples(final SampleType sampleType,
            final String sessionKey, final String defaultGroupIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        BatchSamplesOperation info =
                parseSamples(sampleType, sessionKey, defaultGroupIdentifier,
                        defaultGroupIdentifier != null, true, BatchOperationKind.REGISTRATION);
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerSamples(sessionToken, info.getSamples());
            return info.getResultList();
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    public final List<BatchRegistrationResult> updateSamples(final SampleType sampleType,
            final String sessionKey, final String defaultGroupIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        BatchSamplesOperation info =
                parseSamples(sampleType, sessionKey, defaultGroupIdentifier, false, true,
                        BatchOperationKind.UPDATE);
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.updateSamples(sessionToken, info.getSamples());
            return info.getResultList();
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
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
            final Experiment experiment = genericServer.getExperimentInfo(sessionToken, identifier);
            return experiment;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final Experiment getExperimentInfo(final TechId experimentId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final Experiment experiment =
                    genericServer.getExperimentInfo(sessionToken, experimentId);
            return experiment;
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
            final Material material = genericServer.getMaterialInfo(sessionToken, materialId);
            return material;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public final ExternalData getDataSetInfo(final TechId datasetId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExternalData dataset = genericServer.getDataSetInfo(sessionToken, datasetId);
            return dataset;
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
            BatchSamplesOperation result =
                    parseSamples(experiment.getSampleType(), samplesSessionKey,
                            new GroupIdentifier(identifier.getDatabaseInstanceCode(), identifier
                                    .getSpaceCode()).toString(), experiment.isGenerateCodes(),
                            false, BatchOperationKind.REGISTRATION);
            experiment.setNewSamples(result.getSamples());
            experiment.setSamples(result.getCodes());
        }
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    genericServer.registerExperiment(sessionToken, experiment, attachments);
                }
            }.process(attachmentsSessionKey, getHttpSession(), experiment.getAttachments());
    }

    private BatchSamplesOperation parseSamples(final SampleType sampleType,
            final String sessionKey, String defaultGroupIdentifier,
            final boolean isAutoGenerateCodes, final boolean allowExperiments,
            BatchOperationKind operationKind)
    {
        HttpSession httpSession = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            SampleCodeGenerator sampleCodeGeneratorOrNull =
                    tryGetSampleCodeGenerator(isAutoGenerateCodes);
            uploadedFiles = getUploadedFiles(sessionKey, httpSession);
            return SampleUploadSectionsParser.prepareSamples(sampleType, uploadedFiles,
                    defaultGroupIdentifier, sampleCodeGeneratorOrNull, allowExperiments,
                    operationKind);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            cleanUploadedFiles(sessionKey, httpSession, uploadedFiles);
        }
    }

    private SampleCodeGenerator tryGetSampleCodeGenerator(boolean isAutoGenerateCodes)
    {
        if (isAutoGenerateCodes)
        {
            return new SampleCodeGenerator()
                {
                    public List<String> generateCodes(int size)
                    {
                        return genericServer.generateCodes(getSessionToken(), "S", size);
                    }
                };
        } else
        {
            return null;
        }
    }

    private UploadedFilesBean getUploadedFiles(String sessionKey, HttpSession session)
    {
        assert session.getAttribute(sessionKey) != null
                && session.getAttribute(sessionKey) instanceof UploadedFilesBean : String.format(
                "No UploadedFilesBean object as session attribute '%s' found.", sessionKey);
        return (UploadedFilesBean) session.getAttribute(sessionKey);
    }

    public final List<BatchRegistrationResult> registerMaterials(final MaterialType materialType,
            final String sessionKey)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);
            final BisTabFileLoader<NewMaterial> tabFileLoader =
                    new BisTabFileLoader<NewMaterial>(
                            new IParserObjectFactoryFactory<NewMaterial>()
                                {
                                    public final IParserObjectFactory<NewMaterial> createFactory(
                                            final IPropertyMapper propertyMapper)
                                            throws ParserException
                                    {
                                        return new NewMaterialParserObjectFactory(propertyMapper);
                                    }
                                }, false);
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
            genericServer
                    .registerMaterials(getSessionToken(), materialType.getCode(), newMaterials);
            return results;
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }

    private void cleanUploadedFiles(final String sessionKey, HttpSession session,
            UploadedFilesBean uploadedFiles)
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

    public Date updateSample(final SampleUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Date modificationDate = new Date();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
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
                                    .getSampleIdOrNull(), updates.getProperties(),
                                    convExperimentIdentifierOrNull, attachments, updates
                                            .getVersion(), sampleOwner, updates
                                            .getParentIdentifierOrNull(), updates
                                            .getContainerIdentifierOrNull()));
                    modificationDate.setTime(date.getTime());
                }
            }.process(updates.getSessionKey(), getHttpSession(), updates.getAttachments());
        return modificationDate;
    }

    public Date updateMaterial(TechId materialId, List<IEntityProperty> properties, Date version)
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
            BatchSamplesOperation info =
                    parseSamples(updates.getSampleType(), updates.getSamplesSessionKey(),
                            new GroupIdentifier(newProject.getDatabaseInstanceCode(), newProject
                                    .getSpaceCode()).toString(), updates.isGenerateCodes(), false,
                            BatchOperationKind.REGISTRATION);
            updates.setNewSamples(info.getSamples());
            updates.setSampleCodes(info.getCodes());
        }
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    ExperimentUpdatesDTO updatesDTO =
                            createExperimentUpdatesDTO(updates, attachments);
                    ExperimentUpdateResult updateResult =
                            genericServer.updateExperiment(sessionToken, updatesDTO);
                    result.copyFrom(updateResult);
                }
            }
                .process(updates.getAttachmentSessionKey(), getHttpSession(), updates
                        .getAttachments());
        return result;
    }

    private static ExperimentUpdatesDTO createExperimentUpdatesDTO(ExperimentUpdates updates,
            Collection<NewAttachment> attachments)
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

    public DataSetUpdateResult updateDataSet(final DataSetUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            final String sessionToken = getSessionToken();
            return genericServer.updateDataSet(sessionToken, createDataSetUpdatesDTO(updates));
        } catch (TransactionSystemException e)
        {
            // Deferred triger may throw an exception just before commit.
            // Message in the exception is readable for the user.
            throw UserFailureExceptionTranslator.translate(new UserFailureException(e
                    .getMostSpecificCause().getMessage()));
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private static DataSetUpdatesDTO createDataSetUpdatesDTO(DataSetUpdates updates)
    {
        DataSetUpdatesDTO updatesDTO = new DataSetUpdatesDTO();

        updatesDTO.setDatasetId(updates.getDatasetId());
        updatesDTO.setProperties(updates.getProperties());
        updatesDTO.setVersion(updates.getVersion());
        updatesDTO.setModifiedParentDatasetCodesOrNull(updates
                .getModifiedParentDatasetCodesOrNull());
        String sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        updatesDTO.setSampleIdentifierOrNull(sampleIdentifierOrNull == null ? null
                : SampleIdentifierFactory.parse(sampleIdentifierOrNull));
        String experimentIdentifierOrNull = updates.getExperimentIdentifierOrNull();
        updatesDTO.setExperimentIdentifierOrNull(experimentIdentifierOrNull == null ? null
                : new ExperimentIdentifierFactory(experimentIdentifierOrNull).createIdentifier());
        updatesDTO.setFileFormatTypeCode(updates.getFileFormatTypeCode());
        return updatesDTO;
    }
}
