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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;

/**
 * @author Piotr Buczek
 */
public enum RoleAssignmentColDefKind implements IColumnDefinitionKind<RoleAssignment>
{
    PERSON(new AbstractColumnDefinitionKind<RoleAssignment>(Dict.PERSON)
        {
            @Override
            public String tryGetValue(RoleAssignment entity)
            {
                Person person = entity.getPerson();
                return person == null ? "" : person.getUserId();
            }
        }),

    AUTHORIZATION_GROUP(new AbstractColumnDefinitionKind<RoleAssignment>(Dict.AUTHORIZATION_GROUP)
        {
            @Override
            public String tryGetValue(RoleAssignment entity)
            {
                AuthorizationGroup group = entity.getAuthorizationGroup();
                return group == null ? "" : group.getCode();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<RoleAssignment>(Dict.GROUP)
        {
            @Override
            public String tryGetValue(RoleAssignment entity)
            {
                final Space group = entity.getSpace();
                return group == null ? "" : group.getCode();
            }
        }),

    ROLE(new AbstractColumnDefinitionKind<RoleAssignment>(Dict.ROLE)
        {
            @Override
            public String tryGetValue(RoleAssignment entity)
            {
                return entity.getCode();
            }
        }),

    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<RoleAssignment>(Dict.DATABASE_INSTANCE)
        {
            @Override
            public String tryGetValue(RoleAssignment entity)
            {
                DatabaseInstance databaseInstance = entity.getInstance();
                if (databaseInstance == null)
                {
                    databaseInstance = entity.getSpace().getInstance();
                }
                return databaseInstance.getCode();
            }
        });

    private final AbstractColumnDefinitionKind<RoleAssignment> columnDefinitionKind;

    private RoleAssignmentColDefKind(
            AbstractColumnDefinitionKind<RoleAssignment> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<RoleAssignment> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
