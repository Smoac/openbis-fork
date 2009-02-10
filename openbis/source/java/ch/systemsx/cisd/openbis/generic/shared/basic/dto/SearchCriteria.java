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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Describes search criteria specific to data set search.
 * 
 * @author Izabela Adamczyk
 */
// TODO 2009-02-10, Tomasz Pylak: rename to DataSetSearchCriteria
public class SearchCriteria implements IsSerializable
{
    private List<DataSetSearchCriterion> criteria;

    private CriteriaConnection connection;

    public enum CriteriaConnection implements IsSerializable
    {
        AND, OR
    }

    public SearchCriteria()
    {
    }

    public List<DataSetSearchCriterion> getCriteria()
    {
        return criteria;
    }

    public void setCriteria(List<DataSetSearchCriterion> criteria)
    {
        this.criteria = criteria;
    }

    public CriteriaConnection getConnection()
    {
        return connection;
    }

    public void setConnection(CriteriaConnection connection)
    {
        this.connection = connection;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        for (final DataSetSearchCriterion element : getCriteria())
        {
            if (sb.toString().equals("") == false)
            {
                sb.append(" " + getConnection().name() + " ");
            }
            sb.append(element);
        }
        return sb.toString();
    }
}