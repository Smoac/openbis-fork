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
@JsonObject("Attachment")
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

    @JsonIgnore
    public AttachmentFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(AttachmentFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public String getFileName()
    {
        return this.fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    @JsonIgnore
    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @JsonIgnore
    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @JsonIgnore
    public String getPermlink()
    {
        return this.permlink;
    }

    public void setPermlink(String permlink)
    {
        this.permlink = permlink;
    }

    @JsonIgnore
    public String getLatestVersionPermlink()
    {
        return this.latestVersionPermlink;
    }

    public void setLatestVersionPermlink(String latestVersionPermlink)
    {
        this.latestVersionPermlink = latestVersionPermlink;
    }

    @JsonIgnore
    public Integer getVersion()
    {
        return this.version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
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
    public Attachment getPreviousVersion()
    {
        if (getFetchOptions().hasPreviousVersion())
        {
            return this.previousVersion;
        }
        else
        {
            throw new NotFetchedException("Previous version of attachment  has not been fetched.");
        }
    }

    public void setPreviousVersion(Attachment previousVersion)
    {
        this.previousVersion = previousVersion;
    }

    @JsonIgnore
    public byte[] getContent()
    {
        if (getFetchOptions().hasContent())
        {
            return this.content;
        }
        else
        {
            throw new NotFetchedException("Content has not been fetched.");
        }
    }

    public void setContent(byte[] content)
    {
        this.content = content;
    }

    @Override
    public String toString()
    {
        return "Attachment " + this.fileName + ":" + this.version;
    }

}
