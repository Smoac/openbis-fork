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
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * An interface that contains all data access operations on {@link EntityTypePropertyTypePE}s.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
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

    /**
     * Creates a new {@link EntityTypePropertyTypePE} assignment.
     */
    public void createEntityPropertyTypeAssignment(
            final EntityTypePropertyTypePE entityPropertyTypeAssignement)
            throws DataAccessException;

    /**
     * Returns a list of all entities of given type.
     */
    public List<IEntityPropertiesHolder<EntityPropertyPE>> listEntities(
            final EntityTypePE entityType);

    /**
     * Counts how many times is the specified vocabulary term used as a property value for the
     * entity kind represented by this class.
     */
    public long countTermUsageStatistics(final VocabularyTermPE vocabularyTerm)
            throws DataAccessException;
    
    public List<EntityPropertyPE> listPropertiesByVocabularyTerm(String vocabularyTermCode);
    
    public void updateProperties(List<EntityPropertyPE> properties);

}