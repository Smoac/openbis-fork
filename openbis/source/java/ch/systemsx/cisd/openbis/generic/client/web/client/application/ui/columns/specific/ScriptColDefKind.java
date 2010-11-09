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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * @author Izabela Adamczyk
 */
public enum ScriptColDefKind implements IColumnDefinitionKind<Script>
{
    NAME(new AbstractColumnDefinitionKind<Script>(Dict.NAME)
        {
            @Override
            public String tryGetValue(Script entity)
            {
                return entity.getName();
            }
        }),

    DESCRIPTION(new AbstractColumnDefinitionKind<Script>(Dict.DESCRIPTION)
        {
            @Override
            public String tryGetValue(Script entity)
            {
                return entity.getDescription();
            }
        }),

    SCRIPT(new AbstractColumnDefinitionKind<Script>(Dict.SCRIPT)
        {
            @Override
            public String tryGetValue(Script entity)
            {
                return entity.getScript();
            }
        }),

    ENTITY_KIND(new AbstractColumnDefinitionKind<Script>(Dict.ENTITY_KIND)
        {
            @Override
            public String tryGetValue(Script entity)
            {
                EntityKind kind = entity.getEntityKind();
                return kind == null ? "All" : kind.getDescription();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Script>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Script entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(new AbstractColumnDefinitionKind<Script>(Dict.REGISTRATION_DATE,
            AbstractColumnDefinitionKind.DATE_COLUMN_WIDTH, false)
        {
            @Override
            public String tryGetValue(Script entity)
            {
                return renderRegistrationDate(entity);
            }
        });

    private final AbstractColumnDefinitionKind<Script> columnDefinitionKind;

    private ScriptColDefKind(AbstractColumnDefinitionKind<Script> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Script> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
