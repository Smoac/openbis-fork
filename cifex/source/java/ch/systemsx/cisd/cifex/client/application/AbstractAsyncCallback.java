/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.rpc.StatusCodeException;

import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;

/**
 * An abstract <code>AsyncCallback</code> implementations which only implements
 * {@link AsyncCallback#onFailure(Throwable)} method by showing an appropriate error message.
 * <p>
 * This abstract class should be used instead of implementing your own <code>AsyncCallback</code>
 * implementation as it already does the right job.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T>
{
    private final ViewContext context;

    private final Dialog associatedDialogOrNull;

    public AbstractAsyncCallback(final ViewContext context)
    {
        this(context, null);
    }

    public AbstractAsyncCallback(final ViewContext context, Dialog dialog)
    {
        this.context = context;
        this.associatedDialogOrNull = dialog;
    }

    /** Gives access to internal <code>ViewContext</code>. */
    protected final ViewContext getViewContext()
    {
        return context;
    }

    //
    // AsyncCallback
    //

    public void onFailure(final Throwable caught)
    {
        final String msg;
        if (caught instanceof InvocationException)
        {
            if (StringUtils.isBlank(caught.getMessage()))
            {
                if (caught instanceof StatusCodeException
                        || (((StatusCodeException) caught).getStatusCode() == 0))
                {
                    msg = msg(EXCEPTION_STATUS_CODE0);
                } else
                    msg = msg(EXCEPTION_INVOCATION_MSG);
            } else
            {
                msg = caught.getMessage();
            }
        } else
        {
            final String message = caught.getMessage();
            if (StringUtils.isBlank(message))
            {
                msg = msg(UNKNOWN_FAILURE_MSG, caught.getClass().getName());
            } else
            {
                msg = message;
            }
        }
        MessageBox.alert(msg(MESSAGE_BOX_ERROR_TITLE), msg,
        // go to login page after message box is closed if the problem is caused by invalid session
                new Listener<MessageBoxEvent>()
                    {
                        public void handleEvent(MessageBoxEvent be)
                        {
                            if (caught instanceof InvalidSessionException)
                            {
                                if (associatedDialogOrNull != null)
                                {
                                    associatedDialogOrNull.hide();
                                }
                                context.getPageController().showLoginPage();
                            }
                        }
                    });
    }
}
