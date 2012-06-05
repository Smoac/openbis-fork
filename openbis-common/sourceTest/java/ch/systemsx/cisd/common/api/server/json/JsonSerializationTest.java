/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.api.server.json;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes.NestedEnum;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNestedChild;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrimitiveTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithType;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeAA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeButNoSubtypes;

/**
 * @author pkupczyk
 */
public class JsonSerializationTest
{

    @Test
    public void testSerializeRootType() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeValues(values, false);

        ObjectWithType object = new ObjectWithType();
        setObjectWithTypeFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeRootTypeEmpty() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeValues(values, true);

        ObjectWithType object = new ObjectWithType();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeFirstLevelSubType() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeAValues(values, false);

        ObjectWithTypeA object = new ObjectWithTypeA();
        setObjectWithTypeAFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeFirstLevelSubTypeEmpty() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeAValues(values, true);

        ObjectWithTypeA object = new ObjectWithTypeA();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeSecondLevelSubType() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeAAValues(values, false);

        ObjectWithTypeAA object = new ObjectWithTypeAA();
        setObjectWithTypeAAFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeSecondLevelSubTypeEmpty() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeAAValues(values, true);

        ObjectWithTypeAA object = new ObjectWithTypeAA();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedRootType() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeNestedValues(values, false);

        ObjectNested object = new ObjectNested();
        setObjectWithTypeNestedFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedRootTypeEmpty() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeNestedValues(values, true);

        ObjectNested object = new ObjectNested();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedSubType() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeNestedChildValues(values, false);

        ObjectNestedChild object = new ObjectNestedChild();
        setObjectWithTypeNestedChildFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedSubTypeEmpty() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeNestedChildValues(values, true);

        ObjectNestedChild object = new ObjectNestedChild();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializePolymorphicType() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeValues(values, false);

        ObjectWithType object = new ObjectWithType();
        setObjectWithTypeFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNotPolymorphicType() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithTypeButNoSubtypesValues(values, false);

        ObjectWithTypeButNoSubtypes object = new ObjectWithTypeButNoSubtypes();
        setObjectWithTypeButNoSubtypesFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeObjectWithPrimitiveTypes() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithPrimitiveTypesValues(values, false);

        ObjectWithPrimitiveTypes object = new ObjectWithPrimitiveTypes();
        setObjectWithPrimitiveTypesFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeObjectWithNestedTypes() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithNestedTypesValues(values, false);

        ObjectWithNestedTypes object = new ObjectWithNestedTypes();
        setObjectWithNestedTypesFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeObjectWithEnumTypes() throws Exception
    {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        putObjectWithEnumTypesValues(values, false);

        ObjectWithEnumTypes object = new ObjectWithEnumTypes();
        setObjectWithEnumTypesFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    private void putObjectWithTypeValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectWithType");

        if (empty)
        {
            map.put("base", null);
        } else
        {
            map.put("base", "baseValue");
        }
    }

    private void setObjectWithTypeFields(ObjectWithType object)
    {
        object.base = "baseValue";
    }

    private void putObjectWithTypeAValues(Map<String, Object> map, boolean empty)
    {
        putObjectWithTypeValues(map, empty);
        map.put("@type", "ObjectWithTypeA");

        if (empty)
        {
            map.put("a", null);
        } else
        {
            map.put("a", "aValue");
        }
    }

    private void setObjectWithTypeAFields(ObjectWithTypeA object)
    {
        setObjectWithTypeFields(object);
        object.a = "aValue";
    }

    private void putObjectWithTypeAAValues(Map<String, Object> map, boolean empty)
    {
        putObjectWithTypeAValues(map, empty);
        map.put("@type", "ObjectWithTypeAA");

        if (empty)
        {
            map.put("aa", null);
        } else
        {
            map.put("aa", "aaValue");
        }
    }

    private void setObjectWithTypeAAFields(ObjectWithTypeAA object)
    {
        setObjectWithTypeAFields(object);
        object.aa = "aaValue";
    }

    private void putObjectWithTypeNestedValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectNested");

