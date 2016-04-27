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
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.StringTokenizer;

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
public class IndexingQueryOptimizer
{

    private static int DEFAULT_MAX_DEPTH = 4;

    private IndexingQueryOptimizer()
    {
    }

    /**
     * Modifies a Criteria instance to optimize database queries that are used to do full text indexing. Sets fetch mode to FetchMode.JOIN for all
     * properties of an entity class that are either annotated with IndexedEmbedded or they are eagerly fetched associations annotated with OneToOne,
     * ManyToOne, OneToMany or ManyToMany.
     * 
     * @param clazz Entity class whose instances are to be queried
     * @param criteria Criteria query to be optimized
     * @param maxDepth Maximum depth of recursive properties to set FetchMode to
     * @return Same criteria instance that was given as a parameter, but with new fetch modes.
     */
    public static Criteria minimizeAmountOfSubqueries(Class<?> clazz, Criteria criteria,
            int maxDepth)
    {
        minimizeAmountOfSubqueries(clazz, criteria, maxDepth, "");
        return criteria;
    }

    public static Criteria minimizeAmountOfSubqueries(Class<?> clazz, Criteria criteria)
    {
        minimizeAmountOfSubqueries(clazz, criteria, DEFAULT_MAX_DEPTH, "");
        return criteria;
    }

    private static void minimizeAmountOfSubqueries(Class<?> clazz, Criteria criteria, int maxDepth,
            String path)
    {
        Class<?> iterClass = clazz;
        while (iterClass != null)
        {
            for (Method method : iterClass.getDeclaredMethods())
            {
                if (!method.getReturnType().equals(iterClass))
                {
                    addFetchMode(method, criteria, maxDepth, path);
                }
            }

            iterClass = iterClass.getSuperclass();
        }
    }

    private static void addFetchMode(Method method, Criteria criteria, int maxDepth, String path)
    {
        if (propertyNeedsJoinedFetch(method))
        {
            String newPath = calculatePath(path, method);

            if (depthOf(newPath) > maxDepth)
            {
                return;
            }

            System.out.println("fetch: " + newPath);
            criteria.setFetchMode(newPath, FetchMode.JOIN);

            Class<?> clazz = method.getReturnType();
            if (isCollection(clazz))
            {
                ParameterizedType t = (ParameterizedType) method.getGenericReturnType();
                clazz = (Class<?>) t.getActualTypeArguments()[0];
            }

            minimizeAmountOfSubqueries(clazz, criteria, maxDepth, newPath);
        }
    }

    private static boolean isCollection(Class<?> clazz)
    {
        for (Class<?> c : clazz.getInterfaces())
        {
            if (c.equals(Collection.class))
            {
                return true;
            }
        }
        return false;
    }

    private static int depthOf(String path)
    {
        StringTokenizer tokenizer = new StringTokenizer(path, ".");
        return tokenizer.countTokens();
    }

    private static boolean propertyNeedsJoinedFetch(Method method)
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
