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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
@JsonObject("AttachmentFetchOptions")
public class AttachmentFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private AttachmentFetchOptions previousVersion;

    @JsonProperty
    private EmptyFetchOptions content;

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

    public AttachmentFetchOptions fetchPreviousVersion()
    {
        if (previousVersion == null)
        {
            previousVersion = new AttachmentFetchOptions();
        }
        return previousVersion;
    }

    public AttachmentFetchOptions fetchPreviousVersion(AttachmentFetchOptions fetchOptions)
    {
        return previousVersion = fetchOptions;
    }

    public boolean hasPreviousVersion()
    {
        return previousVersion != null;
    }

    public EmptyFetchOptions fetchContent()
    {
        if (content == null)
        {
            content = new EmptyFetchOptions();
        }
        return content;
    }

    public EmptyFetchOptions fetchContent(EmptyFetchOptions fetchOptions)
    {
        return content = fetchOptions;
    }

    public boolean hasContent()
    {
        return content != null;
    }

}
