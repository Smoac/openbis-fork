/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * <i>Data Access Object</i> for {@link PropertyTypePE}.
 * 
 * @author Christian Ribeaud
 */
public interface IPropertyTypeDAO
{

    /**
     * Lists the property types registered in the database.
     * 
     * @return The list of all {@link PropertyTypePE}s registered in the database. Never returns
     *         <code>null</code> but could return an empty list.
     */
    public List<PropertyTypePE> listPropertyTypes() throws DataAccessException;

    /**
     * Creates a new property type based on the specified DTO.
     * <p>
     * As a side effect {@link PropertyTypePE#setId(Long)} will be invoked with the <i>unique
     * identifier</i> returned by the persistent layer.
     * </p>
     * 
     * @param propertyTypeDTO the DTO to get the information from for the property type that is to
     *            be created. This can not be <code>null</code>.
     */
    public void createPropertyType(final PropertyTypePE propertyTypeDTO)
            throws DataAccessException;

    /**
     * Lists the data types registered in the database.
     * 
     * @return The list of all {@link DataTypePE}s registered in the database. Never returns
     *         <code>null</code> but could return an empty list.
     */
    public List<DataTypePE> listDataTypes() throws DataAccessException;

    /**
     * @return ID of the property type with the given code or <code>null</code> if no such a
     *         property exists.
     */
    public Long tryFindPropertyTypeIdByCode(final String code) throws DataAccessException;

    /**
     * Returns the property type for the specified code.
     */
    public PropertyTypePE tryFindPropertyTypeByCode(String code) throws DataAccessException;
}