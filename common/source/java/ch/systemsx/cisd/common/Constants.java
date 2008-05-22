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

package ch.systemsx.cisd.common;

import org.apache.commons.lang.time.DateUtils;

/**
 * Constants common to more than one CISD project.
 * 
 * @author Bernd Rinn
 */
public final class Constants
{

    private Constants()
    {
        // This class can not be instantiated.
    }

    /** String that indicates a marker file */
    public static final String MARKER_PREFIX = ".MARKER_";

    /** The prefix of marker files that indicate that the processing of some path is finished. */
    public static final String IS_FINISHED_PREFIX = MARKER_PREFIX + "is_finished_";

    /** The prefix of marker files that indicate that the processing of some path is finished. */
    public static final String DELETION_IN_PROGRESS_PREFIX =
            MARKER_PREFIX + "deletion_in_progress_";

    /** The number of milliseconds to wait before considering timeout (<i>10s</i>). */
    public static final long MILLIS_TO_WAIT_BEFORE_TIMEOUT = 10 * DateUtils.MILLIS_PER_SECOND;

    /** The number of milliseconds to sleep before retrying (<i>3s</i>). */
    public static final long MILLIS_TO_SLEEP_BEFORE_RETRYING = 3 * DateUtils.MILLIS_PER_SECOND;
}
