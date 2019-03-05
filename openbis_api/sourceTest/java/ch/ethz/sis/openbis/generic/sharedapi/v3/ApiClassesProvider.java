/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.sharedapi.v3;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.testng.Assert;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloadResult;
import ch.ethz.sis.openbis.generic.dssapi.v3.fastdownload.FastDownloader;

/**
 * @author pkupczyk
 */
public class ApiClassesProvider
{

    private static final String[] PUBLIC_PACKAGES = {
            "ch.ethz.sis.openbis.generic.dssapi.v3",
            "ch.ethz.sis.openbis.generic.asapi.v3"
    };

    private static final Set<Class<?>> NON_SERIALIZABLE_CLASSES = 
            new HashSet<>(Arrays.asList(FastDownloader.class, FastDownloadResult.class));;

    public static Collection<Class<?>> getPublicClasses()
    {
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.getContextClassLoader());
        classLoadersList.add(ClasspathHelper.getStaticClassLoader());

        SubTypesScanner subTypesScanner = new SubTypesScanner();
        subTypesScanner.filterResultsBy(new FilterBuilder().include(".*"));

        FilterBuilder filterBuilder = new FilterBuilder();
        for (String v3PublicPackage : PUBLIC_PACKAGES)
        {
            filterBuilder.include(FilterBuilder.prefix(v3PublicPackage));
        }

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(subTypesScanner, new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(filterBuilder));

        Multimap<String, String> map = reflections.getStore().get(SubTypesScanner.class);

        Collection<String> nonInnerClassesAndTestClasses = Collections2.filter(map.values(), new Predicate<String>()
            {

                @Override
                public boolean apply(String item)
                {
                    return false == (item.contains("$") || item.endsWith("Test"));
                }
            });
        Collection<String> uniqueClassNames = new TreeSet<String>(nonInnerClassesAndTestClasses);
        Collection<Class<?>> uniqueClasses = ImmutableSet.copyOf(ReflectionUtils.forNames(uniqueClassNames))
                .stream().filter(c -> Modifier.isPublic(c.getModifiers())).collect(Collectors.toList());
        Set<Class<?>> nonSerializableConcreteClasses = new HashSet<Class<?>>();

        for (Class<?> uniqueClass : uniqueClasses)
        {
            System.out.println("Found V3 public class:\t" + uniqueClass.getName());

            if (false == Modifier.isAbstract(uniqueClass.getModifiers())
                    && false == Serializable.class.isAssignableFrom(uniqueClass)
                    && false == NON_SERIALIZABLE_CLASSES.contains(uniqueClass))
            {
                nonSerializableConcreteClasses.add(uniqueClass);
            }
        }

        System.out.println();

        if (false == nonSerializableConcreteClasses.isEmpty())
        {
            Assert.fail("Non serializable classes found:\n" + StringUtils.join(nonSerializableConcreteClasses, ",\n"));
        }

        return uniqueClasses;
    }

    public static Collection<Field> getPublicFields(Class<?> clazz)
    {
        Collection<Field> fields = new ArrayList<Field>();
        for (Field field : clazz.getDeclaredFields())
        {
            if (Modifier.isPublic(field.getModifiers()))
            {
                fields.add(field);
            }
        }
        return fields;
    }

    public static Collection<Method> getPublicMethods(Class<?> clazz)
    {
        Collection<Method> methods = new ArrayList<Method>();

        for (Method method : clazz.getDeclaredMethods())
        {
            if (Modifier.isPublic(method.getModifiers()))
            {
                methods.add(method);
            }
        }
        return methods;
    }

}
