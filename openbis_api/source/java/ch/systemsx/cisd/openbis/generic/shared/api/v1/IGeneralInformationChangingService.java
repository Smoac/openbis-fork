/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import java.util.Map;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;

/**
 * Service for changing general informations.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGeneralInformationChangingService extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "general-information-changing";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v1";

    /**
     * URL where the service is exposed via JSON interface.
     */
    public static final String JSON_SERVICE_URL = SERVICE_URL + ".json";

    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties);

    /**
     * Adds new unofficial terms to a vocabulary starting from specified ordinal + 1.
     * <p>
     * 
     * @deprecated Because the parameters refer to an internal openBIS class (TechID).
     */
    @Deprecated
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal);

    /**
     * Adds new unofficial terms to a vocabulary starting from specified ordinal + 1.
     */
    public void addUnofficialVocabularyTerm(String sessionToken, Long vocabularyId,
            NewVocabularyTerm term);

    /**
     * Returns the persistent settings for a given custom web app.
     * 
     * @param webAppId The id of the custom web app to get the display settings for.
     * @since 1.2
     */
    public WebAppSettings getWebAppSettings(String sessionToken, String webAppId);

    /**
     * Sets the persistent settings for a given custom web app.
     * 
     * @param webAppSettings The new display settings
     * @since 1.2
     */
    public void setWebAppSettings(String sessionToken, WebAppSettings webAppSettings);

    /**
     * Creates a new metaproject.
     * 
     * @param name Name of the metaproject
     * @param descriptionOrNull Description of the metaproject
     * @return Newly created metaproject
     * @since 1.3
     */
    public Metaproject createMetaproject(String sessionToken, String name, String descriptionOrNull);

    /**
     * Updates an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject to update
     * @param name New name of the metaproject
     * @param descriptionOrNull New description of the metaproject
     * @return Updated metaproject
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            String name, String descriptionOrNull);

    /**
     * Deletes an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject to delete
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId);

    /**
     * Adds given entities to an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject
     * @param assignmentsToAdd Assignments that should be added to the metaproject
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd);

    /**
     * Removes given entities from an existing metaproject.
     * 
     * @param metaprojectId Id of the metaproject
     * @param assignmentsToRemove Assignments that should be removed from the metaproject
     * @throws UserFailureException when a metaproject with the specified id doesn't exist.
     * @since 1.3
     */
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove);

}
