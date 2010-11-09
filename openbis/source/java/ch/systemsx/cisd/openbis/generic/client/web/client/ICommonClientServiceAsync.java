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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ArchivingResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListPersonsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListScriptsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Null;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;

/**
 * Asynchronous version of {@link ICommonClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICommonClientServiceAsync extends IClientServiceAsync
{
    /** @see ICommonClientService#keepSessionAlive() */
    public void keepSessionAlive(final AsyncCallback<Boolean> asyncCallback);

    /** @see ICommonClientService#listGroups(DefaultResultSetConfig) */
    public void listGroups(DefaultResultSetConfig<String, TableModelRowWithObject<Space>> criteria,
            final AsyncCallback<TypedTableResultSet<Space>> asyncCallback);

    /** @see ICommonClientService#listScripts(ListScriptsCriteria) */
    public void listScripts(ListScriptsCriteria criteria,
            final AsyncCallback<ResultSet<Script>> asyncCallback);

    /** @see ICommonClientService#prepareExportScripts(TableExportCriteria) */
    public void prepareExportScripts(TableExportCriteria<Script> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#prepareExportGroups(TableExportCriteria) */
    public void prepareExportGroups(
            TableExportCriteria<TableModelRowWithObject<Space>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerGroup(String, String) */
    public void registerGroup(String groupCode, String descriptionOrNull,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#registerScript(Script) */
    public void registerScript(Script newScript, AsyncCallback<Void> callback);

    /** @see ICommonClientService#updateScript(IScriptUpdates) */
    public void updateScript(final IScriptUpdates updates, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateGroup(ISpaceUpdates) */
    public void updateGroup(final ISpaceUpdates updates, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listPersons(ListPersonsCriteria) */
    public void listPersons(ListPersonsCriteria criteria,
            AsyncCallback<ResultSet<Person>> asyncCallback);

    /** @see ICommonClientService#prepareExportPersons(TableExportCriteria) */
    public void prepareExportPersons(TableExportCriteria<Person> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerPerson(String) */
    public void registerPerson(String code, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listRoleAssignments(DefaultResultSetConfig) */
    public void listRoleAssignments(DefaultResultSetConfig<String, RoleAssignment> criteria,
            AsyncCallback<ResultSet<RoleAssignment>> asyncCallback);

    /** @see ICommonClientService#prepareExportRoleAssignments(TableExportCriteria) */
    public void prepareExportRoleAssignments(TableExportCriteria<RoleAssignment> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerGroupRole(RoleWithHierarchy, String, Grantee) */
    public void registerGroupRole(RoleWithHierarchy role, String group, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteGroupRole(RoleWithHierarchy, String, Grantee) */
    public void deleteGroupRole(RoleWithHierarchy role, String group, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerInstanceRole(RoleWithHierarchy, Grantee) */
    public void registerInstanceRole(RoleWithHierarchy role, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteInstanceRole(RoleWithHierarchy, Grantee) */
    public void deleteInstanceRole(RoleWithHierarchy role, Grantee grantee,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listSampleTypes() */
    public void listSampleTypes(AsyncCallback<List<SampleType>> asyncCallback);

    /** @see ICommonClientService#listFileTypes() */
    public void listFileTypes(AsyncCallback<List<FileFormatType>> asyncCallback);

    /**
     * @see ICommonClientService#listSamples(ListSampleDisplayCriteria)
     */
    public void listSamples(final ListSampleDisplayCriteria criteria,
            AsyncCallback<ResultSetWithEntityTypes<Sample>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportSamples(TableExportCriteria)
     */
    public void prepareExportSamples(final TableExportCriteria<Sample> criteria,
            AsyncCallback<String> asyncCallback);

    /** @see ICommonClientService#listPropertyTypeAssignments(DefaultResultSetConfig) */
    public void listPropertyTypeAssignments(
            DefaultResultSetConfig<String, EntityTypePropertyType<?>> criteria,
            final AsyncCallback<ResultSet<EntityTypePropertyType<?>>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportPropertyTypeAssignments(TableExportCriteria)
     */
    public void prepareExportPropertyTypeAssignments(
            final TableExportCriteria<EntityTypePropertyType<?>> criteria,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#countPropertyTypedEntities(EntityKind, String, String)
     */
    public void countPropertyTypedEntities(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, AsyncCallback<Integer> asyncCallback);

    /**
     * @see ICommonClientService#listExperiments(ListExperimentsCriteria)
     */
    public void listExperiments(
            final ListExperimentsCriteria criteria,
            AsyncCallback<ResultSet<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportExperiments(TableExportCriteria)
     */
    public void prepareExportExperiments(TableExportCriteria<Experiment> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#prepareExportDataSetSearchHits(TableExportCriteria)
     */
    public void prepareExportDataSetSearchHits(TableExportCriteria<ExternalData> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listPropertyTypes(boolean) */
    public void listPropertyTypes(boolean withRelations,
            AsyncCallback<List<PropertyType>> asyncCallback);

    /** @see ICommonClientService#listPropertyTypes(DefaultResultSetConfig) */
    public void listPropertyTypes(DefaultResultSetConfig<String, PropertyType> criteria,
            final AsyncCallback<ResultSet<PropertyType>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportPropertyTypes(TableExportCriteria)
     */
    public void prepareExportPropertyTypes(final TableExportCriteria<PropertyType> criteria,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#listMatchingEntities(SearchableEntity, String, boolean,
     *      IResultSetConfig)
     */
    public void listMatchingEntities(final SearchableEntity searchableEntity,
            final String queryText, final boolean useWildcardSearchMode,
            final IResultSetConfig<String, MatchingEntity> resultSetConfig,
            final AsyncCallback<ResultSet<MatchingEntity>> asyncCallback);

    /** @see ICommonClientService#prepareExportMatchingEntities(TableExportCriteria) */
    public void prepareExportMatchingEntities(TableExportCriteria<MatchingEntity> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listProjects(DefaultResultSetConfig) */
    public void listProjects(DefaultResultSetConfig<String, Project> criteria,
            final AsyncCallback<ResultSet<Project>> asyncCallback);

    /** @see ICommonClientService#prepareExportProjects(TableExportCriteria) */
    public void prepareExportProjects(TableExportCriteria<Project> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#listVocabularies(boolean, boolean, DefaultResultSetConfig)
     */
    public void listVocabularies(final boolean withTerms, boolean excludeInternal,
            DefaultResultSetConfig<String, Vocabulary> criteria,
            final AsyncCallback<ResultSet<Vocabulary>> asyncCallback);

    /** @see ICommonClientService#prepareExportVocabularies(TableExportCriteria) */
    public void prepareExportVocabularies(TableExportCriteria<Vocabulary> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#listVocabularyTerms(Vocabulary, DefaultResultSetConfig)
     */
    public void listVocabularyTerms(
            Vocabulary vocabulary,
            DefaultResultSetConfig<String, TableModelRowWithObject<VocabularyTermWithStats>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<VocabularyTermWithStats>> callback);

    /** @see ICommonClientService#prepareExportVocabularyTerms(TableExportCriteria) */
    public void prepareExportVocabularyTerms(
            TableExportCriteria<TableModelRowWithObject<VocabularyTermWithStats>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listMaterialTypes(DefaultResultSetConfig) */
    public void listMaterialTypes(DefaultResultSetConfig<String, MaterialType> criteria,
            final AsyncCallback<ResultSet<MaterialType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportMaterialTypes(TableExportCriteria) */
    public void prepareExportMaterialTypes(final TableExportCriteria<MaterialType> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listSampleTypes(DefaultResultSetConfig) */
    public void listSampleTypes(DefaultResultSetConfig<String, SampleType> criteria,
            final AsyncCallback<ResultSet<SampleType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportSampleTypes(TableExportCriteria) */
    public void prepareExportSampleTypes(final TableExportCriteria<SampleType> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listExperimentTypes(DefaultResultSetConfig) */
    public void listExperimentTypes(DefaultResultSetConfig<String, ExperimentType> criteria,
            final AsyncCallback<ResultSet<ExperimentType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportExperimentTypes(TableExportCriteria) */
    public void prepareExportExperimentTypes(final TableExportCriteria<ExperimentType> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listDataSetTypes(DefaultResultSetConfig) */
    public void listDataSetTypes(DefaultResultSetConfig<String, DataSetType> criteria,
            final AsyncCallback<ResultSet<DataSetType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportDataSetTypes(TableExportCriteria) */
    public void prepareExportDataSetTypes(final TableExportCriteria<DataSetType> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listFileTypes(DefaultResultSetConfig) */
    public void listFileTypes(DefaultResultSetConfig<String, AbstractType> criteria,
            final AsyncCallback<ResultSet<AbstractType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportFileTypes(TableExportCriteria) */
    public void prepareExportFileTypes(TableExportCriteria<AbstractType> criteria,
            AsyncCallback<String> callback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportAttachmentVersions(TableExportCriteria) */
    public void prepareExportAttachmentVersions(TableExportCriteria<AttachmentVersions> criteria,
            AsyncCallback<String> callback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * @see ICommonClientService#getExportTable(String, String)
     */
    public void getExportTable(String exportDataKey, String lineSeparator,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#removeResultSet(String)
     */
    public void removeResultSet(final String resultSetKey, final AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#listSampleDataSets(TechId, DefaultResultSetConfig, boolean)
     */
    public void listSampleDataSets(TechId sampleId,
            DefaultResultSetConfig<String, ExternalData> criteria,
            boolean showOnlyDirectlyConnected,
            AsyncCallback<ResultSetWithEntityTypes<ExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listExperimentDataSets(TechId, DefaultResultSetConfig)
     */
    public void listExperimentDataSets(TechId experimentId,
            DefaultResultSetConfig<String, ExternalData> criteria,
            AsyncCallback<ResultSetWithEntityTypes<ExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listDataSetRelationships(TechId, DataSetRelationshipRole,
     *      DefaultResultSetConfig)
     */
    public void listDataSetRelationships(TechId datasetId, DataSetRelationshipRole role,
            DefaultResultSetConfig<String, ExternalData> resultSetConfig,
            AsyncCallback<ResultSetWithEntityTypes<ExternalData>> callback);

    /**
     * @see ICommonClientService#listSearchableEntities()
     */
    public void listSearchableEntities(final AsyncCallback<List<SearchableEntity>> asyncCallback);

    /** @see ICommonClientService#listExperimentTypes() */
    public void listExperimentTypes(
            final AsyncCallback<List<ExperimentType>> listExperimentTypesCallback);

    /** @see ICommonClientService#listDataTypes() */
    public void listDataTypes(final AsyncCallback<List<DataType>> asyncCallback);

    /**
     * @see ICommonClientService#assignPropertyType(NewETPTAssignment assignment)
     */
    public void assignPropertyType(NewETPTAssignment assignment, AsyncCallback<String> process);

    /**
     * @see ICommonClientService#updatePropertyTypeAssignment(NewETPTAssignment)
     */
    public void updatePropertyTypeAssignment(NewETPTAssignment assignmentUpdates,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#unassignPropertyType(EntityKind, String, String)
     */
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, AsyncCallback<Void> callback);

    /** @see ICommonClientService#registerPropertyType(PropertyType) */
    public void registerPropertyType(final PropertyType propertyType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updatePropertyType(IPropertyTypeUpdates) */
    public void updatePropertyType(final IPropertyTypeUpdates updates,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerVocabulary(String, NewVocabulary) */
    public void registerVocabulary(final String termsSessionKey, final NewVocabulary vocabulary,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateVocabulary(IVocabularyUpdates) */
    public void updateVocabulary(final IVocabularyUpdates updates,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#addVocabularyTerms(TechId, List, Long) */
    public void addVocabularyTerms(TechId vocabularyId, List<String> vocabularyTerms,
            Long previousTermOrdinal, AsyncCallback<Void> callback);

    /** @see ICommonClientService#updateVocabularyTerm(IVocabularyTermUpdates) */
    public void updateVocabularyTerm(final IVocabularyTermUpdates updates,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateVocabularyTerms(String,TechId) */
    public void updateVocabularyTerms(final String termsSessionKey, TechId vocabularyId,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteVocabularyTerms(TechId, List, List) */
    public void deleteVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced, AsyncCallback<Void> callback);

    /** @see ICommonClientService#listVocabularyTerms(Vocabulary) */
    public void listVocabularyTerms(Vocabulary vocabulary,
            AsyncCallback<List<VocabularyTerm>> callback);

    /** @see ICommonClientService#registerProject(String, Project) */
    public void registerProject(String sessionKey, Project project,
            final AsyncCallback<Void> projectRegistrationCallback);

    /**
     * @see ICommonClientService#searchForDataSets(DetailedSearchCriteria, IResultSetConfig)
     */
    public void searchForDataSets(DetailedSearchCriteria criteria,
            final IResultSetConfig<String, ExternalData> resultSetConfig,
            final AsyncCallback<ResultSetWithEntityTypes<ExternalData>> callback);

    /**
     * @see ICommonClientService#searchForDataSets(RelatedDataSetCriteria, IResultSetConfig)
     */
    public void searchForDataSets(RelatedDataSetCriteria criteria,
            final IResultSetConfig<String, ExternalData> resultSetConfig,
            final AsyncCallback<ResultSetWithEntityTypes<ExternalData>> callback);

    /**
     * @see ICommonClientService#listMaterialTypes()
     */
    public void listMaterialTypes(AsyncCallback<List<MaterialType>> callback);

    /**
     * @see ICommonClientService#listMaterials(ListMaterialDisplayCriteria)
     */
    public void listMaterials(ListMaterialDisplayCriteria criteria,
            AsyncCallback<ResultSet<Material>> callback);

    /** @see ICommonClientService#prepareExportMaterials(TableExportCriteria) */
    public void prepareExportMaterials(TableExportCriteria<Material> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerMaterialType(MaterialType) */
    public void registerMaterialType(MaterialType entityType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerDataSetType(DataSetType) */
    public void registerDataSetType(DataSetType entityType, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerFileType(FileFormatType) */
    public void registerFileType(FileFormatType type, AsyncCallback<Void> callback);

    /** @see ICommonClientService#registerSampleType(SampleType) */
    public void registerSampleType(SampleType entityType, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerExperimentType(ExperimentType) */
    public void registerExperimentType(ExperimentType entityType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateEntityType(EntityKind, EntityType) */
    public void updateEntityType(EntityKind entityKind, EntityType entityType,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#deleteDataSets(DisplayedOrSelectedDatasetCriteria, String) */
    public void deleteDataSets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria, String reason,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteDataSet(String, String) */
    public void deleteDataSet(String singleData, String reason, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSamples(List, String) */
    public void deleteSamples(List<TechId> sampleIds, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSamples(DisplayedOrSelectedIdHolderCriteria, String) */
    public void deleteSamples(DisplayedOrSelectedIdHolderCriteria<Sample> criteria, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSample(TechId, String) */
    public void deleteSample(TechId sampleIs, String value, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteExperiments(DisplayedOrSelectedIdHolderCriteria, String) */
    public void deleteExperiments(DisplayedOrSelectedIdHolderCriteria<Experiment> criteria,
            String value, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteExperiment(TechId, String) */
    public void deleteExperiment(TechId experimentId, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteProjects(List, String) */
    public void deleteProjects(List<TechId> projectIds, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteGroups(List, String) */
    public void deleteGroups(List<TechId> groupIds, String value, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteScripts(List) */
    public void deleteScripts(List<TechId> scriptIds, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteVocabularies(List, String) */
    public void deleteVocabularies(List<TechId> vocabularyIds, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deletePropertyTypes(List, String) */
    public void deletePropertyTypes(List<TechId> propertyTypeIds, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteAttachments(TechId, AttachmentHolderKind, List, String) */
    public void deleteAttachments(TechId holderId, AttachmentHolderKind holderKind,
            List<String> fileNames, String reason, AsyncCallback<Void> asyncCallback);

    /**
     * @see ICommonClientService#listAttachmentVersions(TechId, AttachmentHolderKind,
     *      DefaultResultSetConfig)
     */
    public void listAttachmentVersions(TechId holderId, AttachmentHolderKind holderKind,
            DefaultResultSetConfig<String, AttachmentVersions> criteria,
            AsyncCallback<ResultSet<AttachmentVersions>> asyncCallback);

    /** @see ICommonClientService#listDataSetTypes() */
    public void listDataSetTypes(AsyncCallback<List<DataSetType>> callback);

    /**
     * @see ICommonClientService#uploadDataSets(DisplayedOrSelectedDatasetCriteria,
     *      DataSetUploadParameters)
     */
    public void uploadDataSets(DisplayedOrSelectedDatasetCriteria criteria,
            DataSetUploadParameters uploadParameters, AsyncCallback<String> callback);

    /** @see ICommonClientService#getLastModificationState() */
    public void getLastModificationState(AsyncCallback<LastModificationState> asyncCallback);

    /**
     * @see ICommonClientService#getExperimentInfo(TechId)
     */
    public void getExperimentInfo(String experimentIdentifier,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see ICommonClientService#getExperimentInfo(TechId)
     */
    public void getExperimentInfo(TechId experimentId,
            final AsyncCallback<Experiment> experimentInfoCallback);

    /**
     * @see ICommonClientService#getProjectInfo(TechId)
     */
    public void getProjectInfo(TechId projectId, AsyncCallback<Project> projectInfoCallback);

    /**
     * @see ICommonClientService#getProjectInfo(BasicProjectIdentifier)
     */
    public void getProjectInfo(BasicProjectIdentifier projectIdentifier,
            AsyncCallback<Project> projectInfoCallback);

    /**
     * @see ICommonClientService#generateCode(String)
     */
    public void generateCode(String codePrefix, AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#updateProject(ProjectUpdates)
     */
    public void updateProject(ProjectUpdates updates, AsyncCallback<Date> projectEditCallback);

    /**
     * @see ICommonClientService#deleteEntityTypes(EntityKind, List)
     */
    public void deleteEntityTypes(EntityKind entityKind, List<String> entityTypeCodes,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#deleteFileFormatTypes(List)
     */
    public void deleteFileFormatTypes(List<String> fileFormatTypeCodes, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#getEntityInformationHolder(EntityKind, String)
     */
    public void getEntityInformationHolder(EntityKind entityKind, String permId,
            AsyncCallback<IEntityInformationHolderWithPermId> callback);

    /**
     * @see ICommonClientService#getMaterialInformationHolder(MaterialIdentifier)
     */
    public void getMaterialInformationHolder(MaterialIdentifier materialIdentifier,
            AsyncCallback<IEntityInformationHolderWithPermId> openEntityDetailsTabCallback);

    /**
     * @see ICommonClientService#getTemplate(EntityKind, String, boolean,boolean,BatchOperationKind)
     */
    public void getTemplate(EntityKind kind, String type, boolean autoGenerate,
            boolean withExperiments, BatchOperationKind operationKind,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#updateFileFormatType(AbstractType type)
     */
    public void updateFileFormatType(AbstractType type, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#updateAttachment(TechId, AttachmentHolderKind, Attachment) */
    public void updateAttachment(TechId holderId, AttachmentHolderKind holderKind,
            Attachment attachment, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listDataStoreServices(DataStoreServiceKind) */
    public void listDataStoreServices(DataStoreServiceKind pluginTaskKind,
            AsyncCallback<List<DatastoreServiceDescription>> callback);

    /**
     * @see ICommonClientService#createReportFromDatasets(DatastoreServiceDescription,
     *      DisplayedOrSelectedDatasetCriteria)
     */
    public void createReportFromDatasets(DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria,
            AsyncCallback<TableModelReference> callback);

    /**
     * @see ICommonClientService#listReport(IResultSetConfig)
     */
    public void listReport(IResultSetConfig<String, TableModelRowWithObject<Null>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<Null>> callback);

    /**
     * @see ICommonClientService#prepareExportReport(TableExportCriteria)
     */
    public void prepareExportReport(
            TableExportCriteria<TableModelRowWithObject<Null>> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#processDatasets(DatastoreServiceDescription,
     *      DisplayedOrSelectedDatasetCriteria)
     */
    public void processDatasets(DatastoreServiceDescription service,
            DisplayedOrSelectedDatasetCriteria criteria, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#archiveDatasets(DisplayedOrSelectedDatasetCriteria)
     */
    public void archiveDatasets(DisplayedOrSelectedDatasetCriteria criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#unarchiveDatasets(DisplayedOrSelectedDatasetCriteria)
     */
    public void unarchiveDatasets(DisplayedOrSelectedDatasetCriteria criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#deleteAuthorizationGroups(List, String)
     */
    public void deleteAuthorizationGroups(List<TechId> createList, String reason,
            AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#prepareExportAuthorizationGroups(TableExportCriteria)
     */
    public void prepareExportAuthorizationGroups(
            TableExportCriteria<AuthorizationGroup> exportCriteria, AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#listAuthorizationGroups(DefaultResultSetConfig)
     */
    public void listAuthorizationGroups(
            DefaultResultSetConfig<String, AuthorizationGroup> resultSetConfig,
            AsyncCallback<ResultSet<AuthorizationGroup>> callback);

    /**
     * @see ICommonClientService#registerAuthorizationGroup(NewAuthorizationGroup)
     */
    public void registerAuthorizationGroup(NewAuthorizationGroup newAuthGroup,
            AsyncCallback<Void> registrationCallback);

    /**
     * @see ICommonClientService#listPersonsInAuthorizationGroup(TechId)
     */
    public void listPersonsInAuthorizationGroup(TechId group, AsyncCallback<List<Person>> callback)
            throws UserFailureException;

    /**
     * @see ICommonClientService#updateAuthorizationGroup(AuthorizationGroupUpdates)
     */
    public void updateAuthorizationGroup(AuthorizationGroupUpdates updates,
            AsyncCallback<Void> registrationCallback);

    /**
     * @see ICommonClientService#listPersons()
     */
    public void listPersons(AsyncCallback<List<Person>> callback);

    /**
     * @see ICommonClientService#listAuthorizationGroups()
     */
    public void listAuthorizationGroups(AsyncCallback<List<AuthorizationGroup>> callback);

    /**
     * @see ICommonClientService#addPersonsToAuthorizationGroup(TechId, List)
     */
    public void addPersonsToAuthorizationGroup(TechId authGroupId, List<String> personsCodes,
            AsyncCallback<Void> registrationCallback);

    /**
     * @see ICommonClientService#removePersonsFromAuthorizationGroup(TechId, List)
     */
    public void removePersonsFromAuthorizationGroup(TechId create, List<String> personsCodes,
            AsyncCallback<Void> callback);

    // -- custom grid filters

    /**
     * @see ICommonClientService#listFilters(String)
     */
    public void listFilters(String gridId, AsyncCallback<List<GridCustomFilter>> callback);

    /**
     * @see ICommonClientService#listFilters(String, DefaultResultSetConfig)
     */
    public void listFilters(String gridId,
            DefaultResultSetConfig<String, GridCustomFilter> resultSetConfig,
            AsyncCallback<ResultSet<GridCustomFilter>> callback);

    /**
     * @see ICommonClientService#prepareExportFilters(TableExportCriteria)
     */
    public void prepareExportFilters(final TableExportCriteria<GridCustomFilter> criteria,
            AsyncCallback<String> asyncCallback);

    /** @see ICommonClientService#registerFilter(NewColumnOrFilter) */
    public void registerFilter(NewColumnOrFilter newFilter, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#deleteFilters(List) */
    public void deleteFilters(List<TechId> filterIds, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateFilter(IExpressionUpdates) */
    public void updateFilter(IExpressionUpdates updates, AsyncCallback<Void> registrationCallback);

    // -- custom grid columns

    /**
     * @see ICommonClientService#listGridCustomColumns(String)
     */
    public void listGridCustomColumns(String gridId, AsyncCallback<List<GridCustomColumn>> callback);

    /**
     * @see ICommonClientService#listGridCustomColumns(String, DefaultResultSetConfig)
     */
    public void listGridCustomColumns(String gridId,
            DefaultResultSetConfig<String, GridCustomColumn> resultSetConfig,
            AsyncCallback<ResultSet<GridCustomColumn>> callback);

    /**
     * @see ICommonClientService#prepareExportColumns(TableExportCriteria)
     */
    public void prepareExportColumns(final TableExportCriteria<GridCustomColumn> criteria,
            AsyncCallback<String> asyncCallback);

    /** @see ICommonClientService#registerColumn(NewColumnOrFilter) */
    public void registerColumn(NewColumnOrFilter newColumn, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#deleteColumns(List) */
    public void deleteColumns(List<TechId> columnIds, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateColumn(IExpressionUpdates) */
    public void updateColumn(IExpressionUpdates updates, AsyncCallback<Void> registrationCallback);

    /** @see ICommonClientService#deleteMaterials(DisplayedOrSelectedIdHolderCriteria, String) */
    public void deleteMaterials(DisplayedOrSelectedIdHolderCriteria<Material> uploadCriteria,
            String value, AsyncCallback<Void> callback);

    /**
     * @see ICommonClientService#lockDatasets(DisplayedOrSelectedDatasetCriteria)
     */
    public void lockDatasets(DisplayedOrSelectedDatasetCriteria criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#unlockDatasets(DisplayedOrSelectedDatasetCriteria)
     */
    public void unlockDatasets(DisplayedOrSelectedDatasetCriteria criteria,
            AsyncCallback<ArchivingResult> callback);

    /**
     * @see ICommonClientService#retrieveLinkFromDataSet(DatastoreServiceDescription, String)
     */
    public void retrieveLinkFromDataSet(DatastoreServiceDescription serviceDescription,
            String dataSetCode, AsyncCallback<LinkModel> callback);

    // --
}
