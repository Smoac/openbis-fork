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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ProjectUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractExpressionPredicate.DeleteGridCustomColumnPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractExpressionPredicate.DeleteGridCustomFilterPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractExpressionPredicate.UpdateGridCustomColumnPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractExpressionPredicate.UpdateGridCustomFilterPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.GroupTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExpressionValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExternalDataValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.MatchingEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ProjectValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.SampleValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IFilterOrColumnUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICommonServer extends IServer
{
    /** Keeps the session with specified token alive. */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public void keepSessionAlive(String sessionToken);

    /**
     * Returns all groups which belong to the specified database instance. *
     * 
     * @return a sorted list of {@link Group}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = GroupValidator.class)
    public List<Group> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier);

    /**
     * Registers a new group with specified code and optional description.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GROUP)
    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull);

    /**
     * Updates a property type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.GROUP)
    public void updateGroup(final String sessionToken, final IGroupUpdates updates);

    /**
     * Registers a new authorization group.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.AUTHORIZATION_GROUP)
    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup);

    /**
     * Deletes selected authorization groups.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.AUTHORIZATION_GROUP)
    public void deleteAuthorizationGroups(String sessionToken, List<TechId> authGroupIds,
            String reason);

    /**
     * Returns all persons from current instance.
     * 
     * @return a sorted list of {@link Person}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Person> listPersons(String sessionToken);

    /**
     * Returns all projects.
     * 
     * @return a sorted list of {@link Project}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ProjectValidator.class)
    public List<Project> listProjects(String sessionToken);

    /**
     * Registers a new person.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PERSON)
    public void registerPerson(String sessionToken, String userID);

    /**
     * Returns a list of all roles.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public List<RoleAssignment> listRoleAssignments(String sessionToken);

    /**
     * Registers a new group role.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void registerGroupRole(
            String sessionToken,
            RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) GroupIdentifier identifier,
            Grantee grantee);

    /**
     * Registers a new instance role.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void registerInstanceRole(String sessionToken, RoleCode roleCode, Grantee grantee);

    /**
     * Deletes role described by given role code, group identifier and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void deleteGroupRole(
            String sessionToken,
            RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) GroupIdentifier groupIdentifier,
            Grantee grantee);

    /**
     * Deletes role described by given role code and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE_ASSIGNMENT)
    public void deleteInstanceRole(String sessionToken, RoleCode roleCode, Grantee grantee);

    /**
     * Lists sample types which are appropriate for listing.
     * 
     * @return a sorted list of {@link SampleType}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<SampleType> listSampleTypes(String sessionToken);

    /**
     * Lists samples using given configuration.
     * 
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Sample> listSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class) final ListSampleCriteria criteria);

    /**
     * Lists experiments.
     * 
     * @return a sorted list of {@link Experiment}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Experiment> listExperiments(
            final String sessionToken,
            ExperimentType experimentType,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) ProjectIdentifier project);

    /**
     * For given sample {@link TechId} returns the corresponding list of {@link ExternalData}.
     * 
     * @return a sorted list of {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExternalData> listSampleExternalData(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) final TechId sampleId,
            final boolean showOnlyDirectlyConnected);

    /**
     * For given experiment {@link TechId} returns the corresponding list of {@link ExternalData}.
     * 
     * @return a sorted list of {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExternalData> listExperimentExternalData(
            final String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) final TechId experimentId);

    /**
     * For given data set {@link TechId} in given relationship <var>role</var> returns corresponding
     * list of {@link ExternalData}.
     * 
     * @return a sorted list of {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExternalData> listDataSetRelationships(final String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) final TechId datasetId,
            final DataSetRelationshipRole role);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = MatchingEntityValidator.class)
    public List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText,
            final boolean useWildcardSearchMode);

    /**
     * List experiment types.
     * 
     * @return a sorted list of {@link ExperimentType}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExperimentType> listExperimentTypes(String sessionToken);

    /**
     * List property types.
     * 
     * @return a sorted list of {@link PropertyType}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<PropertyType> listPropertyTypes(final String sessionToken, boolean withRelations);

    /**
     * Lists data types.
     * 
     * @return a sorted list of {@link DataType}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DataType> listDataTypes(final String sessionToken);

    /**
     * Lists file format types.
     * 
     * @return a sorted list of {@link FileFormatType}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<FileFormatType> listFileFormatTypes(String sessionToken);

    /**
     * Lists vocabularies.
     * 
     * @return a sorted list of {@link Vocabulary}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Vocabulary> listVocabularies(final String sessionToken, final boolean withTerms,
            boolean excludeInternal);

    /**
     * Registers given {@link PropertyType}.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE)
    public void registerPropertyType(final String sessionToken, final PropertyType propertyType);

    /**
     * Updates a property type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.PROPERTY_TYPE)
    public void updatePropertyType(final String sessionToken, final IPropertyTypeUpdates updates);

    /**
     * Deletes specified property types.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE)
    public void deletePropertyTypes(String sessionToken, List<TechId> propertyTypeIds, String reason);

    /**
     * Assigns property type to entity type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue, final String section, final Long previousETPTOrdinal);

    /**
     * Update property type assignment to entity type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
    public void updatePropertyTypeAssignment(final String sessionToken,
            final EntityKind entityKind, final String propertyTypeCode,
            final String entityTypeCode, final boolean isMandatory, final String defaultValue,
            final String section, final Long previousETPTOrdinal);

    /**
     * Unassigns property type to entity type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
    public void unassignPropertyType(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode);

    /**
     * Returns the number of entities of specified kind and type which have a property of specified
     * type.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public int countPropertyTypedEntities(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode);

    /**
     * Registers given {@link NewVocabulary}.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY)
    public void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary);

    /**
     * Updates a vocabulary.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY)
    public void updateVocabulary(String sessionToken, IVocabularyUpdates updates);

    /**
     * Deletes specified vocabularies.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY)
    public void deleteVocabularies(String sessionToken, List<TechId> vocabularyIds, String reason);

    /**
     * Deletes specified projects.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROJECT)
    public void deleteProjects(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) List<TechId> projectIds,
            String reason);

    /**
     * Deletes specified groups.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GROUP)
    public void deleteGroups(String sessionToken,
            @AuthorizationGuard(guardClass = GroupTechIdPredicate.class) List<TechId> groupIds,
            String reason);

    /**
     * Adds new terms to a vocabulary starting from specified ordinal + 1.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<String> vocabularyTerms, Long previousTermOrdinal);

    /**
     * Updates a vocabulary term.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY_TERM)
    public void updateVocabularyTerm(final String sessionToken, final IVocabularyTermUpdates updates);

    /**
     * Deletes from the specified vocabulary the specified terms.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced);

    /**
     * Registers new project.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROJECT)
    public void registerProject(
            String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) ProjectIdentifier projectIdentifier,
            String description, String leaderId, Collection<NewAttachment> attachments);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<ExternalData> searchForDataSets(String sessionToken, DetailedSearchCriteria criteria);

    /**
     * For given {@link TechId} returns the corresponding {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public ExternalData getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> searchForSamples(String sessionToken, DetailedSearchCriteria criteria);

    /**
     * Returns all data sets related to specified entities.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<ExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities entities);

    /**
     * List material types.
     * 
     * @return a sorted list of {@link MaterialType}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<MaterialType> listMaterialTypes(String sessionToken);

    /**
     * Lists materials.
     * 
     * @return a sorted list of {@link Material}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Material> listMaterials(String sessionToken, MaterialType materialType);

    /**
     * Creates a new material type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL_TYPE)
    public void registerMaterialType(String sessionToken, MaterialType entityType);

    /**
     * Updates a material type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL_TYPE)
    public void updateMaterialType(String sessionToken, EntityType entityType);

    /**
     * Creates a new sample type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE_TYPE)
    public void registerSampleType(String sessionToken, SampleType entityType);

    /**
     * Updates a sample type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE_TYPE)
    public void updateSampleType(String sessionToken, EntityType entityType);

    /**
     * Creates a new experiment type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT_TYPE)
    public void registerExperimentType(String sessionToken, ExperimentType entityType);

    /**
     * Updates a experiment type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT_TYPE)
    public void updateExperimentType(String sessionToken, EntityType entityType);

    /**
     * Creates a new file format type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.FILE_FORMAT_TYPE)
    public void registerFileFormatType(String sessionToken, FileFormatType type);

    /**
     * Creates a new data set type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATASET_TYPE)
    public void registerDataSetType(String sessionToken, DataSetType entityType);

    /**
     * Updates a data set type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.DATASET_TYPE)
    public void updateDataSetType(String sessionToken, EntityType entityType);

    /**
     * Deletes specified data sets.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void deleteDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) List<String> dataSetCodes,
            String reason);

    /**
     * Deletes specified samples.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void deleteSamples(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) List<TechId> sampleIds,
            String reason);

    /**
     * Deletes specified experiments.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public void deleteExperiments(
            String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) List<TechId> experimentIds,
            String reason);

    /**
     * Deletes specified attachments (all versions with given file names) of specified experiment.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void deleteExperimentAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            List<String> fileNames, String reason);

    /**
     * Deletes specified attachments (all versions with given file names) of specified sample.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void deleteSampleAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleId,
            List<String> fileNames, String reason);

    /**
     * Deletes specified attachments (all versions with given file names) of specified project.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void deleteProjectAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId,
            List<String> fileNames, String reason);

    /**
     * Returns all attachments (all versions) of specified experiment.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Attachment> listExperimentAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId);

    /**
     * Returns all attachments (all versions) of specified sample.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Attachment> listSampleAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleId);

    /**
     * Returns all attachments (all versions) of specified project.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Attachment> listProjectAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId);

    /**
     * Uploads specified data sets to CIFEX server of specified URL with specified password.
     * 
     * @return a message or an empty string
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public String uploadDataSets(String sessionToken, List<String> dataSetCodes,
            DataSetUploadContext uploadContext);

    /**
     * Lists vocabulary terms of a given vocabulary. Includes terms usage statistics.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary);

    /**
     * Lists vocabulary terms of a given vocabulary.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary);

    /**
     * List data set types.
     * 
     * @return a sorted list of {@link DataSetType}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DataSetType> listDataSetTypes(String sessionToken);

    /**
     * @return Information about the time and kind of the last modification, separately for each
     *         kind of database object.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    public LastModificationState getLastModificationState(String sessionToken);

    /**
     * For given {@link TechId} returns the corresponding {@link Project}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public Project getProjectInfo(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId);

    /**
     * Returns unique code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.USER)
    public String generateCode(String sessionToken, String prefix);

    /**
     * Saves changed project.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public Date updateProject(
            String sessionToken,
            @AuthorizationGuard(guardClass = ProjectUpdatesPredicate.class) ProjectUpdatesDTO updates);

    /**
     * Deletes specified data set types.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.DATASET_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * Deletes specified sample types.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.SAMPLE_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * Deletes specified experiment types.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.EXPERIMENT_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * Deletes specified file format types.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.FILE_FORMAT_TYPE })
    public void deleteFileFormatTypes(String sessionToken, List<String> codes);

    /**
     * Deletes specified material types.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.MATERIAL_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * For given {@link EntityKind} and permanent <var>identifier</var> returns the corresponding
     * {@link IEntityInformationHolder}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public IEntityInformationHolder getEntityInformationHolder(String sessionToken,
            EntityKind entityKind, String permId);

    /**
     * Returns file template available during batch operation of entity of given type.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public String getTemplateColumns(String sessionToken, EntityKind kind, String type,
            boolean autoGenerate, boolean withExperiments, BatchOperationKind operationKind);

    /**
     * Updates file format type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.FILE_FORMAT_TYPE)
    public void updateFileFormatType(String sessionToken, AbstractType type);

    /**
     * Updates the experiment attachment.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.EXPERIMENT)
    public void updateExperimentAttachments(String sessionToken, TechId experimentId,
            Attachment attachment);

    /**
     * Updates the sample attachment.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSampleAttachments(String sessionToken, TechId sampleId, Attachment attachment);

    /**
     * Updates the project attachment.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.PROJECT)
    public void updateProjectAttachments(String sessionToken, TechId projectId,
            Attachment attachment);

    /** Lists all available datastore services of the specified kind */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken,
            DataStoreServiceKind dataStoreServiceKind);

    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) List<String> datasetCodes);

    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.USER)
    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) List<String> datasetCodes);

    /**
     * Returns all authorization groups.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken);

    /**
     * Saves changed authorization group.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.AUTHORIZATION_GROUP)
    public Date updateAuthorizationGroup(String sessionToken, AuthorizationGroupUpdates updates);

    /**
     * Returns all persons belonging to given authorization group.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId);

    /**
     * Adds specified persons to given authorization group.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void addPersonsToAuthorizationGroup(String sessionToken, TechId authorizationGroupId,
            List<String> personsCodes);

    /**
     * Removes specified persons from given authorization group.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void removePersonsFromAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes);

    /**
     * Lists filters available for selected grid.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ExpressionValidator.class)
    public List<GridCustomFilter> listFilters(String sessionToken, String gridId);

    /**
     * Creates a new filter.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_FILTER)
    public void registerFilter(String sessionToken, NewColumnOrFilter filter);

    /**
     * Deletes specified filters.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_FILTER)
    public void deleteFilters(
            String sessionToken,
            @AuthorizationGuard(guardClass = DeleteGridCustomFilterPredicate.class) List<TechId> filterIds);

    /**
     * Updates a filter.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.GRID_CUSTOM_FILTER)
    public void updateFilter(
            String sessionToken,
            @AuthorizationGuard(guardClass = UpdateGridCustomFilterPredicate.class) IFilterOrColumnUpdates updates);

    // columns

    /**
     * Lists columns available for selected grid.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ExpressionValidator.class)
    public List<GridCustomColumn> listGridCustomColumns(String sessionToken, String gridId);

    /**
     * Creates a new column.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_COLUMN)
    public void registerGridCustomColumn(String sessionToken, NewColumnOrFilter column);

    /**
     * Deletes specified columns.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GRID_CUSTOM_COLUMN)
    public void deleteGridCustomColumns(
            String sessionToken,
            @AuthorizationGuard(guardClass = DeleteGridCustomColumnPredicate.class) List<TechId> columnIds);

    /**
     * Updates a column.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.GRID_CUSTOM_COLUMN)
    public void updateGridCustomColumn(
            String sessionToken,
            @AuthorizationGuard(guardClass = UpdateGridCustomColumnPredicate.class) IFilterOrColumnUpdates updates);

    /**
     * Updates vocabulary terms.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.VOCABULARY_TERM)
    public void updateVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> terms);

    /**
     * Deletes specified materials.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void deleteMaterials(String sessionToken, List<TechId> materialIds, String reason);

}
