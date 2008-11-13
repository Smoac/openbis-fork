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
    private final static Map<Class<?>, IFieldComparator<?>> comparators =
            new HashMap<Class<?>, IFieldComparator<?>>();

    static
    {
        comparators.put(Sample.class, new SampleComparator());
    }

    private ComparatorRegistry()
    {
        // Can not be instantiated.
    }

    private final static <T> Comparator<T> getDefaultComparator(final String fieldName)
    {
        return new FieldComparator<T>(fieldName, '_');
    }

    @SuppressWarnings("unchecked")
    private final static <T> Comparator<T> cast(final IFieldComparator<?> comparator)
    {
        return (Comparator<T>) comparator;
    }

    final static <T> Comparator<T> getComparator(final Class<T> clazz, final String fieldName)
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

    //
    // Helper classes
    //

    /**
     * An {@link Comparator} extension which bases its sorting on a specified class field.
     * 
     * @author Christian Ribeaud
     */
    private static interface IFieldComparator<T> extends Comparator<T>
    {
        /**
         * Set the field name which determines the sorting.
         * <p>
         * Must be specified before comparison occurs.
         * </p>
         */
        void setFieldName(final String fieldName);
    }

    private final static class SampleComparator implements IFieldComparator<Sample>
    {
        private FieldComparator<Sample> fieldComparator;

        private String fieldName;

        //
        // IFieldComparator
        //

        public final void setFieldName(final String fieldName)
        {
            assert fieldName != null : "Unspecified field name.";
            this.fieldName = fieldName;
            fieldComparator = new FieldComparator<Sample>(fieldName, '_');
        }

        public final int compare(final Sample o1, final Sample o2)
        {
            assert fieldName != null : "Field name not specified.";
            if (fieldName.equals(null))
            {

            }
            return fieldComparator.compare(o1, o2);
        }
    }
}
