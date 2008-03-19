/*
 * Copyright 2007 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.Test;

/**
 * Test cases for the {@link ClassUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtilsTest
{
    private static interface IA
    {
    }

    private static interface IExtendingIA extends IA
    {
    }

    private static interface IB
    {
    }

    private static class A
    {
    }

    private static class ExtendingA extends A implements IExtendingIA
    {
    }

    private static class ExtendingExtendingA extends ExtendingA implements IB, IA
    {
    }

    @Test
    public void testGatherAllCastableClassesAndInterfacesFor()
    {
        ExtendingExtendingA object = new ExtendingExtendingA();
        Collection<Class<?>> classes = ClassUtils.gatherAllCastableClassesAndInterfacesFor(object);
        Iterator<Class<?>> iterator = classes.iterator();
        assertSame(ExtendingExtendingA.class, iterator.next());
        assertSame(ExtendingA.class, iterator.next());
        assertSame(A.class, iterator.next());
        assertSame(Object.class, iterator.next());
        assertSame(IB.class, iterator.next());
        assertSame(IA.class, iterator.next());
        assertSame(IExtendingIA.class, iterator.next());

        assertEquals(false, iterator.hasNext());
    }

    /**
     * Test method for {@link ch.systemsx.cisd.common.utilities.ClassUtils#getCurrentMethod()}.
     */
    @Test
    public final void testGetCurrentMethod()
    {
        assertEquals("testGetCurrentMethod", ClassUtils.getCurrentMethod().getName());
        // Border cases
        assertEquals(new SameMethodName().getMethodName(), new SameMethodName().getMethodName(
                new Object(), new Object()));
    }

    private final static class SameMethodName
    {

        public final String getMethodName()
        {
            final StackTraceElement[] elements = new Throwable().getStackTrace();
            return elements[0].getMethodName();
        }

        public final String getMethodName(final Object one, final Object two)
        {
            final StackTraceElement[] elements = new Throwable().getStackTrace();
            return elements[0].getMethodName();
        }
    }

    @Test
    public final void testGetMethodOnStack()
    {
        assertEquals("getMethodOnStack", ClassUtils.getMethodOnStack(0).getName());
        assertEquals("testGetMethodOnStack", ClassUtils.getMethodOnStack(1).getName());
        assertNull(ClassUtils.getMethodOnStack(2));
        privateMethodOnStack();
    }

    private final void privateMethodOnStack()
    {
        // If <code>Class.getDeclaredMethods</code> were used instead of <code>Class.getDeclaredMethods</code>,
        // we will have 'ch.systemsx.cisd.common.utilities.ClassUtilsTest.privateMethodOnStack()' here.
        assertNull(ClassUtils.getMethodOnStack(1));
    }

    @Test
    public void testCreateWithDefaultConstructor()
    {
        final CharSequence cs =
                ClassUtils
                        .create(CharSequence.class, StringBuffer.class.getName(), (Object[]) null);
        assertTrue(cs instanceof StringBuffer);
        assertEquals(0, cs.length());
    }

    @Test
    public void testCreateWithPropertiesConstructor()
    {
        final Properties properties = new Properties();
        final Appendable appendable =
                ClassUtils.create(Appendable.class, MyClass.class.getName(), properties);
        assertTrue(appendable instanceof MyClass);
        assertSame(properties, ((MyClass) appendable).properties);
    }

    @Test
    public void testCreateWithIncompatibleSuperclass()
    {
        try
        {
            ClassUtils.create(Float.class, Integer.class.getName(), (Object[]) null);
            fail("AssertionError expected.");
        } catch (final AssertionError e)
        {
            assertEquals(
                    "Class 'java.lang.Integer' does not implements/extends 'java.lang.Float'.", e
                            .getMessage());
        }
    }

    @Test
    public void testCreateInstanceOfAnInterface()
    {
        try
        {
            ClassUtils.create(Float.class, CharSequence.class.getName(), (Object[]) null);
            fail("AssertionError expected.");
        } catch (final AssertionError e)
        {
            assertEquals(
                    "Interface 'java.lang.CharSequence' can not be instanciated as it is an interface.",
                    e.getMessage());
        }
    }

    @Test
    public final void testCreateInstanceWithAnInterfaceAsConstructorArgument()
    {
        final List<String> list = new ArrayList<String>();
        list.add("Hello");
        final Appendable appendable =
                ClassUtils.create(Appendable.class, MyClass.class.getName(), list);
        assertSame(list, ((MyClass) appendable).iterable);
    }

    @Test
    public final void testSetFieldValueWithExpectedThrowable()
    {
        final MyClass myClass = new MyClass((Iterable<String>) null);
        try
        {
            ClassUtils.setFieldValue(myClass, "", null);
            fail("Blank field name.");
        } catch (final AssertionError error)
        {
        }
        assertFalse(ClassUtils.setFieldValue(myClass, "doesNotExist", null));
    }

    @Test
    public final void testSetFieldValue()
    {
        final MyClass myClass = new MyClass((Iterable<String>) null);
        assertNull(myClass.iterable);
        final List<String> list = new ArrayList<String>();
        ClassUtils.setFieldValue(myClass, "iterable", list);
        assertNotNull(myClass.iterable);
        assertSame(list, myClass.iterable);
        ClassUtils.setFieldValue(myClass, "iterable", null);
        assertNull(myClass.iterable);
    }

    @Test
    public final void testSetFieldValueWithSubclass()
    {
        final MyExtendedClass myExtendedClass = new MyExtendedClass((Iterable<String>) null);
        assertNull(myExtendedClass.iterable);
        final List<String> list = new ArrayList<String>();
        ClassUtils.setFieldValue(myExtendedClass, "iterable", list);
        assertNotNull(myExtendedClass.iterable);
        assertSame(list, myExtendedClass.iterable);
        final Object object = new Object();
        ClassUtils.setFieldValue(myExtendedClass, "finalObject", object);
        assertSame(object, myExtendedClass.finalObject);
    }

    //
    // Helper Classes
    //

    public static class MyClass implements Appendable
    {
        Properties properties;

        Iterable<String> iterable;

        public MyClass(final Properties properties)
        {
            this.properties = properties;
        }

        public MyClass(final Iterable<String> iterable)
        {
            this.iterable = iterable;
        }

        //
        // Appendable
        //

        public Appendable append(final char c) throws IOException
        {
            return null;
        }

        public Appendable append(final CharSequence csq, final int start, final int end)
                throws IOException
        {
            return null;
        }

        public Appendable append(final CharSequence csq) throws IOException
        {
            return null;
        }

    }

    public final static class MyExtendedClass extends MyClass
    {

        private final Object finalObject = null;

        public MyExtendedClass(final Iterable<String> iterable)
        {
            super(iterable);
        }

        public MyExtendedClass(final Properties properties)
        {
            super(properties);
        }

    }
}
