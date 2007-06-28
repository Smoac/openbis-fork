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

}
