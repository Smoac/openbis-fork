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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ClassUtils;
import org.hibernate.search.annotations.Indexed;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * A {@link IIndexedEntityFinder} based on a package name specified in the constructor.
 * <p>
 * This does not work recursively and expects to find all the correctly annotated classes in the
 * given package.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class PackageBasedIndexedEntityFinder implements IIndexedEntityFinder
{
    private final String basePackageName;

    private final File basePackageFile;

    private Set<Class<?>> indexedEntities;

    PackageBasedIndexedEntityFinder(final String basePackage)
    {
        assert basePackage != null : "Unspecified base package.";
        this.basePackageName = removeLastDot(basePackage);
        this.basePackageFile = getBasePackageFile(this.basePackageName);
    }

    private final static String removeLastDot(final String basePackage)
    {
        if (basePackage.endsWith("."))
        {
            return basePackage.substring(0, basePackage.length() - 1);
        } else
        {
            return basePackage;
        }
    }

    private final static File getBasePackageFile(final String basePackage)
    {
        try
        {
            final URL url =
                    PackageBasedIndexedEntityFinder.class.getResource("/"
                            + basePackage.replace(".", "/"));
            if (url == null)
            {
                throw new IllegalArgumentException(String.format(
                        "Given package '%s' does not exist.", basePackage));
            }
            return new File(url.toURI());
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private final List<String> getClassFileNames()
    {
        final Collection<File> classes = FileUtils.listFiles(basePackageFile, new String[]
            { "class" }, false);
        final List<String> classNames = new ArrayList<String>(classes.size());
        for (final File classFile : classes)
        {
            classNames.add(FilenameUtils.getBaseName(classFile.getName()));
        }
        return classNames;
    }

    //
    // IIndexedEntityFinder
    //

    public final Set<Class<?>> getIndexedEntities()
    {
        if (indexedEntities == null)
        {
            final List<String> classNames = getClassFileNames();
            final Set<Class<?>> set = new HashSet<Class<?>>();
            for (final String className : classNames)
            {
                try
                {
                    final String fullClassName = basePackageName + "." + className;
                    Class<?> clazz = ClassUtils.getClass(fullClassName, false);
                    if (clazz.isAnnotationPresent(Indexed.class))
                    {
                        set.add(clazz);
                    }
                } catch (final ClassNotFoundException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
            indexedEntities = set;
        }
        return indexedEntities;
    }
}
