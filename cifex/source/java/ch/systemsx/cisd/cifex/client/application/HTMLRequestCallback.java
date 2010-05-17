/*
 * Copyright 2010 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * A {@link RequestCallback} that shows a popup window.
 * 
 * @author Bernd Rinn
 */
public final class HTMLRequestCallback implements RequestCallback
{
    private final ViewContext context;

    private final String panelTitle;

    private final int popupWidth;

    private final int popupHeight;

    public HTMLRequestCallback(ViewContext context, String title, int width, int height)
    {
        this.context = context;
        this.panelTitle = title;
        this.popupWidth = width;
        this.popupHeight = height;
    }

    public HTMLRequestCallback(ViewContext context, String title)
    {
        this(context, title, DefaultLayoutDialog.DEFAULT_WIDTH,
                DefaultLayoutDialog.DEFAULT_HEIGHT);
    }

    public final void onResponseReceived(final Request request, final Response response)
    {
        final DefaultLayoutDialog layoutDialog =
                new DefaultLayoutDialog(context.getMessageResources(), this.panelTitle,
                        this.popupWidth, this.popupHeight, true, true);
        layoutDialog.addText(response.getText());
        layoutDialog.show();
    }

    public void onError(final Request request, final Throwable exception)
    {
        showErrorMessage(exception);
    }

    private void showErrorMessage(final Throwable ex)
    {
        final String msg;
        final String message = ex.getMessage();
        final IMessageResources messageResources = context.getMessageResources();
        if (StringUtils.isBlank(message))
        {
            msg = messageResources.getExceptionWithoutMessage(ex.getClass().getName());
        } else
        {
            msg = message;
        }
        MessageBox.alert(messageResources.getMessageBoxErrorTitle(), msg, null);
    }
}