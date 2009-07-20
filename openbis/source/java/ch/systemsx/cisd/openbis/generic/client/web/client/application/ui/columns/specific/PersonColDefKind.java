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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * @author Piotr Buczek
 */
public enum PersonColDefKind implements IColumnDefinitionKind<Person>
{
    USER_ID(new AbstractColumnDefinitionKind<Person>(Dict.USER_ID)
        {
            @Override
            public String tryGetValue(Person entity)
            {
                return entity.getUserId();
            }
        }),

    FIRST_NAME(new AbstractColumnDefinitionKind<Person>(Dict.FIRST_NAME)
        {
            @Override
            public String tryGetValue(Person entity)
            {
                return entity.getFirstName();
            }
        }),

    LAST_NAME(new AbstractColumnDefinitionKind<Person>(Dict.LAST_NAME)
        {
            @Override
            public String tryGetValue(Person entity)
            {
                return entity.getLastName();
            }
        }),

    EMAIL(new AbstractColumnDefinitionKind<Person>(Dict.EMAIL, 200)
        {
            @Override
            public String tryGetValue(Person entity)
            {
                return entity.getEmail();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Person>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Person entity)
            {
                return (entity.getRegistrator() == null) ? null : renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Person>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(Person entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<Person> columnDefinitionKind;

    private PersonColDefKind(AbstractColumnDefinitionKind<Person> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Person> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
