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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * Columns definition for browsing grid of {@link EntityType} like material or sample types.
 * 
 * @author Tomasz Pylak
 */
public enum EntityTypeColDefKind implements IColumnDefinitionKind<EntityType>
{
    CODE(new AbstractColumnDefinitionKind<EntityType>(Dict.CODE)
        {
            @Override
            public String tryGetValue(EntityType entity)
            {
                return entity.getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<EntityType>(Dict.DESCRIPTION, 300)
        {
            @Override
            public String tryGetValue(EntityType entity)
            {
                return renderMultilineString(entity.getDescription());
            }
        }),

    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<EntityType>(Dict.DATABASE_INSTANCE, true)
        {
            @Override
            public String tryGetValue(EntityType entity)
            {
                return entity.getDatabaseInstance().getCode();
            }
        });

    private final AbstractColumnDefinitionKind<EntityType> columnDefinitionKind;

    private EntityTypeColDefKind(AbstractColumnDefinitionKind<EntityType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<EntityType> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
