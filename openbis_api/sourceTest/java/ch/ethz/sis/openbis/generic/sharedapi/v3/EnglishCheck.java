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

package ch.ethz.sis.openbis.generic.sharedapi.v3;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
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

import ch.systemsx.cisd.common.shared.basic.string.IgnoreCaseComparator;

/**
 * @author pkupczyk
 */
public class EnglishCheck
{

    private static final String[] PUBLIC_PACKAGES = {
            "ch.ethz.sis.openbis.generic.dssapi.v3",
            "ch.ethz.sis.openbis.generic.asapi.v3"
    };

    @Test
    public void test() throws Exception
    {
        Report report = new Report();
        checkClasses(report, getPublicClasses());
        System.out.println(new ReportFormat().showCorrect().showIncorrect().format(report));
        Assert.assertEquals(new ReportFormat().showIncorrect().showOnlyWords().format(report), "");
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

        Collection<String> nonInnerClassesAndTestClasses = Collections2.filter(map.values(), new Predicate<String>()
            {

                @Override
                public boolean apply(String item)
                {
                    return false == (item.contains("$") || item.endsWith("Test"));
                }
            });
        Collection<String> uniqueClassNames = new TreeSet<String>(nonInnerClassesAndTestClasses);
        Collection<Class<?>> uniqueClasses = ImmutableSet.copyOf(ReflectionUtils.forNames(uniqueClassNames));
        Set<Class<?>> nonSerializableConcreteClasses = new HashSet<Class<?>>();

        for (Class<?> uniqueClass : uniqueClasses)
        {
            System.out.println("Found V3 public class:\t" + uniqueClass.getName());

            if (false == Modifier.isAbstract(uniqueClass.getModifiers()) && false == Serializable.class.isAssignableFrom(uniqueClass))
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

    private void checkClasses(Report report, Collection<Class<?>> classes)
    {
        for (Class<?> clazz : classes)
        {
            report.add(new ClassEntry(clazz));
            checkFields(report, getPublicFields(clazz));
            checkMethods(report, getPublicMethods(clazz));
        }
    }

    private void checkFields(Report report, Collection<Field> fields)
    {
        for (Field field : fields)
        {
            report.add(new FieldEntry(field));
        }
    }

    private void checkMethods(Report report, Collection<Method> methods)
    {
        for (Method method : methods)
        {
            report.add(new MethodEntry(method));
        }
    }

    static class Report
    {

        private List<Entry> entries = new LinkedList<Entry>();

        public void add(Entry entry)
        {
            entries.add(entry);
        }

        public List<Entry> getEntries()
        {
            return entries;
        }

    }

    static abstract class Entry
    {

        public abstract String getWord();

        public abstract String getContext();

    }

    static class ClassEntry extends Entry
    {

        private Class<?> clazz;

        public ClassEntry(Class<?> clazz)
        {
            this.clazz = clazz;
        }

        @Override
        public String getWord()
        {
            return clazz.getSimpleName();
        }

        @Override
        public String getContext()
        {
            return clazz.getSimpleName();
        }

    }

    static class FieldEntry extends Entry
    {

        private Field field;

        public FieldEntry(Field field)
        {
            this.field = field;
        }

        @Override
        public String getWord()
        {
            return field.getName();
        }

        @Override
        public String getContext()
        {
            return field.getDeclaringClass().getSimpleName();
        }

    }

    static class MethodEntry extends Entry
    {

        private Method method;

        public MethodEntry(Method method)
        {
            this.method = method;
        }

        @Override
        public String getWord()
        {
            return method.getName();
        }

        @Override
        public String getContext()
        {
            return method.getDeclaringClass().getSimpleName();
        }

    }

    static class ReportFormat
    {

        private boolean showCorrect;

        private boolean showIncorrect;

        private boolean showOnlyWords;

        public ReportFormat showCorrect()
        {
            this.showCorrect = true;
            return this;
        }

        public ReportFormat showIncorrect()
        {
            this.showIncorrect = true;
            return this;
        }

        public ReportFormat showOnlyWords()
        {
            this.showOnlyWords = true;
            return this;
        }

        public String format(Report report)
        {
            Map<String, Set<String>> contextsMap = new HashMap<String, Set<String>>();

            for (Entry entry : report.getEntries())
            {
                Set<String> contexts = contextsMap.get(entry.getWord());

                if (contexts == null)
                {
                    contexts = new TreeSet<String>();
                    contextsMap.put(entry.getWord(), contexts);
                }

                contexts.add(entry.getContext());
            }

            List<String> sortedWords = new ArrayList<String>(contextsMap.keySet());
            Collections.sort(sortedWords, new IgnoreCaseComparator());

            String longestWord = Collections.max(sortedWords, new Comparator<String>()
                {
                    @Override
                    public int compare(String o1, String o2)
                    {
                        return Integer.valueOf(o1.length()).compareTo(Integer.valueOf(o2.length()));
                    }
                });

            StringBuilder content = new StringBuilder();

            for (String word : sortedWords)
            {
                Set<String> contexts = contextsMap.get(word);
                boolean correct = EnglishDictionary.getInstance().contains(word);

                if ((correct && showCorrect) || (false == correct && showIncorrect))
                {
                    if (showOnlyWords)
                    {
                        content.append(word);
                    } else
                    {
                        content.append(StringUtils.rightPad(word, longestWord.length()) + "\t" + (correct ? "OK" : "WRONG") + " " + contexts);
                    }

                    content.append("\n");
                }
            }

            return content.toString();
        }
    }

}