        if (empty)
        {
            map.put("nested", null);
        } else
        {
            map.put("nested", "nestedValue");
        }

    }

    private void setObjectWithTypeNestedFields(ObjectNested object)
    {
        object.nested = "nestedValue";
    }

    private void putObjectWithTypeNestedChildValues(Map<String, Object> map, boolean empty)
    {
        putObjectWithTypeNestedValues(map, empty);
        map.put("@type", "ObjectNestedChild");

        if (empty)
        {
            map.put("nestedChild", null);
        } else
        {
            map.put("nestedChild", "nestedChildValue");
        }
    }

    private void setObjectWithTypeNestedChildFields(ObjectNestedChild object)
    {
        setObjectWithTypeNestedFields(object);
        object.nestedChild = "nestedChildValue";
    }

    private void putObjectWithTypeButNoSubtypesValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectWithTypeButNoSubtypes");

        if (empty)
        {
            map.put("a", null);
            map.put("b", null);
        } else
        {
            map.put("a", "aValue");
            map.put("b", "bValue");
        }
    }

    private void setObjectWithTypeButNoSubtypesFields(ObjectWithTypeButNoSubtypes object)
    {
        object.a = "aValue";
        object.b = "bValue";
    }

    private void putObjectWithPrimitiveTypesValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectWithPrimitiveTypes");

        if (empty)
        {
            map.put("stringField", null);
            map.put("integerObjectField", null);
            map.put("floatObjectField", null);
            map.put("doubleObjectField", null);
            map.put("integerField", null);
            map.put("floatField", null);
            map.put("doubleField", null);
        } else
        {
            map.put("stringField", "stringValue");
            map.put("integerObjectField", new Integer(1));
            map.put("floatObjectField", new Float(2.5f));
            map.put("doubleObjectField", new Double(3.5f));
            map.put("integerField", 4);
            map.put("floatField", 5.5f);
            map.put("doubleField", 6.5d);
        }
    }

    private void setObjectWithPrimitiveTypesFields(ObjectWithPrimitiveTypes object)
    {
        object.stringField = "stringValue";
        object.integerObjectField = new Integer(1);
        object.floatObjectField = new Float(2.5f);
        object.doubleObjectField = new Double(3.5f);
        object.integerField = 4;
        object.floatField = 5.5f;
        object.doubleField = 6.5d;
    }

    private void putObjectWithNestedTypesValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectWithNestedTypes");

        if (empty)
        {
            map.put("propertyNested", null);
            map.put("propertyNestedChild", null);
        } else
        {
            Map<String, Object> nested = new LinkedHashMap<String, Object>();
            nested.put("@type", "ObjectNested");
            nested.put("nested", "nestedValue");

            Map<String, Object> nestedChild = new LinkedHashMap<String, Object>();
            nestedChild.put("@type", "ObjectNestedChild");
            nestedChild.put("nested", "nestedValue");
            nestedChild.put("nestedChild", "nestedChildValue");

            map.put("propertyNested", nestedChild);
            map.put("propertyNestedChild", nestedChild);
        }
    }

    private void setObjectWithNestedTypesFields(ObjectWithNestedTypes object)
    {
        ObjectNested nested = new ObjectNested();
        nested.nested = "nestedValue";

        ObjectNestedChild nestedChild = new ObjectNestedChild();
        nestedChild.nested = "nestedValue";
        nestedChild.nestedChild = "nestedChildValue";

        object.propertyNested = nestedChild;
        object.propertyNestedChild = nestedChild;
    }

    private void putObjectWithEnumTypesValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectWithEnumTypes");

        if (empty)
        {
            map.put("enumField", null);
            map.put("enumSet", null);
            map.put("enumMap", null);
        } else
        {
            EnumSet<NestedEnum> enumSet = EnumSet.of(NestedEnum.VALUE1, NestedEnum.VALUE3);
            EnumMap<NestedEnum, Object> enumMap =
                    new EnumMap<NestedEnum, Object>(Collections.singletonMap(NestedEnum.VALUE2,
                            "value2"));

            map.put("enumField", "VALUE1");
            map.put("enumSet", enumSet);
            map.put("enumMap", enumMap);
        }
    }

    private void setObjectWithEnumTypesFields(ObjectWithEnumTypes object)
    {
        EnumSet<NestedEnum> enumSet = EnumSet.of(NestedEnum.VALUE1, NestedEnum.VALUE3);
        EnumMap<NestedEnum, Object> enumMap =
                new EnumMap<NestedEnum, Object>(Collections.singletonMap(NestedEnum.VALUE2,
                        "value2"));

        object.enumField = NestedEnum.VALUE1;
        object.enumSet = enumSet;
        object.enumMap = enumMap;
    }

    private void serializeObjectAndCheckItsValues(Object object, Map<String, Object> expectedMap)
            throws Exception
    {
        String jsonFromObject = new JsonTestObjectMapper().writeValueAsString(object);
        String jsonFromExpectedMap = new ObjectMapper().writeValueAsString(expectedMap);
        Assert.assertEquals(jsonFromObject, jsonFromExpectedMap);
    }
}
