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

import org.apache.commons.fileupload.ProgressListener;

/**
 * A <code>ProgressListener</code> implementation which uses a threshold expressed in bytes to
 * reduce the progress listener activity.
 * <p>
 * Each time we cross this threshold, the event will be propagated but not before.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class ThresholdProgressListener implements ProgressListener
{

    /** The threshold in bytes. */
    private final long threshold;

    /** How many times we cross the threshold. */
    private long bunch;

    public ThresholdProgressListener(final long threshold)
    {
        assert threshold > 0 : "Given threshold > 0.";
        this.threshold = threshold;
    }

    /** Gets called each time the threshold is crossed. */
    protected abstract void hasProgressed(final long bytesRead, final long contentLength,
            final int items);

    //
    // ProgressListener
    //

    public final void update(final long bytesRead, final long contentLength, final int items)
    {
        final long result = bytesRead / threshold;
        if (result == bunch)
        {
            return;
        }
        bunch = result;
        hasProgressed(bytesRead, contentLength, items);
    }
}
