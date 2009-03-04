/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Maniupations on {@link EntityTypePE} subclasses.
 * 
 * @author Tomasz Pylak
 */
public final class EntityTypeBO extends AbstractBusinessObject implements IEntityTypeBO
{
    private EntityTypePE entityTypePE;

    private EntityKind entityKind;

    public EntityTypeBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private static EntityTypePE convertGeneric(EntityType entityType, EntityKind entityKind,
            DatabaseInstancePE databaseInstance) throws UserFailureException
    {
        EntityTypePE entityTypePE = EntityTypePE.createEntityTypePE(entityKind);
        entityTypePE.setCode(entityType.getCode());
        entityTypePE.setDescription(entityType.getDescription());
        entityTypePE.setDatabaseInstance(databaseInstance);
        return entityTypePE;
    }

    public final void save() throws UserFailureException
    {
        assert entityTypePE != null : "Entity type not defined.";
        assert entityKind != null : "Entity kind not defined.";
        try
        {
            getEntityTypeDAO(entityKind).createEntityType(entityTypePE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("Entity type '%s'.", entityTypePE.getCode()));
        }
    }

    public void define(SampleType entityType)
    {
        SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode(entityType.getCode());
        sampleTypePE.setDescription(entityType.getDescription());
        sampleTypePE.setContainerHierarchyDepth(entityType.getPartOfHierarchyDepth());
        sampleTypePE.setGeneratedFromHierarchyDepth(entityType.getGeneratedFromHierarchyDepth());
        sampleTypePE.setListable(entityType.isListable());
        sampleTypePE.setDatabaseInstance(getHomeDatabaseInstance());

        this.entityKind = EntityKind.SAMPLE;
        this.entityTypePE = sampleTypePE;
    }

    public void define(MaterialType entityType)
    {
        this.entityKind = EntityKind.MATERIAL;
        this.entityTypePE = convertGeneric(entityType, entityKind, getHomeDatabaseInstance());
    }

    public void define(ExperimentType entityType)
    {
        this.entityKind = EntityKind.EXPERIMENT;
        this.entityTypePE = convertGeneric(entityType, entityKind, getHomeDatabaseInstance());
    }
}
