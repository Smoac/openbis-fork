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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.sample.fetchoptions.SampleFetchOptions")
public class SampleFetchOptions extends FetchOptions<Sample> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SampleTypeFetchOptions type;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private ExperimentFetchOptions experiment;

    @JsonProperty
    private PropertyFetchOptions properties;

    @JsonProperty
    private MaterialFetchOptions materialProperties;

    @JsonProperty
    private SampleFetchOptions parents;

    @JsonProperty
    private SampleFetchOptions children;

    @JsonProperty
    private SampleFetchOptions container;

    @JsonProperty
    private SampleFetchOptions components;

    @JsonProperty
    private DataSetFetchOptions dataSets;

    @JsonProperty
    private HistoryEntryFetchOptions history;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    @JsonProperty
    private SampleSortOptions sort;

    // Method automatically generated with DtoGenerator
    public SampleTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new SampleTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with DtoGenerator
    public SampleTypeFetchOptions withTypeUsing(SampleTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasType()
    {
        return type != null;
    }

    // Method automatically generated with DtoGenerator
    public SpaceFetchOptions withSpace()
    {
        if (space == null)
        {
            space = new SpaceFetchOptions();
        }
        return space;
    }

    // Method automatically generated with DtoGenerator
    public SpaceFetchOptions withSpaceUsing(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSpace()
    {
        return space != null;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperiment()
    {
        if (experiment == null)
        {
            experiment = new ExperimentFetchOptions();
        }
        return experiment;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperimentUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiment = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasExperiment()
    {
        return experiment != null;
    }

    // Method automatically generated with DtoGenerator
    public PropertyFetchOptions withProperties()
    {
        if (properties == null)
        {
            properties = new PropertyFetchOptions();
        }
        return properties;
    }

    // Method automatically generated with DtoGenerator
    public PropertyFetchOptions withPropertiesUsing(PropertyFetchOptions fetchOptions)
    {
        return properties = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasProperties()
    {
        return properties != null;
    }

    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterialProperties()
    {
        if (materialProperties == null)
        {
            materialProperties = new MaterialFetchOptions();
        }
        return materialProperties;
    }

    // Method automatically generated with DtoGenerator
    public MaterialFetchOptions withMaterialPropertiesUsing(MaterialFetchOptions fetchOptions)
    {
        return materialProperties = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasMaterialProperties()
    {
        return materialProperties != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withParents()
    {
        if (parents == null)
        {
            parents = new SampleFetchOptions();
        }
        return parents;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withParentsUsing(SampleFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasParents()
    {
        return parents != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withChildren()
    {
        if (children == null)
        {
            children = new SampleFetchOptions();
        }
        return children;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withChildrenUsing(SampleFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasChildren()
    {
        return children != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withContainer()
    {
        if (container == null)
        {
            container = new SampleFetchOptions();
        }
        return container;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withContainerUsing(SampleFetchOptions fetchOptions)
    {
        return container = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasContainer()
    {
        return container != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withComponents()
    {
        if (components == null)
        {
            components = new SampleFetchOptions();
        }
        return components;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withComponentsUsing(SampleFetchOptions fetchOptions)
    {
        return components = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasComponents()
    {
        return components != null;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSets()
    {
        if (dataSets == null)
        {
            dataSets = new DataSetFetchOptions();
        }
        return dataSets;
    }

    // Method automatically generated with DtoGenerator
    public DataSetFetchOptions withDataSetsUsing(DataSetFetchOptions fetchOptions)
    {
        return dataSets = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasDataSets()
    {
        return dataSets != null;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withHistory()
    {
        if (history == null)
        {
            history = new HistoryEntryFetchOptions();
        }
        return history;
    }

    // Method automatically generated with DtoGenerator
    public HistoryEntryFetchOptions withHistoryUsing(HistoryEntryFetchOptions fetchOptions)
    {
        return history = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasHistory()
    {
        return history != null;
    }

    // Method automatically generated with DtoGenerator
    public TagFetchOptions withTags()
    {
        if (tags == null)
        {
            tags = new TagFetchOptions();
        }
        return tags;
    }

    // Method automatically generated with DtoGenerator
    public TagFetchOptions withTagsUsing(TagFetchOptions fetchOptions)
    {
        return tags = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasTags()
    {
        return tags != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withModifierUsing(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasModifier()
    {
        return modifier != null;
    }

    // Method automatically generated with DtoGenerator
    public AttachmentFetchOptions withAttachments()
    {
        if (attachments == null)
        {
            attachments = new AttachmentFetchOptions();
        }
        return attachments;
    }

    // Method automatically generated with DtoGenerator
    public AttachmentFetchOptions withAttachmentsUsing(AttachmentFetchOptions fetchOptions)
    {
        return attachments = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasAttachments()
    {
        return attachments != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SampleSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new SampleSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public SampleSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    public FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("Sample", this);
        f.addFetchOption("Type", type);
        f.addFetchOption("Space", space);
        f.addFetchOption("Experiment", experiment);
        f.addFetchOption("Properties", properties);
        f.addFetchOption("MaterialProperties", materialProperties);
        f.addFetchOption("Parents", parents);
        f.addFetchOption("Children", children);
        f.addFetchOption("Container", container);
        f.addFetchOption("Components", components);
        f.addFetchOption("DataSets", dataSets);
        f.addFetchOption("History", history);
        f.addFetchOption("Tags", tags);
        f.addFetchOption("Registrator", registrator);
        f.addFetchOption("Modifier", modifier);
        f.addFetchOption("Attachments", attachments);
        return f;
    }

}
