/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.webapp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * A map containing persistent settings for an openBIS web app.
 * 
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.webapp.WebAppSettings")
public class WebAppSettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private String webAppId;

    @JsonProperty
    private Map<String, String> settings = new HashMap<>();

    public String getWebAppId()
    {
        return webAppId;
    }

    public void setWebAppId(String webAppId)
    {
        this.webAppId = webAppId;
    }

    public Map<String, String> getSettings()
    {
        return settings;
    }

    public void setSettings(Map<String, String> settings)
    {
        this.settings = settings;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(webAppId);
        builder.append(settings);
        return builder.toString();
    }

}
