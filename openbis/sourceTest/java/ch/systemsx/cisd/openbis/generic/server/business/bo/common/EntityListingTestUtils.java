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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = CodeRecord.class)
public class EntityListingTestUtils
{
    public static <T extends BaseQuery> T createQuery(IDAOFactory daoFactory, Class<T> clazz)
    {
        DatabaseConfigurationContext context = DatabaseContextUtils.getDatabaseContext(daoFactory);
        T query = QueryTool.getQuery(context.getDataSource(), clazz);
        return query;
    }

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

    public static <T> Set<T> asSet(Iterable<T> items)
    {
        Set<T> result = new HashSet<T>();
        for (T item : items)
        {
            result.add(item);
        }
        return result;
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

    public static LongSet createSet(long... values)
    {
        return new LongOpenHashSet(values);
    }

    public static void assertRecursiveEqual(Object o1, Object o2)
    {
        String errMsg =
                "objects not equal: " + ReflectionToStringBuilder.toString(o1) + " and "
                        + ReflectionToStringBuilder.toString(o2);
        assertTrue(errMsg, EqualsBuilder.reflectionEquals(o2, o1));
    }

    // --- generic helpers for entity properties tests ----------------

    public static void checkPropertiesGenericValues(long entityId,
            DataIterator<GenericEntityPropertyRecord> properties)
    {
        assertTrue("no generic properties found", properties.hasNext());
        for (GenericEntityPropertyRecord property : properties)
        {
            assertNotNull(property.value);
            assertEquals(entityId, property.entity_id);
        }
    }

    public static void checkPropertiesVocabularyTermValues(long entityId,
            DataIterator<VocabularyTermRecord> properties)
    {
        assertTrue("no vocabulary properties found", properties.hasNext());
        for (VocabularyTermRecord property : properties)
        {
            assertNotNull(property.code);
            assertEquals(entityId, property.entity_id);
        }
    }

    public static void checkPropertiesMaterialValues(long entityId,
            DataIterator<MaterialEntityPropertyRecord> properties)
    {
        assertTrue("no material properties found", properties.hasNext());
        for (MaterialEntityPropertyRecord property : properties)
        {
            assertNotNull(property.code);
            assertEquals(entityId, property.entity_id);
        }
    }

    public static long getAnyEntityId(List<? extends BaseEntityPropertyRecord> properties)
    {
        return properties.iterator().next().entity_id;
    }

}
