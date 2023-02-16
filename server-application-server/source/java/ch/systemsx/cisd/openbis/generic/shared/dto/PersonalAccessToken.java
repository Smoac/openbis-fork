/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PersonalAccessToken
{

    private String hash;

    private String sessionName;

    private String ownerId;

    private String registratorId;

    private String modifierId;

    private Date validFromDate;

    private Date validToDate;

    private Date registrationDate;

    private Date modificationDate;

    private Date accessDate;

    public String getHash()
    {
        return hash;
    }

    public void setHash(final String hash)
    {
        this.hash = hash;
    }

    public String getSessionName()
    {
        return sessionName;
    }

    public void setSessionName(final String sessionName)
    {
        this.sessionName = sessionName;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(final String ownerId)
    {
        this.ownerId = ownerId;
    }

    public String getRegistratorId()
    {
        return registratorId;
    }

    public void setRegistratorId(final String registratorId)
    {
        this.registratorId = registratorId;
    }

    public String getModifierId()
    {
        return modifierId;
    }

    public void setModifierId(final String modifierId)
    {
        this.modifierId = modifierId;
    }

    public Date getValidFromDate()
    {
        return validFromDate;
    }

    public void setValidFromDate(final Date validFromDate)
    {
        this.validFromDate = validFromDate;
    }

    public Date getValidToDate()
    {
        return validToDate;
    }

    public void setValidToDate(final Date validToDate)
    {
        this.validToDate = validToDate;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(final Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    public Date getAccessDate()
    {
        return accessDate;
    }

    public void setAccessDate(final Date accessDate)
    {
        this.accessDate = accessDate;
    }

    @Override public boolean equals(final Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override public String toString()
    {
        return "PersonalAccessToken{" +
                "ownerId='" + ownerId + '\'' +
                ", sessionName='" + sessionName + '\'' +
                ", validFromDate=" + validFromDate +
                ", validToDate=" + validToDate +
                '}';
    }

}
