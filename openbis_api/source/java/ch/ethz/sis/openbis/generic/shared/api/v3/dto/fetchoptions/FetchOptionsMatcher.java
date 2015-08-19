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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pkupczyk
 */
public class FetchOptionsMatcher
{

    public static FetchOptionsMatchType match(Object fo1, Object fo2)
    {
        if (fo1 == fo2)
        {
            return FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING;
        }
        if (fo1 == null || fo2 == null)
        {
            return null;
        }
        if (false == fo1.getClass().equals(fo2.getClass()))
        {
            return null;
        }

        if (arePartsEqual(fo1, fo2) && areSubLevelPagingAndSortingEqual(fo1, fo2))
        {
            if (areTopLevelPagingAndSortingEqual(fo1, fo2))
            {
                return FetchOptionsMatchType.ALL_PARTS_AND_ALL_PAGING_AND_SORTING;
            } else
            {
                return FetchOptionsMatchType.ALL_PARTS_AND_SUB_PAGING_AND_SORTING;
            }
        } else
        {
            return null;
        }
    }

    private static boolean arePartsEqual(Object o1, Object o2)
    {
        return areEqual(o1, o2, new PartsMatcher());
    }

    private static boolean areTopLevelPagingAndSortingEqual(Object o1, Object o2)
    {
        return areEqual(o1, o2, new TopLevelPagingAndSortingMatcher());
    }

    private static boolean areSubLevelPagingAndSortingEqual(Object o1, Object o2)
    {
        return areEqual(o1, o2, new SubLevelPagingAndSortingMatcher());
    }

    private static boolean areEqual(Object o1, Object o2, Matcher matcher)
    {
        return areEqual(o1, o2, matcher, 0, new HashSet<Pair>());
    }

    private static boolean areEqual(Object o1, Object o2, Matcher matcher, int level, Set<Pair> checked)
    {
        Pair pair = new Pair(o1, o2);

        if (checked.contains(pair))
        {
            return true;
        } else
        {
            checked.add(pair);
        }

        try
        {
            Class<?> clazz = o1.getClass();

            for (Method method : clazz.getMethods())
            {
                if (method.getName().startsWith("has") && false == method.getName().equals("hashCode"))
                {
                    boolean has1 = (boolean) method.invoke(o1);
                    boolean has2 = (boolean) method.invoke(o2);

                    Method withMethod = clazz.getMethod("with" + method.getName().substring(3));

                    Object with1 = null;
                    Object with2 = null;

                    if (has1)
                    {
                        with1 = withMethod.invoke(o1);
                    }

                    if (has2)
                    {
                        with2 = withMethod.invoke(o2);
                    }

                    if (matcher.shouldMatch(level))
                    {
                        if (false == matcher.match(o1, o2, has1, has2, with1, with2))
                        {
                            return false;
                        }
                    }

                    if (with1 != null && with2 != null)
                    {
                        if (false == areEqual(with1, with2, matcher, level + 1, checked))
                        {
                            return false;
                        }
                    }

                }
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }

    private static interface Matcher
    {

        public boolean shouldMatch(int level);

        public boolean match(Object o1, Object o2, boolean has1, boolean has2, Object with1, Object with2);

    }

    private static class PartsMatcher implements Matcher
    {

        @Override
        public boolean shouldMatch(int level)
        {
            return true;
        }

        @Override
        public boolean match(Object o1, Object o2, boolean has1, boolean has2, Object with1, Object with2)
        {
            return has1 == has2;
        }

    }

    private static abstract class PagingAndSortingMatcher implements Matcher
    {

        @Override
        public boolean match(Object o1, Object o2, boolean has1, boolean has2, Object with1, Object with2)
        {
            FetchOptions<?> fo1 = o1 instanceof FetchOptions ? (FetchOptions<?>) o1 : null;
            FetchOptions<?> fo2 = o2 instanceof FetchOptions ? (FetchOptions<?>) o2 : null;

            if (fo1 == null ^ fo2 == null)
            {
                return false;
            }

            if (fo1 != null && fo2 != null)
            {
                // TODO compare sorting
                return areEqual(fo1.getFrom(), fo2.getFrom()) && areEqual(fo1.getCount(), fo2.getCount());
            } else
            {
                return true;
            }
        }

        private boolean areEqual(Object o1, Object o2)
        {
            return o1 == null ? o2 == null : o1.equals(o2);
        }

    }

    private static class TopLevelPagingAndSortingMatcher extends PagingAndSortingMatcher
    {

        @Override
        public boolean shouldMatch(int level)
        {
            return level == 0;
        }

    }

    private static class SubLevelPagingAndSortingMatcher extends PagingAndSortingMatcher
    {

        @Override
        public boolean shouldMatch(int level)
        {
            return level > 0;
        }

    }

    private static class Pair
    {

        public Object object1;

        public Object object2;

        public Pair(Object object1, Object object2)
        {
            this.object1 = object1;
            this.object2 = object2;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((object1 == null) ? 0 : object1.hashCode());
            result = prime * result + ((object2 == null) ? 0 : object2.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;
            if (object1 == null)
            {
                if (other.object1 != null)
                    return false;
            } else if (!object1.equals(other.object1))
                return false;
            if (object2 == null)
            {
                if (other.object2 != null)
                    return false;
            } else if (!object2.equals(other.object2))
                return false;
            return true;
        }

    }

}
