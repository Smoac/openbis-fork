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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * @author Tomasz Pylak
 */
public enum ProjectColDefKind implements IColumnDefinitionKind<Project>
{
    CODE(new AbstractColumnDefinitionKind<Project>(Dict.CODE)
        {
            @Override
            public String tryGetValue(Project entity)
            {
                return entity.getCode();
            }
        }),

    GROUP(new AbstractColumnDefinitionKind<Project>(Dict.GROUP)
        {
            @Override
            public String tryGetValue(Project entity)
            {
                return entity.getSpace().getCode();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<Project>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(Project entity)
            {
                return entity.getDescription();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Project>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Project entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Project>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(Project entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<Project> columnDefinitionKind;

    private ProjectColDefKind(AbstractColumnDefinitionKind<Project> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Project> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
