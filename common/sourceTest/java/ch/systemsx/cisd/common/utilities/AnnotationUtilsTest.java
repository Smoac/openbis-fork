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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.common.utilities.AnnotationUtils.Parameter;

/**
 * Test cases for the {@link AnnotationUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class AnnotationUtilsTest
{

    private final static Method getDoSomethingMethod()
    {
        final Method[] methods = A.class.getDeclaredMethods();
        for (final Method method : methods)
        {
            if (method.getName().equals("doSomething"))
            {
                return method;
            }
        }
        fail();
        return null;
    }

    @Test
    public final void testGetAnnotatedFieldList()
    {
        try
        {
            AnnotationUtils.getAnnotatedFieldList(null, null);
            fail("Null value not accepted.");
        } catch (final AssertionError e)
        {
            // Nothing to do here.
        }
        List<Field> fields = AnnotationUtils.getAnnotatedFieldList(A.class, Deprecated.class);
        assertEquals(1, fields.size());
        fields = AnnotationUtils.getAnnotatedFieldList(B.class, Deprecated.class);
        assertEquals(2, fields.size());
    }

    @Test
    public final void testGetAnnotatedMethodList()
    {
        boolean fail = true;
        try
        {
            AnnotationUtils.getAnnotatedMethodList(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        List<Method> methods = AnnotationUtils.getAnnotatedMethodList(A.class, BeanProperty.class);
        assertEquals(1, methods.size());
        methods = AnnotationUtils.getAnnotatedMethodList(B.class, BeanProperty.class);
        assertEquals(2, methods.size());
        for (final Method method : methods)
        {
            assertEquals(method.getName(), "setA");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public final void testGetAnnotatedParameters()
    {
        boolean fail = true;
        try
        {
            AnnotationUtils.getAnnotatedParameters(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        final Method method = getDoSomethingMethod();
        List<?> annotatedParameters =
                AnnotationUtils.getAnnotatedParameters(method, BeanProperty.class);
        assertEquals(0, annotatedParameters.size());
        annotatedParameters = AnnotationUtils.getAnnotatedParameters(method, Deprecated.class);
        assertEquals(1, annotatedParameters.size());
        final Parameter<Deprecated> parameter = (Parameter<Deprecated>) annotatedParameters.get(0);
        assertEquals(Deprecated.class, parameter.getAnnotation().annotationType());
        assertEquals(Object.class, parameter.getType());
        assertEquals(2, parameter.getIndex());
    }

    //
    // Helper classes
    //

    @SuppressWarnings("unused")
    private static class A
    {

        Object a;

        @Deprecated
        Object b;

        @BeanProperty
        void setA(final Object a)
        {
            this.a = a;
        }

        void doSomething(final Object c, final String d, @Deprecated
        final Object e)
        {
        }
    }

    @SuppressWarnings("unused")
    private final static class B extends A
    {

        @Deprecated
        Object c;

        //
        // A
        //

        @Override
        @BeanProperty
        final void setA(final Object a)
        {
            this.a = a;
        }
    }
}