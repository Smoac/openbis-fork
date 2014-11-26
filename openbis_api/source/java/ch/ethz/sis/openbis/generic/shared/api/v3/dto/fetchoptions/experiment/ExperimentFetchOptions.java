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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("ExperimentFetchOptions")
public class ExperimentFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentTypeFetchOptions type;

    @JsonProperty
    private ProjectFetchOptions project;

    @JsonProperty
    private PropertyFetchOptions properties;

    @JsonProperty
    private TagFetchOptions tags;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    public ExperimentTypeFetchOptions fetchType()
    {
        if (this.type == null)
        {
            this.type = new ExperimentTypeFetchOptions();
        }
        return this.type;
    }

    public ExperimentTypeFetchOptions fetchType(ExperimentTypeFetchOptions fetchOptions)
    {
        return this.type = fetchOptions;
    }

    public boolean hasType()
    {
        return this.type != null;
    }

    public ProjectFetchOptions fetchProject()
    {
        if (this.project == null)
        {
            this.project = new ProjectFetchOptions();
        }
        return this.project;
    }

    public ProjectFetchOptions fetchProject(ProjectFetchOptions fetchOptions)
    {
        return this.project = fetchOptions;
    }

    public boolean hasProject()
    {
        return this.project != null;
    }

    public PropertyFetchOptions fetchProperties()
    {
        if (this.properties == null)
        {
            this.properties = new PropertyFetchOptions();
        }
        return this.properties;
    }

    public PropertyFetchOptions fetchProperties(PropertyFetchOptions fetchOptions)
    {
        return this.properties = fetchOptions;
    }

    public boolean hasProperties()
    {
        return this.properties != null;
    }

    public TagFetchOptions fetchTags()
    {
        if (this.tags == null)
        {
            this.tags = new TagFetchOptions();
        }
        return this.tags;
    }

    public TagFetchOptions fetchTags(TagFetchOptions fetchOptions)
    {
        return this.tags = fetchOptions;
    }

    public boolean hasTags()
    {
        return this.tags != null;
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

    public AttachmentFetchOptions fetchAttachments()
    {
        if (this.attachments == null)
        {
            this.attachments = new AttachmentFetchOptions();
        }
        return this.attachments;
    }

    public AttachmentFetchOptions fetchAttachments(AttachmentFetchOptions fetchOptions)
    {
        return this.attachments = fetchOptions;
    }

    public boolean hasAttachments()
    {
        return this.attachments != null;
    }

}
