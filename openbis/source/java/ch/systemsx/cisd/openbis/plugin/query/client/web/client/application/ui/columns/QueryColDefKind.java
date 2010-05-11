/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.ui.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleYesNoRenderer;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;

/**
 * @author Franz-Josef Elmer
 */
public enum QueryColDefKind implements IColumnDefinitionKind<QueryExpression>
{
    NAME(new AbstractColumnDefinitionKind<QueryExpression>(Dict.NAME)
        {
            @Override
            public String tryGetValue(QueryExpression entity)
            {
                return entity.getName();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<QueryExpression>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(QueryExpression entity)
            {
                return entity.getDescription();
            }
        }),

    EXPRESSION(new AbstractColumnDefinitionKind<QueryExpression>(
            ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict.SQL_QUERY,
            true)
        {
            @Override
            public String tryGetValue(QueryExpression entity)
            {
                return entity.getExpression();
            }
        }),

    PUBLIC(new AbstractColumnDefinitionKind<QueryExpression>(Dict.IS_PUBLIC, true)
        {
            @Override
            public String tryGetValue(QueryExpression entity)
            {
                return SimpleYesNoRenderer.render(entity.isPublic());
            }
        }),

    QUERY_TYPE(new AbstractColumnDefinitionKind<QueryExpression>(
            ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict.QUERY_TYPE,
            true)
        {
            @Override
            public String tryGetValue(QueryExpression entity)
            {
                return entity.getQueryType().name();
            }
        }),

    QUERY_DATABASE(
            new AbstractColumnDefinitionKind<QueryExpression>(
                    ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict.QUERY_DATABASE,
                    true)
                {
                    @Override
                    public String tryGetValue(QueryExpression entity)
                    {
                        return entity.getQueryDatabaseLabel();
                    }
                }),

    REGISTRATOR(new AbstractColumnDefinitionKind<QueryExpression>(Dict.REGISTRATOR, true)
        {
            @Override
            public String tryGetValue(QueryExpression entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<QueryExpression>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, true)
        {
            @Override
            public String tryGetValue(QueryExpression entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<QueryExpression> columnDefinitionKind;

    private QueryColDefKind(AbstractColumnDefinitionKind<QueryExpression> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<QueryExpression> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
