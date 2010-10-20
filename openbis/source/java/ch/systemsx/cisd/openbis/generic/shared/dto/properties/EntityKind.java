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

import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;

/**
 * Enumeration of entity kinds.
 * 
 * @author Franz-Josef Elmer
 */
public enum EntityKind
{
    MATERIAL("material", MaterialPE.class, MaterialTypePE.class, MaterialTypePropertyTypePE.class,
            MaterialPropertyPE.class),

    EXPERIMENT("experiment", ExperimentPE.class, ExperimentTypePE.class,
            ExperimentTypePropertyTypePE.class, ExperimentPropertyPE.class),

    SAMPLE("sample", SamplePE.class, SampleTypePE.class, SampleTypePropertyTypePE.class,
            SamplePropertyPE.class),

    DATA_SET("dataSet", DataPE.class, DataSetTypePE.class, DataSetTypePropertyTypePE.class,
            DataSetPropertyPE.class);

    private final String entityLabel;

    private transient final Class<? extends IEntityInformationWithPropertiesHolder> entityClass;

    private transient final Class<?> typeClass;

    private transient final Class<?> assignmentClass;

    private transient final Class<?> propertyClass;

    private EntityKind(final String entityLabel,
            final Class<? extends IEntityInformationWithPropertiesHolder> entityClass,
            final Class<?> typeClass, final Class<?> assignmentClass, Class<?> propertyClass)
    {
        this.entityLabel = entityLabel;
        this.entityClass = entityClass;
        this.typeClass = typeClass;
        this.assignmentClass = assignmentClass;
        this.propertyClass = propertyClass;
    }

    @SuppressWarnings("unchecked")
    private final static <T> Class<T> cast(final Class<?> clazz)
    {
        return (Class<T>) clazz;
    }

    public final String getLabel()
    {
        return entityLabel;
    }

    public final <T extends EntityTypePE> Class<T> getTypeClass()
    {
        return cast(typeClass);
    }

    public final <T extends EntityTypePropertyTypePE> Class<T> getEntityTypePropertyTypeAssignmentClass()
    {
        return cast(assignmentClass);
    }

    public final <T extends EntityPropertyPE> Class<T> getEntityPropertyClass()
    {
        return cast(propertyClass);
    }

    public final <T extends IEntityInformationWithPropertiesHolder> Class<T> getEntityClass()
    {
        return cast(entityClass);
    }

    public final String getEntityTypeFieldName()
    {
        return entityLabel + "Type";
    }
}
