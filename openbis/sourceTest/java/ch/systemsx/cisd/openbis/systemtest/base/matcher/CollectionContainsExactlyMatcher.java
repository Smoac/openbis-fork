/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.base.matcher;

import java.util.Collection;
import java.util.HashSet;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import ch.systemsx.cisd.openbis.systemtest.base.EqualityChecker;

public class CollectionContainsExactlyMatcher<T> extends TypeSafeMatcher<Collection<T>>
{

    private EqualityChecker<T> equalityChecker;

    private Collection<T> expected;

    public CollectionContainsExactlyMatcher(EqualityChecker<T> equalityChecker, T... elements)
    {
        this.equalityChecker = equalityChecker;
        expected = new HashSet<T>();
        for (T t : elements)
        {
            expected.add(t);
        }
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("A collection containing exactly these elements: " + expected);
    }

    @Override
    public boolean matchesSafely(Collection<T> actual)
    {
        if (actual == null)
        {
            return false;
        }

        if (actual.size() != expected.size())
        {
            return false;
        }

        for (T t : actual)
        {
            int count = 0;
            for (T e : this.expected)
            {
                if (this.equalityChecker.equals(t, e))
                {
                    count++;
                }
            }
            if (count != 1)
            {
                return false;
            }
        }
        return true;
    }

}