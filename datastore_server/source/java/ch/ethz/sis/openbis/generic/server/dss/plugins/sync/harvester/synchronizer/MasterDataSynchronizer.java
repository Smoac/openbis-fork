/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.Monitor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * @author Ganime Betul Akin
 */
public class MasterDataSynchronizer
{
    final ISynchronizerFacade synchronizerFacade;

    final ICommonServer commonServer;

    final String sessionToken;

    final boolean dryRun;

    final Map<TechId, List<VocabularyTerm>> vocabularyTermsToBeDeleted;

    final Map<TechId, String> vocabularyTechIdToCode = new HashMap<TechId, String>();

    private IApplicationServerApi v3api;

    private SyncConfig config;

    public MasterDataSynchronizer(SyncConfig config, Logger operationLog)
    {
        this.config = config;
        String openBisServerUrl = ServiceProvider.getConfigProvider().getOpenBisServerUrl();
        this.dryRun = config.isDryRun();
        this.synchronizerFacade = new SynchronizerFacade(openBisServerUrl, config.getHarvesterUser(),
                config.getHarvesterPass(), dryRun, config.isVerbose(), operationLog);
        this.commonServer = ServiceFinderUtils.getCommonServer(openBisServerUrl);
        this.sessionToken = ServiceFinderUtils.login(commonServer, config.getHarvesterUser(), config.getHarvesterPass());
        v3api = ServiceProvider.getV3ApplicationService();
        vocabularyTermsToBeDeleted = new HashMap<TechId, List<VocabularyTerm>>();
    }

    public void synchronizeMasterData(MasterData masterData, Monitor monitor)
    {
        MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess = masterData.getPropertyAssignmentsToProcess();
        monitor.log("process file format types");
        processFileFormatTypes(masterData.getFileFormatTypesToProcess());
        monitor.log("process validation plugins");
        processValidationPlugins(masterData.getValidationPluginsToProcess());
        monitor.log("process vocabularies");
        processVocabularies(masterData.getVocabulariesToProcess());
        // materials are registered but their property assignments are deferred until after property types are processed
        monitor.log("process material types");
        processEntityTypes(masterData.getMaterialTypesToProcess(), propertyAssignmentsToProcess);
        monitor.log("process property types");
        processPropertyTypes(masterData.getPropertyTypesToProcess());
        monitor.log("process sample types");
        processEntityTypes(masterData.getSampleTypesToProcess(), propertyAssignmentsToProcess);
        monitor.log("process data set types");
        processEntityTypes(masterData.getDataSetTypesToProcess(), propertyAssignmentsToProcess);
        monitor.log("process experiment types");
        processEntityTypes(masterData.getExperimentTypesToProcess(), propertyAssignmentsToProcess);
        monitor.log("process material type property assignments");
        processDeferredMaterialTypePropertyAssignments(propertyAssignmentsToProcess);
        monitor.log("process external data management systems");
        processExternalDataManagementSystems(masterData.getExternalDataManagementSystemsToProcess());

        synchronizerFacade.printSummary();
    }

    private void cleanupUnusedMasterData()
    {
        for (TechId vocabularyId : vocabularyTermsToBeDeleted.keySet())
        {
            synchronizerFacade.deleteVocabularyTerms(vocabularyId, vocabularyTechIdToCode.get(vocabularyId),
                    vocabularyTermsToBeDeleted.get(vocabularyId),
                    Collections.<ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement> emptyList());
        }
    }

    private void processDeferredMaterialTypePropertyAssignments(MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess)
    {
        List<? extends EntityType> existingEntityTypes = getExistingEntityTypes(EntityKind.MATERIAL);
        for (EntityType entityType : existingEntityTypes)
        {
            List<NewETPTAssignment> list = propertyAssignmentsToProcess.get(EntityKind.MATERIAL.name(), entityType.getCode());
            if (list != null)
            {
                processPropertyAssignments(entityType, list);
            }
        }
    }

    private void processValidationPlugins(Map<String, Script> pluginsToProcess)
    {
        List<Script> existingPlugins = commonServer.listScripts(sessionToken, null, null);
        Map<String, Script> pluginMap = new HashMap<String, Script>();
        for (Script type : existingPlugins)
        {
            pluginMap.put(type.getName(), type);
        }

        for (String name : pluginsToProcess.keySet())
        {
            Script incomingplugin = pluginsToProcess.get(name);
            Script existingPluginOrNull = pluginMap.get(name);
            if (existingPluginOrNull != null)
            {
                Date incomingModificationDate = incomingplugin.getModificationDate();
                if (incomingModificationDate != null
                        && incomingModificationDate.after(existingPluginOrNull.getModificationDate()))
                {
                    existingPluginOrNull.setName(incomingplugin.getName());
                    existingPluginOrNull.setScript(incomingplugin.getScript());
                    existingPluginOrNull.setDescription(incomingplugin.getDescription());
                    synchronizerFacade.updateValidationPlugin(existingPluginOrNull);
                }
            } else
            {
                synchronizerFacade.registerValidationPlugin(incomingplugin);
            }
        }
    }

