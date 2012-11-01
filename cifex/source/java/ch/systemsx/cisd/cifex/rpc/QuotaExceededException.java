/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc;

import java.util.Locale;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * An exception that indicates that either the file size or file count quota have been exceeded.
 * 
 * @author Bernd Rinn
 */
public class QuotaExceededException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    public QuotaExceededException(Integer maxFileCount, Long maxFileSize, int currentFileCount,
            double currentFileSize)
    {
        super(constructMessage(maxFileCount, maxFileSize, currentFileCount, currentFileSize));
    }

    private static String constructMessage(Integer maxFileCount, Long maxFileSize,
            int currentFileCount, double currentFileSize)
    {
        if (maxFileCount == null)
        {
            return String.format("The upload would exceed the quota limit of %d MB total size "
                    + "(current usage: %d MB)", maxFileSize, currentFileSize);
        } else if (maxFileSize == null)
        {
            return String.format("The upload would exceed the quota limit of %d files "
                    + "(current usage: %d files)", maxFileCount, maxFileSize, currentFileCount,
                    currentFileSize);
        } else
        {
            return String.format(Locale.ENGLISH,
                    "The upload would exceed the quota limit of %d files or %d MB total size "
                            + "(current usage: %d files, %1.2f MB)", maxFileCount, maxFileSize,
                    currentFileCount, currentFileSize);
        }
    }

}
