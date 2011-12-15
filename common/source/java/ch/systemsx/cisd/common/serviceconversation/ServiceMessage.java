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

/**
 * A service message which is part of a service conversation.
 *
 * @author Bernd Rinn
 */
public class ServiceMessage
{
    private final String conversationId;
    
    private final int messageIdx;
    
    private final Object payload;
    
    private final String exceptionDescription;
    
    public static ServiceMessage terminate(String conversationId)
    {
        return new ServiceMessage(conversationId, 0, null);
    }

    public ServiceMessage(String conversationId, int messageId, Object payload)
    {
        this.conversationId = conversationId;
        this.messageIdx = messageId;
        this.payload = payload;
        this.exceptionDescription = null;
    }

    ServiceMessage(String conversationId, int messageId, String exceptionDescription)
    {
        this.conversationId = conversationId;
        this.messageIdx = messageId;
        this.payload = null;
        this.exceptionDescription = exceptionDescription;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public int getMessageIdx()
    {
        return messageIdx;
    }

    public Object getPayload()
    {
        return payload;
    }

    public boolean isTerminate()
    {
        return (payload == null) && (exceptionDescription == null);
    }

    public boolean isException()
    {
        return (payload == null) && (exceptionDescription != null);
    }
    
    public String tryGetExceptionDescription()
    {
        return exceptionDescription;
    }

    @Override
    public String toString()
    {
        if (isTerminate())
        {
            return "ServiceMessage [conversationId=" + conversationId + ", TERMINATE]";
        } else if (isException())
        {
            return "ServiceMessage [conversationId=" + conversationId + ", messageIdx=" + messageIdx
                    + ", exceptionDescription=" + exceptionDescription + "]";
        } else
        {
            return "ServiceMessage [conversationId=" + conversationId + ", messageIdx=" + messageIdx
                    + ", payload=" + payload + "]";
        }
    }
}