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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions")
public class ObjectKindModificationFetchOptions extends FetchOptions<ObjectKindModification> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ObjectKindModificationSortOptions sort;

    // Method automatically generated with DtoGenerator
    @Override
    public ObjectKindModificationSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new ObjectKindModificationSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public ObjectKindModificationSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("ObjectKindModification", this);
        return f;
    }

}
