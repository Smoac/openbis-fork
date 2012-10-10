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

}
