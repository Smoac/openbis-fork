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
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;

/**
 * An interface that contains all data access operations on {@link EntityTypePropertyTypePE}s.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public interface IEntityPropertyTypeDAO
{

    /**
     * Returns a list of all entity type - property type assignments connected to given
     * {@link EntityTypePE}.
     */
    public List<EntityTypePropertyTypePE> listEntityPropertyTypes(final EntityTypePE entityType)
            throws DataAccessException;

    /**
     * Returns {@link EntityTypePropertyTypePE} assignment connecting given {@link EntityTypePE} and
     * {@link PropertyTypePE} if it exists and null otherwise.
     */
    public EntityTypePropertyTypePE tryFindAssignment(EntityTypePE entityType,
            PropertyTypePE propertyType);

    public int countAssignmentValues(String entityTypeCode, String propertyTypeCode);

    /**
     * Creates a new {@link EntityTypePropertyTypePE} assignment.
     */
    public void createEntityPropertyTypeAssignment(
            final EntityTypePropertyTypePE entityPropertyTypeAssignement)
            throws DataAccessException;

    /**
     * Returns a list of all entities of given <var>entityType</var>.
     */
    public List<IEntityPropertiesHolder> listEntities(final EntityTypePE entityType)
            throws DataAccessException;

    /**
     * Returns a list of ids of all entities of given <var>entityType</var>.
     */
    public List<Long> listEntityIds(final EntityTypePE entityType) throws DataAccessException;

    /**
     * Returns a list of ids of all entities of type from specified assignment with that don't have
     * any value for property assigned with the assignment.
     */
    public List<Long> listIdsOfEntitiesWithoutPropertyValue(
            final EntityTypePropertyTypePE assignment) throws DataAccessException;

    /** Schedules evaluation of dynamic properties */
    public void scheduleDynamicPropertiesEvaluation(final EntityTypePropertyTypePE assignment)
            throws DataAccessException;

    /**
     * Fills term usage statistics for the entity kind represented by this class.
     * 
     * @param termsWithStats all terms of specified vocabulary that will have statistics filled
     */
    public void fillTermUsageStatistics(List<VocabularyTermWithStats> termsWithStats,
            VocabularyPE vocabulary);

    /**
     * Returns a list of all properties referring to the specified vocabulary term.
     */
    public List<EntityPropertyPE> listPropertiesByVocabularyTerm(String vocabularyTermCode);

    /**
     * Updates specified properties.
     */
    public void updateProperties(List<EntityPropertyPE> properties);

    /**
     * Updates given persistent (already saved) <var>entity</var> after successful validation.<br>
     * <br>
     * Useful especially instead of a save() method (used for making entity persistent) after BO
     * update that does not flush.
     * 
     * @param entity the entity to be validated and updated
     */
    public void validateAndSaveUpdatedEntity(EntityTypePropertyTypePE entity);

    /**
     * Deletes specified assignment.
     */
    public void delete(EntityTypePropertyTypePE assignment);

    public void increaseOrdinals(EntityTypePE entityType, Long startOrdinal, int increment);

    /**
     * Creates properties based on given property for entities with specified ids.
     */
    public void createProperties(EntityPropertyPE property, List<Long> entityIds);

}
