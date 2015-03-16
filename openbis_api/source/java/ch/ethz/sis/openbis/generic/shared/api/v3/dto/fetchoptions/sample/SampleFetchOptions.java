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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("dto.fetchoptions.sample.SampleFetchOptions")
public class SampleFetchOptions implements Serializable
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
    private SampleFetchOptions contained;

    @JsonProperty
    private DataSetFetchOptions dataSets;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleTypeFetchOptions withType()
    {
        if (type == null)
        {
            type = new SampleTypeFetchOptions();
        }
        return type;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleTypeFetchOptions withTypeUsing(SampleTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasType()
    {
        return type != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SpaceFetchOptions withSpace()
    {
        if (space == null)
        {
            space = new SpaceFetchOptions();
        }
        return space;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SpaceFetchOptions withSpaceUsing(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasSpace()
    {
        return space != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExperimentFetchOptions withExperiment()
    {
        if (experiment == null)
        {
            experiment = new ExperimentFetchOptions();
        }
        return experiment;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExperimentFetchOptions withExperimentUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiment = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasExperiment()
    {
        return experiment != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PropertyFetchOptions withProperties()
    {
        if (properties == null)
        {
            properties = new PropertyFetchOptions();
        }
        return properties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PropertyFetchOptions withPropertiesUsing(PropertyFetchOptions fetchOptions)
    {
        return properties = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasProperties()
    {
        return properties != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public MaterialFetchOptions withMaterialProperties()
    {
        if (materialProperties == null)
        {
            materialProperties = new MaterialFetchOptions();
        }
        return materialProperties;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public MaterialFetchOptions withMaterialPropertiesUsing(MaterialFetchOptions fetchOptions)
    {
        return materialProperties = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasMaterialProperties()
    {
        return materialProperties != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withParents()
    {
        if (parents == null)
        {
            parents = new SampleFetchOptions();
        }
        return parents;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withParentsUsing(SampleFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasParents()
    {
        return parents != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withChildren()
    {
        if (children == null)
        {
            children = new SampleFetchOptions();
        }
        return children;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withChildrenUsing(SampleFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasChildren()
    {
        return children != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withContainer()
    {
        if (container == null)
        {
            container = new SampleFetchOptions();
        }
        return container;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withContainerUsing(SampleFetchOptions fetchOptions)
    {
        return container = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasContainer()
    {
        return container != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withContained()
    {
        if (contained == null)
        {
            contained = new SampleFetchOptions();
        }
        return contained;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public SampleFetchOptions withContainedUsing(SampleFetchOptions fetchOptions)
    {
        return contained = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasContained()
    {
        return contained != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withDataSets()
    {
        if (dataSets == null)
        {
            dataSets = new DataSetFetchOptions();
        }
        return dataSets;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public DataSetFetchOptions withDataSetsUsing(DataSetFetchOptions fetchOptions)
    {
        return dataSets = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasDataSets()
    {
        return dataSets != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public TagFetchOptions withTags()
    {
        if (tags == null)
        {
            tags = new TagFetchOptions();
        }
        return tags;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public TagFetchOptions withTagsUsing(TagFetchOptions fetchOptions)
    {
        return tags = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasTags()
    {
        return tags != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withModifierUsing(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasModifier()
    {
        return modifier != null;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public AttachmentFetchOptions withAttachments()
    {
        if (attachments == null)
        {
            attachments = new AttachmentFetchOptions();
        }
        return attachments;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public AttachmentFetchOptions withAttachmentsUsing(AttachmentFetchOptions fetchOptions)
    {
        return attachments = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasAttachments()
    {
        return attachments != null;
    }

}
