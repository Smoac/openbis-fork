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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
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
final class FileUploadFeedbackCallback extends AbstractAsyncCallback<FileUploadFeedback>
{

    /**
     * Whether the progress bar has already been initialized (using
     * {@link MessageBox#progress(String, String)}).
     */
    private MessageBox messageBox;

    /**
     * The "submit" button to re-enable.
     */
    private Button submitButton;

    FileUploadFeedbackCallback(final ViewContext context, Button submitButton)
    {
        this(context, null, submitButton);
    }

    private FileUploadFeedbackCallback(final ViewContext context, final MessageBox initialized,
            Button submitButton)
    {
        super(context);
        this.messageBox = initialized;
        this.submitButton = submitButton;
    }

    private final void refreshMainPage()
    {
        getViewContext().getPageController().refreshMainPage();
    }

    private final String createUpdateMessage(final FileUploadFeedback feedback)
    {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(msg(UPLOAD_FILE_FEEDBACK_FILE_LABEL, feedback.getFileName()));
        buffer.append(DOMUtils.BR);
        final String byteRead = FileUtils.byteCountToDisplaySize(feedback.getBytesRead());
        final long length = feedback.getContentLength();
        final String contentLength =
                (length == Long.MAX_VALUE) ? msg(UNKNOWN_LABEL) : FileUtils
                        .byteCountToDisplaySize(length);
        buffer.append(msg(UPLOAD_FILE_FEEDBACK_PROGRESS_LABEL, byteRead, contentLength));
        buffer.append(DOMUtils.BR);
        final long timeLeft = feedback.getTimeLeft();
        if (timeLeft < Long.MAX_VALUE)
        {
            buffer.append(msg(UPLOAD_FILE_FEEDBACK_TIME_REMAINING_LABEL, DateTimeUtils
                    .formatDuration(timeLeft)));
        }
        return buffer.toString();
    }

    @Override
    public final void onFailure(final Throwable caught)
    {
        if (messageBox != null)
        {
            messageBox.close();
        }
        super.onFailure(caught);
        submitButton.enable();
    }

    public final void onSuccess(final FileUploadFeedback result)
    {
        final FileUploadFeedback feedback = result;
        final Message message = feedback.getMessage();
        if (message != null)
        {
            if (messageBox != null)
            {
                messageBox.close();
            }
            WidgetUtils.showMessage(message, new Listener<MessageBoxEvent>()
                {
                    public void handleEvent(MessageBoxEvent be)
                    {
                        submitButton.enable();
                    }
                });
            return;
        }
        if (feedback.isFinished())
        {
            messageBox.close();
            refreshMainPage();
            submitButton.enable();
            return;
        }
        if (messageBox == null)
        {
            messageBox =
                    MessageBox.progress(msg(UPLOAD_FILE_FEEDBACK_MSGBOX_TITLE),
                            msg(UPLOAD_FILE_FEEDBACK_MSG), null);
        } else
        {
            // Convert a percentage to a double between 0 and 1
            messageBox.updateProgress(feedback.getPercentage() * 0.01, "");
            messageBox.updateText(createUpdateMessage(feedback));
        }
        getViewContext().getCifexService().getFileUploadFeedback(
                new FileUploadFeedbackCallback(getViewContext(), messageBox, submitButton));
    }
}