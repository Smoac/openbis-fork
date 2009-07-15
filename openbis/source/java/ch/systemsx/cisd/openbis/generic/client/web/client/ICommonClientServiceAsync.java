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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleSetCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
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
    /** @see ICommonClientService#listGroups(DefaultResultSetConfig) */
    public void listGroups(DefaultResultSetConfig<String, Group> criteria,
            final AsyncCallback<ResultSet<Group>> asyncCallback);

    /** @see ICommonClientService#prepareExportGroups(TableExportCriteria) */
    public void prepareExportGroups(TableExportCriteria<Group> exportCriteria,
            AsyncCallback<String> callback);

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

    /** @see ICommonClientService#listFileTypes() */
    public void listFileTypes(AsyncCallback<List<FileFormatType>> asyncCallback);

    /**
     * @see ICommonClientService#listSamples(ListSampleCriteria)
     */
    public void listSamples(final ListSampleCriteria criteria,
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
     * @see ICommonClientService#listSampleDataSets(TechId, String, DefaultResultSetConfig)
     */
    public void listSampleDataSets(TechId sampleId, String baseIndexURL,
            DefaultResultSetConfig<String, ExternalData> criteria,
            AsyncCallback<ResultSet<ExternalData>> asyncCallback);

    /**
     * @see ICommonClientService#listExperimentDataSets(TechId, String, DefaultResultSetConfig)
     */
    public void listExperimentDataSets(TechId experimentId, String baseIndexURL,
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
     * @see ICommonClientService#updatePropertyTypeAssignment(EntityKind, String, String, boolean,
     *      String)
     */
    public void updatePropertyTypeAssignment(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode, boolean isMandatory, String defaultValue,
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

    /** @see ICommonClientService#addVocabularyTerms(TechId, List) */
    public void addVocabularyTerms(TechId vocabularyId, List<String> vocabularyTerms,
            AsyncCallback<Void> callback);

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
     * @see ICommonClientService#searchForDataSets(String, DataSetSearchCriteria, IResultSetConfig)
     */
    public void searchForDataSets(final String baseIndexURL, DataSetSearchCriteria criteria,
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

    /** @see ICommonClientService#deleteDataSets(List, String) */
    public void deleteDataSets(List<String> dataSetCodes, String reason,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteSamples(List, String) */
    public void deleteSamples(List<TechId> sampleIds, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteExperiments(List, String) */
    public void deleteExperiments(List<TechId> experimentIds, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteProjects(List, String) */
    public void deleteProjects(List<TechId> projectIds, String value,
            AsyncCallback<Void> asyncCallback);

    /** @see ICommonClientService#deleteGroups(List, String) */
    public void deleteGroups(List<TechId> groupIds, String value, AsyncCallback<Void> asyncCallback);

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
     * @see ICommonClientService#getProjectInfo(TechId)
     */
    public void getProjectInfo(TechId projectId, AsyncCallback<Project> projectInfoCallback);

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
            AsyncCallback<IEntityInformationHolder> callback);

    /**
     * @see ICommonClientService#getTemplate(EntityKind, String, boolean)
     */
    public void getTemplate(EntityKind kind, String type, boolean autoGenerate,
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
            AsyncCallback<TableModel> callback);

    /**
     * @see ICommonClientService#processDatasets(DatastoreServiceDescription,
     *      DisplayedOrSelectedDatasetCriteria)
     */
    public void processDatasets(DatastoreServiceDescription service,
            DisplayedOrSelectedDatasetCriteria criteria, AsyncCallback<Void> callback);

}
