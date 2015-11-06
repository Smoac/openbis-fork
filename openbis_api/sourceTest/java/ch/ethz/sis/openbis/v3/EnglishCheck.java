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

package ch.ethz.sis.openbis.v3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * @author pkupczyk
 */
public class EnglishCheck
{

    private static final String[] PUBLIC_PACKAGES = {
            "ch.ethz.sis.openbis.generic.dss.api.v3",
            "ch.ethz.sis.openbis.generic.shared.api.v3"
    };

    @Test(enabled = false)
    public void test() throws Exception
    {
        Report report = new Report();
        report.setPrintCorrect(true);
        report.setPrintIncorrect(true);
        printClasses(report, getPublicClasses());
        Assert.assertTrue(report.isCorrect(), report.getContent());
    }

    private Collection<Class<?>> getPublicClasses()
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

        Collection<String> nonInnerClassNames = Collections2.filter(map.values(), new Predicate<String>()
            {

                @Override
                public boolean apply(String item)
                {
                    return false == item.contains("$");
                }
            });
        Collection<String> uniqueClassNames = new TreeSet<String>(nonInnerClassNames);
        return ImmutableSet.copyOf(ReflectionUtils.forNames(uniqueClassNames));
    }

    private Collection<Field> getPublicFields(Class<?> clazz)
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

    private Collection<Method> getPublicMethods(Class<?> clazz)
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

    private boolean isClassCorrect(Class<?> clazz)
    {
        return EnglishDictionary.getInstance().contains(clazz.getSimpleName());
    }

    private boolean isFieldCorrect(Field field)
    {
        return EnglishDictionary.getInstance().contains(field.getName());
    }

    private boolean isMethodCorrect(Method method)
    {
        return EnglishDictionary.getInstance().contains(method.getName());
    }

    private void printClasses(Report report, Collection<Class<?>> classes)
    {
        Collection<Class<?>> sortedClasses = new TreeSet<Class<?>>(new Comparator<Class<?>>()
            {
                @Override
                public int compare(Class<?> c1, Class<?> c2)
                {
                    return c1.getSimpleName().compareToIgnoreCase(c2.getSimpleName());
                }
            });
        sortedClasses.addAll(classes);

        for (Class<?> clazz : sortedClasses)
        {
            report.println(clazz.getSimpleName(), isClassCorrect(clazz));
            report.increaseIndentation();
            printFields(report, getPublicFields(clazz));
            printMethods(report, getPublicMethods(clazz));
            report.decreaseIndentation();
        }
    }

    private void printFields(Report report, Collection<Field> fields)
    {
        Collection<Field> sortedFields = new TreeSet<Field>(new Comparator<Field>()
            {
                @Override
                public int compare(Field f1, Field f2)
                {
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });
        sortedFields.addAll(fields);

        for (Field field : sortedFields)
        {
            report.println(field.getDeclaringClass().getSimpleName() + "  " + field.getName(), isFieldCorrect(field));
        }
    }

    private void printMethods(Report report, Collection<Method> methods)
    {
        Collection<Method> sortedMethods = new TreeSet<Method>(new Comparator<Method>()
            {
                @Override
                public int compare(Method m1, Method m2)
                {
                    return m1.getName().compareToIgnoreCase(m2.getName());
                }
            });
        sortedMethods.addAll(methods);

        for (Method method : sortedMethods)
        {
            report.println(method.getDeclaringClass().getSimpleName() + "  " + method.getName(), isMethodCorrect(method));
        }
    }

    private class Report
    {

        private static final String INDENTATION = "   ";

        private StringBuilder content = new StringBuilder();

        private String indentation = "";

        private boolean printCorrect;

        private boolean printIncorrect;

        private boolean allCorrect = true;

        public void println(String s, boolean correct)
        {
            allCorrect = allCorrect && correct;

            if ((correct && printCorrect) || (false == correct && printIncorrect))
            {
                if (indentation != null)
                {
                    content.append(indentation);
                }
                content.append(s + " " + (correct ? " OK" : " WRONG") + "\n");
            }
        }

        public void setPrintCorrect(boolean printCorrect)
        {
            this.printCorrect = printCorrect;
        }

        public void setPrintIncorrect(boolean printIncorrect)
        {
            this.printIncorrect = printIncorrect;
        }

        public void increaseIndentation()
        {
            indentation += INDENTATION;
        }

        public void decreaseIndentation()
        {
            if (indentation.length() - INDENTATION.length() >= 0)
            {
                indentation = indentation.substring(0, indentation.length() - INDENTATION.length());
            }
        }

        public boolean isCorrect()
        {
            return allCorrect;
        }

        public String getContent()
        {
            return content.toString();
        }

    }

}