    private void processExternalDataManagementSystems(Map<String, ExternalDms> externalDataManagementSystems)
    {
        List<ExternalDmsPermId> ids = externalDataManagementSystems.keySet().stream()
                .map(ExternalDmsPermId::new).collect(Collectors.toList());
        Map<IExternalDmsId, ExternalDms> existingDmss = v3api.getExternalDataManagementSystems(sessionToken, ids, new ExternalDmsFetchOptions());
        List<ExternalDmsCreation> creations = new ArrayList<>();
        List<ExternalDmsUpdate> updates = new ArrayList<>();
        for (ExternalDms externalDms : externalDataManagementSystems.values())
        {
            if (existingDmss.containsKey(externalDms.getPermId()))
            {
                ExternalDmsUpdate update = new ExternalDmsUpdate();
                update.setExternalDmsId(externalDms.getPermId());
                update.setAddress(externalDms.getAddress());
                update.setLabel(externalDms.getLabel());
                updates.add(update);
            } else
            {
                ExternalDmsCreation creation = new ExternalDmsCreation();
                creation.setCode(externalDms.getCode());
                creation.setLabel(externalDms.getLabel());
                creation.setAddress(externalDms.getAddress());
                creation.setAddressType(externalDms.getAddressType());
                creations.add(creation);
            }
        }
        if (creations.isEmpty() == false && dryRun == false)
        {
            v3api.createExternalDataManagementSystems(sessionToken, creations);
        }
        if (updates.isEmpty() == false && dryRun == false)
        {
            v3api.updateExternalDataManagementSystems(sessionToken, updates);
        }
    }

    private void processFileFormatTypes(Map<String, FileFormatType> fileFormatTypesToProcess)
    {
        List<FileFormatType> fileFormatTypes = commonServer.listFileFormatTypes(sessionToken);
        Map<String, FileFormatType> typeMap = new HashMap<String, FileFormatType>();
        for (FileFormatType type : fileFormatTypes)
        {
            typeMap.put(type.getCode(), type);
        }

        for (String typeCode : fileFormatTypesToProcess.keySet())
        {
            FileFormatType incomingType = fileFormatTypesToProcess.get(typeCode);
            FileFormatType existingTypeOrNull = typeMap.get(typeCode);
            if (existingTypeOrNull != null)
            {
                existingTypeOrNull.setDescription(incomingType.getDescription());
                synchronizerFacade.updateFileFormatType(existingTypeOrNull);
            } else
            {
                synchronizerFacade.registerFileFormatType(incomingType);
            }
        }
    }

    private void processVocabularies(Map<String, NewVocabulary> vocabulariesToProcess)
    {
        List<Vocabulary> existingVocabularies = commonServer.listVocabularies(sessionToken, true, false);
        Map<String, Vocabulary> existingVocabularyMap = new HashMap<String, Vocabulary>();
        for (Vocabulary vocabulary : existingVocabularies)
        {
            existingVocabularyMap.put(vocabulary.getCode(), vocabulary);
        }
        for (String code : vocabulariesToProcess.keySet())
        {
            NewVocabulary newVocabulary = vocabulariesToProcess.get(code);
            String vocabCode = CodeConverter.tryToBusinessLayer(newVocabulary.getCode(), newVocabulary.isInternalNamespace());
            Vocabulary existingVocabulary = existingVocabularyMap.get(vocabCode);
            if (existingVocabulary != null)
            {
                existingVocabulary.setCode(vocabCode);
                existingVocabulary.setDescription(newVocabulary.getDescription());
                existingVocabulary.setInternalNamespace(newVocabulary.isInternalNamespace());
                existingVocabulary.setManagedInternally(newVocabulary.isManagedInternally());
                existingVocabulary.setURLTemplate(newVocabulary.getURLTemplate());
                existingVocabulary.setChosenFromList(newVocabulary.isChosenFromList());
                synchronizerFacade.updateVocabulary(existingVocabulary);
                processVocabularyTerms(sessionToken, commonServer, newVocabulary, existingVocabulary);
            } else
            {
                synchronizerFacade.registerVocabulary(newVocabulary);
            }
        }
    }

