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
 * A class that holds the information about the status of an operation. To be used whenever a failure of an operation is
 * signalled back via a return value rather than an exception.
 * 
 * @author Bernd Rinn
 */
public final class Status
{

    private final StatusFlag flag;

    private final String message;

    /** The status indicating that the operation went fine. */
    public static final Status OK = new Status(StatusFlag.OK, null);

    public Status(StatusFlag flag, String message)
    {
        assert flag != null;
        assert StatusFlag.OK.equals(flag) || message != null;

        this.flag = flag;
        this.message = message;
    }

    /**
     * @return The status flag of the operation.
     */
    public StatusFlag getFlag()
    {
        return flag;
    }

    /**
     * @return The message of the operation if <code>getFlag() != OK</code>, or <code>null</code> otherwise.
     */
    public String getMessage()
    {
        return message;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj instanceof Status == false)
        {
            return false;
        }
        final Status status = (Status) obj;
        return flag.equals(status.flag);
    }

    @Override
    public int hashCode()
    {
        return flag.hashCode();
    }

    @Override
    public String toString()
    {
        if (message != null)
        {
            return flag.toString() + ": \"" + message + "\"";
        } else
        {
            return flag.toString();
        }
    }

}