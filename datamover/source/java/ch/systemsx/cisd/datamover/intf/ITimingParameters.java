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
package ch.systemsx.cisd.datamover.intf;

/**
 * A role that provides the parameters regarding time intervals to be used in the operation.
 * 
 * @author Bernd Rinn
 */
public interface ITimingParameters
{

    /**
     * @return The time interval between to checks for monitoring and reporting in milliseconds.
     */
    public long getCheckIntervalMillis();

    /**
     * @return The time interval that a directory needs to be "quiet" before being moved in
     *         milliseconds.
     */
    public long getQuietPeriodMillis();

    /**
     * @return The time period of inactivity that triggers the alarm (i.e. notification of the
     *         administrator).
     */
    public long getInactivityPeriodMillis();

    /**
     * @return The time interval to wait after a retriable error has occurred before a new attempt
     *         is made.
     */
    public long getIntervalToWaitAfterFailure();

    /**
     * @return The maximal number of retries of a failed retriable operation before giving up on it.
     */
    public int getMaximalNumberOfRetries();

}