    private void processVocabularyTerms(String sessionToken, ICommonServer commonServer, NewVocabulary newVocabulary, Vocabulary existingVocabulary)
    {
        List<VocabularyTerm> incomingTerms = newVocabulary.getTerms();
        Map<String, VocabularyTerm> incomingTermMap = new HashMap<String, VocabularyTerm>();
        for (VocabularyTerm term : incomingTerms)
        {
            incomingTermMap.put(term.getCode(), term);
        }

        List<VocabularyTerm> termsToBeAdded = new ArrayList<VocabularyTerm>();
        List<VocabularyTerm> termsToBeUpdated = new ArrayList<VocabularyTerm>();
        for (VocabularyTerm incomingTerm : incomingTerms)
        {
            String termCode = incomingTerm.getCode();
            VocabularyTerm existingTerm = findTermInVocabulary(existingVocabulary, termCode);
            if (existingTerm == null)
            {
                termsToBeAdded.add(incomingTerm);
            } else
            {
                existingTerm.setLabel(incomingTerm.getLabel());
                existingTerm.setDescription(incomingTerm.getDescription());
                existingTerm.setOrdinal(incomingTerm.getOrdinal());
                termsToBeUpdated.add(existingTerm);
            }
        }

        List<VocabularyTerm> termsToBeDeleted = new ArrayList<VocabularyTerm>();
        for (VocabularyTerm term : existingVocabulary.getTerms())
        {
            String termCode = term.getCode();
            if (incomingTermMap.keySet().contains(termCode) == false)
            {
                termsToBeDeleted.add(term);
            }
        }

        // defer deletions of vocabulary terms until after the entities referencing the terms are synced
        if (termsToBeDeleted.isEmpty() == false)
        {
            vocabularyTermsToBeDeleted.put(new TechId(existingVocabulary.getId()), termsToBeDeleted);
        }
        synchronizerFacade.addVocabularyTerms(existingVocabulary.getCode(), new TechId(existingVocabulary.getId()), termsToBeAdded);
        for (VocabularyTerm term : termsToBeUpdated)
        {
            synchronizerFacade.updateVocabularyTerm(term);
        }
    }

    private VocabularyTerm findTermInVocabulary(Vocabulary existingVocabulary, String termCode)
    {
        for (VocabularyTerm term : existingVocabulary.getTerms())
        {
            if (term.getCode().equals(termCode))
            {
                return term;
            }
        }
        return null;
    }

    private void processEntityTypes(Map<String, ? extends EntityType> entityTypesToProcess,
            MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess)
    {
        if (entityTypesToProcess.keySet().size() == 0)
        {
            return;
        }
        EntityKind entityKind = ((EntityType) entityTypesToProcess.values().toArray()[0]).getEntityKind();
        List<? extends EntityType> existingEntityTypes = getExistingEntityTypes(entityKind);
        Map<String, Object> existingEntityTypesMap = new HashMap<String, Object>();
        for (EntityType entityType : existingEntityTypes)
        {
            existingEntityTypesMap.put(entityType.getCode(), entityType);
        }
        for (String entityTypeCode : entityTypesToProcess.keySet())
        {
            EntityType existingEntityType = (EntityType) existingEntityTypesMap.get(entityTypeCode);
            EntityType incomingEntityType = entityTypesToProcess.get(entityTypeCode);
            List<NewETPTAssignment> list = propertyAssignmentsToProcess.get(entityKind.name(), entityTypeCode);
            if (existingEntityType != null)
            {
                updateEntityType(entityKind, incomingEntityType);
                // existingEntityType.setCode(incomingEntityType.getCode());
                // existingEntityType.setDescription(incomingEntityType.getDescription());
                // existingEntityType.setValidationScript(incomingEntityType.getValidationScript());
                if (list != null && entityKind != EntityKind.MATERIAL) // defer material property assignments until after property types are processed
                {
                    processPropertyAssignments(existingEntityType, list);
                }
            } else
            {
                registerEntityType(entityKind, incomingEntityType);
                if (list != null && entityKind != EntityKind.MATERIAL) // defer material property assignments until after property types are processed
                {
                    assignProperties(list);
                }
            }
        }
    }

    private void assignProperties(List<NewETPTAssignment> list)
    {
        for (NewETPTAssignment newETPTAssignment : list)
        {
            synchronizerFacade.assignPropertyType(newETPTAssignment);
        }
    }

