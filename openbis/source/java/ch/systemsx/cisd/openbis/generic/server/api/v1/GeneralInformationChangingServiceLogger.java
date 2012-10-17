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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.Map;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
class GeneralInformationChangingServiceLogger extends AbstractServerLogger implements
        IGeneralInformationChangingService
{

    public GeneralInformationChangingServiceLogger(ISessionManager<Session> sessionManagerNull,
            IInvocationLoggerContext context)
    {
        super(sessionManagerNull, context);
    }

    @Override
    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties)
    {
        logTracking(sessionToken, "update-sample-properties", "SAMPLE(%s)", sampleID);
    }

    @Override
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        logTracking(sessionToken, "add_unofficial_vocabulary_term",
                "ID(%s) CODE(%s), LABEL(%s), DESCRIPTION(%s), PREVIOUS_ORDINAL(%s)", vocabularyId,
                code, label, description, Long.toString(previousTermOrdinal));
    }

    @Override
    public void addUnofficialVocabularyTerm(String sessionToken, Long vocabularyId,
            NewVocabularyTerm term)
    {
        logTracking(sessionToken, "add-unofficial-vocabulary-term", "VOCABULARY_ID(%s) TERM(%s)",
                vocabularyId, term);

    }

    @Override
    public WebAppSettings getWebAppSettings(String sessionToken, String webAppId)
    {
        logAccess(sessionToken, "get-custom-display-settings", "WEB_APP_ID(%s)", webAppId);
        return null;
    }

    @Override
    public void setWebAppSettings(String sessionToken, WebAppSettings webAppSettings)
    {
        logAccess(sessionToken, "set-custom-display-settings", "WEB_APP_ID(%s)",
                webAppSettings.getWebAppId());
    }

    @Override
    public Metaproject createMetaproject(String sessionToken, String name, String description)
    {
        logAccess(sessionToken, "createMetaproject", "NAME(%s) DESCRIPTION(%s)", name, description);
        return null;
    }

    @Override
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            String name, String descriptionOrNull)
    {
        logAccess(sessionToken, "updateMetaproject", "METAPROJECT_ID(%s) NAME(%s) DESCRIPTION(%s)",
                metaprojectId, name, descriptionOrNull);
        return null;
    }

    @Override
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        logAccess(sessionToken, "deleteMetaproject", "METAPROJECT_ID(%s)", metaprojectId);
    }

    @Override
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd)
    {
        MetaprojectAssignmentsIds assignments =
                assignmentsToAdd != null ? assignmentsToAdd : new MetaprojectAssignmentsIds();

        logAccess(sessionToken, "addToMetaproject",
                "METAPROJECT_ID(%s), EXPERIMENTS(%s), SAMPLES(%s), DATA_SETS(%s), MATERIALS(%s)",
                metaprojectId, abbreviate(assignments.getExperiments()),
                abbreviate(assignments.getSamples()), abbreviate(assignments.getDataSets()),
                abbreviate(assignments.getMaterials()));
    }

    @Override
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove)
    {
        MetaprojectAssignmentsIds assignments =
                assignmentsToRemove != null ? assignmentsToRemove : new MetaprojectAssignmentsIds();

        logAccess(sessionToken, "removeFromMetaproject",
                "METAPROJECT_ID(%s), EXPERIMENTS(%s), SAMPLES(%s), DATA_SETS(%s), MATERIALS(%s)",
                metaprojectId, abbreviate(assignments.getExperiments()),
                abbreviate(assignments.getSamples()), abbreviate(assignments.getDataSets()),
                abbreviate(assignments.getMaterials()));
    }

    @Override
    public int getMajorVersion()
    {
        return 0;
    }

    @Override
    public int getMinorVersion()
    {
        return 0;
    }
}
