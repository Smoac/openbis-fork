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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
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
 * Asynchronous version of {@link ICommonClientService}.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICommonClientServiceAsync extends IClientServiceAsync
{
    /** @see ICommonClientService#listGroups(String) */
    public void listGroups(String databaseInstanceCode, AsyncCallback<List<Group>> callback);

    /** @see ICommonClientService#registerGroup(String, String, String) */
    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#listPersons() */
    public void listPersons(AsyncCallback<List<Person>> asyncCallback);

    /** @see ICommonClientService#registerPerson(String) */
    public void registerPerson(String code, AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listRoles() */
    public void listRoles(AsyncCallback<List<RoleAssignment>> asyncCallback);

    /** @see ICommonClientService#registerGroupRole(RoleSetCode, String, String) */
    public void registerGroupRole(RoleSetCode roleSetCode, String group, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteGroupRole(RoleSetCode, String, String) */
    public void deleteGroupRole(RoleSetCode roleSetCode, String group, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerInstanceRole(RoleSetCode, String) */
    public void registerInstanceRole(RoleSetCode roleSetCode, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteInstanceRole(RoleSetCode, String) */
    public void deleteInstanceRole(RoleSetCode roleSetCode, String person,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listSampleTypes() */
    public void listSampleTypes(AsyncCallback<List<SampleType>> asyncCallback);

    /**
     * @see ICommonClientService#listSamples(ListSampleCriteria, boolean)
     */
    public void listSamples(final ListSampleCriteria criteria, boolean withExperimentAndProperties,
            AsyncCallback<ResultSet<Sample>> asyncCallback);

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
            AsyncCallback<ResultSet<ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment>> asyncCallback);

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

    /** @see ICommonClientService#listPropertyTypes(DefaultResultSetConfig) */
    public void listPropertyTypes(DefaultResultSetConfig<String, PropertyType> criteria,
            final AsyncCallback<ResultSet<PropertyType>> asyncCallback);

    /**
     * @see ICommonClientService#prepareExportPropertyTypes(TableExportCriteria)
     */
    public void prepareExportPropertyTypes(final TableExportCriteria<PropertyType> criteria,
            AsyncCallback<String> asyncCallback);

    /**
     * @see ICommonClientService#listMatchingEntities(SearchableEntity, String, IResultSetConfig)
     */
    public void listMatchingEntities(final SearchableEntity searchableEntity,
            final String queryText, final IResultSetConfig<String, MatchingEntity> resultSetConfig,
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
    public void listVocabularyTerms(Vocabulary vocabulary,
            DefaultResultSetConfig<String, VocabularyTermWithStats> resultSetConfig,
            AsyncCallback<ResultSet<VocabularyTermWithStats>> callback);

    /** @see ICommonClientService#prepareExportVocabularyTerms(TableExportCriteria) */
    public void prepareExportVocabularyTerms(
            TableExportCriteria<VocabularyTermWithStats> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listMaterialTypes(DefaultResultSetConfig) */
    public void listMaterialTypes(DefaultResultSetConfig<String, EntityType> criteria,
            final AsyncCallback<ResultSet<EntityType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportMaterialTypes(TableExportCriteria) */
    public void prepareExportMaterialTypes(final TableExportCriteria<EntityType> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listSampleTypes(DefaultResultSetConfig) */
    public void listSampleTypes(DefaultResultSetConfig<String, EntityType> criteria,
            final AsyncCallback<ResultSet<EntityType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportSampleTypes(TableExportCriteria) */
    public void prepareExportSampleTypes(final TableExportCriteria<EntityType> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listExperimentTypes(DefaultResultSetConfig) */
    public void listExperimentTypes(DefaultResultSetConfig<String, EntityType> criteria,
            final AsyncCallback<ResultSet<EntityType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportExperimentTypes(TableExportCriteria) */
    public void prepareExportExperimentTypes(final TableExportCriteria<EntityType> criteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#listDataSetTypes(DefaultResultSetConfig) */
    public void listDataSetTypes(DefaultResultSetConfig<String, EntityType> criteria,
            final AsyncCallback<ResultSet<EntityType>> asyncCallback)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /** @see ICommonClientService#prepareExportDataSetTypes(TableExportCriteria) */
    public void prepareExportDataSetTypes(final TableExportCriteria<EntityType> criteria,
            AsyncCallback<String> callback);

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
     * @see ICommonClientService#listSampleDataSets(String, DefaultResultSetConfig)
     */
    public void listSampleDataSets(String sampleIdentifier,
            DefaultResultSetConfig<String, ExternalData> criteria,
            AsyncCallback<ResultSet<ExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listExperimentDataSets(String, DefaultResultSetConfig)
     */
    public void listExperimentDataSets(String experimentIdentifier,
            DefaultResultSetConfig<String, ExternalData> criteria,
            AsyncCallback<ResultSet<ExternalData>> asyncCallback);

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
     * @see ICommonClientService#assignPropertyType(EntityKind, String, String, boolean, String)
     */
    public void assignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, boolean isMandatory, String defaultValue,
            AsyncCallback<String> process);

    /**
     * @see ICommonClientService#unassignPropertyType(EntityKind, String, String)
     */
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, AsyncCallback<Void> callback);

    /** @see ICommonClientService#registerPropertyType(PropertyType) */
    public void registerPropertyType(final PropertyType propertyType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerVocabulary(Vocabulary) */
    public void registerVocabulary(final Vocabulary vocabulary,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#addVocabularyTerms(String, List) */
    public void addVocabularyTerms(String vocabularyCode, List<String> vocabularyTerms,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#deleteVocabularyTerms(String, List, List) */
    public void deleteVocabularyTerms(String vocabularyCode, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced, AsyncCallback<Void> callback);

    /** @see ICommonClientService#listVocabularyTerms(Vocabulary) */
    public void listVocabularyTerms(Vocabulary vocabulary,
            AsyncCallback<List<VocabularyTerm>> callback);

    /** @see ICommonClientService#registerProject(String, Project) */
    public void registerProject(String sessionKey, Project project,
            final AsyncCallback<Void> projectRegistrationCallback);

    /**
     * @see ICommonClientService#searchForDataSets(DataSetSearchCriteria, IResultSetConfig)
     */
    public void searchForDataSets(DataSetSearchCriteria criteria,
            final IResultSetConfig<String, ExternalData> resultSetConfig,
            final AsyncCallback<ResultSet<ExternalData>> callback);

    /**
     * @see ICommonClientService#listMaterialTypes()
     */
    public void listMaterialTypes(AsyncCallback<List<MaterialType>> callback);

    /**
     * @see ICommonClientService#listMaterials(ListMaterialCriteria)
     */
    public void listMaterials(ListMaterialCriteria criteria,
            AsyncCallback<ResultSet<Material>> callback);

    /** @see ICommonClientService#prepareExportMaterials(TableExportCriteria) */
    public void prepareExportMaterials(TableExportCriteria<Material> exportCriteria,
            AsyncCallback<String> callback);

    /** @see ICommonClientService#registerMaterialType(MaterialType) */
    public void registerMaterialType(MaterialType entityType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerDataSetType(DataSetType) */
    public void registerDataSetType(DataSetType entityType, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerSampleType(SampleType) */
    public void registerSampleType(SampleType entityType, final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#registerExperimentType(ExperimentType) */
    public void registerExperimentType(ExperimentType entityType,
            final AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#updateEntityType(EntityKind, EntityType) */
    public void updateEntityType(EntityKind entityKind, EntityType entityType,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#deleteDataSets(List, String) */
    public void deleteDataSets(List<String> dataSetCodes, String reason,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#listDataSetTypes() */
    public void listDataSetTypes(AsyncCallback<List<DataSetType>> callback);

    /** @see ICommonClientService#uploadDataSets(List, DataSetUploadParameters) */
    public void uploadDataSets(List<String> dataSetCodes, DataSetUploadParameters uploadParameters,
            AsyncCallback<Void> callback);

    /** @see ICommonClientService#getLastModificationState() */
    public void getLastModificationState(AsyncCallback<LastModificationState> asyncCallback);

    /**
     * @see ICommonClientService#getProjectInfo(String)
     */
    public void getProjectInfo(String projectIdentifier,
            final AsyncCallback<Project> projectInfoCallback);

    /**
     * @see ICommonClientService#generateCode(String)
     */
    public void generateCode(String codePrefix, final AsyncCallback<String> callback);

    /**
     * @see ICommonClientService#updateProject(ProjectUpdates)
     */
    public void updateProject(ProjectUpdates updates, AsyncCallback<Date> projectEditCallback);

    /**
     * @see ICommonClientService#deleteEntityTypes(EntityKind, List)
     */
    public void deleteEntityTypes(EntityKind entityKind, List<String> entityTypeCodes,
            AsyncCallback<Void> callback);

}
