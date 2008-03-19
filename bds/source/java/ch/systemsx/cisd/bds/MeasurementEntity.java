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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Enity of measurement or calculation covered by the data. This is an immutable value object class.
 * 
 * @author Franz-Josef Elmer
 */
public final class MeasurementEntity implements IStorable
{
    static final String FOLDER = "measurement_entity";

    static final String ENTITY_TYPE_DESCRIPTION = "entity_type_description";

    static final String ENTITY_CODE = "entity_code";

    /**
     * Loads the enity from the specified directory.
     * 
     * @throws DataStructureException if file missing.
     */
    static MeasurementEntity loadFrom(IDirectory directory)
    {
        IDirectory folder = Utilities.getSubDirectory(directory, FOLDER);
        String entityTypeDescription = Utilities.getTrimmedString(folder, ENTITY_TYPE_DESCRIPTION);
        String entityCode = Utilities.getTrimmedString(folder, ENTITY_CODE);
        return new MeasurementEntity(entityCode, entityTypeDescription);
    }

    private final String entityTypeDescription;

    private final String entityCode;

    /**
     * Creates an instance for the specified code and type description of the entity
     * 
     * @param entityCode A non-empty string of the enitity code.
     * @param entityTypeDescription A non-empty description of the type of entity.
     */
    public MeasurementEntity(String entityCode, String entityTypeDescription)
    {
        assert entityTypeDescription != null && entityTypeDescription.length() > 0 : "Undefined entity type description";
        this.entityTypeDescription = entityTypeDescription;
        assert entityCode != null && entityCode.length() > 0 : "Undefined entity code";
        this.entityCode = entityCode;
    }

    /**
     * Returns the description of the entity type.
     */
    public final String getEntityTypeDescription()
    {
        return entityTypeDescription;
    }

    /**
     * Returns the entity code.
     */
    public final String getEntityCode()
    {
        return entityCode;
    }

    //
    // IStorable
    //

    /**
     * Saves this instance to the specified directory.
     */
    public final void saveTo(IDirectory directory)
    {
        IDirectory folder = directory.makeDirectory(FOLDER);
        folder.addKeyValuePair(ENTITY_TYPE_DESCRIPTION, entityTypeDescription);
        folder.addKeyValuePair(ENTITY_CODE, entityCode);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof MeasurementEntity == false)
        {
            return false;
        }
        MeasurementEntity entity = (MeasurementEntity) obj;
        return entity.entityTypeDescription.equals(entityTypeDescription)
                && entity.entityCode.equals(entityCode);
    }

    @Override
    public int hashCode()
    {
        return entityTypeDescription.hashCode() * 37 + entityCode.hashCode();
    }

    @Override
    public String toString()
    {
        return "[" + entityCode + ": " + entityTypeDescription + "]";
    }

}
