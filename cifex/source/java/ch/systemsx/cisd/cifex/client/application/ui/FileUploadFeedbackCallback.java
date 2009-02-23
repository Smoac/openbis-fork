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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.gwtext.client.widgets.MessageBox;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.client.application.utils.FileUtils;
import ch.systemsx.cisd.cifex.client.application.utils.WidgetUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;

/**
 * An <code>AbstractAsyncCallback</code> extension for file upload.
 * 
 * @author Christian Ribeaud
 */
final class FileUploadFeedbackCallback extends AbstractAsyncCallback
{

    /**
     * Whether the progress bar has already been initialized (using
     * {@link MessageBox#progress(String, String)}).
     */
    private final boolean initialized;

    FileUploadFeedbackCallback(final ViewContext context)
    {
        this(context, false);
    }

    private FileUploadFeedbackCallback(final ViewContext context, final boolean initialized)
    {
        super(context);
        this.initialized = initialized;
    }

    private final void refreshMainPage()
    {
        getViewContext().getPageController().createMainPage();
    }

    private final String createUpdateMessage(final FileUploadFeedback feedback)
    {
        final StringBuffer buffer = new StringBuffer();
        final IMessageResources messageResources = getViewContext().getMessageResources();
        buffer.append(messageResources.getFileUploadFeedbackFileLabel(feedback.getFileName()));
        buffer.append(DOMUtils.BR);
        final String byteRead = FileUtils.byteCountToDisplaySize(feedback.getBytesRead());
        final long length = feedback.getContentLength();
        final String contentLength =
                length == Long.MAX_VALUE ? messageResources.getUnknownLabel() : FileUtils
                        .byteCountToDisplaySize(length);
        buffer.append(messageResources.getFileUploadFeedbackBytesLabel(byteRead, contentLength));
        buffer.append(DOMUtils.BR);
        final long timeLeft = feedback.getTimeLeft();
        if (timeLeft < Long.MAX_VALUE)
        {
            buffer.append(messageResources.getFileUploadFeedbackTimeLabel(DateTimeUtils
                    .formatDuration(timeLeft)));
        }
        return buffer.toString();
    }

    //
    // AbstractAsyncCallback
    //

    public final void onFailure(final Throwable caught)
    {
        super.onFailure(caught);
        refreshMainPage();
    }

    public final void onSuccess(final Object result)
    {
        final FileUploadFeedback feedback = (FileUploadFeedback) result;
        final IMessageResources messageResources = getViewContext().getMessageResources();
        final Message message = feedback.getMessage();
        if (message != null)
        {
            WidgetUtils.showMessage(message, messageResources);
            refreshMainPage();
            return;
        }
        if (feedback.isFinished())
        {
            MessageBox.hide();
            refreshMainPage();
            return;
        }
        if (initialized == false)
        {
            MessageBox.progress(messageResources.getFileUploadFeedbackTitle(), messageResources
                    .getFileUploadFeedbackMessage());
        } else
        {
            MessageBox.updateProgress(feedback.getPercentage(), createUpdateMessage(feedback));
        }
        getViewContext().getCifexService().getFileUploadFeedback(
                new FileUploadFeedbackCallback(getViewContext(), true));
    }
}