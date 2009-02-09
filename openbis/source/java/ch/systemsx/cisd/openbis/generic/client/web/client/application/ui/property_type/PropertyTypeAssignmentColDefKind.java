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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.renderer.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * @author Tomasz Pylak
 */
public enum PropertyTypeAssignmentColDefKind implements
        IColumnDefinitionKind<EntityTypePropertyType<?>>
{
    PROPERTY_TYPE_CODE(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(
            Dict.PROPERTY_TYPE_CODE, 200)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return entity.getPropertyType().getCode();
            }
        }),

    ENTITY_TYPE_CODE(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.ASSIGNED_TO,
            200)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return entity.getEntityType().getCode();
            }
        }),

    ENTITY_KIND(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.TYPE_OF)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return entity.getEntityKind().getDescription();
            }
        }),

    IS_MANDATORY(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.IS_MANDATORY)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return SimpleYesNoRenderer.render(entity.isMandatory());
            }
        });

    private final AbstractColumnDefinitionKind<EntityTypePropertyType<?>> columnDefinitionKind;

    private PropertyTypeAssignmentColDefKind(
            AbstractColumnDefinitionKind<EntityTypePropertyType<?>> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String getHeaderMsgKey()
    {
        return columnDefinitionKind.getHeaderMsgKey();
    }

    public int getWidth()
    {
        return columnDefinitionKind.getWidth();
    }

    public String id()
    {
        return name();
    }

    public boolean isHidden()
    {
        return columnDefinitionKind.isHidden();
    }

    public String tryGetValue(EntityTypePropertyType<?> entity)
    {
        return columnDefinitionKind.tryGetValue(entity);
    }
}
