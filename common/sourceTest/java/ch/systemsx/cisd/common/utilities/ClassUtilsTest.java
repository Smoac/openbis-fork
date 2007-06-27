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

import java.lang.reflect.Field;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.annotation.Mandatory;

/**
 * Test cases for the {@link ClassUtils} class.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtilsTest
{

    @Test
    public final void testGetCurrentMethod()
    {
        assertEquals("testGetCurrentMethod", ClassUtils.getCurrentMethod().getName());
        // Border cases
        assertEquals(new SameMethodName().getMethodName(), new SameMethodName().getMethodName(new Object(),
                new Object()));
    }

    @Test
    public final void testGetFields()
    {
        List<Field> fields = ClassUtils.getFields(A.class, null, "otherField");
        assertNotNull(fields);
        assert fields.size() == 0;
        // A
        fields = ClassUtils.getFields(A.class, null, "someField");
        assertNotNull(fields);
        assert fields.size() == 1;
        // B
        fields = ClassUtils.getFields(B.class, null, "someField");
        assertNotNull(fields);
        assert fields.size() == 1;
        // C
        fields = ClassUtils.getFields(C.class, null, "someField");
        assertNotNull(fields);
        assert fields.size() == 1;
    }

    @Test
    public final void testGetMandatoryFields()
    {
        List<Field> fields = ClassUtils.getMandatoryFields(A.class);
        assertNotNull(fields);
        assert fields.size() == 0;
        fields = ClassUtils.getMandatoryFields(C.class);
        assertNotNull(fields);
        assert fields.size() == 2;
    }

    //
    // Helper classes
    //

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

    @SuppressWarnings("unused")
    private static class A
    {
        private Object someField;

        private Object mandatoryField;
    }

    private static class B extends A
    {

    }

    @Mandatory(
        { "mandatoryField", "otherMandatoryField", "notPresentField" })
    private final static class C extends B
    {
        @SuppressWarnings("unused")
        private Object otherMandatoryField;
    }
}
