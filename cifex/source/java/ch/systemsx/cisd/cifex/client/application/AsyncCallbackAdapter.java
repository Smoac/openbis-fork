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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An empty <code>AsyncCallback</code> implementation.
 * <p>
 * This implementation does nothing and should be used when you are not interested at all in the callback methods coming
 * from the server. Instead of putting <code>null</code> as <code>AsyncCallback</code> method parameter you should
 * use {@link #EMPTY_ASYNC_CALLBACK} as <i>GWT</i> does not seem to like it very much (an exception is thrown).
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class AsyncCallbackAdapter implements AsyncCallback
{

    /** A unique instance of this class that does nothing. */
    public final static AsyncCallbackAdapter EMPTY_ASYNC_CALLBACK = new AsyncCallbackAdapter();

    public AsyncCallbackAdapter()
    {
    }

    //
    // AsyncCallback
    //

    public void onFailure(Throwable caught)
    {
    }

    public void onSuccess(Object result)
    {
    }
}