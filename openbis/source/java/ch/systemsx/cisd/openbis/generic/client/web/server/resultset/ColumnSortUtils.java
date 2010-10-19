/*
 * Copyright 2010 ETH Zuerich, CISD
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

import org.apache.commons.collections.ComparatorUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;

/**
 * Helper methods for creating comparator for values in a grid column.
 * 
 * @author Piotr Buczek
 */
class ColumnSortUtils
{

    private static final Comparator<String> alphanumComparator = new AlphanumComparator();

    static <T> Comparator<GridRowModel<T>> createComparator(final SortDir sortDir,
            final IColumnDefinition<T> sortField)
    {
        // compare code and identifier columns with a special alphanum comparator
        final Comparator<GridRowModel<T>> comparator =
                isAlphanum(sortField) ? createAlphanumComparator(sortField)
                        : createDefaultComparator(sortField);
        return applySortDir(sortDir, comparator);
    }

    //

    private static <T> boolean isAlphanum(final IColumnDefinition<T> field)
    {
        return field.getIdentifier().contains("CODE")
                || field.getIdentifier().contains("IDENTIFIER");
    }

    // TODO 2010-10-19, Piotr Buczek: extract common part with createAlphanumComparator
    @SuppressWarnings("unchecked")
    private static <T> Comparator<GridRowModel<T>> createDefaultComparator(
            final IColumnDefinition<T> sortField)
    {
        return new Comparator<GridRowModel<T>>()
            {

                public int compare(GridRowModel<T> o1, GridRowModel<T> o2)
                {
                    Comparable v1 = sortField.tryGetComparableValue(o1);
                    Comparable v2 = sortField.tryGetComparableValue(o2);
                    // treat null as minimal value
                    if (v1 == null)
                    {
                        // error messages are bigger
                        if (v2 == null)
                        {
                            String s1 = sortField.getValue(o1);
                            String s2 = sortField.getValue(o2);
                            if (s1 == null)
                            {
                                return -1;
                            } else if (s2 == null)
                            {
                                return -1;
                            } else
                            {
                                return s1.compareTo(s2);
                            }
                        } else
                        {
                            return -1;
                        }
                    } else if (v2 == null)
                    {
                        return 1;
                    } else
                    {
                        return v1.compareTo(v2);
                    }
                }

            };
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparator<GridRowModel<T>> createAlphanumComparator(
            final IColumnDefinition<T> sortField)
    {
        return new Comparator<GridRowModel<T>>()
            {

                public int compare(GridRowModel<T> o1, GridRowModel<T> o2)
                {
                    Comparable v1 = sortField.tryGetComparableValue(o1);
                    Comparable v2 = sortField.tryGetComparableValue(o2);
                    // treat null as minimal value
                    if (v1 == null)
                    {
                        // error messages are bigger
                        if (v2 == null)
                        {
                            String s1 = sortField.getValue(o1);
                            String s2 = sortField.getValue(o2);
                            if (s1 == null)
                            {
                                return -1;
                            } else if (s2 == null)
                            {
                                return -1;
                            } else
                            {
                                return s1.compareTo(s2);
                            }
                        } else
                        {
                            return -1;
                        }
                    } else if (v2 == null)
                    {
                        return 1;
                    } else
                    {
                        return alphanumComparator.compare(v1.toString(), v2.toString());
                    }
                }

            };
    }

    private static <T> Comparator<T> applySortDir(final SortDir sortDir, Comparator<T> comparator)
    {
        if (sortDir == SortDir.DESC)
        {
            return ComparatorUtils.reversedComparator(comparator);
        } else
        {
            return comparator;
        }
    }

}
