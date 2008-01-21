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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.gwtext.client.widgets.MessageBox;

import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * An abstract <code>AsyncCallback</code> implementations which only implements
 * {@link AsyncCallback#onFailure(Throwable)} method by showing an appropriate error message.
 * <p>
 * This abstract class should be used instead of implementing your own <code>AsyncCallback</code> implementation as it
 * already does the right job.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractAsyncCallback implements AsyncCallback
{

    private final IMessageResources messageResources;

    private final IPageController pageController;

    public AbstractAsyncCallback(final IPageController pageController, final IMessageResources messageResources)
    {
        assert messageResources != null : "Given message resources can not be null.";
        this.pageController = pageController;
        this.messageResources = messageResources;
    }

    //
    // AsyncCallback
    //

    public void onFailure(final Throwable caught)
    {
        final String msg;
        if (caught instanceof InvocationException)
        {
            msg = messageResources.getInvocationExceptionMessage();
        } else
        {
            String message = caught.getMessage();
            if (StringUtils.isBlank(message))
            {
                msg = messageResources.getExceptionWithoutMessage(GWT.getTypeName(caught));
            } else
            {
                msg = message;
            }
        }
        MessageBox.alert(messageResources.getMessageBoxErrorTitle(), msg);
        if (caught instanceof InvalidSessionException && pageController != null)
        {
            pageController.createLoginPage();
        }
    }
}
