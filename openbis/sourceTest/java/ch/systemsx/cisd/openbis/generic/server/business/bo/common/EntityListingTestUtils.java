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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses=CodeRecord.class)
public class EntityListingTestUtils
{
    public static PropertyType findPropertyType(PropertyType[] propertyTypes,
            String propertyTypeCode)
    {
        for (PropertyType propertyType : propertyTypes)
        {
            if (propertyType.getCode().equalsIgnoreCase(propertyTypeCode))
            {
                return propertyType;
            }
        }
        fail("Property type not found " + propertyTypeCode);
        return null; // for compiler
    }

    public static <T> List<T> asList(Iterable<T> items)
    {
        List<T> result = new ArrayList<T>();
        for (T item : items)
        {
            result.add(item);
        }
        return result;
    }

    public static <T extends CodeRecord> T findCode(Iterable<T> items, String code)
    {
        for (T item : items)
        {
            if (item.code.equalsIgnoreCase(code))
            {
                return item;
            }
        }
        fail("Code not found " + code);
        return null; // for compiler
    }

    public static <T extends Code<?>> T findCode(List<T> items, String code)
    {
        for (T item : items)
        {
            if (item.getCode().equalsIgnoreCase(code))
            {
                return item;
            }
        }
        fail("No sample type with the given code found " + code);
        return null; // for compiler
    }

}
