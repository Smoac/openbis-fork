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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * @author Antti Luomi
 */
public class FetchModeCreator
{

    private FetchModeCreator()
    {
    }

    public static Criteria addFetchModes(Class<?> clazz, Criteria criteria)
    {
        addFetchModes(clazz, criteria, "", new HashSet<Class<?>>());
        return criteria;
    }

    private static void addFetchModes(Class<?> clazz, Criteria criteria, String path,
            Set<Class<?>> handledClasses)
    {
        if (handledClasses.contains(clazz))
        {
            return;
        }

        handledClasses.add(clazz);

        for (Method method : clazz.getDeclaredMethods())
        {
            addFetchMode(method, criteria, path, handledClasses);
        }
    }

    private static void addFetchMode(Method method, Criteria criteria, String path,
            Set<Class<?>> handledClasses)
    {
        if (propertyNeedsJoinedFetch(method, handledClasses))
        {
            String newPath = calculatePath(path, method);
            criteria.setFetchMode(newPath, FetchMode.JOIN);
            addFetchModes(method.getReturnType(), criteria, newPath, handledClasses);
        }
    }

    private static boolean propertyNeedsJoinedFetch(Method method, Set<Class<?>> handledClasses)
    {
        if (isAnnotatedToBeEagerlyFetched(method))
        {
            return true;
        }

        if (isAnnotatedToBeIndexedEmbedded(method))
        {
            return true;
        }

        return false;
    }

    private static boolean isAnnotatedToBeIndexedEmbedded(Method method)
    {
        return method.getAnnotation(IndexedEmbedded.class) != null;
    }

    private static boolean isAnnotatedToBeEagerlyFetched(Method method)
    {
        FetchType ft = FetchType.LAZY;

        ManyToOne mto = method.getAnnotation(ManyToOne.class);
        ManyToMany mtm = method.getAnnotation(ManyToMany.class);
        OneToOne oto = method.getAnnotation(OneToOne.class);
        OneToMany otm = method.getAnnotation(OneToMany.class);

        if (mto != null)
        {
            ft = mto.fetch();
        } else if (mtm != null)
        {
            ft = mtm.fetch();
        } else if (oto != null)
        {
            ft = oto.fetch();
        } else if (otm != null)
        {
            ft = otm.fetch();
        }

        return FetchType.EAGER.equals(ft);
    }

    private static String calculatePath(String path, Method method)
    {
        String name = method.getName();
        if (name.startsWith("get"))
        {
            if (path.length() == 0)
            {
                return getPropertyNameFromGetter(method);
            } else
            {
                return path + "." + getPropertyNameFromGetter(method);
            }
        } else
        {
            throw new IllegalArgumentException("Not a getter: " + name);
        }
    }

    private static String getPropertyNameFromGetter(Method method)
    {
        String name = method.getName().substring(3);
        String first = name.substring(0, 1).toLowerCase();
        return first + name.substring(1);
    }
}
