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
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AttachmentRegistrationHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
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
    {
        return getSampleGenerationInfo(sampleId).getParent();
    }

    public final void registerSample(final String sessionKey, final NewSample newSample)
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

    public final Material getMaterialInfo(final TechId materialId)
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
    {
        final String sessionToken = getSessionToken();
        if (experiment.isRegisterSamples())
        {
            final ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier();
            BatchSamplesOperation result =
                    parseSamples(
                            experiment.getSampleType(),
                            samplesSessionKey,
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
            Collection<NamedInputStream> files =
                    new ArrayList<NamedInputStream>(uploadedFiles.size());
            for (IUncheckedMultipartFile f : uploadedFiles.iterable())
            {
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename(), f
                        .getBytes()));
            }
            return SampleUploadSectionsParser.prepareSamples(sampleType, files,
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

    public final List<BatchRegistrationResult> registerMaterials(final MaterialType materialType,
            final String sessionKey)
    {
        HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);
            Collection<NamedInputStream> files =
                    new ArrayList<NamedInputStream>(uploadedFiles.size());
            for (IUncheckedMultipartFile f : uploadedFiles.iterable())
            {
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename(), f
                        .getBytes()));
            }
            MaterialLoader loader = new MaterialLoader();
            loader.load(files);
            genericServer.registerMaterials(getSessionToken(), materialType.getCode(),
                    loader.getNewMaterials());
            return loader.getResults();
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }

    public SampleUpdateResult updateSample(final SampleUpdates updates)
    {
        final String sessionToken = getSessionToken();
        final SampleUpdateResult result = new SampleUpdateResult();
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
                    SampleUpdateResult updateResult =
                            genericServer.updateSample(
                                    sessionToken,
                                    new SampleUpdatesDTO(updates.getSampleIdOrNull(), updates
                                            .getProperties(), convExperimentIdentifierOrNull,
                                            attachments, updates.getVersion(), sampleOwner, updates
                                                    .getContainerIdentifierOrNull(), updates
                                                    .getModifiedParentCodesOrNull()));
                    result.copyFrom(updateResult);
                }
            }.process(updates.getSessionKey(), getHttpSession(), updates.getAttachments());
        return result;
    }

    public Date updateMaterial(TechId materialId, List<IEntityProperty> properties, Date version)
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
    {
        final String sessionToken = getSessionToken();
        final ExperimentUpdateResult result = new ExperimentUpdateResult();
        if (updates.isRegisterSamples())
        {
            final ProjectIdentifier newProject =
                    new ProjectIdentifierFactory(updates.getProjectIdentifier()).createIdentifier();
            BatchSamplesOperation info =
                    parseSamples(
                            updates.getSampleType(),
                            updates.getSamplesSessionKey(),
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
            }.process(updates.getAttachmentSessionKey(), getHttpSession(), updates.getAttachments());
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

    public List<BatchRegistrationResult> updateDataSets(DataSetType dataSetType, String sessionKey)
    {

        HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);
            Collection<NamedInputStream> files =
                    new ArrayList<NamedInputStream>(uploadedFiles.size());
            for (IUncheckedMultipartFile f : uploadedFiles.iterable())
            {
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename(), f
                        .getBytes()));
            }
            DataSetLoader loader = new DataSetLoader();
            loader.load(files);
            genericServer.updateDataSets(getSessionToken(), new NewDataSetsWithTypes(dataSetType,
                    loader.getNewDataSets()));
            return loader.getResults();
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }
}
