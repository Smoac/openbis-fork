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

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Registrator of the experiment which corresponds to the data. This is an immutable value object
 * class.
 * 
 * @author Franz-Josef Elmer
 */
public final class ExperimentRegistrator implements IStorable
{
    public static final String EXPERIMENT_REGISTRATOR = "experiment_registrator";

    public static final String FIRST_NAME = "first_name";

    public static final String LAST_NAME = "last_name";

    public static final String EMAIL = "email";

    /**
     * Loads the experiment registrator from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    public static final ExperimentRegistrator loadFrom(final IDirectory directory)
    {
        final IDirectory folder = Utilities.getSubDirectory(directory, EXPERIMENT_REGISTRATOR);
        final String firstName = Utilities.getTrimmedString(folder, FIRST_NAME);
        final String secondName = Utilities.getTrimmedString(folder, LAST_NAME);
        final String email = Utilities.getTrimmedString(folder, EMAIL);
        return new ExperimentRegistrator(firstName, secondName, email);
    }

    private final String firstName;

    private final String lastName;

    private final String email;

    /**
     * Creates an instance for the specified name and e-mail of the registrator.
     * 
     * @param firstName A non-empty string of the first name.
     * @param lastName A non-empty string of the second name.
     * @param email A non-empty string of the email.
     */
    public ExperimentRegistrator(final String firstName, final String lastName, final String email)
    {
        assert firstName != null && firstName.length() > 0 : "Undefined first name";
        this.firstName = firstName;
        assert lastName != null && lastName.length() > 0 : "Undefined second name";
        this.lastName = lastName;
        assert email != null && email.length() > 0 : "Undefined email";
        this.email = email;
    }

    /**
     * Returns the first name.
     */
    public final String getFirstName()
    {
        return firstName;
    }

    /**
     * Returns the second name.
     */
    public final String getLastName()
    {
        return lastName;
    }

    /**
     * Returns the email.
     */
    public final String getEmail()
    {
        return email;
    }

    //
    // IStorable
    //

    /**
     * Saves this instance to the specified directory.
     */
    public final void saveTo(final IDirectory directory)
    {
        final IDirectory folder = directory.makeDirectory(EXPERIMENT_REGISTRATOR);
        folder.addKeyValuePair(FIRST_NAME, firstName);
        folder.addKeyValuePair(LAST_NAME, lastName);
        folder.addKeyValuePair(EMAIL, email);
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentRegistrator == false)
        {
            return false;
        }
        final ExperimentRegistrator registrator = (ExperimentRegistrator) obj;
        return registrator.firstName.equals(firstName) && registrator.lastName.equals(lastName)
                && registrator.email.equals(email);
    }

    @Override
    public final int hashCode()
    {
        return (firstName.hashCode() * 37 + lastName.hashCode()) * 37 + email.hashCode();
    }

    @Override
    public final String toString()
    {
        final ToStringBuilder builder = new ToStringBuilder();
        builder.append(EMAIL, email);
        builder.append(FIRST_NAME, firstName);
        builder.append(LAST_NAME, lastName);
        return builder.toString();
    }

}
