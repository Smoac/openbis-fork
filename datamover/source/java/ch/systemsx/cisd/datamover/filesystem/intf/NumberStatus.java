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

/**
 * A class that holds the information about the result of an operation which is a number. There is a
 * way to find out if an error occurred during operation execution, the result is unavailable then.
 * 
 * @author Tomasz Pylak
 */
public class NumberStatus
{
    private final ResultStatus<Long> result;

    private NumberStatus(Long result, boolean errorOccurred, String messageOrNull)
    {
        this.result = new ResultStatus<Long>(result, errorOccurred, messageOrNull);
    }

    public static final NumberStatus create(long result)
    {
        return new NumberStatus(result, false, null);
    }

    public static final NumberStatus createError(String message)
    {
        return new NumberStatus(null, true, message);
    }

    public static final NumberStatus createError()
    {
        return new NumberStatus(null, true, null);
    }

    /**
     * can be called only if no error occurred, otherwise it fails.
     */
    public long getResult()
    {
        return result.getResult();
    }

    /** has operation finished with an error? */
    public boolean isError()
    {
        return result.isError();
    }

    public String tryGetMessage()
    {
        return result.tryGetMessage();
    }
}
