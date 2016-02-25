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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.experiment.fetchoptions.ExperimentTypeFetchOptions")
public class ExperimentTypeFetchOptions extends FetchOptions<ExperimentType> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentTypeSortOptions sort;

    // Method automatically generated with DtoGenerator
    @Override
    public ExperimentTypeSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new ExperimentTypeSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public ExperimentTypeSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    public FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("ExperimentType", this);
        return f;
    }

}
