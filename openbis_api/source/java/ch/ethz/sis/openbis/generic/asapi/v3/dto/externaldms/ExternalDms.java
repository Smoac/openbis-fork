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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.externaldms.ExternalDms")
public class ExternalDms implements Serializable, ICodeHolder
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private ExternalDmsFetchOptions fetchOptions;

    @JsonProperty
    private String code;

    @JsonProperty
    private String label;

    @JsonProperty
    private String urlTemplate;

    @JsonProperty
    private Boolean openbis;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ExternalDmsFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(ExternalDmsFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(String code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getLabel()
    {
        return label;
    }

    // Method automatically generated with DtoGenerator
    public void setLabel(String label)
    {
        this.label = label;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    // Method automatically generated with DtoGenerator
    public void setUrlTemplate(String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Boolean isOpenbis()
    {
        return openbis;
    }

    // Method automatically generated with DtoGenerator
    public void setOpenbis(Boolean openbis)
    {
        this.openbis = openbis;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "ExternalDms code: " + code;
    }

}
