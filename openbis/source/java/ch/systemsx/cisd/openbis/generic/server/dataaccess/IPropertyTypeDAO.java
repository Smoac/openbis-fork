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
     * Returns the property type for the specified code.
     */
    public PropertyTypePE tryFindPropertyTypeByCode(String code) throws DataAccessException;

    /**
     * Returns all property types including internally managed.
     */
    public List<PropertyTypePE> listAllPropertyTypes();

    /**
     * Returns property types excluding those which are internally managed.
     */
    public List<PropertyTypePE> listPropertyTypes() throws DataAccessException;

    /**
     * Lists the data types registered in the database.
     */
    public List<DataTypePE> listDataTypes();
}