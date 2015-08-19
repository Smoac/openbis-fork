/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;

/**
 * @author pkupczyk
 */
@SuppressWarnings({ "rawtypes", "unchecked", "cast" })
public class SortAndPage
{

    private Set processed = new HashSet();

    public <T, C extends Collection<T>> C sortAndPage(C objects, FetchOptions fo)
    {
        C newObjects = objects;
        newObjects = (C) sort(newObjects, fo);
        newObjects = (C) page(newObjects, fo);
        nest(newObjects, fo);

        return newObjects;
    }

    private Collection sort(Collection objects, FetchOptions fo)
    {
        if (objects == null || objects.isEmpty())
        {
            return objects;
        }

        boolean hasSorting = fo.getSortBy() != null && false == fo.getSortBy().getSortings().isEmpty();

        if (hasSorting)
        {
            Comparator comparator = getComparator(fo);
            Collection sorted = null;

            if (objects instanceof List)
            {
                sorted = new ArrayList();
                sorted.addAll(objects);
                Collections.sort((List) sorted, comparator);
            } else if (objects instanceof Set)
            {
                sorted = new TreeSet(comparator);
                sorted.addAll(objects);
            } else if (objects instanceof Collection)
            {
                sorted = new ArrayList();
                sorted.addAll(objects);
                Collections.sort((List) sorted, comparator);
            }

            return sorted;
        } else
        {
            return objects;
        }
    }

    private Collection page(Collection objects, FetchOptions fo)
    {
        if (objects == null || objects.isEmpty())
        {
            return objects;
        }

        boolean hasPaging = fo.getFrom() != null || fo.getCount() != null;

        if (hasPaging)
        {
            Collection paged = null;

            if (objects instanceof List)
            {
                paged = new ArrayList();
            } else if (objects instanceof Set)
            {
                paged = new LinkedHashSet();
            } else
            {
                throw new IllegalArgumentException("Unsupported collection: " + objects.getClass());
            }

            Integer from = fo.getFrom();
            Integer count = fo.getCount();

            if (from != null && count != null)
            {
                int index = 0;
                for (Object item : objects)
                {
                    if (index >= from && index < from + count)
                    {
                        paged.add(item);
                    }
                    index++;
                }
            }

            return paged;
        } else
        {
            return objects;
        }
    }

    private void nest(Collection objects, FetchOptions fo)
    {
        if (objects == null || objects.isEmpty())
        {
            return;
        }

        try
        {
            for (Object object : objects)
            {
                if (processed.contains(object))
                {
                    continue;
                } else
                {
                    processed.add(object);
                }

                // TODO find the methods only once for given class instead of doing it for each object
                for (Method method : fo.getClass().getMethods())
                {
                    if (method.getName().startsWith("has") && false == method.getName().equals("hashCode"))
                    {
                        String field = method.getName().substring(3);
                        boolean has = (Boolean) method.invoke(fo);

                        if (has)
                        {
                            Method withMethod = fo.getClass().getMethod("with" + field);
                            FetchOptions subFo = (FetchOptions) withMethod.invoke(fo);

                            Method getMethod = object.getClass().getMethod("get" + field);
                            Method setMethod = object.getClass().getMethod("set" + field, getMethod.getReturnType());

                            Object value = getMethod.invoke(object);

                            if (value != null)
                            {
                                if (value instanceof Collection)
                                {
                                    Collection newValue = sortAndPage((Collection) value, subFo);
                                    setMethod.invoke(object, newValue);
                                } else if (value instanceof Map)
                                {
                                    sortAndPage(((Map) value).values(), subFo);
                                } else
                                {
                                    Collection newValue = sortAndPage(Collections.singleton(value), subFo);
                                    setMethod.invoke(object, newValue.iterator().next());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected Comparator getComparator(FetchOptions fetchOptions)
    {
        if (fetchOptions == null)
        {
            return null;
        }

        SortOptions sortBy = (SortOptions) fetchOptions.getSortBy();

        if (sortBy != null)
        {
            final List<Sorting> sortings = sortBy.getSortings();
            if (sortings != null && sortings.size() > 0)
            {
                final Comparator[] comparators = new Comparator[sortings.size()];
                final int[] directions = new int[sortings.size()];

                int index = 0;
                for (Sorting sorting : sortings)
                {
                    Comparator aComparator = sortBy.getComparator(sorting.getField());
                    if (aComparator == null)
                    {
                        throw new IllegalArgumentException("Comparator for field " + sorting.getField() + " not found");
                    }

                    comparators[index] = aComparator;
                    directions[index] = sorting.getOrder().isAsc() ? 1 : -1;
                    index++;
                }

                return new Comparator()
                    {
                        @Override
                        public int compare(Object o1, Object o2)
                        {
                            for (int i = 0; i < sortings.size(); i++)
                            {
                                Comparator c = comparators[i];
                                int d = directions[i];

                                int result = d * c.compare(o1, o2);
                                if (result != 0)
                                {
                                    return result;
                                }
                            }
                            return 0;
                        }
                    };
            }
        }
        return null;
    }

}
