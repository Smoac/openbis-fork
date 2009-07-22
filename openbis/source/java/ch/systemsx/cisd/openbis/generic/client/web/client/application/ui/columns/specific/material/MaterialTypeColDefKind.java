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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.material;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKindFactory.materialTypeColDefKindFactory;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * Columns definition for browsing grid of {@link MaterialType}s.
 * 
 * @author Piotr Buczek
 */
public enum MaterialTypeColDefKind implements IColumnDefinitionKind<MaterialType>
{

    // copy from EntityTypeColDefKind (cannot extend an enum)

    CODE(materialTypeColDefKindFactory.createCodeColDefKind()),

    DESCRIPTION(materialTypeColDefKindFactory.createDescriptionColDefKind()),

    DATABASE_INSTANCE(materialTypeColDefKindFactory.createDatabaseInstanceColDefKind());

    // no specific Sample Type columns

    private final AbstractColumnDefinitionKind<MaterialType> columnDefinitionKind;

    private MaterialTypeColDefKind(AbstractColumnDefinitionKind<MaterialType> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<MaterialType> getDescriptor()
    {
        return columnDefinitionKind;
    }

}
