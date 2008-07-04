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

package ch.systemsx.cisd.datamover.filesystem.intf;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;

/**
 * A class that holds the information about the result of an operation which is a number. There is a
 * way to find out if an error occurred during operation execution, the result is unavailable then.
 * 
 * @author Tomasz Pylak
 */
public final class DateStatus
{
    private final ResultStatus<Long> result;

    private DateStatus(final Long result, final boolean errorOccurred, final String messageOrNull)
    {
        this.result = new ResultStatus<Long>(result, errorOccurred, messageOrNull);
    }

    public static final DateStatus create(final long result)
    {
        return new DateStatus(result, false, null);
    }

    public static final DateStatus createError(final String message)
    {
        return new DateStatus(null, true, message);
    }

    public static final DateStatus createError()
    {
        return new DateStatus(null, true, null);
    }

    /**
     * can be called only if no error occurred, otherwise it fails.
     */
    public final long getResult()
    {
        return result.getResult();
    }

    /** has operation finished with an error? */
    public final boolean isError()
    {
        return result.isError();
    }

    public final String tryGetMessage()
    {
        return result.tryGetMessage();
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        if (isError())
        {
            builder.append("[DateStatus, error: ", tryGetMessage() + "]");
        } else
        {
            final Long thisResult = result.getResult();
            builder.append("[DateStatus, result: ", String.format("%1$tF %1$tT", thisResult) + "]");
        }
        return builder.toString();
    }
}
