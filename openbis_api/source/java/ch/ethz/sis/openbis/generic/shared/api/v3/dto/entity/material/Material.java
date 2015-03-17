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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.MaterialType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("dto.entity.material.Material")
public class Material implements Serializable, IModificationDateHolder, IRegistratorHolder, IPropertiesHolder, IRegistrationDateHolder, ITagsHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private MaterialFetchOptions fetchOptions;

    @JsonProperty
    private MaterialPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private MaterialType type;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private Map<String, Material> materialProperties;

    @JsonProperty
    private Set<Tag> tags;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public MaterialFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setFetchOptions(MaterialFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public MaterialPermId getPermId()
    {
        return permId;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setPermId(MaterialPermId permId)
    {
        this.permId = permId;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public MaterialType getType()
    {
        if (getFetchOptions().hasType())
        {
            return type;
        }
        else
        {
            throw new NotFetchedException("Material type has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setType(MaterialType type)
    {
        this.type = type;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Person getRegistrator()
    {
        if (getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Map<String, String> getProperties()
    {
        if (getFetchOptions().hasProperties())
        {
            return properties;
        }
        else
        {
            throw new NotFetchedException("Properties have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Map<String, Material> getMaterialProperties()
    {
        if (getFetchOptions().hasMaterialProperties())
        {
            return materialProperties;
        }
        else
        {
            throw new NotFetchedException("Material Properties have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Set<Tag> getTags()
    {
        if (getFetchOptions().hasTags())
        {
            return tags;
        }
        else
        {
            throw new NotFetchedException("Tags have not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

}
