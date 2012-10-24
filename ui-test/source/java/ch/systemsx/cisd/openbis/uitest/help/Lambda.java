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

package ch.systemsx.cisd.openbis.uitest.help;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Lambda<T, U>
{
    public abstract U apply(T input);

    public static <T, U> List<U> foreach(List<T> input, Lambda<T, U> lambda)
    {
        List<U> output = new ArrayList<U>();
        for (T t : input)
        {
            output.add(lambda.apply(t));
        }
        return output;
    }

    public static <T, U> Set<U> foreach(Set<T> input, Lambda<T, U> lambda)
    {
        Set<U> output = new HashSet<U>();
        for (T t : input)
        {
            output.add(lambda.apply(t));
        }
        return output;
    }

}
