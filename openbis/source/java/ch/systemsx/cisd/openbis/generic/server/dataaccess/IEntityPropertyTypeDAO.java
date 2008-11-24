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

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;

/**
 * A provider of <code>IEntityPropertiesSchema</code>.
 * 
 * @author Tomasz Pylak
 */
public interface IEntityPropertyTypeDAO
{

    /**
     * Returns a list of all assignment of user properties (i.e. no internal properties) for the
     * given <var>entityTypeId</var>.
     * <p>
     * Can return an empty list if there are no properties or no material with id equal to
     * materialTypeId exist.
     * 
     * @param entityType from the corresponding entity type table
     */
    public List<EntityTypePropertyTypePE> listEntityPropertyTypes(final EntityTypePE entityType)
            throws DataAccessException;

}