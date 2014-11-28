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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("DataSet")
public class DataSet implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetFetchOptions fetchOptions;

    @JsonProperty
    private DataSetPermId permId;

    @JsonProperty
    private String code;

    @JsonProperty
    private Date accessDate;

    @JsonProperty
    private Boolean derived;

    @JsonProperty
    private Boolean placeholder;

    @JsonProperty
    private List<DataSet> parents;

    @JsonProperty
    private List<DataSet> children;

    @JsonProperty
    private List<DataSet> containers;

    @JsonProperty
    private List<DataSet> contained;

    @JsonProperty
    private Set<Tag> tags;

    @JsonProperty
    private DataSetType type;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Experiment experiment;

    @JsonProperty
    private Map<String, String> properties;

    @JsonIgnore
    public DataSetFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(DataSetFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public DataSetPermId getPermId()
    {
        return permId;
    }

    public void setPermId(DataSetPermId permId)
    {
        this.permId = permId;
    }

    @JsonIgnore
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @JsonIgnore
    public Date getAccessDate()
    {
        return accessDate;
    }

    public void setAccessDate(Date accessDate)
    {
        this.accessDate = accessDate;
    }

    @JsonIgnore
    public Boolean isDerived()
    {
        return derived;
    }

    public void setDerived(Boolean derived)
    {
        this.derived = derived;
    }

    @JsonIgnore
    public Boolean isPlaceholder()
    {
        return placeholder;
    }

    public void setPlaceholder(Boolean placeholder)
    {
        this.placeholder = placeholder;
    }

    @JsonIgnore
    public List<DataSet> getParents()
    {
        if (getFetchOptions().hasParents())
        {
            return parents;
        }
        else
        {
            throw new NotFetchedException("Parents has not been fetched.");
        }
    }

    public void setParents(List<DataSet> parents)
    {
        this.parents = parents;
    }

    @JsonIgnore
    public List<DataSet> getChildren()
    {
        if (getFetchOptions().hasChildren())
        {
            return children;
        }
        else
        {
            throw new NotFetchedException("Children has not been fetched.");
        }
    }

    public void setChildren(List<DataSet> children)
    {
        this.children = children;
    }

    @JsonIgnore
    public List<DataSet> getContainers()
    {
        if (getFetchOptions().hasContainers())
        {
            return containers;
        }
        else
        {
            throw new NotFetchedException("Container data sets has not been fetched.");
        }
    }

    public void setContainers(List<DataSet> containers)
    {
        this.containers = containers;
    }

    @JsonIgnore
    public List<DataSet> getContained()
    {
        if (getFetchOptions().hasContained())
        {
            return contained;
        }
        else
        {
            throw new NotFetchedException("Contained data sets has not been fetched.");
        }
    }

    public void setContained(List<DataSet> contained)
    {
        this.contained = contained;
    }

    @JsonIgnore
    public Set<Tag> getTags()
    {
        if (getFetchOptions().hasTags())
        {
            return tags;
        }
        else
        {
            throw new NotFetchedException("Tags has not been fetched.");
        }
    }

    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    @JsonIgnore
    public DataSetType getType()
    {
        if (getFetchOptions().hasType())
        {
            return type;
        }
        else
        {
            throw new NotFetchedException("Sample type has not been fetched.");
        }
    }

    public void setType(DataSetType type)
    {
        this.type = type;
    }

    @JsonIgnore
    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @JsonIgnore
    public Person getModifier()
    {
        if (getFetchOptions().hasModifier())
        {
            return modifier;
        }
        else
        {
            throw new NotFetchedException("Modifier has not been fetched.");
        }
    }

    public void setModifier(Person modifier)
    {
        this.modifier = modifier;
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

    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    @JsonIgnore
    public Experiment getExperiment()
    {
        if (getFetchOptions().hasExperiment())
        {
            return experiment;
        }
        else
        {
            throw new NotFetchedException("Experiment has not been fetched.");
        }
    }

    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    @JsonIgnore
    public Map<String, String> getProperties()
    {
        if (getFetchOptions().hasProperties())
        {
            return properties;
        }
        else
        {
            throw new NotFetchedException("Properties has not been fetched.");
        }
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

}
