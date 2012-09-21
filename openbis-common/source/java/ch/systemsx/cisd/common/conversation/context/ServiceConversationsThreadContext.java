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

package ch.systemsx.cisd.common.conversation.context;

import ch.systemsx.cisd.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.common.conversation.progress.ServiceConversationNullProgressListener;

/**
 * The class contains the logic for communication beetween Hibernate interceptors, which should send
 * service conversation updates, and parts of applications that use service conversations, but don't
 * have acces to the Hibernate objects.
 * <p>
 * It lets the owner of service conversation store the information about it in the thread local
 * variable, from which the interceptor can later read it.
 * 
 * @author Jakub Straszewski
 */
public class ServiceConversationsThreadContext
{
    private static ThreadLocal<IServiceConversationProgressListener> progressListener;

    static
    {
        progressListener = new ThreadLocal<IServiceConversationProgressListener>();
    }

    /**
     * Store progress listener in a thread local context
     */
    public static void setProgressListener(IServiceConversationProgressListener listener)
    {
        progressListener.set(listener);
    }

    /**
     * Remove information about progress listener from the thread local context.
     */
    public static void unsetProgressListener()
    {
        progressListener.remove();
    }

    /**
     * Read the progress listener from the thread local context
     */
    public static IServiceConversationProgressListener getProgressListener()
    {
        IServiceConversationProgressListener listener = progressListener.get();

        if (listener == null)
        {
            return new ServiceConversationNullProgressListener();
        } else
        {
            return listener;
        }
    }
}
