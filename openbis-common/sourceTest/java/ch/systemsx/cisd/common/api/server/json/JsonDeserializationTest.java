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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.server.json.object.ObjectWithContainerTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes.NestedEnum;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithIgnoredProperties;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNestedChild;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrimitiveTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithRenamedProperties;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithType;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeAA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeB;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeButNoSubtypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeC;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeInterface1;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeInterface2;

/**
 * @author pkupczyk
 */
public class JsonDeserializationTest
{

    @Test
    public void testDeserializeJsonWithRootTypeToRootInterface1() throws Exception
    {
        testDeserializeObjectWithType("@type", "ObjectWithType", ObjectWithTypeInterface1.class);
        testDeserializeObjectWithType("@class", ".LegacyObjectWithType",
                ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithType("@type", "ObjectWithType", ObjectWithTypeInterface2.class);
        testDeserializeObjectWithType("@class", ".LegacyObjectWithType",
                ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootType() throws Exception
    {
        testDeserializeObjectWithType("@type", "ObjectWithType", ObjectWithType.class);
        testDeserializeObjectWithType("@class", ".LegacyObjectWithType", ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserializeObjectWithTypeA("@type", "ObjectWithTypeA", ObjectWithTypeInterface1.class);
        testDeserializeObjectWithTypeA("@class", ".LegacyObjectWithTypeA",
                ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithTypeA("@type", "ObjectWithTypeA", ObjectWithTypeInterface2.class);
        testDeserializeObjectWithTypeA("@class", ".LegacyObjectWithTypeA",
                ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootType() throws Exception
    {
        testDeserializeObjectWithTypeA("@type", "ObjectWithTypeA", ObjectWithType.class);
        testDeserializeObjectWithTypeA("@class", ".LegacyObjectWithTypeA", ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeA("@type", "ObjectWithTypeA", ObjectWithTypeA.class);
        testDeserializeObjectWithTypeA("@class", ".LegacyObjectWithTypeA", ObjectWithTypeA.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithFirstLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeA("@type", "ObjectWithTypeA", ObjectWithTypeAA.class);
        testDeserializeObjectWithTypeA("@class", ".LegacyObjectWithTypeA", ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserializeObjectWithTypeAA("@type", "ObjectWithTypeAA", ObjectWithTypeInterface1.class);
        testDeserializeObjectWithTypeAA("@class", ".LegacyObjectWithTypeAA",
                ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithTypeAA("@type", "ObjectWithTypeAA", ObjectWithTypeInterface2.class);
        testDeserializeObjectWithTypeAA("@class", ".LegacyObjectWithTypeAA",
                ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootType() throws Exception
    {
        testDeserializeObjectWithTypeAA("@type", "ObjectWithTypeAA", ObjectWithType.class);
        testDeserializeObjectWithTypeAA("@class", ".LegacyObjectWithTypeAA", ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeAA("@type", "ObjectWithTypeAA", ObjectWithTypeA.class);
        testDeserializeObjectWithTypeAA("@class", ".LegacyObjectWithTypeAA", ObjectWithTypeA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeAA("@type", "ObjectWithTypeAA", ObjectWithTypeAA.class);
        testDeserializeObjectWithTypeAA("@class", ".LegacyObjectWithTypeAA", ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithNestedRootTypeToNestedRootType() throws Exception
    {
        testDeserializeObjectNested("@type", "ObjectNested", ObjectNested.class);
        testDeserializeObjectNested("@class", ".LegacyObjectNested", ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedRootType() throws Exception
    {
        testDeserializeObjectNestedChild("@type", "ObjectNestedChild", ObjectNested.class);
        testDeserializeObjectNestedChild("@class", ".LegacyObjectNestedChild", ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedSubType() throws Exception
    {
        testDeserializeObjectNestedChild("@type", "ObjectNestedChild", ObjectNestedChild.class);
        testDeserializeObjectNestedChild("@class", ".LegacyObjectNestedChild",
                ObjectNestedChild.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithPrimitiveTypes() throws Exception
    {
        testDeserializeObjectWithPrimitiveTypes("@type", "ObjectWithPrimitiveTypes");
        testDeserializeObjectWithPrimitiveTypes("@class", ".LegacyObjectWithPrimitiveTypes");
    }

    @Test
    public void testDeserializeJsonWithObjectWithNestedTypes() throws Exception
    {
        testDeserializeObjectWithNestedTypes("@type", "ObjectWithNestedTypes");
        testDeserializeObjectWithNestedTypes("@class", ".LegacyObjectWithNestedTypes");
    }

    @Test
    public void testDeserializeJsonWithObjectWithEnumTypes() throws Exception
    {
        testDeserializeObjectWithEnumTypes("@type", "ObjectWithEnumTypes");
        testDeserializeObjectWithEnumTypes("@class", ".LegacyObjectWithEnumTypes");
    }

    @Test
    public void testDeserializeJsonWithIgnoredProperties() throws Exception
    {
        testDeserializeObjectWithIgnoredProperties("@type", "ObjectWithIgnoredProperties");
        testDeserializeObjectWithIgnoredProperties("@class", ".LegacyObjectWithIgnoredProperties");
    }

    @Test
    public void testDeserializeJsonWithRenamedProperties() throws Exception
    {
        testDeserializeObjectWithRenamedProperties("@type", "ObjectWithRenamedProperties");
        testDeserializeObjectWithRenamedProperties("@class", ".LegacyObjectWithRenamedProperties");
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithoutTypeToPolymorphicType() throws Exception
    {
        testDeserializeObjectWithType(null, null, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToPolymorphicType() throws Exception
    {
        testDeserializeObjectWithType("@type", "ObjectWithType", ObjectWithType.class);
        testDeserializeObjectWithType("@class", ".LegacyObjectWithType", ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithoutTypeToNotPolymorphicType() throws Exception
    {
        testDeserializeObjectWithTypeButNoSubtypes(null, null, ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToNotPolymorphicType() throws Exception
    {
        testDeserializeObjectWithTypeButNoSubtypes("@type", "ObjectWithTypeButNoSubtypes",
                ObjectWithTypeButNoSubtypes.class);
        testDeserializeObjectWithTypeButNoSubtypes("@class", ".LegacyObjectWithTypeButNoSubtypes",
                ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithLegacyClassDefinedInClassMapping() throws Exception
    {
        testDeserializeObjectWithTypeA("@class", ".LegacyObjectWithTypeA", ObjectWithType.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMapping() throws Exception
    {
        testDeserializeObjectWithTypeC("@class", ".LegacyObjectWithTypeC", ObjectWithType.class);
    }

    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMappingButMatchingType()
            throws Exception
    {
        testDeserializeObjectWithTypeC("@class", ".ObjectWithTypeC", ObjectWithType.class);
        testDeserializeObjectWithTypeC("@class", "ObjectWithTypeC", ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInDifferentInheritanceTrees()
            throws Exception
    {
        testDeserializeObjectWithTypeA("@class", ".LegacyObjectWithTypeA", ObjectWithType.class);
    }

    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInSameInheritanceTree()
            throws Exception
    {
        testDeserializeObjectWithTypeB("@class", ".LegacyObjectWithTypeB", ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueTypeUsedInDifferentInheritanceTrees()
            throws Exception
    {
        testDeserializeObjectWithTypeA("@type", "ObjectWithTypeA", ObjectWithType.class);
    }

    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithNotUniqueTypeUsedInSameInheritanceTree() throws Exception
    {
        testDeserializeObjectWithTypeB("@type", "ObjectWithTypeB", ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInCollection() throws Exception
    {
        Collection<Object> collection = new ArrayList<Object>();
        collection.add(createObjectWithContainerTypes());

        Collection<Object> collectionResult = deserialize(collection, Collection.class);

        Assert.assertEquals(collectionResult.size(), 1);
        assertObjectWithContainerTypes(collectionResult.iterator().next());
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInMap() throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("objectWithContainerTypes", createObjectWithContainerTypes());

        HashMap<String, Object> mapResult = deserialize(map, Map.class);

        Assert.assertEquals(mapResult.size(), 1);
        assertObjectWithContainerTypes(mapResult.get("objectWithContainerTypes"));
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInList() throws Exception
    {
        List<Object> list = new ArrayList<Object>();
        list.add(createObjectWithContainerTypes());

        List<Object> listResult = deserialize(list, List.class);

        Assert.assertEquals(listResult.size(), 1);
        assertObjectWithContainerTypes(listResult.get(0));
    }

    private void testDeserializeObjectWithType(String typeField, String typeValue,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("base", "baseValue");

        ObjectWithType object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.base, "baseValue");
    }

    private void testDeserializeObjectWithTypeA(String typeField, String typeValue,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("base", "baseValue");
        objectMap.put("a", "aValue");

        ObjectWithTypeA object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.base, "baseValue");
        Assert.assertEquals(object.a, "aValue");
    }

    private void testDeserializeObjectWithTypeB(String typeField, String typeValue,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("base", "baseValue");
        objectMap.put("b", "bValue");

        ObjectWithTypeB object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.base, "baseValue");
        Assert.assertEquals(object.b, "bValue");
    }

    private void testDeserializeObjectWithTypeC(String typeField, String typeValue,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("base", "baseValue");
        objectMap.put("c", "cValue");

        ObjectWithTypeC object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.base, "baseValue");
        Assert.assertEquals(object.c, "cValue");
    }

    private void testDeserializeObjectWithTypeAA(String typeField, String typeValue,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("base", "baseValue");
        objectMap.put("a", "aValue");
        objectMap.put("aa", "aaValue");

        ObjectWithTypeAA object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.base, "baseValue");
        Assert.assertEquals(object.a, "aValue");
        Assert.assertEquals(object.aa, "aaValue");
    }

    private void testDeserializeObjectWithTypeButNoSubtypes(String typeField, String typeValue,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("a", "aValue");
        objectMap.put("b", "bValue");

        ObjectWithTypeButNoSubtypes object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.a, "aValue");
        Assert.assertEquals(object.b, "bValue");
    }

    private void testDeserializeObjectWithIgnoredProperties(String typeField, String typeValue)
            throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }

        objectMap.put("property", "propertyValue");
        objectMap.put("propertyWithGetterAndSetter", "propertyWithGetterAndSetterValue");
        objectMap.put("propertyIgnored", "propertyIgnoredValue");
        objectMap.put("propertyWithGetterAndSetterIgnored",
                "propertyWithGetterAndSetterIgnoredValue");

        ObjectWithIgnoredProperties object =
                deserialize(objectMap, ObjectWithIgnoredProperties.class);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.property, "propertyValue");
        Assert.assertEquals(object.getPropertyWithGetterAndSetter(),
                "propertyWithGetterAndSetterValue");
        Assert.assertNull(object.propertyIgnored);
        Assert.assertNull(object.getPropertyWithGetterAndSetterIgnored());
    }

    private void testDeserializeObjectWithRenamedProperties(String typeField, String typeValue)
            throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }

        objectMap.put("property", "propertyValue");
        objectMap.put("propertyWithGetterAndSetter", "propertyWithGetterAndSetterValue");
        objectMap.put("propertyRenamed", "propertyRenamedValue");
        objectMap.put("propertyWithGetterAndSetterRenamed",
                "propertyWithGetterAndSetterRenamedValue");

        ObjectWithRenamedProperties object =
                deserialize(objectMap, ObjectWithRenamedProperties.class);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.property, "propertyValue");
        Assert.assertEquals(object.getPropertyWithGetterAndSetter(),
                "propertyWithGetterAndSetterValue");
        Assert.assertEquals(object.x, "propertyRenamedValue");
        Assert.assertEquals(object.getY(), "propertyWithGetterAndSetterRenamedValue");
    }

    private void testDeserializeObjectWithPrimitiveTypes(String typeField, String typeValue)
            throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }

        objectMap.put("stringField", "stringValue");
        objectMap.put("integerObjectField", new Integer(1));
        objectMap.put("floatObjectField", new Float(2.5f));
        objectMap.put("doubleObjectField", new Double(3.5f));
        objectMap.put("integerField", 4);
        objectMap.put("floatField", 5.5f);
        objectMap.put("doubleField", 6.5d);

        ObjectWithPrimitiveTypes object = deserialize(objectMap, ObjectWithPrimitiveTypes.class);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.stringField, "stringValue");
        Assert.assertEquals(object.integerObjectField, new Integer(1));
        Assert.assertEquals(object.floatObjectField, new Float(2.5f));
        Assert.assertEquals(object.doubleObjectField, new Double(3.5f));
        Assert.assertEquals(object.integerField, 4);
        Assert.assertEquals(object.floatField, 5.5f);
        Assert.assertEquals(object.doubleField, 6.5d);
    }

    private void testDeserializeObjectWithEnumTypes(String typeField, String typeValue)
            throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }

        EnumSet<NestedEnum> enumSet = EnumSet.of(NestedEnum.VALUE1, NestedEnum.VALUE3);
        EnumMap<NestedEnum, Object> enumMap =
                new EnumMap<NestedEnum, Object>(Collections.singletonMap(NestedEnum.VALUE2,
                        "value2"));

        objectMap.put("enumField", "VALUE1");
        objectMap.put("enumSet", enumSet);
        objectMap.put("enumMap", enumMap);

        ObjectWithEnumTypes object = deserialize(objectMap, ObjectWithEnumTypes.class);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.enumField, NestedEnum.VALUE1);
        Assert.assertEquals(object.enumSet, enumSet);
        Assert.assertEquals(object.enumMap, enumMap);
    }

    private void testDeserializeObjectWithNestedTypes(String typeField, String typeValue)
            throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }

        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put("@type", "ObjectNested");
        nested.put("nested", "nestedValue");

        Map<String, Object> nestedChild = new HashMap<String, Object>();
        nestedChild.put("@type", "ObjectNestedChild");
        nestedChild.put("nested", "nestedValue");
        nestedChild.put("nestedChild", "nestedChildValue");

        objectMap.put("propertyNested", nestedChild);
        objectMap.put("propertyNestedChild", nestedChild);

        ObjectWithNestedTypes object = deserialize(objectMap, ObjectWithNestedTypes.class);

        Assert.assertNotNull(object);

        ObjectNestedChild propertyNested = (ObjectNestedChild) object.propertyNested;
        Assert.assertEquals("nestedValue", propertyNested.nested);
        Assert.assertEquals("nestedChildValue", propertyNested.nestedChild);

        ObjectNestedChild propertyNestedChild = object.propertyNestedChild;
        Assert.assertEquals("nestedValue", propertyNestedChild.nested);
        Assert.assertEquals("nestedChildValue", propertyNestedChild.nestedChild);
    }

    private void testDeserializeObjectNested(String typeField, String typeValue, Class<?> rootClass)
            throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("nested", "nestedValue");

        ObjectNested object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.nested, "nestedValue");
    }

    private void testDeserializeObjectNestedChild(String typeField, String typeValue,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = new HashMap<String, Object>();
        if (typeField != null && typeValue != null)
        {
            objectMap.put(typeField, typeValue);
        }
        objectMap.put("nested", "nestedValue");
        objectMap.put("nestedChild", "nestedChildValue");

        ObjectNestedChild object = deserialize(objectMap, rootClass);

        Assert.assertNotNull(object);
        Assert.assertEquals(object.nested, "nestedValue");
        Assert.assertEquals(object.nestedChild, "nestedChildValue");
    }

    private Map<String, Object> createItemWithKnownType()
    {
        Map<String, Object> itemWithKnownType = new HashMap<String, Object>();
        itemWithKnownType.put("@type", "ObjectWithTypeA");
        itemWithKnownType.put("base", "itemWithKnownType_base");
        itemWithKnownType.put("a", "itemWithKnownType_a");
        return itemWithKnownType;
    }

    private void assertItemWithKnownType(Object object)
    {
        Assert.assertEquals(object.getClass(), ObjectWithTypeA.class);
        ObjectWithTypeA itemWithKnownType = (ObjectWithTypeA) object;
        Assert.assertEquals("itemWithKnownType_base", itemWithKnownType.base);
        Assert.assertEquals("itemWithKnownType_a", itemWithKnownType.a);
    }

    private Map<String, Object> createItemWithKnownUniqueClass()
    {
        Map<String, Object> itemWithKnownUniqueClass = new HashMap<String, Object>();
        itemWithKnownUniqueClass.put("@class", ".LegacyObjectWithTypeAA");
        itemWithKnownUniqueClass.put("base", "itemWithKnownUniqueClass_base");
        itemWithKnownUniqueClass.put("a", "itemWithKnownUniqueClass_a");
        itemWithKnownUniqueClass.put("aa", "itemWithKnownUniqueClass_aa");
        return itemWithKnownUniqueClass;
    }

    private void assertItemWithKnownUniqueClass(Object object)
    {
        Assert.assertEquals(object.getClass(), ObjectWithTypeAA.class);
        ObjectWithTypeAA itemWithKnownUniqueClass = (ObjectWithTypeAA) object;
        Assert.assertEquals("itemWithKnownUniqueClass_base", itemWithKnownUniqueClass.base);
        Assert.assertEquals("itemWithKnownUniqueClass_a", itemWithKnownUniqueClass.a);
        Assert.assertEquals("itemWithKnownUniqueClass_aa", itemWithKnownUniqueClass.aa);
    }

    private Map<String, Object> createItemWithKnownButNotUniqueClass()
    {
        Map<String, Object> itemWithKnownButNotUniqueClass = new HashMap<String, Object>();
        itemWithKnownButNotUniqueClass.put("@class", ".LegacyObjectWithTypeA");
        itemWithKnownButNotUniqueClass.put("base", "itemWithKnownButNotUniqueClass_base");
        itemWithKnownButNotUniqueClass.put("a", "itemWithKnownButNotUniqueClass_a");
        return itemWithKnownButNotUniqueClass;
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private void assertItemWithKnownButNotUniqueClass(Object object)
    {
        Assert.assertTrue(object instanceof Map);
        Assert.assertEquals(new HashMap((Map) object), createItemWithKnownButNotUniqueClass());
    }

    private Map<String, Object> createItemWithUnknownType()
    {
        Map<String, Object> itemWithUnknownType = new HashMap<String, Object>();
        itemWithUnknownType.put("@type", "ObjectWithTypeUnknown");
        itemWithUnknownType.put("base", "itemWithUnknownType_base");
        itemWithUnknownType.put("a", "itemWithUnknownType_a");
        return itemWithUnknownType;
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private void assertItemWithUnknownType(Object object)
    {
        Assert.assertTrue(object instanceof Map);
        Assert.assertEquals(new HashMap((Map) object), createItemWithUnknownType());
    }

    private Map<String, Object> createItemWithUnknownClass()
    {
        Map<String, Object> itemWithUnknownClass = new HashMap<String, Object>();
        itemWithUnknownClass.put("@class", ".LegacyObjectWithTypeUnknown");
        itemWithUnknownClass.put("base", "itemWithUnknownClass_base");
        itemWithUnknownClass.put("a", "itemWithUnknownClass_a");
        return itemWithUnknownClass;
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private void assertItemWithUnknownClass(Object object)
    {
        Assert.assertTrue(object instanceof Map);
        Assert.assertEquals(new HashMap((Map) object), createItemWithUnknownClass());
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private Map<String, Object> createObjectWithContainerTypes()
    {
        Collection collection = new ArrayList();
        collection.add(createItemWithKnownType());
        collection.add(createItemWithKnownUniqueClass());
        collection.add(createItemWithKnownButNotUniqueClass());
        collection.add(createItemWithUnknownType());
        collection.add(createItemWithUnknownClass());

        Map map = new HashMap();
        map.put("itemWithKnownType", createItemWithKnownType());
        map.put("itemWithKnownUniqueClass", createItemWithKnownUniqueClass());
        map.put("itemWithKnownButNotUniqueClass", createItemWithKnownButNotUniqueClass());
        map.put("itemWithUnknownType", createItemWithUnknownType());
        map.put("itemWithUnknownClass", createItemWithUnknownClass());

        List list = new ArrayList();
        list.add(createItemWithKnownType());
        list.add(createItemWithKnownUniqueClass());
        list.add(createItemWithKnownButNotUniqueClass());
        list.add(createItemWithUnknownType());
        list.add(createItemWithUnknownClass());

        Map<String, Object> object = new HashMap<String, Object>();

        object.put("@type", "ObjectWithContainerTypes");
        object.put("collectionWithoutType", collection);
        object.put("collectionWithObjectType", collection);
        object.put("collectionWithSpecificType", collection);

        object.put("mapWithoutType", map);
        object.put("mapWithObjectType", map);
        object.put("mapWithSpecificType", map);

        object.put("listWithoutType", list);
        object.put("listWithObjectType", list);
        object.put("listWithSpecificType", list);

        return object;
    }

    private void assertObjectWithContainerTypes(Object object)
    {
        Assert.assertEquals(object.getClass(), ObjectWithContainerTypes.class);
        ObjectWithContainerTypes objectWithContainers = (ObjectWithContainerTypes) object;
        assertItemsCollection(objectWithContainers.collectionWithObjectType);
        assertItemsCollection(objectWithContainers.collectionWithoutType);
        assertItemsCollection(objectWithContainers.collectionWithSpecificType);
        assertItemsMap(objectWithContainers.mapWithObjectType);
        assertItemsMap(objectWithContainers.mapWithoutType);
        assertItemsMap(objectWithContainers.mapWithSpecificType);
        assertItemsCollection(objectWithContainers.listWithObjectType);
        assertItemsCollection(objectWithContainers.listWithoutType);
        assertItemsCollection(objectWithContainers.listWithSpecificType);
    }

    @SuppressWarnings(
        { "rawtypes" })
    private void assertItemsCollection(Collection collection)
    {
        Assert.assertEquals(collection.size(), 5);
        Iterator iterator = collection.iterator();
        assertItemWithKnownType(iterator.next());
        assertItemWithKnownUniqueClass(iterator.next());
        assertItemWithKnownButNotUniqueClass(iterator.next());
        assertItemWithUnknownType(iterator.next());
        assertItemWithUnknownClass(iterator.next());
    }

    @SuppressWarnings(
        { "rawtypes" })
    private void assertItemsMap(Map map)
    {
        Assert.assertEquals(map.size(), 5);
        assertItemWithKnownType(map.get("itemWithKnownType"));
        assertItemWithKnownUniqueClass(map.get("itemWithKnownUniqueClass"));
        assertItemWithKnownButNotUniqueClass(map.get("itemWithKnownButNotUniqueClass"));
        assertItemWithUnknownType(map.get("itemWithUnknownType"));
        assertItemWithUnknownClass(map.get("itemWithUnknownClass"));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(Object object, Class<?> rootClass) throws Exception
    {
        ObjectMapper mapper = new JsonTestObjectMapper();
        Object result = mapper.readValue(mapper.writeValueAsString(object), rootClass);
        return (T) result;
    }

}
