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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.PhysicalDataFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.datastore.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.history.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
 */
@JsonObject("dto.fetchoptions.dataset.DataSetFetchOptions")
public class DataSetFetchOptions extends FetchOptions<DataSet> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetTypeFetchOptions type;

    @JsonProperty
    private DataStoreFetchOptions dataStore;

    @JsonProperty
    private PhysicalDataFetchOptions physicalData;

    @JsonProperty
    private LinkedDataFetchOptions linkedData;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private SampleFetchOptions sample;

    @JsonProperty
    private PropertyFetchOptions properties;

    @JsonProperty
    private MaterialFetchOptions materialProperties;

    @JsonProperty
    private DataSetFetchOptions parents;

    @JsonProperty
    private DataSetFetchOptions children;

    @JsonProperty
    private DataSetFetchOptions containers;

    @JsonProperty
    private DataSetFetchOptions components;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private HistoryEntryFetchOptions history;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private DataSetSortOptions sort;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new DataSetTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetTypeFetchOptions withTypeUsing(DataSetTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasType()
    {
        return type != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataStoreFetchOptions withDataStore()
    {
        if (dataStore == null)
        {
            dataStore = new DataStoreFetchOptions();
        }
        return dataStore;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataStoreFetchOptions withDataStoreUsing(DataStoreFetchOptions fetchOptions)
    {
        return dataStore = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasDataStore()
    {
        return dataStore != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PhysicalDataFetchOptions withPhysicalData()
    {
        if (physicalData == null)
        {
            physicalData = new PhysicalDataFetchOptions();
        }
        return physicalData;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PhysicalDataFetchOptions withPhysicalDataUsing(PhysicalDataFetchOptions fetchOptions)
    {
        return physicalData = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasPhysicalData()
    {
        return physicalData != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public LinkedDataFetchOptions withLinkedData()
    {
        if (linkedData == null)
        {
            linkedData = new LinkedDataFetchOptions();
        }
        return linkedData;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public LinkedDataFetchOptions withLinkedDataUsing(LinkedDataFetchOptions fetchOptions)
    {
        return linkedData = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasLinkedData()
    {
        return linkedData != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public ExperimentFetchOptions withExperiment()
    {
        if (experiment == null)
        {
            experiment = new ExperimentFetchOptions();
        }
        return experiment;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public ExperimentFetchOptions withExperimentUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiment = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasExperiment()
    {
        return experiment != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public SampleFetchOptions withSample()
    {
        if (sample == null)
        {
            sample = new SampleFetchOptions();
        }
        return sample;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public SampleFetchOptions withSampleUsing(SampleFetchOptions fetchOptions)
    {
        return sample = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasSample()
    {
        return sample != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PropertyFetchOptions withProperties()
    {
        if (properties == null)
        {
            properties = new PropertyFetchOptions();
        }
        return properties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PropertyFetchOptions withPropertiesUsing(PropertyFetchOptions fetchOptions)
    {
        return properties = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasProperties()
    {
        return properties != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public MaterialFetchOptions withMaterialProperties()
    {
        if (materialProperties == null)
        {
            materialProperties = new MaterialFetchOptions();
        }
        return materialProperties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public MaterialFetchOptions withMaterialPropertiesUsing(MaterialFetchOptions fetchOptions)
    {
        return materialProperties = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasMaterialProperties()
    {
        return materialProperties != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withParents()
    {
        if (parents == null)
        {
            parents = new DataSetFetchOptions();
        }
        return parents;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withParentsUsing(DataSetFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasParents()
    {
        return parents != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withChildren()
    {
        if (children == null)
        {
            children = new DataSetFetchOptions();
        }
        return children;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withChildrenUsing(DataSetFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasChildren()
    {
        return children != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withContainers()
    {
        if (containers == null)
        {
            containers = new DataSetFetchOptions();
        }
        return containers;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withContainersUsing(DataSetFetchOptions fetchOptions)
    {
        return containers = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasContainers()
    {
        return containers != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withComponents()
    {
        if (components == null)
        {
            components = new DataSetFetchOptions();
        }
        return components;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public DataSetFetchOptions withComponentsUsing(DataSetFetchOptions fetchOptions)
    {
        return components = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasComponents()
    {
        return components != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public TagFetchOptions withTags()
    {
        if (tags == null)
        {
            tags = new TagFetchOptions();
        }
        return tags;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public TagFetchOptions withTagsUsing(TagFetchOptions fetchOptions)
    {
        return tags = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasTags()
    {
        return tags != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public HistoryEntryFetchOptions withHistory()
    {
        if (history == null)
        {
            history = new HistoryEntryFetchOptions();
        }
        return history;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public HistoryEntryFetchOptions withHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return history = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasHistory()
    {
        return history != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PersonFetchOptions withModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PersonFetchOptions withModifierUsing(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasModifier()
    {
        return modifier != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    @Override
    public DataSetSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new DataSetSortOptions();
        }
        return sort;
    }

    @Override
    public DataSetSortOptions getSortBy()
    {
        return sort;
    }
}
