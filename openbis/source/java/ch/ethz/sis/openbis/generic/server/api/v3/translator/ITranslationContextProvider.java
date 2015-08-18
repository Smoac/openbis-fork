/*
 * Copyright 2013 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public interface ITranslationContextProvider
{
    /**
     * Get the translation context for the current session
     * 
     * @param session
     * @param useCache whether to cache the context for later use
     * @return
     */
    TranslationContext getTranslationContext(Session session, boolean useCache);

    /**
     * Optional call to notify the server that the corresponding session context is not going to be used ever
     * 
     * @param session
     */
    void discardTranslationContext(Session session);
}