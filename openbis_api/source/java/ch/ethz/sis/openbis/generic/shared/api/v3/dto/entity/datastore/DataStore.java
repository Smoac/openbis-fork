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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.datastore;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.datastore.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * Class automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
 */
@JsonObject("dto.entity.datastore.DataStore")
public class DataStore implements Serializable, ICodeHolder, IModificationDateHolder, IRegistrationDateHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataStoreFetchOptions fetchOptions;

    @JsonProperty
    private String code;

    @JsonProperty
    private String downloadUrl;

    @JsonProperty
    private String remoteUrl;

    @JsonProperty
    private Date registrationDate;

    @JsonProperty
    private Date modificationDate;

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public DataStoreFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setFetchOptions(DataStoreFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    public String getRemoteUrl()
    {
        return remoteUrl;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setRemoteUrl(String remoteUrl)
    {
        this.remoteUrl = remoteUrl;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    @JsonIgnore
    @Override
    public Date getModificationDate()
    {
        return modificationDate;
    }

    // Method automatically generated with {@link ch.ethz.sis.openbis.generic.server.api.v3.helper.generators.DtoGenerator}
    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    @Override
    public String toString()
    {
        return "DataStore code: " + code;
    }

}
