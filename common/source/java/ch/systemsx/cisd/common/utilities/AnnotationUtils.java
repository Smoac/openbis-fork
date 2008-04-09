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

package ch.systemsx.cisd.common.utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * General utility methods for working with annotations.
 * 
 * @author Christian Ribeaud
 */
public final class AnnotationUtils
{

    private AnnotationUtils()
    {
        // Can not be instantiated.
    }

    /**
     * For given <code>Class</code> returns a list of methods that are annotated with given
     * <var>annotationClass</var>.
     */
    public final static List<Method> getAnnotatedMethodList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass)
    {
        return AnnotationUtils.getAnnotatedMethodList(clazz, annotationClass, null);
    }

    /**
     * For given <code>Class</code> returns a list of methods that are annotated with given
     * <var>annotationClass</var>.
     * 
     * @param methods if <code>null</code>, then a new <code>List</code> is created.
     */
    private final static List<Method> getAnnotatedMethodList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass, final List<Method> methods)
    {
        assert clazz != null : "Unspecified class.";
        assert annotationClass != null : "Unspecified annotation class.";
        List<Method> list = methods;
        if (list == null)
        {
            list = new ArrayList<Method>();
        }
        for (final Method method : clazz.getDeclaredMethods())
        {
            if (method.getAnnotation(annotationClass) != null)
            {
                list.add(method);
            }
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null)
        {
            return getAnnotatedMethodList(superclass, annotationClass, list);
        }
        return list;
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with given
     * <var>annotationClass</var>.
     */
    public final static List<Field> getAnnotatedFieldList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass)
    {
        return AnnotationUtils.getAnnotatedFieldList(clazz, annotationClass, null);
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with given
     * <var>annotationClass</var>.
     * 
     * @param fields if <code>null</code>, then a new <code>List</code> is created.
     */
    private final static List<Field> getAnnotatedFieldList(final Class<?> clazz,
            final Class<? extends Annotation> annotationClass, final List<Field> fields)
    {
        assert clazz != null : "Unspecified class.";
        assert annotationClass != null : "Unspecified annotation class.";
        List<Field> list = fields;
        if (list == null)
        {
            list = new ArrayList<Field>();
        }
        for (final Field field : clazz.getDeclaredFields())
        {
            if (field.getAnnotation(annotationClass) != null)
            {
                list.add(field);
            }
        }
        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null)
        {
            return getAnnotatedFieldList(superclass, annotationClass, list);
        }
        return list;
    }
}
