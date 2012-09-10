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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import ch.systemsx.cisd.common.conversation.IProgressListener;

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
    private static ThreadLocal<IProgressListener> progressListener;

    static
    {
        progressListener = new ThreadLocal<IProgressListener>();
    }

    /**
     * Store progress listener in a thread local context
     */
    public static void setProgressListener(IProgressListener listener)
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
    public static IProgressListener getProgressListener()
    {
        return progressListener.get();
    }
}
