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
import ch.systemsx.cisd.cifex.client.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.client.dto.Message;

/**
 * An <code>AbstractAsyncCallback</code> extension for file upload.
 * 
 * @author Christian Ribeaud
 */
final class FileUploadFeedbackCallback extends AbstractAsyncCallback
{

    FileUploadFeedbackCallback(final ViewContext context)
    {
        super(context);
    }

    //
    // AbstractAsyncCallback
    //

    public final void onFailure(final Throwable caught)
    {
        super.onFailure(caught);
        getViewContext().getPageController().createMainPage();
    }

    public final void onSuccess(final Object res)
    {
        final IMessageResources messageResources = getViewContext().getMessageResources();
        MessageBox.progress(messageResources.getFileUploadFeedbackTitle(), messageResources
                .getFileUploadFeedbackMessage());
        getViewContext().getCifexService().getFileUploadFeedback(
                new InternalFileUploadFeedbackCallback(getViewContext()));
    }

    //
    // Helper classes
    //

    private final static class InternalFileUploadFeedbackCallback extends AbstractAsyncCallback
    {

        private InternalFileUploadFeedbackCallback(final ViewContext context)
        {
            super(context);
        }

        private final void finishDownload()
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
                buffer.append(messageResources.getFileUploadFeedbackTimeLabel(DateTimeUtils.formatDuration(timeLeft)));
            }
            return buffer.toString();
        }

        //
        // AbstractAsyncCallback
        //

        public final void onFailure(final Throwable caught)
        {
            super.onFailure(caught);
            finishDownload();
        }

        public final void onSuccess(final Object result)
        {
            final FileUploadFeedback feedback = (FileUploadFeedback) result;
            final Message message = feedback.getMessage();
            final IMessageResources messageResources = getViewContext().getMessageResources();
            if (message != null)
            {
                WidgetUtils.showMessage(message, messageResources);
                finishDownload();
            }
            if (feedback.isTerminated() == false)
            {
                MessageBox.updateProgress(feedback.getPercentage(), createUpdateMessage(feedback));
                getViewContext().getCifexService().getFileUploadFeedback(
                        new InternalFileUploadFeedbackCallback(getViewContext()));
            } else
            {
                MessageBox.alert(messageResources.getMessageBoxInfoTitle(), messageResources
                        .getFileUploadFeedbackFinished());
                finishDownload();
            }
        }
    }
}