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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Utility class containing methods related to properties.
 * 
 * @author Izabela Adamczyk
 */
public class EntityPropertyUtils
{
    public static List<PropertyType> extractTypes(List<IEntityProperty> properties)
    {
        ArrayList<PropertyType> types = new ArrayList<PropertyType>();
        for (IEntityProperty p : properties)
        {
            types.add(p.getPropertyType());
        }
        return types;
    }
}
