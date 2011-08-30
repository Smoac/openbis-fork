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
import ch.systemsx.cisd.openbis.generic.shared.basic.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

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

    LABEL(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.LABEL, true)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return entity.getPropertyType().getLabel();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.DESCRIPTION, true)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return entity.getPropertyType().getDescription();
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
        }),

    DATA_TYPE(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.DATA_TYPE, 200)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return PropertyTypeColDefKind.renderDataType(entity.getPropertyType());
            }
        }),

    ORDINAL(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.ORDINAL, 100, true)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return entity.getOrdinal().toString();
            }
        }),

    SECTION(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.SECTION)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return entity.getSection();
            }
        }),

    IS_DYNAMIC(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.IS_DYNAMIC)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return SimpleYesNoRenderer.render(entity.isDynamic());
            }
        }),

    IS_MANAGED(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.IS_MANAGED)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return SimpleYesNoRenderer.render(entity.isManaged());
            }
        }),

    SCRIPT(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(Dict.SCRIPT)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                Script script = entity.getScript();
                if (script != null)
                {
                    return script.getName();
                }
                return null;
            }
        }),

    IS_SHOWN_IN_EDIT_VIEW(new AbstractColumnDefinitionKind<EntityTypePropertyType<?>>(
            Dict.IS_SHOWN_IN_EDIT_VIEW)
        {
            @Override
            public String tryGetValue(EntityTypePropertyType<?> entity)
            {
                return SimpleYesNoRenderer.render(entity.isShownInEditView());
            }
        }),

    ;

    private final AbstractColumnDefinitionKind<EntityTypePropertyType<?>> columnDefinitionKind;

    private PropertyTypeAssignmentColDefKind(
            AbstractColumnDefinitionKind<EntityTypePropertyType<?>> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<EntityTypePropertyType<?>> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
