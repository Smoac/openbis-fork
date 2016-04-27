/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.mail;

import java.io.Serializable;

/**
 * A class to represent an email address.
 * 
 * @author Bernd Rinn
 */
public final class EMailAddress implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String emailAddressOrNull;

    private final String personalNameOrNull;

    public EMailAddress(String emailAddressOrNull)
    {
        this(emailAddressOrNull, null);
    }
    
    public EMailAddress(String emailAddressOrNull, String personalNameOrNull)
    {
        this.emailAddressOrNull = emailAddressOrNull;
        this.personalNameOrNull = personalNameOrNull;
    }

    public String tryGetEmailAddress()
    {
        return emailAddressOrNull;
    }

    public String tryGetPersonalName()
    {
        return personalNameOrNull;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        if (emailAddressOrNull == null)
        {
            return "EmailAddress[null]";
        }
        if (personalNameOrNull == null)
        {
            return "EmailAddress{email=" + emailAddressOrNull + "}";
        } else
        {
            return "EmailAddress{email=" + emailAddressOrNull + ", personalname='" + personalNameOrNull
                    + "'}";
        }
    }

}
