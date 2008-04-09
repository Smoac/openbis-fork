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

package ch.systemsx.cisd.common.exceptions;

/**
 * The status of an operation. To be used whenever a failure of an operation is signalled back via a
 * return value rather than an exception.
 * 
 * @author Bernd Rinn
 */
public enum StatusFlag
{

    /** The operation has been successful. */
    OK,
    /** An error has occured. Retrying the operation might remedy the problem. */
    RETRIABLE_ERROR,
    /** A fatal error has occured. */
    FATAL_ERROR

}