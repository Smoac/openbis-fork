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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Logger class for {@link CommonServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class CommonServerLogger extends AbstractServerLogger implements ICommonServer
{
    /**
     * Creates an instance for the specified session manager, invocation status and elapsed time.
     * The session manager is used to retrieve user information which will be a part of the log
     * message.
     */
    CommonServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful, final long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    //
    // IGenericServer
    //

    public List<GroupPE> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final String command = "list_groups";
        if (identifier == null || identifier.getDatabaseInstanceCode() == null)
        {
            logAccess(sessionToken, command);
        } else
        {
            logAccess(sessionToken, command, "DATABASE-INSTANCE(%s)", identifier);
        }
        return null;
    }

    public void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        logTracking(sessionToken, "register_group", "CODE(%s)", groupCode);
    }

    public List<PersonPE> listPersons(final String sessionToken)
    {
        logAccess(sessionToken, "list_persons");
        return null;
    }

    public void registerPerson(final String sessionToken, final String userID)
    {
        logTracking(sessionToken, "register_person", "CODE(%s)", userID);

    }

    public List<RoleAssignmentPE> listRoles(final String sessionToken)
    {
        logAccess(sessionToken, "list_roles");
        return null;
    }

    public void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s)  PERSON(%s)", roleCode, person);

    }

    public void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) PERSON(%s)", roleCode, person);

    }

    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_sample_types");
        return null;
    }

    public final List<SamplePE> listSamples(final String sessionToken,
            final ListSampleCriteriaDTO criteria)
    {
        logAccess(sessionToken, "list_samples", "TYPE(%s) OWNERS(%s) CONTAINER(%s) EXPERIMENT(%s)",
                criteria.getSampleType(), criteria.getOwnerIdentifiers(), criteria
                        .getContainerSampleId(), criteria.getExperimentId());
        return null;
    }

    public final List<SamplePropertyPE> listSamplesProperties(final String sessionToken,
            final ListSampleCriteriaDTO criteria, final List<PropertyTypePE> propertyCodes)
    {
        logAccess(sessionToken, "list_samples_properties", "CRITERIA(%s) PROPERTIES(%s)", criteria,
                propertyCodes.size());
        return null;
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<ExternalDataPE> listSampleExternalData(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "list_external_data", "ID(%s)", sampleId);
        return null;
    }

    public List<ExternalDataPE> listExperimentExternalData(final String sessionToken,
            final TechId experimentId)
    {
        logAccess(sessionToken, "list_external_data", "ID(%s)", experimentId);
        return null;
    }

    public final List<SearchHit> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText)
    {
        logAccess(sessionToken, "list_matching_entities", "SEARCHABLE-ENTITIES(%s) QUERY-TEXT(%s)",
                Arrays.toString(searchableEntities), queryText);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)", newSample
                .getSampleType(), newSample.getIdentifier());
    }

    public List<ExperimentPE> listExperiments(final String sessionToken,
            final ExperimentTypePE experimentType, final ProjectIdentifier project)
    {
        logAccess(sessionToken, "list_experiments", "TYPE(%s) PROJECT(%s)", experimentType, project);
        return null;
    }

    public List<ProjectPE> listProjects(final String sessionToken)
    {
        logAccess(sessionToken, "list_projects");
        return null;
    }

    public List<ExperimentTypePE> listExperimentTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_experiment_types");
        return null;
    }

    public List<PropertyTypePE> listPropertyTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_property_types");
        return null;
    }

    public final List<DataTypePE> listDataTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_data_types");
        return null;
    }

    public List<FileFormatTypePE> listFileFormatTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_file_format_types");
        return null;
    }

    public final List<VocabularyPE> listVocabularies(final String sessionToken, boolean withTerms,
            boolean excludeInternal)
    {
        logAccess(sessionToken, "list_vocabularies");
        return null;
    }

    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "assign_property_type", " PROPERTY_TYPE(%S) " + entityTypeFormat
                + " MANDATORY(%S) DEFAULT(%S)", propertyTypeCode, entityTypeCode, isMandatory,
                defaultValue);
        return null;
    }

    public void updatePropertyTypeAssignment(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode, boolean isMandatory, String defaultValue)
    {
        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "update_property_type_assignment", " PROPERTY_TYPE(%S) "
                + entityTypeFormat + " MANDATORY(%S) DEFAULT(%S)", propertyTypeCode,
                entityTypeCode, isMandatory, defaultValue);
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
        logTracking(sessionToken, "register_property_type", "PROPERTY_TYPE(%s)", propertyType
                .getCode());
    }

    public final void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary)
    {
        logTracking(sessionToken, "register_vocabulary", "VOCABULARY(%s)", vocabulary.getCode());
    }

    public void updateVocabulary(String sessionToken, IVocabularyUpdates updates)
    {
        logTracking(sessionToken, "update_vocabulary", "ID(%s) CODE(%s)", updates.getId(), updates
                .getCode());
    }

    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<String> vocabularyTerms)
    {
        logTracking(sessionToken, "add_vocabulary_terms", "ID(%s) NUMBER_OF_TERMS(%s)",
                vocabularyId, Integer.toString(vocabularyTerms.size()));
    }

    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced)
    {
        logTracking(sessionToken, "delete_vocabulary_terms",
                "VOCABULARY_ID(%s) NUMBER_OF_TERMS(%s) REPLACEMENTS(%s)", vocabularyId,
                termsToBeDeleted.size() + termsToBeReplaced.size(), termsToBeReplaced);
    }

    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId, List<AttachmentPE> attachments)
    {
        logTracking(sessionToken, "register_project", "PROJECT(%s) ATTACHMNETS(%s)",
                projectIdentifier, attachments.size());
    }

    public List<ExternalDataPE> searchForDataSets(String sessionToken,
            DataSetSearchCriteria criteria)
    {
        logAccess(sessionToken, "search_for_datasets");
        return null;
    }

    public List<MaterialTypePE> listMaterialTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_material_types");
        return null;
    }

    public List<MaterialPE> listMaterials(String sessionToken, MaterialTypePE materialType)
    {
        logAccess(sessionToken, "list_materials", "TYPE(%s)", materialType);
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

    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason)
    {
        logTracking(sessionToken, "delete_data_sets", "CODES(%s) REASON(%s)", dataSetCodes, reason);
    }

    public void deleteSamples(String sessionToken, List<TechId> sampleIds, String reason)
    {
        logTracking(sessionToken, "delete_samples", "IDS(%s) REASON(%s)", sampleIds, reason);
    }

    public void deleteExperiments(String sessionToken, List<TechId> experimentIds, String reason)
    {
        logTracking(sessionToken, "delete_experiments", "IDS(%s) REASON(%s)", experimentIds, reason);
    }

    public void deleteExperimentAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_experiment_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, fileNames, reason);
    }

    public void deleteSampleAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_sample_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, fileNames, reason);
    }

    public void deleteProjectAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        logTracking(sessionToken, "delete_project_attachments", "ID(%s) FILES(%s) REASON(%s)",
                experimentId, fileNames, reason);
    }

    public List<AttachmentPE> listExperimentAttachments(String sessionToken, TechId experimentId)
    {
        logAccess(sessionToken, "list_experiment_attachments", "ID(%s)", experimentId);
        return null;
    }

    public List<AttachmentPE> listSampleAttachments(String sessionToken, TechId sampleId)
    {
        logAccess(sessionToken, "list_sample_attachments", "ID(%s)", sampleId);
        return null;
    }

    public List<AttachmentPE> listProjectAttachments(String sessionToken, TechId projectId)
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

    public Set<VocabularyTermPE> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        logAccess(sessionToken, "list_vocabulary_terms", "VOCABULARY(%s)", vocabulary.getCode());
        return null;
    }

    public List<DataSetTypePE> listDataSetTypes(String sessionToken)
    {
        logAccess(sessionToken, "list_data_set_types");
        return null;
    }

    public LastModificationState getLastModificationState(String sessionToken)
    {
        return null;
    }

    public ProjectPE getProjectInfo(String sessionToken, TechId projectId)
    {
        logAccess(sessionToken, "get_project_info", "ID(%s)", projectId);
        return null;
    }

    public IEntityInformationHolder getEntityInformationHolder(String sessionToken,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, String permId)
    {

        final String entityTypeFormat = entityKind.name() + "_TYPE(%S)";
        logTracking(sessionToken, "get_entity_information_holder", entityTypeFormat
                + " PERM_ID(%S) ", entityKind, permId);
        return null;
    }

    public String generateCode(String sessionToken, String prefix)
    {
        logAccess(sessionToken, "generate_code", "PREFIX(%s)", prefix);
        return null;
    }

    public Date updateProject(String sessionToken, ProjectUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_project", "PROJECT(%s) ATTACHMENTS_ADDED(%s)", updates
                .getIdentifier(), updates.getAttachments().size());
        return null;
    }

    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_data_set_types", "CODES(%s)", StringUtils
                .join(entityTypesCodes.toArray(new String[0])));

    }

    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_experiment_types", "CODES(%s)", StringUtils
                .join(entityTypesCodes.toArray(new String[0])));

    }

    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_material_types", "CODES(%s)", StringUtils
                .join(entityTypesCodes.toArray(new String[0])));

    }

    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes)
    {
        logTracking(sessionToken, "delete_sample_types", "CODES(%s)", StringUtils
                .join(entityTypesCodes.toArray(new String[0])));

    }

    public List<String> getTemplateColumns(String sessionToken, EntityKind entityKind, String type,
            boolean autoGenerate)
    {
        logAccess(sessionToken, "get_template_columns",
                "ENTITY_KIND(%s) ENTITY_TYPE(%s) AUTO_GENERATE(%s)", entityKind, type, autoGenerate);
        return null;
    }

    public void deleteFileFormatTypes(String sessionToken, List<String> codes)
    {
        logTracking(sessionToken, "delete_file_format_types", "CODES(%s)", StringUtils.join(codes
                .toArray(new String[0])));

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

    public void updateProjectAttachments(String sessionToken, TechId projectId,
            Attachment attachment)
    {
        logTracking(sessionToken, "update_project_attachment", "PROJECT_ID(%s) ATTACHMENT(%s)",
                projectId, attachment.getFileName());

    }

    public void updateSampleAttachments(String sessionToken, TechId sampleId, Attachment attachment)
    {
        logTracking(sessionToken, "update_samle_attachment", "SAMPLE_ID(%s) ATTACHMENT(%s)",
                sampleId, attachment.getFileName());

    }

    public void deleteProjects(String sessionToken, List<TechId> projectIds, String reason)
    {
        logTracking(sessionToken, "delete_projects", "IDS(%s) REASON(%s)", projectIds, reason);
    }

}
