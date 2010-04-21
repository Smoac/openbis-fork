/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;

/**
 * Definition of material table columns.
 * 
 * @author Izabela Adamczyk
 */
public enum CommonMaterialColDefKind implements IColumnDefinitionKind<Material>
{
    CODE(new AbstractColumnDefinitionKind<Material>(Dict.CODE)
        {
            @Override
            public String tryGetValue(Material entity)
            {
                return entity.getCode();
            }

            @Override
            public String tryGetLink(Material entity)
            {
                return LinkExtractor.tryExtract(entity);
            }

        }),

    MATERIAL_TYPE(new AbstractColumnDefinitionKind<Material>(Dict.MATERIAL_TYPE, true)
        {
            @Override
            public String tryGetValue(Material entity)
            {
                return entity.getMaterialType().getCode();
            }
        }),

    DATABASE_INSTANCE(new AbstractColumnDefinitionKind<Material>(Dict.DATABASE_INSTANCE, true)
        {
            @Override
            public String tryGetValue(Material entity)
            {
                return entity.getDatabaseInstance().getCode();
            }
        }),

    REGISTRATOR(new AbstractColumnDefinitionKind<Material>(Dict.REGISTRATOR)
        {
            @Override
            public String tryGetValue(Material entity)
            {
                return renderRegistrator(entity);
            }
        }),

    REGISTRATION_DATE(
            new AbstractColumnDefinitionKind<Material>(Dict.REGISTRATION_DATE, 200, false)
                {
                    @Override
                    public String tryGetValue(Material entity)
                    {
                        return renderRegistrationDate(entity);
                    }
                });

    private final AbstractColumnDefinitionKind<Material> columnDefinitionKind;

    private CommonMaterialColDefKind(AbstractColumnDefinitionKind<Material> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<Material> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
