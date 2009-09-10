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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * {@link IFilterBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public class FilterBO extends AbstractBusinessObject implements IFilterBO
{
    private FilterPE filter;

    public FilterBO(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
    }

    public void define(NewFilter newFilter) throws UserFailureException
    {
        filter = new FilterPE();
        filter.setDescription(newFilter.getDescription());
        filter.setExpression(newFilter.getExpression());
        filter.setGridId(newFilter.getGridId());
        filter.setName(newFilter.getName());
        filter.setPublic(newFilter.isPublic());
        filter.setRegistrator(findRegistrator());
    }

    public void loadDataByTechId(TechId id)
    {
        try
        {
            filter = getFilterDAO().getByTechId(id);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }

    }

    // AI add tests: define, save
    public void save() throws UserFailureException
    {
        assert filter != null : "Filter not defined";
        try
        {
            getFilterDAO().createFilter(filter);
        } catch (final DataAccessException e)
        {
            throwException(e, "Filter '" + filter + "'");
        }
    }

}
