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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("ProjectFetchOptions")
public class ProjectFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    public SpaceFetchOptions fetchSpace()
    {
        if (this.space == null)
        {
            this.space = new SpaceFetchOptions();
        }
        return this.space;
    }

    public SpaceFetchOptions fetchSpace(SpaceFetchOptions fetchOptions)
    {
        return this.space = fetchOptions;
    }

    public boolean hasSpace()
    {
        return this.space != null;
    }

    public PersonFetchOptions fetchRegistrator()
    {
        if (this.registrator == null)
        {
            this.registrator = new PersonFetchOptions();
        }
        return this.registrator;
    }

    public PersonFetchOptions fetchRegistrator(PersonFetchOptions fetchOptions)
    {
        return this.registrator = fetchOptions;
    }

    public boolean hasRegistrator()
    {
        return this.registrator != null;
    }

    public PersonFetchOptions fetchModifier()
    {
        if (this.modifier == null)
        {
            this.modifier = new PersonFetchOptions();
        }
        return this.modifier;
    }

    public PersonFetchOptions fetchModifier(PersonFetchOptions fetchOptions)
    {
        return this.modifier = fetchOptions;
    }

    public boolean hasModifier()
    {
        return this.modifier != null;
    }

}
