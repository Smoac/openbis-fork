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

package ch.systemsx.cisd.datamover.console.client.application;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;

import ch.systemsx.cisd.datamover.console.client.InvalidSessionException;
import ch.systemsx.cisd.datamover.console.client.application.utils.StringUtils;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T>
{
    protected final ViewContext viewContext;
    
    public AbstractAsyncCallback(ViewContext viewContext)
    {
        this.viewContext = viewContext;
    }
    
    public void onFailure(Throwable caught)
    {
        IMessageResources messageResources = viewContext.getMessageResources();
        final String msg;
        if (caught instanceof InvocationException)
        {
            if (StringUtils.isBlank(caught.getMessage()))
            {
                msg = messageResources.getInvocationExceptionMessage();
            } else
            {
                msg = caught.getMessage();
            }
        } else
        {
            final String message = caught.getMessage();
            if (StringUtils.isBlank(message))
            {
                msg = messageResources.getExceptionWithoutMessage(caught.getClass().getName());
            } else
            {
                msg = message;
            }
        }
        Window.alert(msg);
        if (caught instanceof InvalidSessionException)
        {
            viewContext.getPageController().reload();
        }
    }

}
