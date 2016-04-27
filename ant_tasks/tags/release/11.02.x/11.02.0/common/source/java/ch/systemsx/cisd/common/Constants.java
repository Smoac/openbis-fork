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

import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.utilities.DateFormatThreadLocal;

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

    /** The prefix of marker files that indicate that a directory is currently being processed. */
    public static final String PROCESSING_PREFIX = MARKER_PREFIX + "processing_";

    /** The file name of the file that contains file names which are known to be bad. */
    public static final String FAULTY_PATH_FILENAME = ".faulty_paths";

    //
    // Timing
    //

    /** Time in seconds to wait before a timeout occurs. */
    public static final int TIMEOUT_SECONDS = 60;

    /** Time in milli-seconds to wait before a timeout occurs.. */
    public static final long MILLIS_TO_WAIT_BEFORE_TIMEOUT =
            TIMEOUT_SECONDS * DateUtils.MILLIS_PER_SECOND;

    /** Time interval in seconds to wait after an operation failed before retrying it. */
    public static final int INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS = 10;

    /** Time interval in milli-seconds to wait after an operation failed before retrying it. */
    public static final long MILLIS_TO_SLEEP_BEFORE_RETRYING =
            INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS * DateUtils.MILLIS_PER_SECOND;

    /** Maximal number of retries when an operation fails. */
    public static final int MAXIMUM_RETRY_COUNT = 11;

    //
    // Date formatting
    //

    /** The date format pattern. */
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    /** The uniform date format used. */
    public static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            new DateFormatThreadLocal(DATE_FORMAT_PATTERN);

}
