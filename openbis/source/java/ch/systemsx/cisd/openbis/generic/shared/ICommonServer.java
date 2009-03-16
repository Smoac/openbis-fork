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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.MatchingEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ProjectValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetSearchHitDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
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
    public void registerGroupRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            GroupIdentifier identifier, String person);

    /**
     * Registers a new instance role.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerInstanceRole(String sessionToken, RoleCode roleCode, String person);

    /**
     * Deletes role described by given role code, group identifier and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void deleteGroupRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            GroupIdentifier groupIdentifier, String person);

    /**
     * Deletes role described by given role code and user id.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
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
    public List<ExperimentPE> listExperiments(final String sessionToken,
            ExperimentTypePE experimentType,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            ProjectIdentifier project);

    /**
     * For given {@link SampleIdentifier} returns the corresponding list of {@link ExternalDataPE}.
     * 
     * @return a sorted list of {@link ExternalDataPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExternalDataPE> listExternalData(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            final SampleIdentifier identifier);

    /**
     * For given {@link ExperimentIdentifier} returns the corresponding list of
     * {@link ExternalDataPE}.
     * 
     * @return a sorted list of {@link ExternalDataPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ExternalDataPE> listExternalData(final String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            final ExperimentIdentifier identifier);

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
    public void registerPropertyType(final String sessionToken, final PropertyType propertyType);

    /**
     * Assigns property type to entity type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public String assignPropertyType(final String sessionToken, final EntityKind entityKind,
            final String propertyTypeCode, final String entityTypeCode, final boolean isMandatory,
            final String defaultValue);

    /**
     * Registers given {@link Vocabulary}.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerVocabulary(final String sessionToken, final Vocabulary vocabulary);

    /**
     * Registers new project.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void registerProject(String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            ProjectIdentifier projectIdentifier, String description, String leaderId);

    /**
     * Performs an <i>Hibernate Search</i> based on given parameters.
     */
    // TODO 2009-03-16 FJE, @ReturnValueFilter missing
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DataSetSearchHitDTO> searchForDataSets(String sessionToken,
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
    public void registerMaterialType(String sessionToken, MaterialType entityType);

    /**
     * Creates a new sample type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerSampleType(String sessionToken, SampleType entityType);

    /**
     * Creates a new experiment type.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerExperimentType(String sessionToken, ExperimentType entityType);

    /**
     * Deletes specified data sets.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void deleteDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            List<String> dataSetCodes);

    /**
     * Saves changed experiment.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    public void editExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            ExperimentIdentifier experimentIdentifier, List<ExperimentProperty> properties);

    /**
     * Saves changed material.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    // TODO 2009-03-16 IA, Authorization guard for material identifiers
    public void editMaterial(String sessionToken, MaterialIdentifier identifier,
            List<MaterialProperty> properties);

    /**
     * Saves changed sample.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    public void editSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier identifier, List<SampleProperty> properties);

}
