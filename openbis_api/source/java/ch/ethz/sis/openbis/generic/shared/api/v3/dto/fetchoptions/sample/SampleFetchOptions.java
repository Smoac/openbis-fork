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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
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
@JsonObject("SampleFetchOptions")
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
    private SampleFetchOptions parents;

    @JsonProperty
    private SampleFetchOptions children;

    @JsonProperty
    private SampleFetchOptions container;

    @JsonProperty
    private SampleFetchOptions contained;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    public SampleTypeFetchOptions fetchType()
    {
        if (type == null)
        {
            type = new SampleTypeFetchOptions();
        }
        return type;
    }

    public SampleTypeFetchOptions fetchType(SampleTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    public boolean hasType()
    {
        return type != null;
    }

    public SpaceFetchOptions fetchSpace()
    {
        if (space == null)
        {
            space = new SpaceFetchOptions();
        }
        return space;
    }

    public SpaceFetchOptions fetchSpace(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    public boolean hasSpace()
    {
        return space != null;
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

    public SampleFetchOptions fetchParents()
    {
        if (parents == null)
        {
            parents = new SampleFetchOptions();
        }
        return parents;
    }

    public SampleFetchOptions fetchParents(SampleFetchOptions fetchOptions)
    {
        return parents = fetchOptions;
    }

    public boolean hasParents()
    {
        return parents != null;
    }

    public SampleFetchOptions fetchChildren()
    {
        if (children == null)
        {
            children = new SampleFetchOptions();
        }
        return children;
    }

    public SampleFetchOptions fetchChildren(SampleFetchOptions fetchOptions)
    {
        return children = fetchOptions;
    }

    public boolean hasChildren()
    {
        return children != null;
    }

    public SampleFetchOptions fetchContainer()
    {
        if (container == null)
        {
            container = new SampleFetchOptions();
        }
        return container;
    }

    public SampleFetchOptions fetchContainer(SampleFetchOptions fetchOptions)
    {
        return container = fetchOptions;
    }

    public boolean hasContainer()
    {
        return container != null;
    }

    public SampleFetchOptions fetchContained()
    {
        if (contained == null)
        {
            contained = new SampleFetchOptions();
        }
        return contained;
    }

    public SampleFetchOptions fetchContained(SampleFetchOptions fetchOptions)
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

    public AttachmentFetchOptions fetchAttachments()
    {
        if (attachments == null)
        {
            attachments = new AttachmentFetchOptions();
        }
        return attachments;
    }

    public AttachmentFetchOptions fetchAttachments(AttachmentFetchOptions fetchOptions)
    {
        return attachments = fetchOptions;
    }

    public boolean hasAttachments()
    {
        return attachments != null;
    }

}
