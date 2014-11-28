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

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public ExperimentTypeFetchOptions fetchType()
    {
        if (type == null)
        {
            type = new ExperimentTypeFetchOptions();
        }
        return type;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public ExperimentTypeFetchOptions fetchType(ExperimentTypeFetchOptions fetchOptions)
    {
        return type = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public boolean hasType()
    {
        return type != null;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public ProjectFetchOptions fetchProject()
    {
        if (project == null)
        {
            project = new ProjectFetchOptions();
        }
        return project;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public ProjectFetchOptions fetchProject(ProjectFetchOptions fetchOptions)
    {
        return project = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public boolean hasProject()
    {
        return project != null;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public PropertyFetchOptions fetchProperties()
    {
        if (properties == null)
        {
            properties = new PropertyFetchOptions();
        }
        return properties;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public PropertyFetchOptions fetchProperties(PropertyFetchOptions fetchOptions)
    {
        return properties = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public boolean hasProperties()
    {
        return properties != null;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public TagFetchOptions fetchTags()
    {
        if (tags == null)
        {
            tags = new TagFetchOptions();
        }
        return tags;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public TagFetchOptions fetchTags(TagFetchOptions fetchOptions)
    {
        return tags = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public boolean hasTags()
    {
        return tags != null;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public PersonFetchOptions fetchRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public PersonFetchOptions fetchRegistrator(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public PersonFetchOptions fetchModifier()
    {
        if (modifier == null)
        {
            modifier = new PersonFetchOptions();
        }
        return modifier;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public PersonFetchOptions fetchModifier(PersonFetchOptions fetchOptions)
    {
        return modifier = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public boolean hasModifier()
    {
        return modifier != null;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public AttachmentFetchOptions fetchAttachments()
    {
        if (attachments == null)
        {
            attachments = new AttachmentFetchOptions();
        }
        return attachments;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public AttachmentFetchOptions fetchAttachments(AttachmentFetchOptions fetchOptions)
    {
        return attachments = fetchOptions;
    }

    /**
     * Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    public boolean hasAttachments()
    {
        return attachments != null;
    }

}
