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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("dto.entity.attachment.Attachment")
public class Attachment implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private AttachmentFetchOptions fetchOptions;

    @JsonProperty
    private String fileName;

    @JsonProperty
    private String title;

    @JsonProperty
    private String description;

    @JsonProperty
    private String permlink;

    @JsonProperty
    private String latestVersionPermlink;

    @JsonProperty
    private Integer version;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Person registrator;

    @JsonProperty
    private Attachment previousVersion;

    @JsonProperty
    private byte[] content;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public AttachmentFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setFetchOptions(AttachmentFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public String getFileName()
    {
        return fileName;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public String getTitle()
    {
        return title;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setTitle(String title)
    {
        this.title = title;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public String getPermlink()
    {
        return permlink;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setPermlink(String permlink)
    {
        this.permlink = permlink;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public String getLatestVersionPermlink()
    {
        return latestVersionPermlink;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setLatestVersionPermlink(String latestVersionPermlink)
    {
        this.latestVersionPermlink = latestVersionPermlink;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public Integer getVersion()
    {
        return version;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setVersion(Integer version)
    {
        this.version = version;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public Person getRegistrator()
    {
        if (getFetchOptions().hasRegistrator())
        {
            return registrator;
        }
        else
        {
            throw new NotFetchedException("Registrator has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setRegistrator(Person registrator)
    {
        this.registrator = registrator;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public Attachment getPreviousVersion()
    {
        if (getFetchOptions().hasPreviousVersion())
        {
            return previousVersion;
        }
        else
        {
            throw new NotFetchedException("Previous version of attachment  has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setPreviousVersion(Attachment previousVersion)
    {
        this.previousVersion = previousVersion;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    @JsonIgnore
    public byte[] getContent()
    {
        if (getFetchOptions().hasContent())
        {
            return content;
        }
        else
        {
            throw new NotFetchedException("Content has not been fetched.");
        }
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
    public void setContent(byte[] content)
    {
        this.content = content;
    }

    @Override
    public String toString()
    {
        return "Attachment " + fileName + ":" + version;
    }

}
