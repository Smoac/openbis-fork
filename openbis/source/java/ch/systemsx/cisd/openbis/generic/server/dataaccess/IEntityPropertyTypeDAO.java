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

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

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

    /**
     * @return null if the given property is not assigned to the given material type, otherwise
     *         description of the assignment.
     */
    public EntityTypePropertyTypePE tryFindAssignment(final EntityTypePE entityType,
            final PropertyTypePE propertyType) throws DataAccessException;

    /**
     * Creates a new assignment of given material type with given mandatory flag for the specified
     * DTO.
     * <p>
     * As a side effect {@link EntityTypePropertyTypePE#setId(Long)} will be invoked with the
     * <i>unique identifier</i> returned by the persistent layer.
     * </p>
     */
    public void createEntityPropertyTypeAssignment(
            final EntityTypePropertyTypePE entityPropertyTypeAssignement)
            throws DataAccessException;

    /**
     * changes assignment of the property to the given mandatory flag. Does not check if it is a
     * coherent operation.
     */
    public void updateEntityTypePropertyType(EntityTypePropertyTypePE entityPropertyType)
            throws DataAccessException;

    /**
     * Returns a list of the code and ID of all entities with no property value for the specified
     * entity type and entity property type.
     */
    public List<IIdAndCodeHolder> listEntitiesWithMissingPropertyValues(
            EntityTypePropertyTypePE entityPropertyType) throws DataAccessException;

    /**
     * Removes property type assignment of materials of given material type.
     * <p>
     * This does nothing if the table row corresponding to given <var>materialTypeId</var> and
     * <var>propertyTypeId</var> could not be found.
     * </p>
     */
    public void unassignEntityPropertyType(final EntityTypePE entityType,
            final PropertyTypePE propertyType) throws DataAccessException;

    /**
     * Lists all relations between types of the specified type and property types.
     */
    public List<EntityTypePropertyTypePE> listEntityPropertyTypeRelations()
            throws DataAccessException;

    /**
     * Creates a property value for the specified entity.
     */
    public void createPropertyValue(EntityPropertyPE property);

    /**
     * Counts the number of entities of the specified entity type with a non-<code>null</code>
     * property value of the specified property type.
     */
    public long countEntitiesWithProperty(long entityTypeId, long propertyTypeId)
            throws DataAccessException;

}