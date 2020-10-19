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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.Monitor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
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
                String diff = calculateDiff(existingPluginOrNull, incomingplugin);
                if (StringUtils.isNotBlank(diff) && config.isMasterDataUpdateAllowed())
                {
                    incomingplugin.setModificationDate(existingPluginOrNull.getModificationDate());
                    incomingplugin.setId(existingPluginOrNull.getId());
                    synchronizerFacade.updateValidationPlugin(incomingplugin, diff);
                }
            } else
            {
                synchronizerFacade.registerValidationPlugin(incomingplugin);
            }
        }
    }

    private String calculateDiff(Script existingPlugin, Script incomingPlugin)
    {
        DiffBuilder diffBuilder = new DiffBuilder(existingPlugin, incomingPlugin, ToStringStyle.SHORT_PREFIX_STYLE, false)
                .append("description", existingPlugin.getDescription(), incomingPlugin.getDescription());
        String existingScript = existingPlugin.getScript();
        String incomingScript = incomingPlugin.getScript();
        int indexOfDifference = StringUtils.indexOfDifference(existingScript, incomingScript);
        if (indexOfDifference >= 0)
        {
            String existingSnippet = StringUtils.left(existingScript.substring(indexOfDifference), 20);
            String incommingSnippet = StringUtils.left(incomingScript.substring(indexOfDifference), 20);
            diffBuilder.append("script", existingSnippet, incommingSnippet);
        }
        return render(diffBuilder.build(), existingPlugin, incomingPlugin);
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
                if (StringUtils.equals(existingTypeOrNull.getDescription(), incomingType.getDescription()) == false)
                {
                    existingTypeOrNull.setDescription(incomingType.getDescription());
                    synchronizerFacade.updateFileFormatType(existingTypeOrNull);
                }
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
            String vocabCode = newVocabulary.getCode();
            Vocabulary existingVocabulary = existingVocabularyMap.get(vocabCode);
            if (existingVocabulary != null)
            {
                if (existingVocabulary.isManagedInternally()
                        && isSystem(existingVocabulary.getRegistrator().getUserId()))
                {
                    continue;
                }
                String diff = calculateDiff(existingVocabulary, newVocabulary);
                if (StringUtils.isNotBlank(diff) && config.isMasterDataUpdateAllowed())
                {
                    newVocabulary.setModificationDate(existingVocabulary.getModificationDate());
                    newVocabulary.setId(existingVocabulary.getId());
                    synchronizerFacade.updateVocabulary(newVocabulary, diff);
                }
                processVocabularyTerms(sessionToken, commonServer, newVocabulary, existingVocabulary);
            } else
            {
                synchronizerFacade.registerVocabulary(newVocabulary);
            }
        }
    }

    private boolean isSystem(String userId)
    {
        return "system".equals(userId);
    }

    private String calculateDiff(Vocabulary existingVocabulary, NewVocabulary newVocabulary)
    {
        DiffBuilder diffBuilder = new DiffBuilder(existingVocabulary, newVocabulary, ToStringStyle.SHORT_PREFIX_STYLE, false)
                .append("description", existingVocabulary.getDescription(), newVocabulary.getDescription())
                .append("urlTemplate", existingVocabulary.getURLTemplate(), newVocabulary.getURLTemplate())
                .append("managedInternally", existingVocabulary.isManagedInternally(), newVocabulary.isManagedInternally())
                .append("chosenFromList", existingVocabulary.isChosenFromList(), newVocabulary.isChosenFromList());
        DiffResult diffResult = diffBuilder.build();
        return render(diffResult, existingVocabulary, newVocabulary);
    }

    private void processVocabularyTerms(String sessionToken, ICommonServer commonServer, NewVocabulary newVocabulary, Vocabulary existingVocabulary)
    {
        Map<String, VocabularyTerm> existingTerms = getTermsByCode(existingVocabulary);
        List<VocabularyTerm> termsToBeAdded = new ArrayList<VocabularyTerm>();
        for (VocabularyTerm incomingTerm : newVocabulary.getTerms())
        {
            VocabularyTerm existingTerm = existingTerms.get(incomingTerm.getCode());
            if (existingTerm == null)
            {
                termsToBeAdded.add(incomingTerm);
            } else
            {
                String diff = calculateDiff(existingTerm, incomingTerm);
                if (StringUtils.isNotBlank(diff) && config.isMasterDataUpdateAllowed())
                {
                    incomingTerm.setModificationDate(existingTerm.getModificationDate());
                    incomingTerm.setId(existingTerm.getId());
                    synchronizerFacade.updateVocabularyTerm(existingVocabulary.getCode(), incomingTerm, diff);
                }
            }
        }

        synchronizerFacade.addVocabularyTerms(existingVocabulary.getCode(), new TechId(existingVocabulary.getId()), termsToBeAdded);
    }

    private String calculateDiff(VocabularyTerm existingTerm, VocabularyTerm incomingTerm)
    {
        DiffBuilder diffBuilder = new DiffBuilder(existingTerm, incomingTerm, ToStringStyle.SHORT_PREFIX_STYLE, false)
                .append("label", existingTerm.getLabel(), incomingTerm.getLabel())
                .append("description", existingTerm.getDescription(), incomingTerm.getDescription())
                .append("url", existingTerm.getUrl(), incomingTerm.getUrl());
        DiffResult diffResult = diffBuilder.build();
        return render(diffResult, existingTerm, incomingTerm);
    }

    private Map<String, VocabularyTerm> getTermsByCode(Vocabulary vocabulary)
    {
        Map<String, VocabularyTerm> result = new TreeMap<>();
        for (VocabularyTerm vocabularyTerm : vocabulary.getTerms())
        {
            result.put(vocabularyTerm.getCode(), vocabularyTerm);
        }
        return result;
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
                String diff = calculateDiff(entityKind, existingEntityType, incomingEntityType);
                if (StringUtils.isNotBlank(diff) && config.isMasterDataUpdateAllowed())
                {
                    incomingEntityType.setModificationDate(existingEntityType.getModificationDate());
                    updateEntityType(entityKind, incomingEntityType, diff);
                }
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
            EntityTypePropertyType foundType = findInExistingPropertyAssignments(newETPTAssignment, assignedPropertyTypes);
            if (foundType != null)
            {
                String diff = calculateDiff(foundType, newETPTAssignment);
                if (StringUtils.isNotBlank(diff) && config.isMasterDataUpdateAllowed())
                {
                    newETPTAssignment.setModificationDate(foundType.getModificationDate());
                    synchronizerFacade.updatePropertyTypeAssignment(newETPTAssignment, diff);
                }
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
    private String calculateDiff(EntityTypePropertyType existingAssignment, NewETPTAssignment incomingAssignment)
    {
        DiffBuilder diffBuilder = new DiffBuilder(existingAssignment, incomingAssignment, ToStringStyle.SHORT_PREFIX_STYLE, false)
                .append("mandatory", existingAssignment.isMandatory(), incomingAssignment.isMandatory())
                .append("section", existingAssignment.getSection(), incomingAssignment.getSection())
                // ch.systemsx.cisd.openbis.generic.server.business.bo.EntityTypePropertyTypeBO.createAssignment() increases
                // the provided ordinal by one. Thus, we have to subtract 1 in order to get the same ordinal.
                .append("ordinal", new Long(existingAssignment.getOrdinal() - 1), incomingAssignment.getOrdinal())
                .append("showInEdit", existingAssignment.isShownInEditView(),
                        incomingAssignment.isDynamic() ? false : incomingAssignment.isShownInEditView())
                .append("plugin", getPluginName(existingAssignment.getScript()), incomingAssignment.getScriptName());
        Script plugin = existingAssignment.getScript();
        ScriptType existingPluginType = plugin == null ? null : plugin.getScriptType();
        ScriptType incomingPluginType = incomingAssignment.isDynamic() ? ScriptType.DYNAMIC_PROPERTY
                : (incomingAssignment.isManaged() ? ScriptType.MANAGED_PROPERTY : null);
        diffBuilder.append("pluginType", existingPluginType, incomingPluginType);
        DiffResult diffResult = diffBuilder.build();
        return render(diffResult, existingAssignment, incomingAssignment);

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
    private EntityTypePropertyType findInExistingPropertyAssignments(NewETPTAssignment incomingETPTAssignment,
            List<? extends EntityTypePropertyType> assignedPropertyTypes)
    {
        for (EntityTypePropertyType entityTypePropertyType : assignedPropertyTypes)
        {
            if (entityTypePropertyType.getPropertyType().getCode().equals(incomingETPTAssignment.getPropertyTypeCode()))
            {
                return entityTypePropertyType;
            }
        }
        return null;
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

    private void updateEntityType(EntityKind entityKind, EntityType incomingEntityType, String diff)
    {
        switch (entityKind)
        {
            case SAMPLE:
                synchronizerFacade.updateSampleType(incomingEntityType, diff);
                break;
            case DATA_SET:
                synchronizerFacade.updateDataSetType(incomingEntityType, diff);
                break;
            case EXPERIMENT:
                synchronizerFacade.updateExperimentType(incomingEntityType, diff);
                break;
            case MATERIAL:
                synchronizerFacade.updateMaterialType(incomingEntityType, diff);
                break;
            default:
                throw new UserFailureException("update not implemented for entity kind: " + entityKind.name());
        }
    }

    private String calculateDiff(EntityKind entityKind, EntityType existingEntityType, EntityType incomingEntityType)
    {
        DiffBuilder diffBuilder = new DiffBuilder(existingEntityType, incomingEntityType, ToStringStyle.SHORT_PREFIX_STYLE, false)
                .append("description", existingEntityType.getDescription(), incomingEntityType.getDescription())
                .append("validationPlugin", getPluginName(existingEntityType), getPluginName(incomingEntityType));
        switch (entityKind)
        {
            case SAMPLE:
                appendToDiffBuilder(diffBuilder, (SampleType) existingEntityType, (SampleType) incomingEntityType);
                break;
            case DATA_SET:
                appendToDiffBuilder(diffBuilder, (DataSetType) existingEntityType, (DataSetType) incomingEntityType);
                break;
            case EXPERIMENT:
                appendToDiffBuilder(diffBuilder, (ExperimentType) existingEntityType, (ExperimentType) incomingEntityType);
                break;
            case MATERIAL:
                appendToDiffBuilder(diffBuilder, (MaterialType) existingEntityType, (MaterialType) incomingEntityType);
                break;
            default:
                throw new UserFailureException("update not implemented for entity kind: " + entityKind.name());
        }
        DiffResult diffResult = diffBuilder.build();
        return render(diffResult, existingEntityType, incomingEntityType);
    }

    private String getPluginName(EntityType entityType)
    {
        return getPluginName(entityType.getValidationScript());
    }

    private String getPluginName(Script plugin)
    {
        return plugin == null ? null : plugin.getName();
    }

    private static String render(DiffResult diffResult, Object existing, Object incoming)
    {
        List<Diff<?>> diffs = diffResult.getDiffs();
        if (diffs.isEmpty())
        {
            return "";
        }
        ToStringBuilder builderExisting = new ToStringBuilder(existing, diffResult.getToStringStyle());
        ToStringBuilder builderIncoming = new ToStringBuilder(incoming, diffResult.getToStringStyle());
        for (Diff<?> diff : diffs)
        {
            builderExisting.append(diff.getFieldName(), diff.getLeft());
            builderIncoming.append(diff.getFieldName(), diff.getRight());
        }
        return "incoming " + builderIncoming.build() + " differs from existing " + builderExisting.build();
    }

    private void appendToDiffBuilder(DiffBuilder diffBuilder, SampleType existingType, SampleType incomingType)
    {
        diffBuilder.append("generatedCodePrefix", existingType.getGeneratedCodePrefix(), incomingType.getGeneratedCodePrefix())
                .append("listable", existingType.isListable(), incomingType.isListable())
                .append("showContainer", existingType.isShowContainer(), incomingType.isShowContainer())
                .append("showParents", existingType.isShowParents(), incomingType.isShowParents())
                .append("showParentMetadata", existingType.isShowParentMetadata(), incomingType.isShowParentMetadata())
                .append("subcodeUnique", existingType.isSubcodeUnique(), incomingType.isSubcodeUnique())
                .append("autoGeneratedCode", existingType.isAutoGeneratedCode(), incomingType.isAutoGeneratedCode());
    }

    private void appendToDiffBuilder(DiffBuilder diffBuilder, DataSetType existingType, DataSetType incomingType)
    {
        diffBuilder.append("mainDataSetPattern", existingType.getMainDataSetPattern(), incomingType.getMainDataSetPattern())
                .append("mainDataSetPath", existingType.getMainDataSetPath(), incomingType.getMainDataSetPath());
    }

    private void appendToDiffBuilder(DiffBuilder diffBuilder, ExperimentType existingType, ExperimentType incomingType)
    {
    }

    private void appendToDiffBuilder(DiffBuilder diffBuilder, MaterialType existingType, MaterialType incomingType)
    {
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
        Map<String, String> registratorByTypeCode = getRegistratorByTypeCode();
        for (String propTypeCode : propertyTypesToProcess.keySet())
        {
            PropertyType incomingPropertyType = propertyTypesToProcess.get(propTypeCode);
            String propertyTypeCode = incomingPropertyType.getCode();
            PropertyType existingPropertyType = propertyTypeMap.get(propertyTypeCode);
            if (existingPropertyType != null)
            {
                if (existingPropertyType.isManagedInternally() 
                        && isSystem(registratorByTypeCode.get(existingPropertyType.getCode())))
                {
                    continue;
                }
                String diff = calculateDiff(existingPropertyType, incomingPropertyType);
                if (StringUtils.isNotBlank(diff) && config.isMasterDataUpdateAllowed())
                {
                    incomingPropertyType.setModificationDate(existingPropertyType.getModificationDate());
                    incomingPropertyType.setId(existingPropertyType.getId());
                    synchronizerFacade.updatePropertyType(incomingPropertyType, diff);
                }
            } else
            {
                synchronizerFacade.registerPropertyType(incomingPropertyType);
            }
        }
    }

    private Map<String, String> getRegistratorByTypeCode()
    {
        PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
        fetchOptions.withRegistrator();
        PropertyTypeSearchCriteria searchCriteria = new PropertyTypeSearchCriteria();
        return v3api.searchPropertyTypes(sessionToken, searchCriteria, fetchOptions).getObjects()
                .stream().collect(Collectors.toMap(
                        type -> type.getCode(), type -> type.getRegistrator().getUserId()));
    }

    private String calculateDiff(PropertyType existingPropertyType, PropertyType incomingPropertyType)
    {
        DiffBuilder diffBuilder = new DiffBuilder(existingPropertyType, incomingPropertyType, ToStringStyle.SHORT_PREFIX_STYLE, false)
                .append("label", existingPropertyType.getLabel(), incomingPropertyType.getLabel())
                .append("dataType", existingPropertyType.getDataType().getCode(), incomingPropertyType.getDataType().getCode())
                .append("description", existingPropertyType.getDescription(), incomingPropertyType.getDescription())
                .append("managedInternally", existingPropertyType.isManagedInternally(), incomingPropertyType.isManagedInternally())
                .append("vocabulary", getCode(existingPropertyType.getVocabulary()),
                        getCode(incomingPropertyType.getVocabulary()))
                .append("material", getCode(existingPropertyType.getMaterialType()),
                        getCode(incomingPropertyType.getMaterialType()));
        DiffResult diffResult = diffBuilder.build();
        return render(diffResult, existingPropertyType, incomingPropertyType);
    }

    private String getCode(Vocabulary vocabulary)
    {
        if (vocabulary == null)
        {
            return null;
        }
        String code = vocabulary.getCode();
        if (vocabulary.isManagedInternally() && code.startsWith("$") == false)
        {
            code = "$" + code;
        }
        return code;
    }

    private String getCode(ICodeHolder codeHolder)
    {
        return codeHolder == null ? null : codeHolder.getCode();
    }

}
