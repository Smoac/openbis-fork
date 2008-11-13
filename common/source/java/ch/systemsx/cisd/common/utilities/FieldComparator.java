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

package ch.systemsx.cisd.common.utilities;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * A {@link Comparator} implementation based on an internal field specified in the constructor.
 * <p>
 * No that this field MUST implement the {@link Comparable} interface.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class FieldComparator<T> implements Comparator<T>
{
    private final String classFieldName;

    private final Map<MapKey, Field> cache = new HashMap<MapKey, Field>();

    public FieldComparator(final String classFieldName)
    {
        assert classFieldName != null : "Unspecified field name.";
        this.classFieldName = classFieldName;
    }

    private final Object getField(final Object object, final String fieldName)
    {
        final Class<?> clazz = object.getClass();
        final MapKey mapKey = new MapKey(clazz, fieldName);
        Field field = cache.get(mapKey);
        if (field == null)
        {
            field = ClassUtils.tryGetDeclaredField(clazz, fieldName);
            if (field == null)
            {
                throw new IllegalArgumentException(String.format(
                        "Field name '%s' could not be found in class '%s'.", fieldName, clazz
                                .getName()));
            }
            cache.put(mapKey, field);
        }
        try
        {
            return field.get(object);
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private final Comparable<Object> getComparable(final T t)
    {
        final String[] fieldNames = StringUtils.split(classFieldName, '.');
        Object fieldValue = t;
        for (int i = 0; i < fieldNames.length; i++)
        {
            fieldValue = getField(fieldValue, fieldNames[i]);
        }
        if (fieldValue instanceof Comparable == false)
        {
            throw new IllegalArgumentException(String.format(
                    "Object '%s' does not implement the Comparable interface.", fieldValue));
        }
        return cast(fieldValue);
    }

    @SuppressWarnings("unchecked")
    private final static Comparable<Object> cast(final Object object)
    {
        return (Comparable<Object>) object;
    }

    //
    // Comparator
    //

    public final int compare(final T o1, final T o2)
    {
        if (o1 == null)
        {
            return o2 == null ? 0 : -1;
        }
        if (o2 == null)
        {
            return 1;
        }
        final Comparable<Object> comparable1 = getComparable(o1);
        final Comparable<Object> comparable2 = getComparable(o2);
        return comparable1.compareTo(comparable2);
    }

    //
    // Helper classes
    //

    private final static class MapKey extends AbstractHashable
    {
        final Class<?> clazz;

        final String fieldName;

        MapKey(final Class<?> clazz, final String fieldName)
        {
            this.clazz = clazz;
            this.fieldName = fieldName;
        }
    }
}
