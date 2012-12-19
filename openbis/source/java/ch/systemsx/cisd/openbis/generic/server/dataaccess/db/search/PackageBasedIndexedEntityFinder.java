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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.search.annotations.Indexed;

import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.reflection.IClassFilter;

/**
 * A {@link IIndexedEntityFinder} based on a package name specified in the constructor. Classes
 * ending with <code>Test</code> will be ignored.
 * <p>
 * This does not work recursively and expects to find all the correctly annotated classes in the
 * given package.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class PackageBasedIndexedEntityFinder implements IIndexedEntityFinder
{
    private Set<Class<?>> indexedEntities;

    public PackageBasedIndexedEntityFinder(final String packageName)
    {
        assert packageName != null : "Unspecified package name.";
        indexedEntities = createIndexedEntities(packageName);
    }

    private HashSet<Class<?>> createIndexedEntities(final String packageName)
    {
        final List<Class<?>> classes = ClassUtils.listClasses(packageName, new IClassFilter()
            {

                //
                // IClassFilter
                //

                @Override
                public boolean accept(String fullyQualifiedClassName)
                {
                    return fullyQualifiedClassName.endsWith("Test") == false;
                }

                @Override
                public final boolean accept(final Class<?> clazz)
                {
                    if (clazz.isAnnotationPresent(Indexed.class))
                    {
                        if (clazz.getSuperclass() != null && accept(clazz.getSuperclass()))
                        {
                            // don't index data sets twice (for DataPE and for ExternalDataPE)
                            return false;
                        } else
                        {
                            return true;
                        }
                    } else
                    {
                        return false;
                    }
                }
            });
        return new HashSet<Class<?>>(classes);
    }

    //
    // IIndexedEntityFinder
    //

    @Override
    public final Set<Class<?>> getIndexedEntities()
    {
        return indexedEntities;
    }
}
