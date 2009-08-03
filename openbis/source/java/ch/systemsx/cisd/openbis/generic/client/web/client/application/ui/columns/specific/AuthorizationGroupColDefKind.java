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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;

/**
 * @author Izabela Adamczyk
 */
public enum AuthorizationGroupColDefKind implements IColumnDefinitionKind<AuthorizationGroup>
{
    CODE(new AbstractColumnDefinitionKind<AuthorizationGroup>(Dict.CODE)
        {
            @Override
            public String tryGetValue(AuthorizationGroup entity)
            {
                return entity.getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<AuthorizationGroup>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(AuthorizationGroup entity)
            {
                return entity.getDescription();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<AuthorizationGroup>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(AuthorizationGroup entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<AuthorizationGroup>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(AuthorizationGroup entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<AuthorizationGroup> columnDefinitionKind;

    private AuthorizationGroupColDefKind(
            AbstractColumnDefinitionKind<AuthorizationGroup> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<AuthorizationGroup> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
