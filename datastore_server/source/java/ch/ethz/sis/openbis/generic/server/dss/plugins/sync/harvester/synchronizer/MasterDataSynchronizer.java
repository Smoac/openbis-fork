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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class MasterDataSynchronizer
{
    final String sessionToken;

    final ICommonServer commonServer;

    final ResourceListParserData.MasterData masterData;

    final Map<TechId, List<VocabularyTerm>> vocabularyTermsToBeDeleted;

    public MasterDataSynchronizer(ResourceListParserData.MasterData masterData)
    {
        String openBisServerUrl = ServiceProvider.getConfigProvider().getOpenBisServerUrl();
        this.sessionToken = ServiceProvider.getOpenBISService().getSessionToken();
        this.commonServer = ServiceFinderUtils.getCommonServer(sessionToken, openBisServerUrl);
        this.masterData = masterData;
        vocabularyTermsToBeDeleted = new HashMap<TechId, List<VocabularyTerm>>();
    }
    
    public void synchronizeMasterData() {
        MultiKeyMap<String, List<NewETPTAssignment>> propertyAssignmentsToProcess = masterData.getPropertyAssignmentsToProcess();
        processVocabularies(masterData.getVocabulariesToProcess());
        // materials are registered but their property assignments are deferred until after property types are processed
        processEntityTypes(masterData.getMaterialTypesToProcess(), propertyAssignmentsToProcess);
        processPropertyTypes(masterData.getPropertyTypesToProcess());
        processEntityTypes(masterData.getSampleTypesToProcess(), propertyAssignmentsToProcess);
        processEntityTypes(masterData.getDataSetTypesToProcess(), propertyAssignmentsToProcess);
        processEntityTypes(masterData.getExperimentTypesToProcess(), propertyAssignmentsToProcess);
        processDeferredMaterialTypePropertyAssignments(propertyAssignmentsToProcess);
    }

    public void cleanupUnusedMasterData()
    {
        for (TechId vocabularyId : vocabularyTermsToBeDeleted.keySet())
        {
            commonServer.deleteVocabularyTerms(sessionToken, vocabularyId, vocabularyTermsToBeDeleted.get(vocabularyId),
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
                commonServer.updateVocabulary(sessionToken, existingVocabulary);
                // vocabulary.setTerms(newVocabulary.getTerms());

                processVocabularyTerms(sessionToken, commonServer, newVocabulary, existingVocabulary);
            }
            else
            {
                commonServer.registerVocabulary(sessionToken, newVocabulary);
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
            }
            else
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
        commonServer.addVocabularyTerms(sessionToken, new TechId(existingVocabulary.getId()), termsToBeAdded, null, true);
        for (VocabularyTerm term : termsToBeUpdated)
        {
            commonServer.updateVocabularyTerm(sessionToken, term);
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
                if (entityKind != EntityKind.MATERIAL) // defer material property assignments until after property types are processed
                {
                    if (list != null)
                    {
                        processPropertyAssignments(existingEntityType, list);
                    }
                }
            }
            else
            {
                registerEntityType(entityKind, incomingEntityType);
                if (list != null)
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
            commonServer.assignPropertyType(sessionToken, newETPTAssignment);
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
                commonServer.updatePropertyTypeAssignment(sessionToken, newETPTAssignment);
            }
            else
            {
                commonServer.assignPropertyType(sessionToken, newETPTAssignment);
            }
        }
        // remove property assignments that are no longer valid
        for (EntityTypePropertyType etpt : assignedPropertyTypes)
        {
            if (findInIncomingPropertyAssignments(etpt, incomingAssignmentsList) == false)
            {
                commonServer.unassignPropertyType(sessionToken, existingEntityType.getEntityKind(), etpt.getPropertyType().getCode(), etpt
                        .getEntityType().getCode());
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
                commonServer.registerSampleType(sessionToken, (SampleType) incomingEntityType);
                break;
            case DATA_SET:
                commonServer.registerDataSetType(sessionToken, (DataSetType) incomingEntityType);
                break;
            case EXPERIMENT:
                commonServer.registerExperimentType(sessionToken, (ExperimentType) incomingEntityType);
                break;
            case MATERIAL:
                commonServer.registerMaterialType(sessionToken, (MaterialType) incomingEntityType);
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
                commonServer.updateSampleType(sessionToken, incomingEntityType);
                break;
            case DATA_SET:
                commonServer.updateDataSetType(sessionToken, incomingEntityType);
                break;
            case EXPERIMENT:
                commonServer.updateExperimentType(sessionToken, incomingEntityType);
                break;
            case MATERIAL:
                commonServer.updateMaterialType(sessionToken, incomingEntityType);
                break;
            default:
                throw new UserFailureException("update not implemented for entity kind: " + entityKind.name());
        }
    }

    private List<? extends EntityType> getExistingEntityTypes(EntityKind entityKind)
    {
        switch (entityKind) {
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
                commonServer.updatePropertyType(sessionToken, propertyTypeOrNull);
            }
            else
            {
                commonServer.registerPropertyType(sessionToken, incomingPropertyType);
            }
        }
    }
}
