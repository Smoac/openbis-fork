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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKindFactory.entityTypeColDefKindFactory;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * Columns definition for browsing grid of {@link EntityType} like material or sample types.
 * 
 * @author Tomasz Pylak
 */
public enum EntityTypeColDefKind implements IColumnDefinitionKind<EntityType>
{
    CODE(entityTypeColDefKindFactory.createCodeColDefKind()),

    DESCRIPTION(entityTypeColDefKindFactory.createDescriptionColDefKind()),

    DATABASE_INSTANCE(entityTypeColDefKindFactory.createDatabaseInstanceColDefKind());

    private final AbstractColumnDefinitionKind<EntityType> columnDefinitionKind;

    private EntityTypeColDefKind(AbstractColumnDefinitionKind<EntityType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<EntityType> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
