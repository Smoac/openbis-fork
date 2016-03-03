/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.tag.fetchoptions.TagFetchOptions")
public class TagFetchOptions extends FetchOptions<Tag> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions owner;

    @JsonProperty
    private TagSortOptions sort;

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwner()
    {
        if (owner == null)
        {
            owner = new PersonFetchOptions();
        }
        return owner;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withOwnerUsing(PersonFetchOptions fetchOptions)
    {
        return owner = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasOwner()
    {
        return owner != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public TagSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new TagSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public TagSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Tag", this);
        f.addFetchOption("Owner", owner);
        return f;
    }

}
