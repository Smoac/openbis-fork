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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("DataSetFetchOptions")
public class DataSetFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetFetchOptions parents;

    @JsonProperty
    private DataSetFetchOptions children;

    @JsonProperty
    private DataSetFetchOptions containers;

    @JsonProperty
    private DataSetFetchOptions contained;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private DataSetTypeFetchOptions type;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private PropertyFetchOptions properties;

    public DataSetFetchOptions fetchParents()
    {
        if (parents == null)
        {
            parents = new DataSetFetchOptions();
        }
        return parents;
    }

    public DataSetFetchOptions fetchParents(DataSetFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    public boolean hasParents()
    {
        return parents != null;
    }

    public DataSetFetchOptions fetchChildren()
    {
        if (children == null)
        {
            children = new DataSetFetchOptions();
        }
        return children;
    }

    public DataSetFetchOptions fetchChildren(DataSetFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    public boolean hasChildren()
    {
        return children != null;
    }

    public DataSetFetchOptions fetchContainers()
    {
        if (containers == null)
        {
            containers = new DataSetFetchOptions();
        }
        return containers;
    }

    public DataSetFetchOptions fetchContainers(DataSetFetchOptions fetchOptions)
    {
        return containers = fetchOptions;
    }

    public boolean hasContainers()
    {
        return containers != null;
    }

    public DataSetFetchOptions fetchContained()
    {
        if (contained == null)
        {
            contained = new DataSetFetchOptions();
        }
        return contained;
    }

    public DataSetFetchOptions fetchContained(DataSetFetchOptions fetchOptions)
    {
        return contained = fetchOptions;
    }

    public boolean hasContained()
    {
        return contained != null;
    }

    public TagFetchOptions fetchTags()
    {
        if (tags == null)
        {
            tags = new TagFetchOptions();
        }
        return tags;
    }

    public TagFetchOptions fetchTags(TagFetchOptions fetchOptions)
    {
        return tags = fetchOptions;
    }

    public boolean hasTags()
    {
        return tags != null;
    }

    public DataSetTypeFetchOptions fetchType()
    {
        if (type == null)
        {
            type = new DataSetTypeFetchOptions();
        }
        return type;
    }

    public DataSetTypeFetchOptions fetchType(DataSetTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    public boolean hasType()
    {
        return type != null;
    }

    public PersonFetchOptions fetchModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    public PersonFetchOptions fetchModifier(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    public boolean hasModifier()
    {
        return modifier != null;
    }

    public PersonFetchOptions fetchRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    public PersonFetchOptions fetchRegistrator(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    public ExperimentFetchOptions fetchExperiment()
    {
        if (experiment == null)
        {
            experiment = new ExperimentFetchOptions();
        }
        return experiment;
    }

    public ExperimentFetchOptions fetchExperiment(ExperimentFetchOptions fetchOptions)
    {
        return experiment = fetchOptions;
    }

    public boolean hasExperiment()
    {
        return experiment != null;
    }

    public PropertyFetchOptions fetchProperties()
    {
        if (properties == null)
        {
            properties = new PropertyFetchOptions();
        }
        return properties;
    }

    public PropertyFetchOptions fetchProperties(PropertyFetchOptions fetchOptions)
    {
        return properties = fetchOptions;
    }

    public boolean hasProperties()
    {
        return properties != null;
    }

}
