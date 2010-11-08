/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.ReflectionStringTraverser.ReflectionFieldVisitor;

/**
 * Tests for {@link ReflectionStringTraverser}
 * 
 * @author Tomasz Pylak
 */
public class ReflectionPrimitiveFieldTraverserTest extends AssertJUnit
{
    private static class ReflectionStringCapitalizerVisitor implements ReflectionFieldVisitor
    {
        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            return value.toUpperCase();
        }
    }

    private static class TestStaticFinal
    {
        private static String dummyStaticString = "static";

        @SuppressWarnings("unused")
        private final String dummyFinalString = "final";
    }

    private static class TestClass
    {
        @SuppressWarnings("unused")
        private TestStaticFinal dummyFinalStatic = new TestStaticFinal();

        @SuppressWarnings("unused")
        boolean dummyBoolean = false;

        private List<TestClass> list;

        protected TestClass[] array;

        private List<String> stringList;

        protected String[] stringArray;

        public TestClass complexChild;

        private String text;
    }

    @Test
    public void test()
    {
        TestClass object = create("object");
        object.list = new ArrayList<TestClass>(Arrays.asList(create("list1"), create("list2")));
        object.array = new TestClass[]
            { create("array1"), create("array2") };
        object.complexChild = create("complexChild");
        object.stringList = new ArrayList<String>(Arrays.asList("stringList1"));
        object.stringArray = new String[]
            { "stringArray1" };

        ReflectionStringTraverser.traverse(object, new ReflectionStringCapitalizerVisitor());

        assertEquals("OBJECT", object.text);

        assertEquals(2, object.list.size());
        assertEquals("LIST1", object.list.get(0).text);
        assertEquals("LIST2", object.list.get(1).text);

        assertEquals(2, object.array.length);
        assertEquals("ARRAY1", object.array[0].text);
        assertEquals("ARRAY2", object.array[1].text);

        assertEquals(1, object.stringList.size());
        assertEquals("STRINGLIST1", object.stringList.get(0));

        assertEquals(1, object.stringArray.length);
        assertEquals("STRINGARRAY1", object.stringArray[0]);

        assertEquals("COMPLEXCHILD", object.complexChild.text);
        assertEquals("static", TestStaticFinal.dummyStaticString);
    }

    private static TestClass create(String value)
    {
        TestClass object = new TestClass();
        object.text = value;
        return object;
    }
}
