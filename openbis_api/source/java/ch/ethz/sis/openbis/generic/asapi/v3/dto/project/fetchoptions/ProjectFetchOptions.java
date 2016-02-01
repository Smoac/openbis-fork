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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.fetchoptions.HistoryEntryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.project.fetchoptions.ProjectFetchOptions")
public class ProjectFetchOptions extends FetchOptions<Project> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentFetchOptions experiments;

    @JsonProperty
    private SampleFetchOptions samples;

    @JsonProperty
    private HistoryEntryFetchOptions history;

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

    @JsonProperty
    private ProjectSortOptions sort;

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperiments()
    {
        if (experiments == null)
        {
            experiments = new ExperimentFetchOptions();
        }
        return experiments;
    }

    // Method automatically generated with DtoGenerator
    public ExperimentFetchOptions withExperimentsUsing(ExperimentFetchOptions fetchOptions)
    {
        return experiments = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasExperiments()
    {
        return experiments != null;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSamples()
    {
        if (samples == null)
        {
            samples = new SampleFetchOptions();
        }
        return samples;
    }

    // Method automatically generated with DtoGenerator
    public SampleFetchOptions withSamplesUsing(SampleFetchOptions fetchOptions)
    {
        return samples = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSamples()
    {
        return samples != null;
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
    public PersonFetchOptions withLeader()
    {
        if (leader == null)
        {
            leader = new PersonFetchOptions();
        }
        return leader;
    }

    // Method automatically generated with DtoGenerator
    public PersonFetchOptions withLeaderUsing(PersonFetchOptions fetchOptions)
    {
        return leader = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasLeader()
    {
        return leader != null;
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
    public ProjectSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new ProjectSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public ProjectSortOptions getSortBy()
    {
        return sort;
    }
}
