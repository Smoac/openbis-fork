/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A class that encapsulates the details about entity registration.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class EntityRegistrationDetails implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize an EntityRegistrationDetails object.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class EntityRegistrationDetailsInitializer
    {
        private String firstName;

        private String lastName;

        private String email;

        private String userId;

        private Date registrationDate;

        public String getFirstName()
        {
            return firstName;
        }

        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }

        public String getLastName()
        {
            return lastName;
        }

        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }

        public String getEmail()
        {
            return email;
        }

        public void setEmail(String email)
        {
            this.email = email;
        }

        public String getUserId()
        {
            return userId;
        }

        public void setUserId(String userId)
        {
            this.userId = userId;
        }

        public Date getRegistrationDate()
        {
            return registrationDate;
        }

        public void setRegistrationDate(Date registrationDate)
        {
            this.registrationDate = registrationDate;
        }

    }

    private final String userFirstName;

    private final String userLastName;

    private final String userEmail;

    private final String userId;

    private final Date registrationDate;

    public EntityRegistrationDetails(EntityRegistrationDetailsInitializer initializer)
    {
        this.userFirstName = initializer.getFirstName();
        this.userLastName = initializer.getLastName();
        this.userEmail = initializer.getEmail();
        this.userId = initializer.getUserId();
        this.registrationDate = initializer.getRegistrationDate();
    }

    public String getUserFirstName()
    {
        return userFirstName;
    }

    public String getUserLastName()
    {
        return userLastName;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public String getUserId()
    {
        return userId;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof EntityRegistrationDetails == false)
        {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();
        EntityRegistrationDetails other = (EntityRegistrationDetails) obj;
        builder.append(getUserId(), other.getUserId());
        builder.append(getRegistrationDate(), other.getRegistrationDate());
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getUserId());
        builder.append(getRegistrationDate());
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getUserId());
        builder.append(getRegistrationDate());
        return builder.toString();
    }

}
