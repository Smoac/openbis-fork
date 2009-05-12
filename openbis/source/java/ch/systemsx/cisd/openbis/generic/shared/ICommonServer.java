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
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ProjectUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExternalDataValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.MatchingEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ProjectValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface ICommonServer extends IServer
{
    /**
     * Returns all groups which belong to the specified database instance. *
     * 
     * @return a sorted list of {@link GroupPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = GroupValidator.class)
    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier);

    /**
     * Registers a new group with specified code and optional description and group leader ID.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.GROUP)
    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull);

    /**
     * Returns all persons from current instance.
     * 
     * @return a sorted list of {@link PersonPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<PersonPE> listPersons(String sessionToken);

    /**
     * Returns all projects.
     * 
     * @return a sorted list of {@link ProjectPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ProjectValidator.class)
    public List<ProjectPE> listProjects(String sessionToken);

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
    public List<RoleAssignmentPE> listRoles(String sessionToken);

    /**
     * Registers a new group role.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE)
    public void registerGroupRole(
            String sessionToken,
            RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) GroupIdentifier identifier,
            String person);

    /**
     * Registers a new instance role.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE)
    public void registerInstanceRole(String sessionToken, RoleCode roleCode, String person);

    /**
     * Deletes role described by given role code, group identifier and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE)
    public void deleteGroupRole(
            String sessionToken,
            RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) GroupIdentifier groupIdentifier,
            String person);

    /**
     * Deletes role described by given role code and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.ROLE)
    public void deleteInstanceRole(String sessionToken, RoleCode roleCode, String person);

    /**
     * Lists sample types which are appropriate for listing.
     * 
     * @return a sorted list of {@link SampleTypePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<SampleTypePE> listSampleTypes(String sessionToken);

    /**
     * Lists samples using given configuration.
     * 
     * @return a sorted list of {@link SamplePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<SamplePE> listSamples(final String sessionToken,
            final ListSampleCriteriaDTO criteria);

    /**
     * Lists experiments.
     * 
     * @return a sorted list of {@link ExperimentPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExperimentPE> listExperiments(
            final String sessionToken,
            ExperimentTypePE experimentType,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) ProjectIdentifier project);

    /**
     * For given {@link SampleIdentifier} returns the corresponding list of {@link ExternalDataPE}.
     * 
     * @return a sorted list of {@link ExternalDataPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExternalDataPE> listExternalData(
            final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) final SampleIdentifier identifier);

    /**
     * For given {@link ExperimentIdentifier} returns the corresponding list of
     * {@link ExternalDataPE}.
     * 
     * @return a sorted list of {@link ExternalDataPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExternalDataPE> listExternalData(
            final String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) final ExperimentIdentifier identifier);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = MatchingEntityValidator.class)
    public List<SearchHit> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText);

    /**
     * List experiment types.
     * 
     * @return a sorted list of {@link ExperimentTypePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExperimentTypePE> listExperimentTypes(String sessionToken);

    /**
     * List property types.
     * 
     * @return a sorted list of {@link PropertyTypePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<PropertyTypePE> listPropertyTypes(final String sessionToken);

    /**
     * Lists data types.
     * 
     * @return a sorted list of {@link DataTypePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DataTypePE> listDataTypes(final String sessionToken);

    /**
     * Lists file format types.
     * 
     * @return a sorted list of {@link FileFormatTypePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<FileFormatTypePE> listFileFormatTypes(String sessionToken);

    /**
     * Lists vocabularies.
     * 
     * @return a sorted list of {@link VocabularyPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<VocabularyPE> listVocabularies(final String sessionToken, final boolean withTerms,
            boolean excludeInternal);

    /**
     * Registers given {@link PropertyType}.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE)
    public void registerPropertyType(final String sessionToken, final PropertyType propertyType);

    /**
     * Assigns property type to entity type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.PROPERTY_TYPE_ASSIGNMENT)
    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue);

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
     * Registers given {@link Vocabulary}.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY)
    public void registerVocabulary(final String sessionToken, final Vocabulary vocabulary);

    /**
     * Adds new terms to a vocabulary.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void addVocabularyTerms(String sessionToken, String vocabularyCode,
            List<String> vocabularyTerms);

    /**
     * Deletes from the specified vocabulary the specified terms.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.VOCABULARY_TERM)
    public void deleteVocabularyTerms(String sessionToken, String vocabularyCode,
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
            String description, String leaderId, List<AttachmentPE> attachments);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<ExternalDataPE> searchForDataSets(String sessionToken,
            DataSetSearchCriteria criteria);

    /**
     * List material types.
     * 
     * @return a sorted list of {@link MaterialTypePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<MaterialTypePE> listMaterialTypes(String sessionToken);

    /**
     * Lists materials.
     * 
     * @return a sorted list of {@link MaterialPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<MaterialPE> listMaterials(String sessionToken, MaterialTypePE materialType);

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
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL_TYPE)
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
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE_TYPE)
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
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT_TYPE)
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
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATASET_TYPE)
    public void updateDataSetType(String sessionToken, EntityType entityType);

    /**
     * Deletes specified data sets.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.DATA_SET)
    public void deleteDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) List<String> dataSetCodes,
            String reason);

    /**
     * Uploads specified data sets to CIFEX server of specified URL with specified password.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public void uploadDataSets(String sessionToken, List<String> dataSetCodes,
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
    public Set<VocabularyTermPE> listVocabularyTerms(String sessionToken, Vocabulary vocabulary);

    /**
     * List data set types.
     * 
     * @return a sorted list of {@link DataSetTypePE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DataSetTypePE> listDataSetTypes(String sessionToken);

    /**
     * @return Information about the time and kind of the last modification, separately for each
     *         kind of database object.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    public LastModificationState getLastModificationState(String sessionToken);

    /**
     * For given {@link TechId} returns the corresponding {@link ProjectPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public ProjectPE getProjectInfo(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId);

    /**
     * Returns unique code.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
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
     * Deletes specified material types.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.MATERIAL_TYPE, ObjectKind.PROPERTY_TYPE_ASSIGNMENT })
    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes);

    /**
     * For given {@link EntityKind} and <var>identifier</var> returns the corresponding
     * {@link IEntityInformationHolder}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public IEntityInformationHolder getEntityInformationHolder(String sessionToken,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind,
            String identifier); // TODO 2009-05-05, Piotr Buczek: add authorization?
}
