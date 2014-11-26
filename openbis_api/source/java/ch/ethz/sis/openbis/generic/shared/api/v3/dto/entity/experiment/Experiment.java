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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("Experiment")
public class Experiment implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExperimentFetchOptions fetchOptions;

    @JsonProperty
    private ExperimentPermId permId;

    @JsonProperty
    private ExperimentIdentifier identifier;

    @JsonProperty
    private String code;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    @JsonProperty
    private ExperimentType type;

    @JsonProperty
    private Project project;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private Set<Tag> tags;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Person modifier;

    @JsonProperty
    private List<Attachment> attachments;

    @JsonIgnore
    public ExperimentFetchOptions getFetchOptions()
    {
        return this.fetchOptions;
    }

    public void setFetchOptions(ExperimentFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public ExperimentPermId getPermId()
    {
        return this.permId;
    }

    public void setPermId(ExperimentPermId permId)
    {
        this.permId = permId;
    }

    @JsonIgnore
    public ExperimentIdentifier getIdentifier()
    {
        return this.identifier;
    }

    public void setIdentifier(ExperimentIdentifier identifier)
    {
        this.identifier = identifier;
    }

    @JsonIgnore
    public String getCode()
    {
        return this.code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    @JsonIgnore
    public Date getRegistrationDate()
    {
        return this.registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @JsonIgnore
    public Date getModificationDate()
    {
        return this.modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @JsonIgnore
    public ExperimentType getType()
    {
        if (getFetchOptions().hasType())
        {
            return this.type;
        }
        else
        {
            throw new NotFetchedException("Experiment type has not been fetched.");
        }
    }

    public void setType(ExperimentType type)
    {
        this.type = type;
    }

    @JsonIgnore
    public Project getProject()
    {
        if (getFetchOptions().hasProject())
        {
            return this.project;
        }
        else
        {
            throw new NotFetchedException("Project has not been fetched.");
        }
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    @JsonIgnore
    public Map<String, String> getProperties()
    {
        if (getFetchOptions().hasProperties())
        {
            return this.properties;
        }
        else
        {
            throw new NotFetchedException("Properties has not been fetched.");
        }
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    @JsonIgnore
    public Set<Tag> getTags()
    {
        if (getFetchOptions().hasTags())
        {
            return this.tags;
        }
        else
        {
            throw new NotFetchedException("Tags has not been fetched.");
        }
    }

    public void setTags(Set<Tag> tags)
    {
        this.tags = tags;
    }

    @JsonIgnore
    public Person getRegistrator()
    {
        if (getFetchOptions().hasRegistrator())
        {
            return this.registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    @JsonIgnore
    public Person getModifier()
    {
        if (getFetchOptions().hasModifier())
        {
            return this.modifier;
        }
        else
        {
            throw new NotFetchedException("Modifier has not been fetched.");
        }
    }

    public void setModifier(Person modifier)
    {
        this.modifier = modifier;
    }

    @JsonIgnore
    public List<Attachment> getAttachments()
    {
        if (getFetchOptions().hasAttachments())
        {
            return this.attachments;
        }
        else
        {
            throw new NotFetchedException("Attachments has not been fetched.");
        }
    }

    public void setAttachments(List<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    @Override
    public String toString()
    {
        return "Experiment " + this.permId;
    }

}
