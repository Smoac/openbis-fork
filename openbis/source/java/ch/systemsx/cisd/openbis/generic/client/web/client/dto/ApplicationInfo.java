/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * A bean that contains information about the application.
 * 
 * @author Franz-Josef Elmer
 */
public final class ApplicationInfo implements IsSerializable
{
    private String version;

    private String cifexURL;

    private String cifexRecipient;

    private boolean archivingConfigured;

    private WebClientConfiguration webClientConfiguration;

    public String getCifexRecipient()
    {
        return cifexRecipient;
    }

    public void setCifexRecipient(String cifexRecipient)
    {
        this.cifexRecipient = cifexRecipient;
    }

    public final String getVersion()
    {
        return version;
    }

    public final void setVersion(final String version)
    {
        this.version = version;
    }

    public void setCIFEXURL(String cifexURL)
    {
        this.cifexURL = cifexURL;
    }

    public final String getCIFEXURL()
    {
        return cifexURL;
    }

    public boolean isArchivingConfigured()
    {
        return archivingConfigured;
    }

    public void setArchivingConfigured(boolean archivingConfigured)
    {
        this.archivingConfigured = archivingConfigured;
    }

    public void setWebClientConfiguration(WebClientConfiguration configuration)
    {
        this.webClientConfiguration = configuration;
    }

    public WebClientConfiguration getWebClientConfiguration()
    {
        return webClientConfiguration;
    }

}
