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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An exception that signals to the server that an exception happened during service execution on
 * the client.
 * 
 * @author Bernd Rinn
 */
public class ClientExecutionException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final String serviceConversationId;

    private final String description;

    ClientExecutionException(String serviceConversationId, String description)
    {
        super("Client execution exception in service conversation " + serviceConversationId);
        this.serviceConversationId = serviceConversationId;
        this.description = description;
    }

    public String getServiceConversationId()
    {
        return serviceConversationId;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return "ClientExecutionException [serviceConversationId=" + serviceConversationId
                + ", description=" + description + "]";
    }

    @Override
    public void printStackTrace()
    {
        System.err.println(getMessage());
        System.err.println(getDescription());
    }

    @Override
    public void printStackTrace(PrintStream s)
    {
        s.println(getMessage());
        s.println(getDescription());
    }

    @Override
    public void printStackTrace(PrintWriter s)
    {
        s.println(getMessage());
        s.println(getDescription());
    }

}
