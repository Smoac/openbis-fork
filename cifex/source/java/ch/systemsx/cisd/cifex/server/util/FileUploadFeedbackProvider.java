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

package ch.systemsx.cisd.cifex.server.util;

import java.io.Serializable;

import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;

/**
 * A class that provides feedback for {@link FileUploadFeedback} to a client.
 * 
 * @author Bernd Rinn
 */
public final class FileUploadFeedbackProvider implements Serializable
{
    private static final long serialVersionUID = 1L;

    private FileUploadFeedback feedback;

    private Message message;

    private boolean fileUploadIsProgressing;

    /**
     * Sets the <var>feedback</var>, possibly overwriting older feedback. Any messages will be
     * cleared. This needs to be called when a new upload starts to let it know that now an upload
     * is proceeding.
     */
    public synchronized void set(final FileUploadFeedback feedback)
    {
        assert feedback != null;
        assert feedback.getMessage() == null;
        assert feedback.isFinished() == false;

        this.fileUploadIsProgressing = true;
        this.message = null;
        this.feedback = feedback;
        notify();
    }

    /**
     * Sets the upload process to finished.
     */
    public synchronized void setFileUploadFinished()
    {
        this.fileUploadIsProgressing = false;
        this.feedback = null;
        notifyAll();
    }

    /**
     * Sets a message instead of progress. Also sets the upload process to finished.
     */
    public synchronized void setMessage(final Message message)
    {
        assert message != null;

        this.message = message;
        setFileUploadFinished();
    }

    /**
     * Returns the current feedback.
     * <p>
     * If an upload process is ongoing but no feedback is available, wait for it to become
     * available.
     * <p>
     * If no upload process is ongoing, a fake feedback object indicating this circumstance will be
     * returned. If a message has been set by {@link #setMessage(Message)}, the feedback will
     * contain it.
     */
    public synchronized FileUploadFeedback take() throws InterruptedException
    {
        FileUploadFeedback feedbackOrNull = tryGet();
        if (feedbackOrNull == null)
        {
            wait();
            feedbackOrNull = tryGet();
        }
        return feedbackOrNull;
    }

    private FileUploadFeedback tryGet()
    {
        if (fileUploadIsProgressing == false) // Currently no upload in progress? Provide fake
                                                // progress object.
        {
            final FileUploadFeedback fakeFeedback = new FileUploadFeedback();
            fakeFeedback.setFinished(true);
            if (message != null)
            {
                fakeFeedback.setMessage(message);
            }
            return fakeFeedback;
        }
        final FileUploadFeedback result = feedback;
        feedback = null;
        return result;
    }

}
