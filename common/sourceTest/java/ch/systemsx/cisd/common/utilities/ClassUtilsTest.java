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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.Test;

/**
 * Test cases for the {@link ClassUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtilsTest
{

    /**
     * Test method for {@link ch.systemsx.cisd.common.utilities.ClassUtils#getCurrentMethod()}.
     */
    @Test
    public final void testGetCurrentMethod()
    {
        assertEquals("testGetCurrentMethod", ClassUtils.getCurrentMethod().getName());
        // Border cases
        assertEquals(new SameMethodName().getMethodName(), new SameMethodName().getMethodName(new Object(),
                new Object()));
    }

    private final static class SameMethodName
    {

        public final String getMethodName()
        {
            StackTraceElement[] elements = new Throwable().getStackTrace();
            return elements[0].getMethodName();
        }

        public final String getMethodName(Object one, Object two)
        {
            StackTraceElement[] elements = new Throwable().getStackTrace();
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
        CharSequence cs = ClassUtils.create(CharSequence.class, StringBuffer.class.getName(), null);
        assertTrue(cs instanceof StringBuffer);
        assertEquals(0, cs.length());
    }

    @Test
    public void testCreateWithPropertiesConstructor()
    {
        Properties properties = new Properties();
        Appendable appendable = ClassUtils.create(Appendable.class, MyClass.class.getName(), properties);
        assertTrue(appendable instanceof MyClass);
        assertSame(properties, ((MyClass) appendable).getProperties());
    }

    public static class MyClass implements Appendable
    {
        private final Properties properties;

        public MyClass(Properties properties)
        {
            this.properties = properties;
        }

        public final Properties getProperties()
        {
            return properties;
        }

        public Appendable append(char c) throws IOException
        {
            return null;
        }

        public Appendable append(CharSequence csq, int start, int end) throws IOException
        {
            return null;
        }

        public Appendable append(CharSequence csq) throws IOException
        {
            return null;
        }

    }

    @Test
    public void testCreateWithIncompatibleSuperclass()
    {
        try
        {
            ClassUtils.create(Float.class, Integer.class.getName(), null);
            fail("AssertionError expected.");
        } catch (AssertionError e)
        {
            assertEquals("Class 'java.lang.Integer' does not implements/extends 'java.lang.Float'.", e.getMessage());
        }
    }

    @Test
    public void testCreateInstanceOfAnInterface()
    {
        try
        {
            ClassUtils.create(Float.class, CharSequence.class.getName(), null);
            fail("AssertionError expected.");
        } catch (AssertionError e)
        {
            assertEquals("Interface 'java.lang.CharSequence' can not be instanciated as it is an interface.", e
                    .getMessage());
        }
    }
}
