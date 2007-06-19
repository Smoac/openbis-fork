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

import static org.testng.AssertJUnit.*;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link ClassUtils} class.
 * 
 * @author Christian Ribeaud
 */
public class ClassUtilsTest
{

    /**
     * Test method for {@link ch.systemsx.cisd.common.utilities.ClassUtils#getCurrentMethod()}.
     */
    @Test
    public final void testGetCurrentMethod()
    {
        assertEquals("testGetCurrentMethod", ClassUtils.getCurrentMethod().getName());
        // Border cases
        assertEquals(new SameMethodName().getMethodName(), new SameMethodName().getMethodName(new Object(), new Object()));
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

    private static class SimpleBean
    {
        private final int number;

        private final String string;

        SimpleBean(int number, String string)
        {
            this.number = number;
            this.string = string;
        }

        public int getNumber()
        {
            return number;
        }

        public String getString()
        {
            return string;
        }

        String getIgnoreThisBecauseItIsNotPublic()
        {
            AssertJUnit.fail("Should be ignore because not public");
            return null;
        }
    }

    @Test
    public void testCheckGettersForNullOK()
    {
        final SimpleBean bean = new SimpleBean(1, "");
        assert ClassUtils.checkGettersNotNull(bean) == bean;
    }

    @Test
    public void testCheckGettersForNullOKNullBean()
    {
        assertNull(ClassUtils.checkGettersNotNull(null));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCheckGettersForNullStringNull()
    {
        final SimpleBean bean = new SimpleBean(1, null);
        ClassUtils.checkGettersNotNull(bean);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCheckGettersForNullInt0()
    {
        final SimpleBean bean = new SimpleBean(0, "test");
        ClassUtils.checkGettersNotNull(bean);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCheckGettersForNullForbiddenArray()
    {
        ClassUtils.checkGettersNotNull(new Object[0]);
    }

    @Test
    public void testCheckGettersForNullListOK()
    {
        final SimpleBean bean1 = new SimpleBean(1, "test");
        final SimpleBean bean2 = new SimpleBean(5, "test2");
        final List<SimpleBean> beanList = Arrays.asList(bean1, bean2);
        assert ClassUtils.checkGettersNotNull(beanList) == beanList;
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCheckGettersForNullListInt0()
    {
        final SimpleBean bean1 = new SimpleBean(1, "test");
        final SimpleBean bean2 = new SimpleBean(0, "test2");
        ClassUtils.checkGettersNotNull(Arrays.asList(bean1, bean2));
    }

}
