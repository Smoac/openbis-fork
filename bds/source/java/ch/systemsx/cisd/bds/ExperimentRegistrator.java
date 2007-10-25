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

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Registrator of the experiment which corresponds to the data. This is an immutable value object class.
 *
 * @author Franz-Josef Elmer
 */
public final class ExperimentRegistrator
{
    static final String FOLDER = "experiment_registrator";
    static final String FIRST_NAME = "first_name";
    static final String SECOND_NAME = "last_name";
    static final String EMAIL = "email";
    
    /**
     * Loads the experiment registaror from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    static ExperimentRegistrator loadFrom(IDirectory directory)
    {
        IDirectory folder = Utilities.getSubDirectory(directory, FOLDER);
        String firstName = Utilities.getTrimmedString(folder, FIRST_NAME);
        String secondName = Utilities.getTrimmedString(folder, SECOND_NAME);
        String email = Utilities.getTrimmedString(folder, EMAIL);
        return new ExperimentRegistrator(firstName, secondName, email);
    }
    
    private final String firstName;
    private final String secondName;
    private final String email;

    /**
     * Creates an instance for the specified name and e-mail of the registrator.
     *
     * @param firstName A non-empty string of the first name.
     * @param secondName A non-empty string of the second name.
     * @param email A non-empty string of the email.
     */
    public ExperimentRegistrator(String firstName, String secondName, String email)
    {
        assert firstName != null && firstName.length() > 0 : "Undefined first name";
        this.firstName = firstName;
        assert secondName != null && secondName.length() > 0 : "Undefined second name";
        this.secondName = secondName;
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
    public final String getSecondName()
    {
        return secondName;
    }
    
    /**
     * Returns the email.
     */
    public final String getEmail()
    {
        return email;
    }
    
    /**
     * Saves this instance to the specified directory.
     */
    void saveTo(IDirectory directory)
    {
        IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(FIRST_NAME, firstName);
        folder.addKeyValuePair(SECOND_NAME, secondName);
        folder.addKeyValuePair(EMAIL, email);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof ExperimentRegistrator == false)
        {
            return false;
        }
        ExperimentRegistrator registrator = (ExperimentRegistrator) obj;
        return registrator.firstName.equals(firstName) && registrator.secondName.equals(secondName)
                && registrator.email.equals(email);
    }

    @Override
    public int hashCode()
    {
        return (firstName.hashCode() * 37 + secondName.hashCode()) * 37 + email.hashCode();
    }

    @Override
    public String toString()
    {
        return firstName + " " + secondName + ", e-mail:" + email;
    }
    
    
}
