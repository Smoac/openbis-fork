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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AttachmentRegistrationHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueCodeViolationExceptionAbstract;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewEntitiesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.parser.NamedInputStream;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.BatchSamplesOperation;
import ch.systemsx.cisd.openbis.generic.shared.parser.SampleUploadSectionsParser.SampleCodeGenerator;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.MaterialUploadSectionsParser;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.server.parser.MaterialUploadSectionsParser.BatchMaterialsOperation;
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

    @Override
    public final SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final SampleParentWithDerived sampleParentWithDerived =
                    genericServer.getSampleInfo(sessionToken, sampleId);
            transformXML(sampleParentWithDerived.getParent());
            return sampleParentWithDerived;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
    public final Sample getSampleInfo(final TechId sampleId)
    {
        return getSampleGenerationInfo(sampleId).getParent();
    }

    @Override
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

    @Override
    public final List<BatchRegistrationResult> registerSamples(final SampleType sampleType,
            final String sessionKey, final String defaultGroupIdentifier, boolean updateExisting)
    {
        boolean isAutogenerateCodes = defaultGroupIdentifier != null;

        BatchOperationKind operationKind =
                updateExisting ? BatchOperationKind.UPDATE : BatchOperationKind.REGISTRATION;
        BatchSamplesOperation info =
                parseSamples(sampleType, sessionKey, defaultGroupIdentifier, isAutogenerateCodes,
                        true, null, operationKind);
        try
        {
            final String sessionToken = getSessionToken();
            genericServer.registerOrUpdateSamples(sessionToken, info.getSamples());
            return info.getResultList();
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            if (e.getCause() instanceof SampleUniqueCodeViolationExceptionAbstract)
            {
                SampleUniqueCodeViolationExceptionAbstract codeException =
                        (SampleUniqueCodeViolationExceptionAbstract) e.getCause();

                if (isAutogenerateCodes)
                {
                    throw new UserFailureException(
                            String.format(
                                    "Import failed because the autogenerated codes are no longer unique. Somebody has created a sample (code: %s) that matched one of the autogenerated codes. Please run the import once again.",
                                    codeException.getSampleCode()));
                } else
                {
                    throw new UserFailureException(String.format(
                            "Import failed because sample (code: %s) already exists.",
                            codeException.getSampleCode()));
                }
            } else
            {
                throw UserFailureExceptionTranslator.translate(e);
            }
        }
    }

    @Override
    public final List<BatchRegistrationResult> registerOrUpdateSamplesAndMaterials(
            final String sessionKey, final String defaultGroupIdentifier, boolean updateExisting,
            boolean async, String userEmail) throws UserFailureException
    {
        BatchOperationKind operationKind =
                updateExisting ? BatchOperationKind.UPDATE : BatchOperationKind.REGISTRATION;

        final SampleType sampleType = new SampleType();
        sampleType.setCode(EntityType.DEFINED_IN_FILE);

        HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);
            BatchSamplesOperation samplesInfo =
                    parseSamples(sampleType, session, uploadedFiles, defaultGroupIdentifier,
                            defaultGroupIdentifier != null, true, "SAMPLES", operationKind);

            final MaterialType materialType = new MaterialType();
            materialType.setCode(EntityType.DEFINED_IN_FILE);
            BatchMaterialsOperation materialsInfo =
                    parseMaterials(session, uploadedFiles, materialType, "MATERIALS",
                            updateExisting);
            final String sessionToken = getSessionToken();

            if (async)
            {
                genericServer.registerOrUpdateSamplesAndMaterialsAsync(sessionToken,
                        samplesInfo.getSamples(), materialsInfo.getMaterials(), userEmail);

                List<BatchRegistrationResult> results = new ArrayList<BatchRegistrationResult>();
                results.add(new BatchRegistrationResult(uploadedFiles.iterable().iterator().next()
                        .getOriginalFilename(),
                        "When the import is complete the confirmation or failure report will be sent by email."));

                return results;
            } else
            {
                genericServer.registerOrUpdateSamplesAndMaterials(sessionToken,
                        samplesInfo.getSamples(), materialsInfo.getMaterials());

                List<BatchRegistrationResult> results = new ArrayList<BatchRegistrationResult>();
                results.addAll(materialsInfo.getResultList());
                results.addAll(samplesInfo.getResultList());
                return results;
            }
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }

    @Override
    public final List<BatchRegistrationResult> updateSamples(final SampleType sampleType,
            final String sessionKey, final String defaultGroupIdentifier)
    {
        BatchSamplesOperation info =
                parseSamples(sampleType, sessionKey, defaultGroupIdentifier, false, true, null,
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

    @Override
    public final ExternalData getDataSetInfo(final TechId datasetId)
    {
        try
        {
            final String sessionToken = getSessionToken();
            final ExternalData dataset = genericServer.getDataSetInfo(sessionToken, datasetId);
            transformXML(dataset);
            return dataset;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
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
                            new SpaceIdentifier(identifier.getDatabaseInstanceCode(), identifier
                                    .getSpaceCode()).toString(), experiment.isGenerateCodes(),
                            false, null, BatchOperationKind.REGISTRATION);
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
            HttpSession httpSession, UploadedFilesBean uploadedFiles,
            String defaultGroupIdentifier, final boolean isAutoGenerateCodes,
            final boolean allowExperiments, String excelSheetName, BatchOperationKind operationKind)
    {
        boolean updateExisting = (operationKind == BatchOperationKind.UPDATE);
        SampleCodeGenerator sampleCodeGeneratorOrNull =
                tryGetSampleCodeGenerator(isAutoGenerateCodes, sampleType.getGeneratedCodePrefix());
        Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
        for (IUncheckedMultipartFile f : uploadedFiles.iterable())
        {
            files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
        }
        BatchSamplesOperation batchSamplesOperation =
                SampleUploadSectionsParser.prepareSamples(sampleType, files,
                        defaultGroupIdentifier, sampleCodeGeneratorOrNull, allowExperiments,
                        excelSheetName, operationKind);
        setUpdatePossibility(batchSamplesOperation.getSamples(), updateExisting);
        return batchSamplesOperation;

    }

    private BatchSamplesOperation parseSamples(final SampleType sampleType,
            final String sessionKey, String defaultGroupIdentifier,
            final boolean isAutoGenerateCodes, final boolean allowExperiments,
            String excelSheetName, BatchOperationKind operationKind)
    {
        HttpSession httpSession = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, httpSession);
            return parseSamples(sampleType, httpSession, uploadedFiles, defaultGroupIdentifier,
                    isAutoGenerateCodes, allowExperiments, excelSheetName, operationKind);
        } finally
        {
            cleanUploadedFiles(sessionKey, httpSession, uploadedFiles);
        }
    }

    private static void setUpdatePossibility(
            List<? extends NewEntitiesWithTypes<?, ?>> batchOperation, boolean updateExisting)
    {
        for (NewEntitiesWithTypes<?, ?> entitiesWithTypes : batchOperation)
        {
            entitiesWithTypes.setAllowUpdateIfExist(updateExisting);
        }
    }

    private SampleCodeGenerator tryGetSampleCodeGenerator(boolean isAutoGenerateCodes,
            final String codePrefix)
    {
        if (isAutoGenerateCodes)
        {
            return new SampleCodeGenerator()
                {
                    @Override
                    public List<String> generateCodes(int size)
                    {
                        return genericServer.generateCodes(getSessionToken(), codePrefix,
                                EntityKind.SAMPLE, size);
                    }
                };
        } else
        {
            return null;
        }
    }

    @Override
    public final List<BatchRegistrationResult> registerMaterials(final MaterialType materialType,
            boolean updateExisting, final String sessionKey)
    {
        String sessionToken = getSessionToken();
        BatchMaterialsOperation results =
                parseMaterials(sessionKey, materialType, null, updateExisting);
        List<NewMaterialsWithTypes> materials = results.getMaterials();
        genericServer.registerOrUpdateMaterials(sessionToken, materials);
        return results.getResultList();
    }

    @Override
    public final List<BatchRegistrationResult> registerExperiments(
            final ExperimentType experimentType, final String sessionKey)
    {
        String sessionToken = getSessionToken();
        ExperimentLoader loader = parseExperiments(sessionKey);
        genericServer.registerExperiments(
                sessionToken,
                new NewExperimentsWithType(experimentType.getCode(), loader
                        .getNewBasicExperiments()));
        return loader.getResults();
    }

    @Override
    public List<BatchRegistrationResult> updateMaterials(MaterialType materialType,
            String sessionKey, boolean ignoreUnregisteredMaterials)
    {
        String sessionToken = getSessionToken();

        BatchMaterialsOperation results = parseMaterials(sessionKey, materialType, null, true);
        int updateCount =
                genericServer.updateMaterials(sessionToken, results.getMaterials(),
                        ignoreUnregisteredMaterials);
        String message = updateCount + " material(s) updated";
        if (ignoreUnregisteredMaterials)
        {
            int ignoredCount = -updateCount;
            for (NewMaterialsWithTypes m : results.getMaterials())
            {
                ignoredCount += m.getNewEntities().size();
            }
            if (ignoredCount > 0)
            {
                message += ", " + ignoredCount + " ignored.";
            } else
            {
                message += ", non ignored.";
            }
        } else
        {
            message += ".";
        }
        return Arrays.asList(new BatchRegistrationResult(results.getResultList().get(0)
                .getFileName(), message));
    }

    private ExperimentLoader parseExperiments(String sessionKey)
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
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
            }
            ExperimentLoader loader = new ExperimentLoader();
            loader.load(files);
            return loader;
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }

    private BatchMaterialsOperation parseMaterials(HttpSession session,
            UploadedFilesBean uploadedFiles, MaterialType materialType, String excelSheetName,
            boolean updateExisting)
    {
        Collection<NamedInputStream> files = new ArrayList<NamedInputStream>(uploadedFiles.size());
        for (IUncheckedMultipartFile f : uploadedFiles.iterable())
        {
            files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
        }

        BatchMaterialsOperation batchMaterialsOperation =
                MaterialUploadSectionsParser.prepareMaterials(materialType, files, excelSheetName);
        setUpdatePossibility(batchMaterialsOperation.getMaterials(), updateExisting);
        return batchMaterialsOperation;
    }

    private BatchMaterialsOperation parseMaterials(String sessionKey, MaterialType materialType,
            String excelSheetName, boolean updateExisting)
    {
        HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        try
        {
            uploadedFiles = getUploadedFiles(sessionKey, session);

            return parseMaterials(session, uploadedFiles, materialType, excelSheetName,
                    updateExisting);
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }

    @Override
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

                    SampleUpdatesDTO updatesDTO =
                            new SampleUpdatesDTO(updates.getSampleIdOrNull(),
                                    updates.getProperties(), convExperimentIdentifierOrNull,
                                    attachments, updates.getVersion(), sampleOwner,
                                    updates.getContainerIdentifierOrNull(),
                                    updates.getModifiedParentCodesOrNull());
                    updatesDTO.setMetaprojectsOrNull(updates.getMetaprojectsOrNull());

                    SampleUpdateResult updateResult =
                            genericServer.updateSample(sessionToken, updatesDTO);

                    result.copyFrom(updateResult);
                }
            }.process(updates.getSessionKey(), getHttpSession(), updates.getAttachments());
        return result;
    }

    @Override
    public Date updateMaterial(TechId materialId, List<IEntityProperty> properties,
            String[] metaprojects, Date version)
    {
        try
        {
            final String sessionToken = getSessionToken();
            return genericServer.updateMaterial(sessionToken, materialId, properties, metaprojects,
                    version);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    @Override
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
                            new SpaceIdentifier(newProject.getDatabaseInstanceCode(), newProject
                                    .getSpaceCode()).toString(), updates.isGenerateCodes(), false,
                            null, BatchOperationKind.REGISTRATION);
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
        updatesDTO.setMetaprojectsOrNull(updates.getMetaprojectsOrNull());
        return updatesDTO;
    }

    @Override
    public DataSetUpdateResult updateDataSet(final DataSetUpdates updates)
    {
        final String sessionToken = getSessionToken();
        return genericServer.updateDataSet(sessionToken, createDataSetUpdatesDTO(updates));
    }

    private static DataSetUpdatesDTO createDataSetUpdatesDTO(DataSetUpdates updates)
    {
        DataSetUpdatesDTO updatesDTO = new DataSetUpdatesDTO();

        updatesDTO.setDatasetId(updates.getDatasetId());
        updatesDTO.setProperties(updates.getProperties());
        updatesDTO.setVersion(updates.getVersion());
        updatesDTO.setModifiedParentDatasetCodesOrNull(updates
                .getModifiedParentDatasetCodesOrNull());
        updatesDTO.setModifiedContainedDatasetCodesOrNull(updates
                .getModifiedContainedDatasetCodesOrNull());
        String sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        updatesDTO.setSampleIdentifierOrNull(sampleIdentifierOrNull == null ? null
                : SampleIdentifierFactory.parse(sampleIdentifierOrNull));
        String experimentIdentifierOrNull = updates.getExperimentIdentifierOrNull();
        updatesDTO.setExperimentIdentifierOrNull(experimentIdentifierOrNull == null ? null
                : new ExperimentIdentifierFactory(experimentIdentifierOrNull).createIdentifier());
        updatesDTO.setFileFormatTypeCode(updates.getFileFormatTypeCode());
        updatesDTO.setExternalCode(updates.getExternalCode());
        updatesDTO.setExternalDataManagementSystemCode(updates
                .getExternalDataManagementSystemCode());
        updatesDTO.setMetaprojectsOrNull(updates.getMetaprojectsOrNull());
        return updatesDTO;
    }

    @Override
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
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
            }
            DataSetLoader loader = new DataSetLoader();
            loader.load(files);
            genericServer.updateDataSets(getSessionToken(), new NewDataSetsWithTypes(dataSetType,
                    loader.getNewDataSets()));
            return loader.getResults();
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }

    @Override
    public List<BatchRegistrationResult> updateExperiments(ExperimentType experimentType,
            String sessionKey)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
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
                files.add(new NamedInputStream(f.getInputStream(), f.getOriginalFilename()));
            }
            UpdatedExperimentLoader loader = new UpdatedExperimentLoader();
            loader.load(files);
            genericServer.updateExperiments(getSessionToken(), new UpdatedExperimentsWithType(
                    experimentType, loader.getNewBasicExperiments()));
            return loader.getResults();
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }
    }
}
