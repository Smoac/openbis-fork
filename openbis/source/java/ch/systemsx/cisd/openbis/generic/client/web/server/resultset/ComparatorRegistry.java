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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


import ch.systemsx.cisd.common.utilities.FieldComparator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/**
 * A registry of {@link Comparator} implementation used by {@link IResultSetManager} implementation
 * to sort the data.
 * 
 * @author Christian Ribeaud
 */
final class ComparatorRegistry
{
    private final Map<Class<?>, IFieldComparator<?>> comparators =
            new HashMap<Class<?>, IFieldComparator<?>>();

    ComparatorRegistry()
    {
        comparators.put(Sample.class, new SampleComparator());
        // Can not be instantiated.
    }

    private final <T> Comparator<T> getDefaultComparator(final String fieldName)
    {
        return new FieldComparator<T>(fieldName, '_');
    }

    @SuppressWarnings("unchecked")
    private final <T> Comparator<T> cast(final IFieldComparator<?> comparator)
    {
        return (Comparator<T>) comparator;
    }

    final <T> Comparator<T> getComparator(final Class<T> clazz, final String fieldName)
    {
        assert clazz != null : "Unspecified class.";
        assert fieldName != null : "Unspecified field name.";
        final IFieldComparator<?> comparator = comparators.get(clazz);
        if (comparator == null)
        {
            return getDefaultComparator(fieldName);
        }
        comparator.setFieldName(fieldName);
        return cast(comparator);
    }
}
