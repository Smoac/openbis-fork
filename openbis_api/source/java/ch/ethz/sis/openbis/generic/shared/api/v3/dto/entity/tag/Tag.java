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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("Tag")
public class Tag implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private TagFetchOptions fetchOptions;

    @JsonProperty
    private TagPermId permId;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty(value="private")
    private Boolean isPrivate;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person owner;

    @JsonIgnore
    public TagFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(TagFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public TagPermId getPermId()
    {
        return permId;
    }

    public void setPermId(TagPermId permId)
    {
        this.permId = permId;
    }

    @JsonIgnore
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @JsonIgnore
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @JsonIgnore
    public Boolean isPrivate()
    {
        return isPrivate;
    }

    public void setPrivate(Boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    @JsonIgnore
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @JsonIgnore
    public Person getOwner()
    {
        if (getFetchOptions().hasOwner())
        {
            return owner;
        }
        else
        {
            throw new NotFetchedException("Owner has not been fetched.");
        }
    }

    public void setOwner(Person owner)
    {
        this.owner = owner;
    }

}
