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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
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
     * Returns a list of all groups which belong to the specified database instance.
     */
    public List<Group> listGroups(String databaseInstanceCode) throws UserFailureException;

    /**
     * Registers a new group with specified code and optional description and group leader ID.
     */
    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull)
            throws UserFailureException;

    /**
     * Returns a list of all persons which belong to the current database instance.
     */
    public List<Person> listPersons() throws UserFailureException;

    /**
     * Registers a new person with specified code.
     */
    public void registerPerson(String code) throws UserFailureException;

    /**
     * Returns a list of all roles.
     */
    public List<RoleAssignment> listRoles() throws UserFailureException;

    /**
     * Registers a new role from given role set code, group code and person code
     */
    public void registerGroupRole(RoleSetCode roleSetCode, String group, String person)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code, group code and person code
     */
    public void deleteGroupRole(RoleSetCode roleSetCode, String group, String person)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code and person code
     */
    public void registerInstanceRole(RoleSetCode roleSetCode, String person)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code and person code
     */
    public void deleteInstanceRole(RoleSetCode roleSetCode, String person)
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

    public ResultSet<? extends EntityType> listMaterialTypes(
            DefaultResultSetConfig<String, MaterialType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for MaterialType.
     */
    public String prepareExportMaterialTypes(final TableExportCriteria<MaterialType> criteria)
            throws UserFailureException;

    public ResultSet<? extends EntityType> listSampleTypes(
            DefaultResultSetConfig<String, SampleType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for SampleType.
     */
    public String prepareExportSampleTypes(final TableExportCriteria<SampleType> criteria)
            throws UserFailureException;

    public ResultSet<? extends EntityType> listExperimentTypes(
            DefaultResultSetConfig<String, ExperimentType> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link #prepareExportSamples(TableExportCriteria)}, but for ExperimentType.
     */
    public String prepareExportExperimentTypes(final TableExportCriteria<ExperimentType> criteria)
            throws UserFailureException;

    public ResultSet<? extends EntityType> listDataSetTypes(
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
     * Registers given {@link PropertyType}.
     */
    public void registerPropertyType(final String termsSessionKey, final PropertyType propertyType)
            throws UserFailureException;

    /**
     * Registers given {@link Vocabulary}.
     */
    public void registerVocabulary(final String termsSessionKey, final Vocabulary vocabulary)
            throws UserFailureException;

    /** Adds specified terms to the specified vocabulary. */
    public void addVocabularyTerms(String vocabularyCode, List<String> vocabularyTerms)
            throws UserFailureException;

    /**
     * Deletes the specified terms of the specified vocabulary. Terms in use will be replaced.
     */
    public void deleteVocabularyTerms(String vocabularyCode, List<VocabularyTerm> termsToBeDeleted,
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
    public String uploadDataSets(List<String> dataSetCodes, DataSetUploadParameters uploadParameters)
            throws UserFailureException;

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
     * For given {@link EntityKind} and <var>identifier</var> returns the corresponding
     * {@link IEntityInformationHolder}.
     */
    public IEntityInformationHolder getEntityInformationHolder(EntityKind entityKind,
            String identifier) throws UserFailureException;

    /**
     * Returns example file format of new entities.
     */
    public String getTemplate(EntityKind kind, String type, boolean autoGenerate);

}
