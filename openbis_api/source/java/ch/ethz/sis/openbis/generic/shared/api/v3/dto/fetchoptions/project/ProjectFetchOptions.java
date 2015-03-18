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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("dto.fetchoptions.project.ProjectFetchOptions")
public class ProjectFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentFetchOptions experiments;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions modifier;

    @JsonProperty
    private PersonFetchOptions leader;

    @JsonProperty
    private AttachmentFetchOptions attachments;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExperimentFetchOptions withExperiments()
    {
        if (experiments == null)
        {
            experiments = new ExperimentFetchOptions();
        }
        return experiments;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public ExperimentFetchOptions withExperimentsUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiments = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasExperiments()
    {
        return experiments != null;
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
    public PersonFetchOptions withLeader()
    {
        if (leader == null)
        {
            leader = new PersonFetchOptions();
        }
        return leader;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public PersonFetchOptions withLeaderUsing(PersonFetchOptions fetchOptions)
    {
        return leader = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public boolean hasLeader()
    {
        return leader != null;
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
