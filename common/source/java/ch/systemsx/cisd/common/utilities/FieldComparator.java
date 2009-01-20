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
 * @deprecated Avoid using this class. It requires to specify class field name, which reduces
 *             benefits of strong typing.
 * @author Christian Ribeaud
 */
@Deprecated
public class FieldComparator<T> implements Comparator<T>
{

    private final static char DEFAULT_SEPARATOR = '.';

    private final String classFieldName;

    private final Map<MapKey, Field> cache = new HashMap<MapKey, Field>();

    private final char separator;

    public FieldComparator(final String classFieldName)
    {
        this(classFieldName, DEFAULT_SEPARATOR);
    }

    public FieldComparator(final String classFieldName, final char separator)
    {
        assert classFieldName != null : "Unspecified field name.";
        this.classFieldName = classFieldName;
        this.separator = separator;
    }

    private final Object getField(final Object object, final String fieldName)
    {
        assert object != null : "Unspecified object.";
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

    private final Comparable<Object> tryGetComparable(final T t)
    {
        final String[] fieldNames = StringUtils.split(classFieldName, separator);
        Object fieldValue = t;
        for (int i = 0; i < fieldNames.length && fieldValue != null; i++)
        {
            fieldValue = getField(fieldValue, fieldNames[i]);
        }
        if (fieldValue == null)
        {
            return null;
        }
        if (fieldValue instanceof Comparable == false)
        {
            throw new IllegalArgumentException(String.format(
                    "Class '%s' does not implement the Comparable interface.", fieldValue
                            .getClass().getName()));
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
        final Comparable<Object> comparable1 = tryGetComparable(o1);
        final Comparable<Object> comparable2 = tryGetComparable(o2);
        if (comparable1 == null)
        {
            return comparable2 == null ? 0 : -1;
        }
        if (comparable2 == null)
        {
            return 1;
        }
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
