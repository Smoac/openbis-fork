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

package ch.systemsx.cisd.cifex.server;

import java.util.concurrent.BlockingQueue;

import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.ProgressListener;

import ch.systemsx.cisd.cifex.client.dto.FileUploadFeedback;

/**
 * A <code>ProgressListener</code> implementation which sets a <code>FileUploadFeedback</code> as session attribute.
 * 
 * @author Christian Ribeaud
 */
final class FileUploadProgressListener implements ProgressListener
{
    private final HttpSession httpSession;

    private final String[] pathnamesToUpload;

    /**
     * Registers the time at which this object has been instantiated.
     * <p>
     * We assume that once this object has been instantiated, the first call to {@link #update(long, long, int)} will
     * happen really soon.
     * </p>
     */
    private final long start;

    FileUploadProgressListener(final HttpSession httpSession, final String[] pathnamesToUpload)
    {
        this.httpSession = httpSession;
        this.pathnamesToUpload = pathnamesToUpload;
        start = System.currentTimeMillis();
    }

    private final FileUploadFeedback createFileUploadFeedback(final long bytesRead, final long contentLength,
            final int items)
    {
        final FileUploadFeedback feedback = new FileUploadFeedback();
        feedback.setBytesRead(bytesRead);
        feedback.setContentLength(contentLength);
        if (items > 0)
        {
            feedback.setFileName(pathnamesToUpload[items - 1]);
        }
        feedback.setTimeLeft(createTimeLeft(bytesRead, contentLength));
        return feedback;
    }

    private final long createTimeLeft(final long bytesRead, final long contentLength)
    {
        final long timeSpent = System.currentTimeMillis() - start;
        if (contentLength < 0 || bytesRead == 0 || timeSpent == 0)
        {
            return Long.MAX_VALUE;
        }
        return contentLength / bytesRead * timeSpent;
    }

    //
    // ProgressListener
    //

    public final void update(final long bytesRead, final long contentLength, final int items)
    {
        final BlockingQueue<FileUploadFeedback> queue =
                (BlockingQueue<FileUploadFeedback>) httpSession.getAttribute(CIFEXServiceImpl.UPLOAD_FEEDBACK_QUEUE);
        assert queue != null : "Queue must not be null.";
        queue.clear();
        queue.add(createFileUploadFeedback(bytesRead, contentLength, items));
    }
}
