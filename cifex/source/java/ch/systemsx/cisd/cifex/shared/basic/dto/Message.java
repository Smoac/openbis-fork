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

package ch.systemsx.cisd.cifex.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Message has a type and a message text.
 * 
 * @author Franz-Josef Elmer
 */
public class Message implements IsSerializable, Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Type { INFO, WARNING, ERROR }
    
    private Type type;

    private String messageText;

    public Message()
    {
        // For serialization
    }

    public Message(final Type type, final String messageText)
    {
        this.type = type;
        this.messageText = messageText;
    }

    public final Type getType()
    {
        return type;
    }

    public final void setType(final Type type)
    {
        this.type = type;
    }

    public final String getMessageText()
    {
        return messageText;
    }

    public final void setMessageText(final String messageText)
    {
        this.messageText = messageText;
    }

}
