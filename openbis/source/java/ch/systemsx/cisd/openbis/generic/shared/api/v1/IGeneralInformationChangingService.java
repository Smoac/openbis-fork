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

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

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

    @Transactional
    public void updateSampleProperties(String sessionToken, long sampleID,
            Map<String, String> properties);

    /**
     * Adds new unofficial terms to a vocabulary starting from specified ordinal + 1.
     * <p>
     * 
     * @deprecated Because the parameters refer to an internal openBIS class (TechID).
     */
    @Transactional
    @Deprecated
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal);

    /**
     * Adds new unofficial terms to a vocabulary starting from specified ordinal + 1.
     */
    @Transactional
    public void addUnofficialVocabularyTerm(String sessionToken, Long vocabularyId,
            NewVocabularyTerm term);

}
