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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Immutable class which holds the date of registration of an experiment. 
 *
 * @author Franz-Josef Elmer
 */
public final class ExperimentRegistratorDate
{
    static final String FILE_NAME = "experiment_registration_date";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    
    static ExperimentRegistratorDate loadFrom(IDirectory directory)
    {
        String dateAsString = Utilities.getTrimmedString(directory, FILE_NAME);
        try
        {
            return new ExperimentRegistratorDate(DATE_FORMAT.parse(dateAsString));
        } catch (ParseException ex)
        {
            throw new DataStructureException("Couldn't be parsed as a date: " + dateAsString);
        }
    }
    
    private final Date date;
    
    /**
     * Creates an instance for the specified date.
     */
    public ExperimentRegistratorDate(Date date)
    {
        this.date = date;
    }

    /**
     * Returns the date;
     */
    public final Date getDate()
    {
        return date;
    }
    
    /**
     * Saves this instance to the specified directory.
     */
    void saveTo(IDirectory directory)
    {
        directory.addKeyValuePair(FILE_NAME, DATE_FORMAT.format(date));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentRegistratorDate == false)
        {
            return false;
        }
        return ((ExperimentRegistratorDate) obj).getDate().getTime() == date.getTime();
    }

    @Override
    public int hashCode()
    {
        return (int) date.getTime();
    }

    @Override
    public String toString()
    {
        return DATE_FORMAT.format(date);
    }
    
}
