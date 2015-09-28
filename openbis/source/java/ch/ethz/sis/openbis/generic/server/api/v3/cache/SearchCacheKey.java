/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.cache;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptionsMatcher;

/**
 * @author pkupczyk
 */
public class SearchCacheKey<CRITERIA, FETCH_OPTIONS> implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String sessionToken;

    private CRITERIA criterion;

    private FETCH_OPTIONS fetchOptions;

    public SearchCacheKey(String sessionToken, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        this.sessionToken = sessionToken;
        this.criterion = criteria;
        this.fetchOptions = fetchOptions;
    }

    @Override
    public int hashCode()
    {
        return sessionToken.hashCode() + criterion.getClass().hashCode() + fetchOptions.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        SearchCacheKey<?, ?> other = (SearchCacheKey<?, ?>) obj;
        return sessionToken.equals(other.sessionToken) && criterion.equals(other.criterion)
                && FetchOptionsMatcher.arePartsEqual(fetchOptions, other.fetchOptions);
    }
}
