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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;

/**
 * Column definitions for the grid custom columns.<br>
 * It's very similar to the column definition enum for filters, but the code cannot be common
 * because enums can inherit only from interfaces.
 * 
 * @author Tomasz Pylak
 */
public enum CustomGridColumnColDefKind implements IColumnDefinitionKind<GridCustomColumn>
{
    CODE(new AbstractColumnDefinitionKind<GridCustomColumn>(Dict.CODE)
        {
            @Override
            public String tryGetValue(GridCustomColumn entity)
            {
                return entity.getCode();
            }
        }),

    NAME(new AbstractColumnDefinitionKind<GridCustomColumn>(Dict.NAME)
        {
            @Override
            public String tryGetValue(GridCustomColumn entity)
            {
                return entity.getName();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<GridCustomColumn>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(GridCustomColumn entity)
            {
                return entity.getDescription();
            }
        }),

    EXPRESSION(new AbstractColumnDefinitionKind<GridCustomColumn>(Dict.EXPRESSION, true)
        {
            @Override
            public String tryGetValue(GridCustomColumn entity)
            {
                return entity.getExpression();
            }
        }),

    PUBLIC(new AbstractColumnDefinitionKind<GridCustomColumn>(Dict.IS_PUBLIC, true)
        {
            @Override
            public String tryGetValue(GridCustomColumn entity)
            {
                return SimpleYesNoRenderer.render(entity.isPublic());
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<GridCustomColumn>(Dict.REGISTRATOR, true)
        {
            @Override
            public String tryGetValue(GridCustomColumn entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<GridCustomColumn>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, true)
        {
            @Override
            public String tryGetValue(GridCustomColumn entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<GridCustomColumn> columnDefinitionKind;

    private CustomGridColumnColDefKind(
            AbstractColumnDefinitionKind<GridCustomColumn> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<GridCustomColumn> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
