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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.EntityPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.material.CommonMaterialColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Allows to define material table row expectations.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialRow extends Row
{

    public MaterialRow(final String code)
    {
        super();
        withCell(CommonMaterialColDefKind.CODE.id(), code);
    }

    public final MaterialRow userProperty(final String propertyCode, final Object value)
    {
        return property(propertyCode, value, false);
    }

    public final MaterialRow internalProperty(final String propertyCode, final Object value)
    {
        return property(propertyCode, value, true);
    }

    private final MaterialRow property(final String propertyCode, final Object value,
            boolean internalNamespace)
    {
        final PropertyType propertyType = createPropertyType(propertyCode, internalNamespace);
        final String propertyIdentifier =
                new EntityPropertyColDef<Material>(propertyType, true).getIdentifier();
        withCell(propertyIdentifier, value);
        return this;
    }

    private final static PropertyType createPropertyType(final String propertyCode,
            boolean internalNamespace)
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setInternalNamespace(internalNamespace);
        propertyType.setSimpleCode(propertyCode);
        return propertyType;
    }

}