    @SuppressWarnings("rawtypes")
    private void processPropertyAssignments(EntityType existingEntityType,
            List<NewETPTAssignment> incomingAssignmentsList)
    {
        assert incomingAssignmentsList != null : "Null assignments list";
        List<? extends EntityTypePropertyType> assignedPropertyTypes = existingEntityType.getAssignedPropertyTypes();
        for (NewETPTAssignment newETPTAssignment : incomingAssignmentsList)
        {
            boolean found = findInExistingPropertyAssignments(newETPTAssignment, assignedPropertyTypes);
            if (found)
            {
                synchronizerFacade.updatePropertyTypeAssignment(newETPTAssignment);
            } else
            {
                synchronizerFacade.assignPropertyType(newETPTAssignment);
            }
        }
        if (config.isPropertyUnassignmentAllowed())
        {
            // remove property assignments that are no longer valid
            for (EntityTypePropertyType etpt : assignedPropertyTypes)
            {
                if (findInIncomingPropertyAssignments(etpt, incomingAssignmentsList) == false)
                {
                    synchronizerFacade.unassignPropertyType(existingEntityType.getEntityKind(), etpt.getPropertyType().getCode(), etpt
                            .getEntityType().getCode());
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean findInIncomingPropertyAssignments(EntityTypePropertyType existingEtpt, List<NewETPTAssignment> incomingAssignmentsList)
    {
        for (NewETPTAssignment newETPTAssignment : incomingAssignmentsList)
        {
            if (newETPTAssignment.getPropertyTypeCode().equals(existingEtpt.getPropertyType().getCode()))
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private boolean findInExistingPropertyAssignments(NewETPTAssignment incomingETPTAssignment,
            List<? extends EntityTypePropertyType> assignedPropertyTypes)
    {
        for (EntityTypePropertyType entityTypePropertyType : assignedPropertyTypes)
        {
            if (entityTypePropertyType.getPropertyType().getCode().equals(incomingETPTAssignment.getPropertyTypeCode()))
            {
                return true;
            }
        }
        return false;
    }

    private void registerEntityType(EntityKind entityKind, EntityType incomingEntityType)
    {
        switch (entityKind)
        {
            case SAMPLE:
                synchronizerFacade.registerSampleType((SampleType) incomingEntityType);
                break;
            case DATA_SET:
                synchronizerFacade.registerDataSetType((DataSetType) incomingEntityType);
                break;
            case EXPERIMENT:
                synchronizerFacade.registerExperimentType((ExperimentType) incomingEntityType);
                break;
            case MATERIAL:
                synchronizerFacade.registerMaterialType((MaterialType) incomingEntityType);
                break;
            default:
                throw new UserFailureException("register not implemented for entity kind: " + entityKind.name());
        }
    }

    private void updateEntityType(EntityKind entityKind, EntityType incomingEntityType)
    {
        switch (entityKind)
        {
            case SAMPLE:
                synchronizerFacade.updateSampleType(incomingEntityType);
                break;
            case DATA_SET:
                synchronizerFacade.updateDataSetType(incomingEntityType);
                break;
            case EXPERIMENT:
                synchronizerFacade.updateExperimentType(incomingEntityType);
                break;
            case MATERIAL:
                synchronizerFacade.updateMaterialType(incomingEntityType);
                break;
            default:
                throw new UserFailureException("update not implemented for entity kind: " + entityKind.name());
        }
    }

    private List<? extends EntityType> getExistingEntityTypes(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case SAMPLE:
                return commonServer.listSampleTypes(sessionToken);
            case DATA_SET:
                return commonServer.listDataSetTypes(sessionToken);
            case EXPERIMENT:
                return commonServer.listExperimentTypes(sessionToken);
            case MATERIAL:
                return commonServer.listMaterialTypes(sessionToken);
            default:
                return null;
        }
    }

    private void processPropertyTypes(Map<String, PropertyType> propertyTypesToProcess)
    {
        List<PropertyType> propertyTypes = commonServer.listPropertyTypes(sessionToken, false);
        Map<String, PropertyType> propertyTypeMap = new HashMap<String, PropertyType>();
        for (PropertyType propertyType : propertyTypes)
        {
            propertyTypeMap.put(propertyType.getCode(), propertyType);
        }

        for (String propTypeCode : propertyTypesToProcess.keySet())
        {
            PropertyType incomingPropertyType = propertyTypesToProcess.get(propTypeCode);
            String propertyTypeCode = incomingPropertyType.getCode();
            PropertyType propertyTypeOrNull = propertyTypeMap.get(propertyTypeCode);
            if (propertyTypeOrNull != null)
            {
                propertyTypeOrNull.setLabel(incomingPropertyType.getLabel());
                propertyTypeOrNull.setDescription(incomingPropertyType.getDescription());
                synchronizerFacade.updatePropertyType(propertyTypeOrNull);
            } else
            {
                synchronizerFacade.registerPropertyType(incomingPropertyType);
            }
        }
    }
}
