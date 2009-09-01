/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Describes detailed search criteria specific to an entity.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchCriteria implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<DetailedSearchCriterion> criteria;

    private SearchCriteriaConnection connection;

    public DetailedSearchCriteria()
    {
    }

    public List<DetailedSearchCriterion> getCriteria()
    {
        return criteria;
    }

    public void setCriteria(List<DetailedSearchCriterion> criteria)
    {
        this.criteria = criteria;
    }

    public SearchCriteriaConnection getConnection()
    {
        return connection;
    }

    public void setConnection(SearchCriteriaConnection connection)
    {
        this.connection = connection;
    }

    public boolean isEmpty()
    {
        return criteria == null || criteria.isEmpty();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        for (final DetailedSearchCriterion element : getCriteria())
        {
            if (sb.length() > 0)
            {
                sb.append(" " + getConnection().name() + " ");
            }
            sb.append(element);
        }
        return sb.toString();
    }
}