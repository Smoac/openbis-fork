/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AuthorizationGroupFetchOptions extends FetchOptions<AuthorizationGroup> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private PersonFetchOptions registrator;

    @JsonProperty
    private PersonFetchOptions users;
    
    @JsonProperty
    private AuthorizationGroupSortOptions sort;

    public PersonFetchOptions withRegistrator()
    {
        if (registrator == null)
        {
            registrator = new PersonFetchOptions();
        }
        return registrator;
    }

    public PersonFetchOptions withRegistratorUsing(PersonFetchOptions fetchOptions)
    {
        return registrator = fetchOptions;
    }

    public boolean hasRegistrator()
    {
        return registrator != null;
    }

    public PersonFetchOptions withUsers()
    {
        if (users == null)
        {
            users = new PersonFetchOptions();
        }
        return users;
    }
    
    public PersonFetchOptions withUsersUsing(PersonFetchOptions fetchOptions)
    {
        return users = fetchOptions;
    }
    
    public boolean hasUsers()
    {
        return users != null;
    }
    
    @Override
    public AuthorizationGroupSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new AuthorizationGroupSortOptions();
        }
        return sort;
    }

    @Override
    public AuthorizationGroupSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("AuthorizationGroup", this);
        f.addFetchOption("Registrator", registrator);
        return f;
    }

}
