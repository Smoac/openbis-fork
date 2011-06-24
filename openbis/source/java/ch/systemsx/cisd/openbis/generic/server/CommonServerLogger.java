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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DynamicPropertyEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleParentWithDerivedDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Logger class for {@link CommonServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class CommonServerLogger extends AbstractServerLogger implements ICommonServerForInternalUse
{
    /**
     * Creates an instance for the specified session manager, invocation status and elapsed time.
     * The session manager is used to retrieve user information which will be a part of the log
     * message.
     */
    CommonServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    //
    // IGenericServer
    //

    public SessionContextDTO tryToAuthenticateAsSystem()
    {
        return null;
    }

    public List<Space> listSpaces(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final String command = "list_spaces";
        if (identifier == null || identifier.getDatabaseInstanceCode() == null)
        {
            logAccess(sessionToken, command);
        } else
        {
            logAccess(sessionToken, command, "DATABASE-INSTANCE(%s)", identifier);
        }
        return null;
    }

    public void registerSpace(final String sessionToken, final String groupCode,
            final String descriptionOrNull)
    {
        logTracking(sessionToken, "register_space", "CODE(%s)", groupCode);
    }

    public void updateScript(String sessionToken, IScriptUpdates updates)
    {
        logTracking(sessionToken, "update_script", "SCRIPT(%s)", updates.getId());
    }

    public void updateSpace(String sessionToken, ISpaceUpdates updates)
    {
        logTracking(sessionToken, "update_space", "SPACE(%s)", updates);
    }

    public List<Person> listPersons(final String sessionToken)
    {
        logAccess(sessionToken, "list_persons");
        return null;
    }

    public void registerPerson(final String sessionToken, final String userID)
    {
        logTracking(sessionToken, "register_person", "CODE(%s)", userID);

    }

    public List<RoleAssignment> listRoleAssignments(final String sessionToken)
    {
        logAccess(sessionToken, "list_roles");
        return null;
    }

    public void registerSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s) SPACE(%s) GRANTEE(%s)", roleCode,
                spaceIdentifier, grantee);

    }

    public void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s)  GRANTEE(%s)", roleCode, grantee);

    }

    public void deleteSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) SPACE(%s) GRANTEE(%s)", roleCode,
                spaceIdentifier, grantee);

    }

    public void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) GRANTEE(%s)", roleCode, grantee);

    }

    public final List<SampleType> listSampleTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_sample_types");
        return null;
    }

    public List<Sample> listSamples(String sessionToken, ListSampleCriteria criteria)
    {
        if (criteria.isIncludeSpace())
        {
            logAccess(sessionToken, "list_samples",
                    "TYPE(%s) OWNERS(space=%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(), criteria.getSpaceCode(),
                    criteria.getContainerSampleIds(), criteria.getParentSampleId(),
                    criteria.getChildSampleId(), criteria.getExperimentId());
        } else if (criteria.isIncludeInstance())
        {
            logAccess(
                    sessionToken,
                    "list_samples",
                    "TYPE(%s) OWNERS(instance=%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(), criteria.getSampleType().getDatabaseInstance(),
                    criteria.getContainerSampleIds(), criteria.getParentSampleId(),
                    criteria.getChildSampleId(), criteria.getExperimentId());
        } else
        {
            logAccess(sessionToken, "list_samples",
                    "TYPE(%s) CONTAINER(%s) PARENT(%s) CHILD(%s) EXPERIMENT(%s)",
                    criteria.getSampleType(), criteria.getContainerSampleIds(),
                    criteria.getParentSampleId(), criteria.getChildSampleId(),
                    criteria.getExperimentId());
        }
        return null;
    }

    public final List<SamplePropertyPE> listSamplesProperties(final String sessionToken,
            final ListSampleCriteriaDTO criteria, final List<PropertyTypePE> propertyCodes)
    {
        logAccess(sessionToken, "list_samples_properties", "CRITERIA(%s) PROPERTIES(%s)", criteria,
                abbreviate(propertyCodes));
        return null;
    }

    public final SampleParentWithDerivedDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<ExternalData> listSampleExternalData(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        logAccess(sessionToken, "list_external_data", "ID(%s) DIRECT(%s)", sampleId,
                showOnlyDirectlyConnected);
        return null;
    }

    public List<ExternalData> listExperimentExternalData(final String sessionToken,
            final TechId experimentId)
    {
        logAccess(sessionToken, "list_external_data", "ID(%s)", experimentId);
        return null;
    }

    public List<ExternalData> listDataSetRelationships(String sessionToken, TechId datasetId,
            DataSetRelationshipRole role)
    {
        logAccess(sessionToken, "list_dataset_relationships", "ID(%s), ROLE(%s)", datasetId, role);
        return null;
    }

    public final List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText,
            final boolean useWildcardSearchMode, int maxSIze)
    {
        logAccess(sessionToken, "list_matching_entities",
                "SEARCHABLE-ENTITIES(%s) QUERY-TEXT(%s) WILDCARD_MODE(%s) MAX_SIZE(%s)",
                abbreviate(searchableEntities), queryText, useWildcardSearchMode, maxSIze);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)",
                newSample.getSampleType(), newSample.getIdentifier());
    }

    public List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final ProjectIdentifier project)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) PROJECT(%s)", experimentType, project);
        return null;
    }

    public List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final SpaceIdentifier space)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) SPACE(%s)", experimentType, space);
        return null;
    }

    public List<Project> listProjects(final String sessionToken)
    {
        logAccess(sessionToken, "list_projects");
        return null;
    }

    public List<ExperimentType> listExperimentTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_experiment_types");
        return null;
    }

    public List<PropertyType> listPropertyTypes(final String sessionToken, boolean withRelations)
    {
        logAccess(sessionToken, "list_property_types", withRelations ? "WITH_RELATIONS" : "");
        return null;
    }

    public final List<DataType> listDataTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_data_types");
        return null;
    }

    public final List<Script> listScripts(final String sessionToken, ScriptType scriptTypeOrNull,
            EntityKind entityKindOrNull)
    {
        logAccess(sessionToken, "list_scripts", "SCRIPT_TYPE(%s) ENTITY_KIND(%s)",
                scriptTypeOrNull, entityKindOrNull);
        return null;
    }

    public List<FileFormatType> listFileFormatTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_file_format_types");
        return null;
    }

    public final List<Vocabulary> listVocabularies(final String sessionToken, boolean withTerms,
            boolean excludeInternal)
    {
        logAccess(sessionToken, "list_vocabularies");
        return null;
    }

    public String assignPropertyType(final String sessionToken, NewETPTAssignment assignment)
    {
        final String entityTypeFormat = assignment.getEntityKind().name() + "_TYPE(%S)";
        logTracking(sessionToken, "assign_property_type", " PROPERTY_TYPE(%S) " + entityTypeFormat
                + " MANDATORY(%S) DEFAULT(%S) SECTION(%S) PREVIOUS_ORDINAL(%S)",
                assignment.getPropertyTypeCode(), assignment.getEntityTypeCode(),
                assignment.isMandatory(), assignment.getDefaultValue(), assignment.getSection(),
                assignment.getOrdinal());
        return null;
    }

    public void updatePropertyTypeAssignment(String sessionToken,
            NewETPTAssignment assignmentUpdates)
    {
        final String entityTypeFormat = assignmentUpdates.getEntityKind().name() + "_TYPE(%S)";
        logTracking(sessionToken, "update_property_type_assignment", " PROPERTY_TYPE(%S) "
                + entityTypeFormat + " MANDATORY(%S) DEFAULT(%S) SECTION(%S) PREVIOUS_ORDINAL(%S)",
                assignmentUpdates.getPropertyTypeCode(), assignmentUpdates.getEntityTypeCode(),
                assignmentUpdates.isMandatory(), assignmentUpdates.getDefaultValue(),
                assignmentUpdates.getSection(), assignmentUpdates.getOrdinal());
    }

    public void unassignPropertyType(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "unassign_property_type", " PROPERTY_TYPE(%S) "
                + entityTypeFormat, propertyTypeCode, entityTypeCode);
    }

    public int countPropertyTypedEntities(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logAccess(sessionToken, "count_property_typed_entities", "PROPERTY_TYPE(%s) "
                + entityTypeFormat, propertyTypeCode, entityTypeCode);
        return 0;
    }

    public final void registerPropertyType(final String sessionToken,
            final PropertyType propertyType)
    {
        logTracking(sessionToken, "register_property_type", "PROPERTY_TYPE(%s)",
                propertyType.getCode());
    }

    public void updatePropertyType(String sessionToken, IPropertyTypeUpdates updates)
    {
        logTracking(sessionToken, "update_property_type", "PROPERTY_TYPE(%s)", updates);
    }

    public final void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary)
    {
        logTracking(sessionToken, "register_vocabulary", "VOCABULARY(%s)", vocabulary.getCode());
    }

    public void updateVocabulary(String sessionToken, IVocabularyUpdates updates)
    {
        logTracking(sessionToken, "update_vocabulary", "ID(%s) CODE(%s)", updates.getId(),
                updates.getCode());
    }

    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<String> vocabularyTerms, Long previousTermOrdinal)
    {
        logTracking(sessionToken, "add_vocabulary_terms", "ID(%s) TERMS(%s) PREVIOUS_ORDINAL(%s)",
                vocabularyId, abbreviate(vocabularyTerms), Long.toString(previousTermOrdinal));
    }

    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        logTracking(sessionToken, "add_unofficial_vocabulary_terms",
                "ID(%s) CODE(%s), LABEL(%s), DESCRIPTION(%s), PREVIOUS_ORDINAL(%s)", vocabularyId,
                code, label, description, Long.toString(previousTermOrdinal));
    }

    public void updateVocabularyTerm(String sessionToken, IVocabularyTermUpdates updates)
    {
        logTracking(sessionToken, "update_vocabulary_term", "VOCABULARY_TERM(%s)", updates);
    }

    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced)
    {
        logTracking(sessionToken, "delete_vocabulary_terms",
                "VOCABULARY_ID(%s) DELETED(%s) REPLACEMENTS(%s)", vocabularyId,
                abbreviate(termsToBeDeleted), abbreviate(termsToBeReplaced));
    }

    public void makeVocabularyTermsOfficial(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeOfficial)
    {
        logTracking(sessionToken, "make_vocabulary_terms_official",
                "VOCABULARY_ID(%s) OFFICIAL(%s)", vocabularyId, abbreviate(termsToBeOfficial));
    }

    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId, Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "register_project", "PROJECT(%s) ATTACHMNETS(%s)",
                projectIdentifier, abbreviate(attachments));
    }

    public List<ExternalData> searchForDataSets(String sessionToken, DetailedSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_datasets", "criteria(%s)", criteria);
        return null;
    }

    public List<Sample> searchForSamples(String sessionToken, DetailedSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_samples", "criteria(%s)", criteria);
        return null;
    }

    public ExternalData getDataSetInfo(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getDataSetInfo", "datasetId(%s)", datasetId.getId());
        return null;
    }

    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates)
    {
        logTracking(sessionToken, "updateDataSet", "DATA_SET(%s)", updates.getDatasetId());
        return null;
    }

    public List<ExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities entities)
    {
        logAccess(sessionToken, "list_related_datasets");
        return null;
    }

    public List<MaterialType> listMaterialTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_material_types");
        return null;
    }

    public List<Material> listMaterials(String sessionToken, MaterialType materialType,
            boolean withProperties)
    {
        logAccess(sessionToken, "list_materials", "TYPE(%s) withProperties(%s)", materialType,
                withProperties);
        return null;
    }

    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties)
    {
        logAccess(sessionToken, "list_materials", "TYPE(%s) IDS(%s) withProperties(%s)",
                criteria.tryGetMaterialType(), criteria.tryGetMaterialIds() == null ? "-"
                        : abbreviate(criteria.tryGetMaterialIds()), withProperties);
        return null;
    }

    public void registerMaterialType(String sessionToken, MaterialType entityType)
    {
        logTracking(sessionToken, "register_material_type", "CODE(%s)", entityType.getCode());
    }

    public void updateMaterialType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_material_type", "CODE(%s)", entityType.getCode());
    }

    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        logTracking(sessionToken, "register_sample_type", "CODE(%s)", entityType.getCode());
    }

    public void updateSampleType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_sample_type", "CODE(%s)", entityType.getCode());
    }

    public void registerExperimentType(String sessionToken, ExperimentType entityType)
    {
        logTracking(sessionToken, "register_experiment_type", "CODE(%s)", entityType.getCode());
    }

    public void updateExperimentType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_experiment_type", "CODE(%s)", entityType.getCode());
    }

    public void registerFileFormatType(String sessionToken, FileFormatType type)
    {
        logTracking(sessionToken, "register_file_format_type", "CODE(%s)", type.getCode());
    }

    public void registerDataSetType(String sessionToken, DataSetType entityType)
    {
        logTracking(sessionToken, "register_data_set_type", "CODE(%s)", entityType.getCode());
    }

    public void updateDataSetType(String sessionToken, EntityType entityType)
    {
        logTracking(sessionToken, "update_data_set_type", "CODE(%s)", entityType.getCode());
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType deletionType)
    {
        logTracking(sessionToken, "delete_data_sets", "TYPE(%s) CODES(%s) REASON(%s)",
                deletionType, abbreviate(dataSetCodes), reason);
    }

    public void deleteSamples(String sessionToken, List<TechId> sampleIds, String reason,
            DeletionType deletionType)
    {
        logTracking(sessionToken, "delete_samples", "TYPE(%s) IDS(%s) REASON(%s)", deletionType,
                abbreviate(sampleIds), reason);
    }

    public void deleteExperiments(String sessionToken, List<TechId> experimentIds, String reason,
            DeletionType deletionType)
    {
        logTracking(sessionToken, "delete_experiments", "TYPE(%s) IDS(%s) REASON(%s)",
                deletionType, abbreviate(experimentIds), reason);
    }

    public void deleteVocabularies(String sessionToken, List<TechId> vocabularyIds, String reason)
    {
        logTracking(sessionToken, "delete_vocabularies", "IDS(%s) REASON(%s)",
                abbreviate(vocabularyIds), reason);
    }

    public void deletePropertyTypes(String sessionToken, List<TechId> propertyTypeIds, String reason)
    {
        logTracking(sessionToken, "delete_property_types", "IDS(%s) REASON(%s)",
                abbreviate(propertyTypeIds), reason);
    }

    public void deleteProjects(String sessionToken, List<TechId> projectIds, String reason)
    {
        logTracking(sessionToken, "delete_projects", "IDS(%s) REASON(%s)", abbreviate(projectIds),
                reason);
    }

    public void deleteSpaces(String sessionToken, List<TechId> groupIds, String reason)
    {
        logTracking(sessionToken, "delete_spaces", "IDS(%s) REASON(%s)", abbreviate(groupIds),
                reason);
    }

    public void deleteScripts(String sessionToken, List<TechId> scriptIds)
    {
        logTracking(sessionToken, "delete_scripts", "IDS(%s)", abbreviate(scriptIds));
    }

    public void deleteExperimentAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_experiment_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, abbreviate(fileNames), reason);
    }

    public void deleteSampleAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_sample_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, abbreviate(fileNames), reason);
    }

    public void deleteProjectAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_project_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, abbreviate(fileNames), reason);
    }

    public List<Attachment> listExperimentAttachments(String sessionToken, TechId experimentId)
    {
        logAccess(sessionToken, "list_experiment_attachments", "ID(%s)", experimentId);
        return null;
    }

    public List<Attachment> listSampleAttachments(String sessionToken, TechId sampleId)
    {
        logAccess(sessionToken, "list_sample_attachments", "ID(%s)", sampleId);
        return null;
    }

    public List<Attachment> listProjectAttachments(String sessionToken, TechId projectId)
    {
        logAccess(sessionToken, "list_project_attachments", "ID(%s)", projectId);
        return null;
    }

    public String uploadDataSets(String sessionToken, List<String> dataSetCodes,
            DataSetUploadContext uploadContext)
    {
        logTracking(sessionToken, "upload_data_sets", "CODES(%s) CIFEX-URL(%s) FILE(%s)",
                dataSetCodes, uploadContext.getCifexURL(), uploadContext.getFileName());
        return null;
    }

    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary)
    {
        logAccess(sessionToken, "list_vocabulary_terms_with_statistics", "VOCABULARY(%s)",
                vocabulary.getCode());
        return null;
    }

    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        logAccess(sessionToken, "list_vocabulary_terms", "VOCABULARY(%s)", vocabulary.getCode());
        return null;
    }

    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_data_set_types");
        return null;
    }

    public LastModificationState getLastModificationState(String sessionToken)
    {
        return null;
    }

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "get_sample_info", "ID(%s)", sampleId);
        return null;
    }

    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_sample",
                "SAMPLE(%s), CHANGE_TO_EXPERIMENT(%s) ATTACHMENTS(%s)",
                updates.getSampleIdOrNull(), updates.getExperimentIdentifierOrNull(), updates
                        .getAttachments().size());
        return null;
    }

    public Experiment getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        logAccess(sessionToken, "get_experiment_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public Experiment getExperimentInfo(final String sessionToken, final TechId experimentId)
    {
        logAccess(sessionToken, "get_experiment_info", "ID(%s)", experimentId);
        return null;
    }

    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates)
    {
        logTracking(sessionToken, "update_experiment", "EXPERIMENT(%s)", updates.getExperimentId());
        return null;
    }

    public Project getProjectInfo(String sessionToken, TechId projectId)
    {
        logAccess(sessionToken, "get_project_info", "ID(%s)", projectId);
        return null;
    }

    public Project getProjectInfo(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        logAccess(sessionToken, "get_project_info", "IDENTIFIER(%s)", projectIdentifier);
        return null;
    }

    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, String permId)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "get_entity_information_holder", entityTypeFormat
                + " PERM_ID(%S) ", entityKind, permId);
        return null;
    }

    public IEntityInformationHolderWithPermId getMaterialInformationHolder(String sessionToken,
            MaterialIdentifier identifier)
    {
        logTracking(sessionToken, "get_material_information_holder", " IDENTIFIER(%S) ", identifier);
        return null;
    }

    public Material getMaterialInfo(String sessionToken, MaterialIdentifier identifier)
    {
        logTracking(sessionToken, "getMaterialInfo", " IDENTIFIER(%S) ", identifier);
        return null;
    }

    public Material getMaterialInfo(final String sessionToken, final TechId materialId)
    {
        logAccess(sessionToken, "get_material_info", "ID(%s)", materialId);
        return null;
    }

    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, Date version)
    {
        logTracking(sessionToken, "edit_material", "MATERIAL(%s)", materialId);
        return null;
    }

    public String generateCode(String sessionToken, String prefix)
    {
        logAccess(sessionToken, "generate_code", "PREFIX(%s)", prefix);
        return null;
    }

    public Date updateProject(String sessionToken, ProjectUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_project", "PROJECT_ID(%s) ATTACHMENTS_ADDED(%s)",
                updates.getTechId(), updates.getAttachments().size());
        return null;
    }

    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_data_set_types", "CODES(%s)",
                abbreviate(entityTypesCodes));
    }

    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_experiment_types", "CODES(%s)",
                abbreviate(entityTypesCodes));
    }

    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_material_types", "CODES(%s)",
                abbreviate(entityTypesCodes));
    }

    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_sample_types", "CODES(%s)", abbreviate(entityTypesCodes));
    }

    public String getTemplateColumns(String sessionToken, EntityKind entityKind, String type,
            boolean autoGenerate, boolean withExperiments, BatchOperationKind operationKind)
    {
        logAccess(sessionToken, "get_template_columns",
                "ENTITY_KIND(%s) ENTITY_TYPE(%s) AUTO_GENERATE(%s) WITH_EXP(%s) OPERATION(%s)",
                entityKind, type, autoGenerate, withExperiments, operationKind.getDescription());
        return null;
    }

    public void deleteFileFormatTypes(String sessionToken, List<String> codes)
    {
        logTracking(sessionToken, "delete_file_format_types", "CODES(%s)", abbreviate(codes));
    }

    public void updateFileFormatType(String sessionToken, AbstractType type)
    {
        logTracking(sessionToken, "update_file_format_type", "CODE(%s)", type.getCode());

    }

    public void updateExperimentAttachments(String sessionToken, TechId experimentId,
            Attachment attachment)
    {
        logTracking(sessionToken, "update_experiment_attachment",
                "EXPERIMENT_ID(%s) ATTACHMENT(%s)", experimentId, attachment.getFileName());
    }

    public void addExperimentAttachment(String sessionToken, TechId experimentId,
            NewAttachment attachment)
    {
        logTracking(sessionToken, "add_experiment_attachment", "EXPERIMENT_ID(%s) ATTACHMENT(%s)",
                experimentId, attachment.getFileName());
    }

    public void updateProjectAttachments(String sessionToken, TechId projectId,
            Attachment attachment)
    {
        logTracking(sessionToken, "update_project_attachment", "PROJECT_ID(%s) ATTACHMENT(%s)",
                projectId, attachment.getFileName());
    }

    public void addProjectAttachments(String sessionToken, TechId projectId,
            NewAttachment attachment)
    {
        logTracking(sessionToken, "add_project_attachment", "PROJECT_ID(%s) ATTACHMENT(%s)",
                projectId, attachment.getFileName());
    }

    public void updateSampleAttachments(String sessionToken, TechId sampleId, Attachment attachment)
    {
        logTracking(sessionToken, "update_sample_attachment", "SAMPLE_ID(%s) ATTACHMENT(%s)",
                sampleId, attachment.getFileName());
    }

    public void addSampleAttachments(String sessionToken, TechId sampleId, NewAttachment attachment)
    {
        logTracking(sessionToken, "add_sample_attachment", "SAMPLE_ID(%s) ATTACHMENT(%s)",
                sampleId, attachment.getFileName());
    }

    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken,
            DataStoreServiceKind dataStoreServiceKind)
    {
        logAccess(sessionToken, "listDataStoreServices");
        return null;
    }

    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        logAccess(sessionToken, "createReportFromDatasets", "SERVICE(%s), DATASETS(%s)",
                serviceDescription, abbreviate(datasetCodes));
        return null;
    }

    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        logTracking(sessionToken, "processDatasets", "SERVICE(%s), DATASETS(%s)",
                serviceDescription, abbreviate(datasetCodes));
    }

    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup)
    {
        logTracking(sessionToken, "registerAuthorizationGroup", "CODE(%s)",
                newAuthorizationGroup.getCode());

    }

    public void registerScript(String sessionToken, Script script)
    {
        logTracking(sessionToken, "registerScript", "NAME(%s)", script.getName());

    }

    public void deleteAuthorizationGroups(String sessionToken, List<TechId> authGroupIds,
            String reason)
    {
        logTracking(sessionToken, "deleteAuthorizationGroups", "TECH_IDS(%s)",
                abbreviate(authGroupIds));
    }

    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken)
    {
        logAccess(sessionToken, "listAuthorizatonGroups");
        return null;
    }

    public Date updateAuthorizationGroup(String sessionToken, AuthorizationGroupUpdates updates)
    {
        logTracking(sessionToken, "updateAuthorizationGroup", "TECH_ID(%s)", updates.getId());
        return null;
    }

    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId)
    {
        logAccess(sessionToken, "listPersonInAuthorizationGroup", "ID(%s)", authorizatonGroupId);
        return null;
    }

    public void addPersonsToAuthorizationGroup(String sessionToken, TechId authorizationGroupId,
            List<String> personsCodes)
    {
        logTracking(sessionToken, "addPersonsToAuthorizationGroup", "TECH_ID(%s) PERSONS(%s)",
                authorizationGroupId, abbreviate(personsCodes));
    }

    public void removePersonsFromAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes)
    {
        logTracking(sessionToken, "removePersonsFromAuthorizationGroup", "TECH_ID(%s) PERSONS(%s)",
                authorizationGroupId, abbreviate(personsCodes));
    }

    public List<GridCustomFilter> listFilters(String sessionToken, String gridId)
    {
        logAccess(sessionToken, "listFilters", "GRID(%s)", gridId);
        return null;
    }

    public void registerFilter(String sessionToken, NewColumnOrFilter filter)
    {
        logTracking(sessionToken, "registerFilter", "FILTER(%s)", filter);
    }

    public void deleteFilters(String sessionToken, List<TechId> filterIds)
    {
        logTracking(sessionToken, "deleteFilters", "TECH_IDS(%s)", abbreviate(filterIds));
    }

    public void updateFilter(String sessionToken, IExpressionUpdates updates)
    {
        logTracking(sessionToken, "updateFilters", "ID(%s) NAME(%s)", updates.getId(),
                updates.getName());
    }

    // -- columns

    public void registerGridCustomColumn(String sessionToken, NewColumnOrFilter column)
    {
        logTracking(sessionToken, "registerGridCustomColumn", "COLUMN(%s)", column);
    }

    public void deleteGridCustomColumns(String sessionToken, List<TechId> columnIds)
    {
        logTracking(sessionToken, "deleteGridCustomColumns", "TECH_IDS(%s)", abbreviate(columnIds));
    }

    public void updateGridCustomColumn(String sessionToken, IExpressionUpdates updates)
    {
        logTracking(sessionToken, "updateGridCustomColumn", "ID(%s) NAME(%s)", updates.getId(),
                updates.getName());
    }

    public void keepSessionAlive(String sessionToken)
    {
        logAccess(Level.DEBUG, sessionToken, "keepSessionAlive", "TOKEN(%s)", sessionToken);
    }

    public void updateVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> terms)
    {
        logTracking(sessionToken, "update_vocabulary_terms", "VOCABULARY_TERMS(%s) VOCABULARY(%s)",
                abbreviate(terms), vocabularyId);
    }

    public void deleteMaterials(String sessionToken, List<TechId> materialIds, String reason)
    {
        logTracking(sessionToken, "delete_materials", "IDS(%s) REASON(%s)",
                abbreviate(materialIds), reason);
    }

    public int lockDatasets(String sessionToken, List<String> datasetCodes)
    {
        logTracking(sessionToken, "lockDatasets", "DATASETS(%s)", abbreviate(datasetCodes));
        return 0;
    }

    public int unlockDatasets(String sessionToken, List<String> datasetCodes)
    {
        logTracking(sessionToken, "unlockDatasets", "DATASETS(%s)", abbreviate(datasetCodes));
        return 0;
    }

    public LinkModel retrieveLinkFromDataSet(String sessionToken,
            DatastoreServiceDescription serviceDescription, String dataSetCode)
    {
        {
            logAccess(sessionToken, "retrieveLinkFromDataSet", "SERVICE(%s), DATASET(%s)",
                    serviceDescription, dataSetCode);
            return null;
        }
    }

    public Script getScriptInfo(String sessionToken, TechId scriptId)
    {
        logAccess(sessionToken, "getScriptInfo", "SCRIPT(%s)", scriptId);
        return null;
    }

    public String evaluate(String sessionToken, DynamicPropertyEvaluationInfo info)
    {
        logAccess(sessionToken, "evaluate", "%s(%s)", info.getEntityKind().name(),
                info.getEntityIdentifier());
        return null;
    }

    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            BasicEntityDescription info)
    {
        logAccess(sessionToken, "getEntityInformationHolder", "KIND(%s) IDENTIFIER(%s)",
                info.getEntityKind(), info.getEntityIdentifier());
        return null;
    }

    public void updateManagedPropertyOnExperiment(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnExperiment",
                "ID(%s) PROPERTY(%s) ACTION(%s)", experimentId,
                managedProperty.getPropertyTypeCode(), updateAction.getName());
    }

    public void updateManagedPropertyOnSample(String sessionToken, TechId sampleId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnSample",
                "ID(%s) PROPERTY(%s) ACTION(%s)", sampleId, managedProperty.getPropertyTypeCode(),
                updateAction.getName());
    }

    public void updateManagedPropertyOnDataSet(String sessionToken, TechId dataSetId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnDataSet",
                "ID(%s) PROPERTY(%s) ACTION(%s)", dataSetId, managedProperty.getPropertyTypeCode(),
                updateAction.getName());
    }

    public void updateManagedPropertyOnMaterial(String sessionToken, TechId materialId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        logTracking(sessionToken, "updateManagedPropertyOnMaterial",
                "ID(%s) PROPERTY(%s) ACTION(%s)", materialId,
                managedProperty.getPropertyTypeCode(), updateAction.getName());
    }

    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        logAccess(sessionToken, "getDefaultPutDataStoreBaseURL");
        return null;
    }

    public void updateDataSetProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        logTracking(sessionToken, "updateDataSetProperty", "ID(%s) MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    public void updateExperimentProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        logTracking(sessionToken, "updateExperimentProperty", "ID(%s) MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    public void updateSampleProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        logTracking(sessionToken, "updateSampleProperty", "ID(%s) MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    public void updateMaterialProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)

    {
        logTracking(sessionToken, "updateMaterialProperty", "ID(%s) MODIFIED_PROPERTIES(%s)",
                entityId, abbreviate(modifiedProperties));
    }

    public List<Experiment> listExperiments(String sessionToken,
            Collection<ExperimentIdentifier> experimentIdentifiers)
    {
        logTracking(sessionToken, "listExperiments", "experimentIdentifiers(%s)",
                abbreviate(experimentIdentifiers));
        return null;
    }

}
