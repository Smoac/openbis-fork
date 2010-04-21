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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreenerPlateValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.authorization.ScreenerReadonlyPlatePredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.PlateIdentifier;

/**
 * This interface is a part of the official public screening API. It is forbidden to change it in a
 * non-backward-compatible manner without discussing it with all screening customers.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningApiServer
{
    /**
     * Authenticates the user with a given password.
     * 
     *@return sessionToken if authentication suceeded, null otherwise
     */
    @Transactional(readOnly = true)
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
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = ScreenerPlateValidator.class)
    List<Plate> listPlates(String sessionToken) throws IllegalArgumentException;

    /**
     * For a given set of plates (given by space / plate bar code), provide the list of all data
     * sets containing feature vectors for each of these plates.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerReadonlyPlatePredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException;

    /**
     * For a given set of plates provide the list of all data sets containing images for each of
     * these plates.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    List<ImageDatasetReference> listImageDatasets(
            String sessionToken,
            @AuthorizationGuard(guardClass = ScreenerReadonlyPlatePredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException;

    /**
     * Converts a given list of dataset codes to dataset identifiers.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) List<String> datasetCodes);
}
