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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListPersonsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;

/**
 * Service interface for the generic GWT client.
 * <p>
 * Each method should throw {@link UserFailureException}. The authorization framework can throw it
 * when the user has insufficient privileges. If it is not marked, the GWT client will report
 * unexpected exception.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public interface ICommonClientService extends IClientService
{
    /**
     * Returns a list of all groups.
     */
    public ResultSet<Group> listGroups(DefaultResultSetConfig<String, Group> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for groups.
     */
    public String prepareExportGroups(final TableExportCriteria<Group> criteria)
            throws UserFailureException;

    /**
     * Registers a new group with specified code and optional description.
     */
    public void registerGroup(String groupCode, String descriptionOrNull)
            throws UserFailureException;

    /**
     * Updates group.
     */
    public void updateGroup(final IGroupUpdates updates) throws UserFailureException;

    /**
     * Returns a list of all persons which belong to the current database instance.
     */
    public ResultSet<Person> listPersons(ListPersonsCriteria criteria) throws UserFailureException;

    /**
     * Returns a list of persons registered in given database instance.
     */
    public List<Person> listPersons() throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for persons.
     */
    public String prepareExportPersons(final TableExportCriteria<Person> criteria)
            throws UserFailureException;

    /**
     * Registers a new person with specified code.
     */
    public void registerPerson(String code) throws UserFailureException;

    /**
     * Returns a list of all role assignments.
     */
    public ResultSet<RoleAssignment> listRoleAssignments(
            DefaultResultSetConfig<String, RoleAssignment> criteria) throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for role assignments.
     */
    public String prepareExportRoleAssignments(final TableExportCriteria<RoleAssignment> criteria)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code, group code and grantee.
     */
    public void registerGroupRole(RoleSetCode roleSetCode, String group, Grantee grantee)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code, group code and grantee.
     */
    public void deleteGroupRole(RoleSetCode roleSetCode, String group, Grantee grantee)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code and grantee.
     */
    public void registerInstanceRole(RoleSetCode roleSetCode, Grantee grantee)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code and grantee.
     */
    public void deleteInstanceRole(RoleSetCode roleSetCode, Grantee grantee)
            throws UserFailureException;

    /**
     * Returns a list of sample types.
     */
    public List<SampleType> listSampleTypes() throws UserFailureException;

    /**
     * Returns a list of samples for given sample type.
     */
    public ResultSet<Sample> listSamples(final ListSampleCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a key which can be used be the export servlet (and eventually
     * {@link #getExportTable(String, String)}) to reference the export criteria in an easy way.
     */
    public String prepareExportSamples(final TableExportCriteria<Sample> criteria)
            throws UserFailureException;

    /**
     * Returns a list of experiments.
     */
    public ResultSet<Experiment> listExperiments(final ListExperimentsCriteria criteria)
            throws UserFailureException;

    /**
     * Returns a list of materials.
     */
    public ResultSet<Material> listMaterials(final ListMaterialCriteria criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for experiments.
     */

    public String prepareExportExperiments(final TableExportCriteria<Experiment> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for data set search hits.
     */
    public String prepareExportDataSetSearchHits(TableExportCriteria<ExternalData> exportCriteria)
            throws UserFailureException;

    /**
     * Lists the entities matching the search.
     */
    public ResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final IResultSetConfig<String, MatchingEntity> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for matching entites.
     */
    public String prepareExportMatchingEntities(final TableExportCriteria<MatchingEntity> criteria)
            throws UserFailureException;

    /**
     * Returns all property types.
     */
    public List<PropertyType> listPropertyTypes(boolean withRelations) throws UserFailureException;

    /**
     * Returns a chunk of the property types list.
     */
    public ResultSet<PropertyType> listPropertyTypes(
            DefaultResultSetConfig<String, PropertyType> criteria) throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for property types.
     */
    public String prepareExportPropertyTypes(final TableExportCriteria<PropertyType> criteria)
            throws UserFailureException;

    /**
     * Returns a chunk of the property types assignment list.
     */
    public ResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignments(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for property types assignments.
     */
    public String prepareExportPropertyTypeAssignments(
            final TableExportCriteria<EntityTypePropertyType<?>> criteria)
            throws UserFailureException;

    /**
     * Returns the number of entities of specified kind and type which have a property of specified
     * type.
     */
    public int countPropertyTypedEntities(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode) throws UserFailureException;

    /**
     * Returns a list of all projects.
     */
    public ResultSet<Project> listProjects(DefaultResultSetConfig<String, Project> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for projects.
     */
    public String prepareExportProjects(final TableExportCriteria<Project> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all vocabularies.
     * <p>
     * Note that the vocabulary terms are included/loaded.
     * </p>
     */
    public ResultSet<Vocabulary> listVocabularies(boolean withTerms, boolean excludeInternal,
            DefaultResultSetConfig<String, Vocabulary> criteria) throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for Vocabularies.
     */
    public String prepareExportVocabularies(final TableExportCriteria<Vocabulary> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all vocabulary terms for a specified vocabulary.
     */
    public ResultSet<VocabularyTermWithStats> listVocabularyTerms(Vocabulary vocabulary,
            DefaultResultSetConfig<String, VocabularyTermWithStats> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for Vocabulary Terms.
     */
    public String prepareExportVocabularyTerms(TableExportCriteria<VocabularyTermWithStats> criteria)
            throws UserFailureException;

    public ResultSet<MaterialType> listMaterialTypes(
            DefaultResultSetConfig<String, MaterialType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for MaterialType.
     */
    public String prepareExportMaterialTypes(final TableExportCriteria<MaterialType> criteria)
            throws UserFailureException;

    public ResultSet<SampleType> listSampleTypes(DefaultResultSetConfig<String, SampleType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for SampleType.
     */
    public String prepareExportSampleTypes(final TableExportCriteria<SampleType> criteria)
            throws UserFailureException;

    public ResultSet<ExperimentType> listExperimentTypes(
            DefaultResultSetConfig<String, ExperimentType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for ExperimentType.
     */
    public String prepareExportExperimentTypes(final TableExportCriteria<ExperimentType> criteria)
            throws UserFailureException;

    public ResultSet<DataSetType> listDataSetTypes(
            DefaultResultSetConfig<String, DataSetType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for DataSetType.
     */
    public String prepareExportDataSetTypes(final TableExportCriteria<DataSetType> criteria)
            throws UserFailureException;

    public ResultSet<FileFormatType> listFileTypes(
            DefaultResultSetConfig<String, FileFormatType> criteria) throws UserFailureException;

    public List<FileFormatType> listFileTypes() throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for FileType.
     */
    public String prepareExportFileTypes(TableExportCriteria<FileFormatType> criteria)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for AttachmentVersions.
     */
    public String prepareExportAttachmentVersions(TableExportCriteria<AttachmentVersions> criteria)
            throws UserFailureException;

    /**
     * Assumes that preparation of the export ( {@link #prepareExportSamples(TableExportCriteria)}
     * or {@link #prepareExportExperiments(TableExportCriteria)} has been invoked before and
     * returned with an exportDataKey passed here as a parameter.
     */
    public String getExportTable(String exportDataKey, String lineSeparator)
            throws UserFailureException;

    /**
     * Removes the session result set associated with given key.
     */
    public void removeResultSet(final String resultSetKey) throws UserFailureException;

    /**
     * For given <var>sampleId</var> returns corresponding list of {@link ExternalData}.
     */
    public ResultSet<ExternalData> listSampleDataSets(final TechId sampleId,
            final String baseIndexURL, DefaultResultSetConfig<String, ExternalData> criteria)
            throws UserFailureException;

    /**
     * For given <var>experimentId</var> returns corresponding list of {@link ExternalData}.
     */
    public ResultSet<ExternalData> listExperimentDataSets(final TechId experimentId,
            final String baseIndexURL, DefaultResultSetConfig<String, ExternalData> criteria)
            throws UserFailureException;

    /**
     * Lists the searchable entities.
     */
    public List<SearchableEntity> listSearchableEntities() throws UserFailureException;

    /**
     * Returns a list of all experiment types.
     */
    public List<ExperimentType> listExperimentTypes() throws UserFailureException;

    /**
     * Returns a list of all data types.
     */
    public List<DataType> listDataTypes() throws UserFailureException;

    /**
     * Assigns property type to entity type.
     */
    public String assignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, boolean isMandatory, String defaultValue)
            throws UserFailureException;

    /**
     * Unassigns property type to entity type.
     */
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode) throws UserFailureException;

    /**
     * Updates specified property type assignment.
     */
    public void updatePropertyTypeAssignment(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, boolean isMandatory, String defaultValue)
            throws UserFailureException;

    /**
     * Registers given {@link PropertyType}.
     */
    public void registerPropertyType(final PropertyType propertyType) throws UserFailureException;

    /**
     * Updates property type.
     */
    public void updatePropertyType(final IPropertyTypeUpdates updates) throws UserFailureException;

    /**
     * Registers given {@link NewVocabulary}.
     */
    public void registerVocabulary(final String termsSessionKey, final NewVocabulary vocabulary)
            throws UserFailureException;

    /**
     * Updates vocabulary.
     */
    public void updateVocabulary(final IVocabularyUpdates updates) throws UserFailureException;

    /** Adds specified terms to the specified vocabulary. */
    public void addVocabularyTerms(TechId vocabularyId, List<String> vocabularyTerms)
            throws UserFailureException;

    /**
     * Updates vocabulary term.
     */
    public void updateVocabularyTerm(final IVocabularyTermUpdates updates)
            throws UserFailureException;

    /**
     * Deletes the specified terms of the specified vocabulary. Terms in use will be replaced.
     */
    public void deleteVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced) throws UserFailureException;

    /** Lists terms of a specified vocabulary */
    public List<VocabularyTerm> listVocabularyTerms(Vocabulary vocabulary)
            throws UserFailureException;

    /**
     * Registers given {@link Project}.
     */
    public void registerProject(String sessionKey, final Project project)
            throws UserFailureException;

    /**
     * Returns {@link ExternalData} fulfilling given {@link DataSetSearchCriteria}.
     */
    public ResultSet<ExternalData> searchForDataSets(final String baseIndexURL,
            DataSetSearchCriteria criteria,
            final IResultSetConfig<String, ExternalData> resultSetConfig)
            throws UserFailureException;

    /**
     * Returns a list of all material types.
     */
    public List<MaterialType> listMaterialTypes() throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for materials.
     */
    public String prepareExportMaterials(final TableExportCriteria<Material> criteria)
            throws UserFailureException;

    /** Registers a new material type */
    public void registerMaterialType(MaterialType entityType) throws UserFailureException;

    /** Registers a new data set type */
    public void registerDataSetType(DataSetType entityType) throws UserFailureException;

    /** Registers a new file type */
    public void registerFileType(FileFormatType type) throws UserFailureException;

    /** Registers a new sample type */
    public void registerSampleType(SampleType entityType) throws UserFailureException;

    /** Registers a new experiment type */
    public void registerExperimentType(ExperimentType entityType) throws UserFailureException;

    /**
     * Updates specified entity type of specified kind.
     */
    public void updateEntityType(EntityKind entityKind, EntityType entityType)
            throws UserFailureException;

    /**
     * Updates project.
     */
    public Date updateProject(ProjectUpdates updates) throws UserFailureException;

    /** Deletes the specified data sets. */
    public void deleteDataSets(List<String> dataSetCodes, String reason)
            throws UserFailureException;

    /** Deletes the specified samples. */
    public void deleteSamples(List<TechId> sampleIds, String reason) throws UserFailureException;

    /** Deletes the specified experiments. */
    public void deleteExperiments(List<TechId> experimentIds, String reason)
            throws UserFailureException;

    /** Deletes the specified projects. */
    public void deleteProjects(List<TechId> projectIds, String reason) throws UserFailureException;

    /** Deletes the specified groups. */
    public void deleteGroups(List<TechId> groupIds, String reason) throws UserFailureException;

    /** Deletes the specified vocabularies. */
    public void deleteVocabularies(List<TechId> vocabualryIds, String reason)
            throws UserFailureException;

    /** Deletes the specified property types. */
    public void deletePropertyTypes(List<TechId> propertyTypeIds, String reason)
            throws UserFailureException;

    /**
     * Deletes specified attachments (all versions with given file names) of specified attachment
     * holder.
     */
    public void deleteAttachments(TechId holderId, AttachmentHolderKind holderKind,
            List<String> fileNames, String reason) throws UserFailureException;

    /**
     * Returns a list of all attachments which belong to the specified holder grouped in
     * {@link AttachmentVersions}.
     */
    public ResultSet<AttachmentVersions> listAttachmentVersions(TechId holderId,
            AttachmentHolderKind holderKind,
            DefaultResultSetConfig<String, AttachmentVersions> criteria)
            throws UserFailureException;

    /**
     * Returns a list of all available data set types.
     */
    public List<DataSetType> listDataSetTypes() throws UserFailureException;

    /**
     * Uploads the specified data sets to the specified CIFEX server using the specified parameters.
     * 
     * @return a message or an empty string.
     */
    public String uploadDataSets(DisplayedOrSelectedDatasetCriteria criteria,
            DataSetUploadParameters uploadParameters) throws UserFailureException;

    /**
     * Information about the time and kind of the last modification, separately for each kind of
     * database object.
     */
    public LastModificationState getLastModificationState() throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link Project}.
     */
    public Project getProjectInfo(final TechId projectId) throws UserFailureException;

    /**
     * Generates unique code.
     */
    public String generateCode(final String prefix) throws UserFailureException;

    /**
     * Delete entity types.
     */
    public void deleteEntityTypes(final EntityKind entityKind, final List<String> codes)
            throws UserFailureException;

    /**
     * Delete file format types.
     */
    public void deleteFileFormatTypes(List<String> fileFormatTypeCodes) throws UserFailureException;

    /**
     * For given {@link EntityKind} and <var>identifier</var> returns the corresponding
     * {@link IEntityInformationHolder}.
     */
    public IEntityInformationHolder getEntityInformationHolder(EntityKind entityKind,
            String identifier) throws UserFailureException;

    /**
     * Returns example file format of new entities.
     */
    public String getTemplate(EntityKind kind, String type, boolean autoGenerate,
            boolean withExperiments) throws UserFailureException;

    /**
     * Updates the file format.
     */
    public void updateFileFormatType(AbstractType type) throws UserFailureException;

    /**
     * Updates the attachment.
     */
    public void updateAttachment(TechId holderId, AttachmentHolderKind holderKind,
            Attachment attachment) throws UserFailureException;

    /**
     * For given {@link DataStoreServiceKind} returns a list of all corresponding
     * {@link DatastoreServiceDescription}s.
     */
    public List<DatastoreServiceDescription> listDataStoreServices(
            DataStoreServiceKind pluginTaskKind) throws UserFailureException;

    /**
     * Uses the specified datastore service to generate reports from the specified datasets.
     */
    public TableModelReference createReportFromDatasets(
            DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
            throws UserFailureException;

    /**
     * Returns a list of datasets report rows.
     */
    public ResultSet<TableModelRow> listDatasetReport(
            DefaultResultSetConfig<String, TableModelRow> resultSetConfig)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for TableModelRow.
     */
    public String prepareExportDatasetReport(TableExportCriteria<TableModelRow> exportCriteria)
            throws UserFailureException;

    /**
     * Uses the specified datastore service to schedule processing of the specified datasets.
     */
    public void processDatasets(DatastoreServiceDescription service,
            DisplayedOrSelectedDatasetCriteria criteria) throws UserFailureException;

    /**
     * Deletes selected authorization groups.
     */
    public void deleteAuthorizationGroups(List<TechId> createList, String reason)
            throws UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for AuthorizationGroups.
     */
    public String prepareExportAuthorizationGroups(
            TableExportCriteria<AuthorizationGroup> exportCriteria) throws UserFailureException;

    /**
     * Returns {@link AuthorizationGroup}s for given criteria.
     */
    public ResultSet<AuthorizationGroup> listAuthorizationGroups(
            DefaultResultSetConfig<String, AuthorizationGroup> resultSetConfig)
            throws UserFailureException;

    /**
     * Returns a list of authorization groups registered in given database instance.
     */
    public List<AuthorizationGroup> listAuthorizationGroups() throws UserFailureException;

    /**
     * Creates a new authorization group.
     */
    public void registerAuthorizationGroup(NewAuthorizationGroup newAuthorizationGroup)
            throws UserFailureException;

    /**
     * Returns a list persons belonging to given authorization group.
     */
    public List<Person> listPersonsInAuthorizationGroup(TechId group) throws UserFailureException;

    /**
     * Updates given authorization group.
     */
    public void updateAuthorizationGroup(AuthorizationGroupUpdates updates)
            throws UserFailureException;

    /**
     * Adds persons with specified codes to the authorization group with given tech id.
     */
    public void addPersonsToAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes) throws UserFailureException;

    /**
     * Removes persons with specified codesfrom the authorization group with given tech id.
     */
    public void removePersonsFromAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes) throws UserFailureException;
}
