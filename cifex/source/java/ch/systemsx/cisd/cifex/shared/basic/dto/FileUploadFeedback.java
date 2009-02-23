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

package ch.systemsx.cisd.cifex.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A <i>DTO</i> giving some feedback about the file upload process that is currently running on the
 * server.
 * 
 * @author Christian Ribeaud
 */
public final class FileUploadFeedback implements IsSerializable, Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     * The request content length (in bytes).
     * <p>
     * Default value is <code>-1</code> meaning we do not know the request content length.
     * </p>
     */
    private long contentLength = -1;

    /** The bytes read till now. */
    private long bytesRead;

    /** The file that is currently uploaded. */
    private String fileName;

    /**
     * Approximative time left until this file upload is finished (in milliseconds).
     * <p>
     * Default value is <code>Long.MAX_VALUE</code> meaning that we have no clue about the time
     * left.
     * </p>
     */
    private long timeLeft = Long.MAX_VALUE;

    /** Warning or error message coming from the server pointing out that some problem happened. */
    private Message message;

    /** Whether the file upload is terminated. */
    private boolean finished;

    public final long getContentLength()
    {
        return contentLength;
    }

    public final void setContentLength(final long contentLength)
    {
        this.contentLength = contentLength;
    }

    public final String getFileName()
    {
        return fileName;
    }

    public final void setFileName(final String fileName)
    {
        this.fileName = fileName;
    }

    public final long getBytesRead()
    {
        return bytesRead;
    }

    public final void setBytesRead(final long bytesRead)
    {
        this.bytesRead = bytesRead;
    }

    public final long getTimeLeft()
    {
        return timeLeft;
    }

    public final void setTimeLeft(final long timeLeft)
    {
        this.timeLeft = timeLeft;
    }

    public final void setMessage(final Message message)
    {
        this.message = message;
    }

    public final boolean isFinished()
    {
        return finished;
    }

    public final void setFinished(final boolean isTerminated)
    {
        this.finished = isTerminated;
    }

    //
    // Helper methods
    //

    public final int getPercentage()
    {
        if (contentLength == -1)
        {
            // ensure we never reach 100% but show progress
            return (int) (bytesRead * 100.0 / (bytesRead + 10000));
        }
        return (int) (bytesRead * 100 / contentLength);
    }

    public final Message getMessage()
    {
        return message;
    }
}