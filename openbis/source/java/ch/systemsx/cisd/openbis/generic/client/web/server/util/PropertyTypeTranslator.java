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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author Izabela Adamczyk
 */
public class PropertyTypeTranslator
{

    public static PropertyType translate(PropertyTypePE propertyType)
    {
        final PropertyType result = new PropertyType();
        result.setSimpleCode(propertyType.getSimpleCode());
        result.setInternalNamespace(propertyType.isInternalNamespace());
        result.setLabel(propertyType.getLabel());
        return result;
    }

    public static PropertyTypePE translate(final PropertyType propertyType)
    {
        final PropertyTypePE result = new PropertyTypePE();
        result.setSimpleCode(propertyType.getSimpleCode());
        result.setInternalNamespace(propertyType.isInternalNamespace());
        result.setLabel(propertyType.getLabel());
        return result;
    }

    public static List<PropertyTypePE> translate(List<PropertyType> propertyCodes)
    {
        List<PropertyTypePE> result = new ArrayList<PropertyTypePE>();
        for (PropertyType s : propertyCodes)
        {
            result.add(translate(s));
        }
        return result;
    }

}
