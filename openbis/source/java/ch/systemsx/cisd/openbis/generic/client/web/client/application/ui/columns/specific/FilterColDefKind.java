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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Filter;

/**
 * Column definitions for the filters.
 * 
 * @author Izabela Adamczyk
 */
public enum FilterColDefKind implements IColumnDefinitionKind<Filter>
{
    NAME(new AbstractColumnDefinitionKind<Filter>(Dict.NAME)
        {
            @Override
            public String tryGetValue(Filter entity)
            {
                return entity.getName();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<Filter>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(Filter entity)
            {
                return entity.getDescription();
            }
        }),

    EXPRESSION(new AbstractColumnDefinitionKind<Filter>(Dict.EXPRESSION, true)
        {
            @Override
            public String tryGetValue(Filter entity)
            {
                return entity.getExpression();
            }
        }),

    PUBLIC(new AbstractColumnDefinitionKind<Filter>(Dict.IS_PUBLIC, true)
        {
            @Override
            public String tryGetValue(Filter entity)
            {
                return SimpleYesNoRenderer.render(entity.isPublic());
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Filter>(Dict.REGISTRATOR, true)
        {
            @Override
            public String tryGetValue(Filter entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Filter>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, true)
        {
            @Override
            public String tryGetValue(Filter entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<Filter> columnDefinitionKind;

    private FilterColDefKind(AbstractColumnDefinitionKind<Filter> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Filter> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
