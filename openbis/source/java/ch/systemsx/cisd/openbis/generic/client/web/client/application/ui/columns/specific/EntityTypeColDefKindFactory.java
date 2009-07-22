/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Factory of column definitions common in grid browsers of {@link EntityType} subclasses like material or sample types.
 *
 * @author Piotr Buczek
 */
public class EntityTypeColDefKindFactory<T extends EntityType>
{
    public static final EntityTypeColDefKindFactory<EntityType> entityTypeColDefKindFactory =
            new EntityTypeColDefKindFactory<EntityType>();

    public static final EntityTypeColDefKindFactory<ExperimentType> experimentTypeColDefKindFactory =
            new EntityTypeColDefKindFactory<ExperimentType>();

    public static final EntityTypeColDefKindFactory<MaterialType> materialTypeColDefKindFactory =
            new EntityTypeColDefKindFactory<MaterialType>();

    public static final EntityTypeColDefKindFactory<SampleType> sampleTypeColDefKindFactory =
            new EntityTypeColDefKindFactory<SampleType>();

    public static final EntityTypeColDefKindFactory<DataSetType> dataSetTypeColDefKindFactory =
            new EntityTypeColDefKindFactory<DataSetType>();

    public AbstractColumnDefinitionKind<T> createCodeColDefKind()
    {
        return new AbstractColumnDefinitionKind<T>(Dict.CODE)
            {
                @Override
                public String tryGetValue(T entity)
                {
                    return entity.getCode();
                }
            };
    }

    public AbstractColumnDefinitionKind<T> createDescriptionColDefKind()
    {
        return new AbstractColumnDefinitionKind<T>(Dict.DESCRIPTION, 300)
            {
                @Override
                public String tryGetValue(EntityType entity)
                {
                    return entity.getDescription();
                }
            };
    }

    public AbstractColumnDefinitionKind<T> createDatabaseInstanceColDefKind()
    {
        return new AbstractColumnDefinitionKind<T>(Dict.DATABASE_INSTANCE, true)
            {
                @Override
                public String tryGetValue(EntityType entity)
                {
                    return entity.getDatabaseInstance().getCode();
                }
            };
    }
}
