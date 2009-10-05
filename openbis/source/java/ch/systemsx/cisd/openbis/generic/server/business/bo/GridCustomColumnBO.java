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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IFilterOrColumnUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Operations on grid custom columns.
 * 
 * @author Tomasz Pylak
 */
public class GridCustomColumnBO extends AbstractBusinessObject implements
        IGridCustomFilterOrColumnBO
{
    private static final String CUSTOM_COLUMN_ID_PREFIX = "$";

    private GridCustomColumnPE column;

    public GridCustomColumnBO(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
    }

    public void define(NewColumnOrFilter newColumn) throws UserFailureException
    {
        column = new GridCustomColumnPE();
        column.setCode(createUniqueCode(newColumn));
        column.setLabel(newColumn.getName());

        column.setDescription(newColumn.getDescription());
        column.setExpression(newColumn.getExpression());
        column.setGridId(newColumn.getGridId());
        column.setPublic(newColumn.isPublic());
        column.setRegistrator(findRegistrator());
    }

    private static String createUniqueCode(NewColumnOrFilter newColumn)
    {
        String name = newColumn.getName();
        String code = name.replace(" ", "_");
        code = code.toLowerCase();
        return CUSTOM_COLUMN_ID_PREFIX + code;
    }

    public void loadDataByTechId(TechId id)
    {
        try
        {
            column = getGridCustomColumnDAO().getByTechId(id);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }

    }

    public void deleteByTechId(TechId groupId) throws UserFailureException
    {
        loadDataByTechId(groupId);
        try
        {
            getGridCustomColumnDAO().delete(column);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Column '%s'", column));
        }
    }

    public void save() throws UserFailureException
    {
        assert column != null : "Column not defined";
        try
        {
            getGridCustomColumnDAO().createColumn(column);
        } catch (final DataAccessException e)
        {
            throwException(e, "Column '" + column + "'");
        }
    }

    public void update(IFilterOrColumnUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));

        column.setLabel(updates.getName());
        column.setDescription(updates.getDescription());
        column.setExpression(updates.getExpression());
        column.setPublic(updates.isPublic());

        validateAndSave();
    }

    private void validateAndSave()
    {
        getGridCustomColumnDAO().validateAndSaveUpdatedEntity(column);
    }

}
