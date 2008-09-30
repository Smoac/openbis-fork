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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Contition which checks whether the class of the callback object is as specified.
 *
 * @author Franz-Josef Elmer
 */
public class CallbackClassCondition implements ICallbackCondition<Object>
{
    private final Class<?> callbackClass;

    /**
     * Creates an instance for the specified callback class.
     */
    public CallbackClassCondition(Class<? extends AsyncCallback<?>> callbackClass)
    {
        this.callbackClass = callbackClass;
    }
    
    public boolean valid(AsyncCallback<Object> callback, Object result)
    {
        return callbackClass.equals(callback.getClass());
    }

}
