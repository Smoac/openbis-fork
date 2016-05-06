/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.property.fetchoptions.PropertyAssignmentFetchOptions")
public class PropertyAssignmentFetchOptions extends FetchOptions<PropertyAssignment>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private VocabularyFetchOptions vocabulary;
    
    @JsonProperty
    private PropertyAssignmentSortOptions sort;

    public VocabularyFetchOptions withVocabulary()
    {
        if (vocabulary == null)
        {
            vocabulary = new VocabularyFetchOptions();
        }
        return vocabulary;
    }

    public VocabularyFetchOptions withVocabularyUsing(VocabularyFetchOptions fetchOptions)
    {
        return vocabulary = fetchOptions;
    }

    public boolean hasVocabulary()
    {
        return vocabulary != null;
    }

    @Override
    public PropertyAssignmentSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new PropertyAssignmentSortOptions();
        }
        return sort;
    }

    @Override
    public PropertyAssignmentSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("PropertyAssignment", this);
        f.addFetchOption("Vocabulary", vocabulary);
        return f;
    }
}
