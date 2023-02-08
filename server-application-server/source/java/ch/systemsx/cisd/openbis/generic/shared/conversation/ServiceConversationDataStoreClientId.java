/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.conversation;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author pkupczyk
 */
public class ServiceConversationDataStoreClientId implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String dataStoreCode;

    public ServiceConversationDataStoreClientId(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode.toUpperCase();
    }

    public String getDataStoreCode()
    {
        return dataStoreCode;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(dataStoreCode);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        ServiceConversationDataStoreClientId that = (ServiceConversationDataStoreClientId) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(dataStoreCode, that.dataStoreCode);
        return builder.isEquals();
    }

    @Override
    public String toString()
    {
        return "data store client: " + dataStoreCode;
    }

}
