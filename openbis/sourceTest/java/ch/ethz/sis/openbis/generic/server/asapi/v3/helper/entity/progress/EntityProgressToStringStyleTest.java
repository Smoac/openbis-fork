package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.testng.annotations.Test;

public class EntityProgressToStringStyleTest
{
    @Test
    public void testWithNulls()
    {
        TestClass o = new TestClass();
        assertToStringEquals(o, "TestClass[primitive=0]");
    }

    @Test
    public void testWithPrimitive()
    {
        TestClass o = new TestClass();
        o.primitive = 1;
        assertToStringEquals(o, "TestClass[primitive=1]");
    }

    @Test
    public void testWithString()
    {
        TestClass o = new TestClass();
        o.string = "abc";
        assertToStringEquals(o, "TestClass[primitive=0, string=abc]");
    }

    @Test
    public void testWithInteger()
    {
        TestClass o = new TestClass();
        o.integer = 1;
        assertToStringEquals(o, "TestClass[primitive=0, integer=1]");
    }

    @Test
    public void testWithObject()
    {
        TestClass o = new TestClass();
        o.object = new TestClass();
        assertToStringEquals(o, "TestClass[primitive=0, object=TestClass[primitive=0]]");
    }

    @Test
    public void testWithPrimitiveArray()
    {
        TestClass o = new TestClass();
        o.primitiveArray = new int[] { 0, 1, 2 };
        assertToStringEquals(o, "TestClass[primitive=0, primitiveArray=[0, 1, 2]]");
    }

    @Test
    public void testWithStringArray()
    {
        TestClass o = new TestClass();
        o.stringArray = new String[] { "abc", "def", "ghi" };
        assertToStringEquals(o, "TestClass[primitive=0, stringArray=[abc, def, ghi]]");
    }

    @Test
    public void testWithIntegerArray()
    {
        TestClass o = new TestClass();
        o.integerArray = new Integer[] { 1, 2, 3 };
        assertToStringEquals(o, "TestClass[primitive=0, integerArray=[1, 2, 3]]");
    }

    @Test
    public void testWithObjectArray()
    {
        TestClass nested1 = new TestClass();
        nested1.primitive = 1;
        TestClass nested2 = new TestClass();
        nested2.primitive = 2;

        TestClass o = new TestClass();
        o.objectArray = new TestClass[] { nested1, nested2 };

        assertToStringEquals(o, "TestClass[primitive=0, objectArray=[TestClass[primitive=1], TestClass[primitive=2]]]");
    }

    @Test
    public void testWithNullCollectionItem()
    {
        TestClass o = new TestClass();
        o.stringCollection = Arrays.asList("abc", null, "ghi");
        assertToStringEquals(o, "TestClass[primitive=0, stringCollection=[abc, null, ghi]]");
    }

    @Test
    public void testWithStringCollection()
    {
        TestClass o = new TestClass();
        o.stringCollection = Arrays.asList("abc", "def", "ghi");
        assertToStringEquals(o, "TestClass[primitive=0, stringCollection=[abc, def, ghi]]");
    }

    @Test
    public void testWithIntegerCollection()
    {
        TestClass o = new TestClass();
        o.integerCollection = Arrays.asList(1, 2, 3);
        assertToStringEquals(o, "TestClass[primitive=0, integerCollection=[1, 2, 3]]");
    }

    @Test
    public void testWithObjectCollection()
    {
        TestClass nested1 = new TestClass();
        nested1.primitive = 1;
        TestClass nested2 = new TestClass();
        nested2.primitive = 2;

        TestClass o = new TestClass();
        o.objectCollection = Arrays.asList(nested1, nested2);

        assertToStringEquals(o, "TestClass[primitive=0, objectCollection=[TestClass[primitive=1], TestClass[primitive=2]]]");
    }

    @Test
    public void testWithNullMapKeyAndValue()
    {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("k1", "v1");
        map.put(null, null);

        TestClass o = new TestClass();
        o.stringMap = map;

        assertToStringEquals(o, "TestClass[primitive=0, stringMap={k1=v1, null=null}]");
    }

    @Test
    public void testWithStringMap()
    {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        TestClass o = new TestClass();
        o.stringMap = map;

        assertToStringEquals(o, "TestClass[primitive=0, stringMap={k1=v1, k2=v2}]");
    }

    @Test
    public void testWithIntegerMap()
    {
        Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
        map.put(1, 11);
        map.put(2, 22);

        TestClass o = new TestClass();
        o.integerMap = map;

        assertToStringEquals(o, "TestClass[primitive=0, integerMap={1=11, 2=22}]");
    }

    @Test
    public void testWithObjectMap()
    {
        TestClass nested1 = new TestClass();
        nested1.primitive = 1;
        TestClass nested2 = new TestClass();
        nested2.primitive = 2;

        Map<TestClass, TestClass> map = new LinkedHashMap<TestClass, TestClass>();
        map.put(nested1, nested1);
        map.put(nested2, nested2);

        TestClass o = new TestClass();
        o.objectMap = map;

        assertToStringEquals(o,
                "TestClass[primitive=0, objectMap={TestClass[primitive=1]=TestClass[primitive=1], TestClass[primitive=2]=TestClass[primitive=2]}]");
    }

    @Test
    public void testWithCircularReferences()
    {
        TestClass o1 = new TestClass();
        o1.primitive = 1;
        TestClass o2 = new TestClass();
        o2.primitive = 2;

        o1.object = o2;
        o2.object = o1;

        assertToStringEquals(o1,
                "TestClass[primitive=1, object=TestClass[primitive=2, object=" + o1.getClass().getName() + "@"
                        + Integer.toHexString(System.identityHashCode(o1)) + "]]");
    }

    private void assertToStringEquals(Object object, String expectedToString)
    {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(object, EntityProgressToStringStyle.ENTITY_PROGRESS_STYLE);
        assertEquals(builder.toString(), expectedToString);
    }

}
