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

package ch.systemsx.cisd.openbis.generic.shared.dto.properties;

import java.io.Serializable;

/**
 * Available types of entity properties in the database.
 * 
 * @author Tomasz Pylak
 */
public enum EntityDataType implements Serializable
{
    INTEGER("INT"), VARCHAR("STRING"), REAL("DOUBLE"), TIMESTAMP("DATE"), BOOLEAN("BOOLEAN"),
    CONTROLLEDVOCABULARY("VOCABULARY"), MATERIAL("MATERIAL");

    private final String niceRepresentation;

    private EntityDataType(final String niceRepresentation)
    {
        this.niceRepresentation = niceRepresentation;
    }

    /**
     * Returns a nice representation of this enumeration item.
     */
    public final String getNiceRepresentation()
    {
        return niceRepresentation;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getNiceRepresentation();
    }
}