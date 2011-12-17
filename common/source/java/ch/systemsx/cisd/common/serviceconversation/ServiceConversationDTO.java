/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.serviceconversation;

import java.io.Serializable;

/**
 * A data transfer object to save to provide the information about a new service conversation.
 *
 * @author Bernd Rinn
 */
public class ServiceConversationDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String serviceConversationId;
    
    private int clientTimeoutInMillis;

    ServiceConversationDTO(String serviceConversationId, int clientTimeout)
    {
        this.serviceConversationId = serviceConversationId;
        this.clientTimeoutInMillis = clientTimeout;
    }

    public int getClientTimeoutInMillis()
    {
        return clientTimeoutInMillis;
    }

    public void setClientTimeoutInMillis(int clientTimeoutInMillis)
    {
        this.clientTimeoutInMillis = clientTimeoutInMillis;
    }

    public String getServiceConversationId()
    {
        return serviceConversationId;
    }
}
