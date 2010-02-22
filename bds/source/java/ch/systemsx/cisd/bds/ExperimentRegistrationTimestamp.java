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

package ch.systemsx.cisd.bds;

import java.util.Date;

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Immutable class which holds the timestamp of registration of an experiment.
 * 
 * @author Franz-Josef Elmer
 */
public final class ExperimentRegistrationTimestamp implements IStorable
{
    public static final String EXPERIMENT_REGISTRATION_TIMESTAMP =
            "experiment_registration_timestamp";

    /**
     * Creates an instance for the specified date.
     */
    public ExperimentRegistrationTimestamp(final Date date)
    {
        this.date = date;
    }

    public final static ExperimentRegistrationTimestamp loadFrom(final IDirectory directory)
    {
        return new ExperimentRegistrationTimestamp(Utilities.tryGetDate(directory,
                EXPERIMENT_REGISTRATION_TIMESTAMP));
    }

    private final Date date;

    private final String getFormattedDate()
    {
        return ch.systemsx.cisd.common.Constants.DATE_FORMAT.get().format(date);
    }

    /**
     * Returns the date;
     */
    public final Date getDate()
    {
        return date;
    }

    //
    // IStorable
    //

    /**
     * Saves this instance to the specified directory.
     */
    public final void saveTo(final IDirectory directory)
    {
        directory.addKeyValuePair(EXPERIMENT_REGISTRATION_TIMESTAMP, getFormattedDate());
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentRegistrationTimestamp == false)
        {
            return false;
        }
        return ((ExperimentRegistrationTimestamp) obj).getDate().getTime() == date.getTime();
    }

    @Override
    public final int hashCode()
    {
        return (int) date.getTime();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder = new ToStringBuilder();
        builder.append(EXPERIMENT_REGISTRATION_TIMESTAMP, getFormattedDate());
        return builder.toString();
    }
}
