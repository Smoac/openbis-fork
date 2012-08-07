/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.webapp;

import com.google.gwt.http.client.UrlBuilder;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A web application URL that will be used for displaying the web application in an IFRAME.
 * 
 * @author pkupczyk
 */
public class WebAppUrl
{

    private UrlBuilder builder = new UrlBuilder();

    public WebAppUrl(String openbisProtocol, String openbisHost, String webAppCode, String sessionId)
    {
        if (openbisProtocol == null)
        {
            throw new IllegalArgumentException("OpenBIS protocol cannot be null");
        }
        if (openbisHost == null)
        {
            throw new IllegalArgumentException("OpenBIS host cannot be null");
        }
        if (webAppCode == null)
        {
            throw new IllegalArgumentException("Web application code cannot be null");
        }
        if (sessionId == null)
        {
            throw new IllegalArgumentException("Session id cannot be null");
        }

        builder.setProtocol(openbisProtocol);
        builder.setHost(openbisHost);
        // TODO remove the hardcoded part after development is finished
        builder.setPath("ch.systemsx.cisd.openbis.plugin.screening.OpenBIS/resources/applications/"
                + webAppCode + "/html");
        builder.setParameter(WebAppUrlParameter.SESSION_ID.getName(), sessionId);
    }

    public void setEntityKind(EntityKind entityKind)
    {
        if (entityKind != null)
        {
            builder.setParameter(WebAppUrlParameter.ENTITY_KIND.getName(), entityKind.name());
        }
    }

    public void setEntityType(BasicEntityType entityType)
    {
        if (entityType != null)
        {
            builder.setParameter(WebAppUrlParameter.ENTITY_TYPE.getName(), entityType.getCode());
        }
    }

    public void setEntityIdentifier(String entityIdentifier)
    {
        if (entityIdentifier != null)
        {
            builder.setParameter(WebAppUrlParameter.ENTITY_IDENTIFIER.getName(), entityIdentifier);
        }
    }

    public void setEntityPermId(String entityPermId)
    {
        if (entityPermId != null)
        {
            builder.setParameter(WebAppUrlParameter.ENTITY_PERM_ID.getName(), entityPermId);
        }
    }

    @Override
    public String toString()
    {
        return builder.buildString();
    }
}
