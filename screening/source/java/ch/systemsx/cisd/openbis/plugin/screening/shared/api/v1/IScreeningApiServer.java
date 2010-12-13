/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.api.MinimalMinorVersion;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ExperimentIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.PlateIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.PlateWellReferenceWithDatasetsValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreeningExperimentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreeningPlateListReadOnlyPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreeningPlateValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;

/**
 * This interface is a part of the official public screening API. It is forbidden to change it in a
 * non-backward-compatible manner without discussing it with all screening customers.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningApiServer extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "screening";

    /**
     * The major version of this service.
     */
    public static final int MAJOR_VERSION = 1;

    /**
     * Service part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-api-v1";

    /**
     * Authenticates the user with a given password.
     * 
     * @return sessionToken if authentication succeeded, <code>null</code> otherwise.
     */
    @Transactional
    // this is not a readOnly transaction - it can create new users
    String tryLoginScreening(String userId, String userPassword) throws IllegalArgumentException;

    /**
     * Logout the session with the specified session token.
     */
    @Transactional(readOnly = true)
    void logoutScreening(final String sessionToken) throws IllegalArgumentException;

    /**
     * Return the list of all visible plates assigned to any experiment, along with their
     * hierarchical context (space, project, experiment).
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ScreeningPlateValidator.class)
    List<Plate> listPlates(String sessionToken) throws IllegalArgumentException;

    /**
     * Return the list of all visible experiments, along with their hierarchical context (space,
     * project).
     * 
     * @since 1.1
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = ScreeningExperimentValidator.class)
    @MinimalMinorVersion(1)
    List<ExperimentIdentifier> listExperiments(String sessionToken);

    /**
     * For a given set of plates (given by space / plate bar code), provide the list of all data
     * sets containing feature vectors for each of these plates.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException;

    /**
     * For a given set of plates provide the list of all data sets containing images for each of
     * these plates.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    List<ImageDatasetReference> listImageDatasets(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException;

    /**
     * Converts a given list of dataset codes to dataset identifiers.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    List<IDatasetIdentifier> getDatasetIdentifiers(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes);

    /**
     * For the given <var>experimentIdentifier</var>, find all plate locations that are connected to
     * the specified <var>materialIdentifier</var>. If <code>findDatasets == true</code>, find also
     * the connected image and image analysis data sets for the relevant plates.
     * 
     * @since 1.1
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @MinimalMinorVersion(1)
    List<PlateWellReferenceWithDatasets> listPlateWells(
            String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class) ExperimentIdentifier experimentIdentifer,
            MaterialIdentifier materialIdentifier, boolean findDatasets);

    /**
     * For the given <var>materialIdentifier</var>, find all plate locations that are connected to
     * it. If <code>findDatasets == true</code>, find also the connected image and image analysis
     * data sets for the relevant plates.
     * 
     * @since 1.2
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = PlateWellReferenceWithDatasetsValidator.class)
    @MinimalMinorVersion(2)
    List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets);

    /**
     * For the given <var>plateIdentifier</var> find all wells that are connected to it.
     * 
     * @since 1.3
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @MinimalMinorVersion(3)
    public List<WellIdentifier> listPlateWells(
            String sessionToken,
            @AuthorizationGuard(guardClass = PlateIdentifierPredicate.class) PlateIdentifier plateIdentifier);

    /**
     * For a given list of <var>plates</var>, return the mapping of plate wells to materials
     * contained in each well.
     * 
     * @param plates The list of plates to get the mapping for
     * @param materialTypeIdentifierOrNull If not <code>null</code>, consider only materials of the
     *            given type for the mapping.
     * @return A list of well to material mappings, one element for each plate.
     * @since 1.2
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @MinimalMinorVersion(2)
    List<PlateWellMaterialMapping> listPlateMaterialMapping(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull);

}
