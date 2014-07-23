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
        if (type == null)
        {
            type = new ExperimentTypeFetchOptions();
        }
        return type;
    }

    public ExperimentTypeFetchOptions fetchType(ExperimentTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    public boolean hasType()
    {
        return type != null;
    }

    public ProjectFetchOptions fetchProject()
    {
        if (project == null)
        {
            project = new ProjectFetchOptions();
        }
        return project;
    }

    public ProjectFetchOptions fetchProject(ProjectFetchOptions fetchOptions)
    {
        return project = fetchOptions;
    }

    public boolean hasProject()
    {
        return project != null;
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
