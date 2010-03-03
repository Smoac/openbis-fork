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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * @author Piotr Buczek
 */
public enum GroupColDefKind implements IColumnDefinitionKind<Space>
{
    CODE(new AbstractColumnDefinitionKind<Space>(Dict.CODE)
        {
            @Override
            public String tryGetValue(Space entity)
            {
                return entity.getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<Space>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(Space entity)
            {
                return entity.getDescription();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Space>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Space entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Space>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(Space entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<Space> columnDefinitionKind;

    private GroupColDefKind(AbstractColumnDefinitionKind<Space> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Space> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
