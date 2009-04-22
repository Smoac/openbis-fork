/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * Enum for all generic {@link IDisplayTypeIDGenerator}s.
 *
 * @author Franz-Josef Elmer
 */
public enum DisplayTypeIDGenerator implements IDisplayTypeIDGenerator
{
    ENTITY_BROWSER_GRID("entity-browser-grid"), 
    EXPERIMENT_DETAILS_GRID("experiment-details-grid"),
    SAMPLE_DETAILS_GRID("sample-details-grid"),
    TYPE_BROWSER_GRID("type-browser-grid"),
    SEARCH_RESULT_GRID("search-result-grid"),
    DATA_SET_SEARCH_RESULT_GRID("data-set-search-result-grid"),
    PROJECT_BROWSER_GRID("project-browser-grid"),
    PERSON_BROWSER_GRID("person-browser-grid"),
    GROUPS_BROWSER_GRID("group-browser-grid"),
    ROLE_ASSIGNMENT_BROWSER_GRID("role-assignment-browser-grid"),
    PROPERTY_TYPE_BROWSER_GRID("property-type-browser-grid"),
    PROPERTY_TYPE_ASSIGNMENT_BROWSER_GRID("property-type-assignment-browser-grid"),
    VOCABULARY_BROWSER_GRID("vocabulary-browser-grid"),
    VOCABULARY_TERMS_GRID("vocabulary-terms-grid"),
    ;
    
    private final String genericNameOrPrefix;

    private DisplayTypeIDGenerator(String genericNameOrPrefix)
    {
        this.genericNameOrPrefix = genericNameOrPrefix;
    }
    
    public String createID(EntityKind entityKindOrNull, EntityType entityTypeOrNull)
    {
        String id = genericNameOrPrefix;
        if (entityKindOrNull != null)
        {
            id += "-" + entityKindOrNull.toString();
        }
        if (entityTypeOrNull != null)
        {
            id += "-" + entityTypeOrNull.getCode();
        }
        return id;
    }
}
