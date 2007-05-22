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

package ch.systemsx.cisd.common.logging;

/**
 * This enum represents the categories of logging defined in the CISD logging policy.
 * 
 * @author Bernd Rinn
 */
public enum LogCategory
{

    /** Log category for data (read) access events. */
    ACCESS,
    /** Log category for authentication and authorization events. */
    AUTH,
    /** Log category for log events related to the machine's state (low-level). */
    MACHINE,
    /** Log category for events that require immediate notification of an administrator. */
    NOTIFY,
    /** Log category for (normal) operational events. */
    OPERATION,
    /** Log category for data manipulation events (write access). */
    TRACKING

}
