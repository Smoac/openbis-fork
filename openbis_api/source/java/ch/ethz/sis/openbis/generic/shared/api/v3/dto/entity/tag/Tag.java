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
    private String code;

    @JsonProperty
    private String description;

    @JsonProperty(value="private")
    private Boolean isPrivate;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person owner;

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    @JsonIgnore
    public TagFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public void setFetchOptions(TagFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    @JsonIgnore
    public TagPermId getPermId()
    {
        return permId;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public void setPermId(TagPermId permId)
    {
        this.permId = permId;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    @JsonIgnore
    public String getCode()
    {
        return code;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public void setCode(String code)
    {
        this.code = code;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    @JsonIgnore
    public String getDescription()
    {
        return description;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    @JsonIgnore
    public Boolean isPrivate()
    {
        return isPrivate;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public void setPrivate(Boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    @JsonIgnore
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
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

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public void setOwner(Person owner)
    {
        this.owner = owner;
    }

}
