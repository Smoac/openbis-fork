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

package ch.systemsx.cisd.common.fileconverter;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;

/**
 * A class that holds the information about a compression failure.
 * 
 * @author Bernd Rinn
 */
public class FailureRecord
{
    private final File failedFile;

    private final Status failureStatus;

    private final Throwable throwableOrNull;

    FailureRecord(File failedFile, Status failureStatus)
    {
        this.failedFile = failedFile;
        this.failureStatus = failureStatus;
        this.throwableOrNull = null;
    }

    FailureRecord(File failedFile, Throwable throwableOrNull)
    {
        this.failedFile = failedFile;
        this.failureStatus =
                Status.createError("Exceptional condition: "
                        + throwableOrNull.getClass().getSimpleName());
        this.throwableOrNull = throwableOrNull;
    }

    /**
     * Returns the file that caused the failure.
     */
    public final File getFailedFile()
    {
        return failedFile;
    }

    /**
     * Returns the {@link Status} of the failure. Can have a {@link StatusFlag} of
     * {@link StatusFlag#RETRIABLE_ERROR} if retrying the operation did not help.
     */
    public final Status getFailureStatus()
    {
        return failureStatus;
    }

    /**
     * Returns the {@link Throwable}, if any has occurred in the compression method.
     */
    public final Throwable tryGetThrowable()
    {
        return throwableOrNull;
    }
}