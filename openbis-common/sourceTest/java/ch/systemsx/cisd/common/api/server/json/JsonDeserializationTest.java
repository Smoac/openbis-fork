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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.api.server.json.object.ObjectWithContainerTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithDateTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes.NestedEnum;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithIgnoredProperties;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNestedChild;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrimitiveTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrivateAccess;
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
        testDeserializeObjectWithType(true, false, ObjectWithTypeInterface1.class);
        testDeserializeObjectWithType(false, true, ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithType(true, false, ObjectWithTypeInterface2.class);
        testDeserializeObjectWithType(false, true, ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootType() throws Exception
    {
        testDeserializeObjectWithType(true, false, ObjectWithType.class);
        testDeserializeObjectWithType(false, true, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserializeObjectWithTypeA(true, false, ObjectWithTypeInterface1.class);
        testDeserializeObjectWithTypeA(false, true, ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithTypeA(true, false, ObjectWithTypeInterface2.class);
        testDeserializeObjectWithTypeA(false, true, ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootType() throws Exception
    {
        testDeserializeObjectWithTypeA(true, false, ObjectWithType.class);
        testDeserializeObjectWithTypeA(false, true, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeA(true, false, ObjectWithTypeA.class);
        testDeserializeObjectWithTypeA(false, true, ObjectWithTypeA.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithFirstLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeA(true, false, ObjectWithTypeAA.class);
        testDeserializeObjectWithTypeA(false, true, ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserializeObjectWithTypeAA(true, false, ObjectWithTypeInterface1.class);
        testDeserializeObjectWithTypeAA(false, true, ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithTypeAA(true, false, ObjectWithTypeInterface2.class);
        testDeserializeObjectWithTypeAA(false, true, ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootType() throws Exception
    {
        testDeserializeObjectWithTypeAA(true, false, ObjectWithType.class);
        testDeserializeObjectWithTypeAA(false, true, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeAA(true, false, ObjectWithTypeA.class);
        testDeserializeObjectWithTypeAA(false, true, ObjectWithTypeA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeAA(true, false, ObjectWithTypeAA.class);
        testDeserializeObjectWithTypeAA(false, true, ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithNestedRootTypeToNestedRootType() throws Exception
    {
        testDeserializeObjectNested(true, false, ObjectNested.class);
        testDeserializeObjectNested(false, true, ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedRootType() throws Exception
    {
        testDeserializeObjectNestedChild(true, false, ObjectNested.class);
        testDeserializeObjectNestedChild(false, true, ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedSubType() throws Exception
    {
        testDeserializeObjectNestedChild(true, false, ObjectNestedChild.class);
        testDeserializeObjectNestedChild(false, true, ObjectNestedChild.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithPrimitiveTypes() throws Exception
    {
        testDeserializeObjectWithPrimitiveTypes(true, false);
        testDeserializeObjectWithPrimitiveTypes(false, true);
    }

    @Test
    public void testDeserializeJsonWithObjectWithNestedTypes() throws Exception
    {
        testDeserializeObjectWithNestedTypes(true, false);
        testDeserializeObjectWithNestedTypes(false, true);
    }

    @Test
    public void testDeserializeJsonWithObjectWithEnumTypes() throws Exception
    {
        testDeserializeObjectWithEnumTypes(true, false);
        testDeserializeObjectWithEnumTypes(false, true);
    }

    @Test
    public void testDeserializeJsonWithObjectWithDateTypes() throws Exception
    {
        testDeserializeObjectWithDateTypes(true, false);
        testDeserializeObjectWithDateTypes(false, true);
    }

    @Test
    public void testDeserializeJsonWithObjectWithContainerTypes() throws Exception
    {
        testDeserializeObjectWithContainerTypes(true, false);
        testDeserializeObjectWithContainerTypes(false, true);
    }

    @Test
    public void testDeserializeJsonWithIgnoredProperties() throws Exception
    {
        testDeserializeObjectWithIgnoredProperties(true, false);
        testDeserializeObjectWithIgnoredProperties(false, true);
    }

    @Test
    public void testDeserializeJsonWithRenamedProperties() throws Exception
    {
        testDeserializeObjectWithRenamedProperties(true, false);
        testDeserializeObjectWithRenamedProperties(false, true);
    }

    @Test
    public void testDeserializeJsonWithPrivateAccess() throws Exception
    {
        testDeserializeObjectWithPrivateAccess(true, false);
        testDeserializeObjectWithPrivateAccess(false, true);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithoutTypeToPolymorphicType() throws Exception
    {
        testDeserializeObjectWithType(false, false, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToPolymorphicType() throws Exception
    {
        testDeserializeObjectWithType(true, false, ObjectWithType.class);
        testDeserializeObjectWithType(false, true, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithoutTypeToNotPolymorphicType() throws Exception
    {
        testDeserializeObjectWithTypeButNoSubtypes(false, false, ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToNotPolymorphicType() throws Exception
    {
        testDeserializeObjectWithTypeButNoSubtypes(true, false, ObjectWithTypeButNoSubtypes.class);
        testDeserializeObjectWithTypeButNoSubtypes(false, true, ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithLegacyClassDefinedInClassMapping() throws Exception
    {
        testDeserializeObjectWithTypeA(false, true, ObjectWithType.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMapping() throws Exception
    {
        testDeserializeObjectWithTypeC(false, true, ObjectWithType.class);
    }

    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMappingButMatchingType()
            throws Exception
    {
        Map<String, Object> map = createObjectWithTypeC(false, false);
        map.put("@class", ".ObjectWithTypeC");
        deserialize(map, ObjectWithTypeC.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInDifferentInheritanceTrees()
            throws Exception
    {
        testDeserializeObjectWithTypeA(false, true, ObjectWithType.class);
    }

    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInSameInheritanceTree()
            throws Exception
    {
        testDeserializeObjectWithTypeB(false, true, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueTypeUsedInDifferentInheritanceTrees()
            throws Exception
    {
        testDeserializeObjectWithTypeA(true, false, ObjectWithType.class);
    }

    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithNotUniqueTypeUsedInSameInheritanceTree() throws Exception
    {
        testDeserializeObjectWithTypeB(true, false, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInCollection() throws Exception
    {
        testDeserializeJsonWithNestedMapsInCollection(true, false);
        testDeserializeJsonWithNestedMapsInCollection(false, true);
    }

    private void testDeserializeJsonWithNestedMapsInCollection(boolean includeType,
            boolean includeClass) throws Exception
    {
        Collection<Object> collection = new ArrayList<Object>();
        collection.add(createObjectWithContainerTypes(includeType, includeClass));

        Collection<Object> collectionResult = deserialize(collection, Collection.class);

        Assert.assertEquals(collectionResult.size(), 1);
        assertObjectWithContainerTypes(collectionResult.iterator().next());
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInMap() throws Exception
    {
        testDeserializeJsonWithNestedMapsInMap(true, false);
        testDeserializeJsonWithNestedMapsInMap(false, true);
    }

    private void testDeserializeJsonWithNestedMapsInMap(boolean includeType, boolean includeClass)
            throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("objectWithContainerTypes",
                createObjectWithContainerTypes(includeType, includeClass));

        HashMap<String, Object> mapResult = deserialize(map, Map.class);

        Assert.assertEquals(mapResult.size(), 1);
        assertObjectWithContainerTypes(mapResult.get("objectWithContainerTypes"));
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInList() throws Exception
    {
        testDeserializeJsonWithNestedMapsInList(true, false);
        testDeserializeJsonWithNestedMapsInList(false, true);
    }

    private void testDeserializeJsonWithNestedMapsInList(boolean includeType, boolean includeClass)
            throws Exception
    {
        List<Object> list = new ArrayList<Object>();
        list.add(createObjectWithContainerTypes(includeType, includeClass));

        List<Object> listResult = deserialize(list, List.class);

        Assert.assertEquals(listResult.size(), 1);
        assertObjectWithContainerTypes(listResult.get(0));
    }

    private void testDeserializeObjectWithType(boolean includeType, boolean includeClass,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> map = createObjectWithType(includeType, includeClass);

        Object object = deserialize(map, rootClass);

        assertObjectWithType(object);
    }

    private void testDeserializeObjectWithTypeA(boolean includeType, boolean includeClass,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> map = createObjectWithTypeA(includeType, includeClass);

        ObjectWithTypeA object = deserialize(map, rootClass);

        assertObjectWithTypeA(object);
    }

    private void testDeserializeObjectWithTypeB(boolean includeType, boolean includeClass,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> map = createObjectWithTypeB(includeType, includeClass);

        ObjectWithTypeB object = deserialize(map, rootClass);

        assertObjectWithTypeB(object);
    }

    private void testDeserializeObjectWithTypeC(boolean includeType, boolean includeClass,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> map = createObjectWithTypeC(includeType, includeClass);

        ObjectWithTypeC object = deserialize(map, rootClass);

        assertObjectWithTypeC(object);
    }

    private void testDeserializeObjectWithTypeAA(boolean includeType, boolean includeClass,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> map = createObjectWithTypeAA(includeType, includeClass);

        ObjectWithTypeAA object = deserialize(map, rootClass);

        assertObjectWithTypeAA(object);
    }

    private void testDeserializeObjectWithTypeButNoSubtypes(boolean includeType,
            boolean includeClass, Class<?> rootClass) throws Exception
    {
        Map<String, Object> map = createObjectWithTypeButNoSubtypes(includeType, includeClass);

        ObjectWithTypeButNoSubtypes object = deserialize(map, rootClass);

        assertObjectWithTypeButNoSubtypes(object);
    }

    private void testDeserializeObjectWithIgnoredProperties(boolean includeType,
            boolean includeClass) throws Exception
    {
        Map<String, Object> map = createObjectWithIgnoredProperties(includeType, includeClass);

        ObjectWithIgnoredProperties object = deserialize(map, ObjectWithIgnoredProperties.class);

        assertObjectWithIgnoredProperties(object);
    }

    private void testDeserializeObjectWithRenamedProperties(boolean includeType,
            boolean includeClass) throws Exception
    {
        Map<String, Object> map = createObjectWithRenamedProperties(includeType, includeClass);

        ObjectWithRenamedProperties object = deserialize(map, ObjectWithRenamedProperties.class);

        assertObjectWithRenamedProperties(object);
    }

    private void testDeserializeObjectWithPrivateAccess(boolean includeType, boolean includeClass)
            throws Exception
    {
        Map<String, Object> map = createObjectWithPrivateAccess(includeType, includeClass);

        ObjectWithPrivateAccess object = deserialize(map, ObjectWithPrivateAccess.class);

        assertObjectWithPrivateAccess(object);
    }

    private void testDeserializeObjectWithPrimitiveTypes(boolean includeType, boolean includeClass)
            throws Exception
    {
        Map<String, Object> map = createObjectWithPrimitiveTypes(includeType, includeClass);

        ObjectWithPrimitiveTypes object = deserialize(map, ObjectWithPrimitiveTypes.class);

        assertObjectWithPrimitiveTypes(object);
    }

    private void testDeserializeObjectWithEnumTypes(boolean includeType, boolean includeClass)
            throws Exception
    {
        Map<String, Object> map = createObjectWithEnumTypes(includeType, includeClass);

        ObjectWithEnumTypes object = deserialize(map, ObjectWithEnumTypes.class);

        assertObjectWithEnumTypes(object);
    }

    private void testDeserializeObjectWithDateTypes(boolean includeType, boolean includeClass)
            throws Exception
    {
        Map<String, Object> map = createObjectWithDateTypes(includeType, includeClass);

        ObjectWithDateTypes object = deserialize(map, ObjectWithDateTypes.class);

        assertObjectWithDateTypes(object);
    }

    private void testDeserializeObjectWithContainerTypes(boolean includeType, boolean includeClass)
            throws Exception
    {
        Map<String, Object> map = createObjectWithContainerTypes(includeType, includeClass);

        ObjectWithContainerTypes object = deserialize(map, ObjectWithContainerTypes.class);

        assertObjectWithContainerTypes(object);
    }

    private void testDeserializeObjectWithNestedTypes(boolean includeType, boolean includeClass)
            throws Exception
    {
        Map<String, Object> map = createObjectWithNestedTypes(includeType, includeClass);

        ObjectWithNestedTypes object = deserialize(map, ObjectWithNestedTypes.class);

        assertObjectWithNestedTypes(object);
    }

    private void testDeserializeObjectNested(boolean includeType, boolean includeClass,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = createObjectNested(includeType, includeClass);

        ObjectNested object = deserialize(objectMap, rootClass);

        assertObjectNested(object);
    }

    private void testDeserializeObjectNestedChild(boolean includeType, boolean includeClass,
            Class<?> rootClass) throws Exception
    {
        Map<String, Object> objectMap = createObjectNestedChild(includeType, includeClass);

        ObjectNestedChild object = deserialize(objectMap, rootClass);

        assertObjectNestedChild(object);
    }

    private Map<String, Object> createObjectWithType(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithType");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithType");
        }
        map.put("base", "baseValue");
        return map;
    }

    private void assertObjectWithType(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithType.class);
        ObjectWithType casted = (ObjectWithType) object;
        Assert.assertEquals(casted.base, "baseValue");
    }

    private Map<String, Object> createObjectWithTypeA(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithTypeA");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithTypeA");
        }
        map.put("base", "baseValue");
        map.put("a", "aValue");
        return map;
    }

    private void assertObjectWithTypeA(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithTypeA.class);
        ObjectWithTypeA casted = (ObjectWithTypeA) object;
        Assert.assertEquals(casted.base, "baseValue");
        Assert.assertEquals(casted.a, "aValue");
    }

    private Map<String, Object> createObjectWithTypeB(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithTypeB");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithTypeB");
        }
        map.put("base", "baseValue");
        map.put("b", "bValue");
        return map;
    }

    private void assertObjectWithTypeB(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithTypeB.class);
        ObjectWithTypeB casted = (ObjectWithTypeB) object;
        Assert.assertEquals(casted.base, "baseValue");
        Assert.assertEquals(casted.b, "bValue");
    }

    private Map<String, Object> createObjectWithTypeC(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithTypeC");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithTypeC");
        }
        map.put("base", "baseValue");
        map.put("c", "cValue");
        return map;
    }

    private void assertObjectWithTypeC(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithTypeC.class);
        ObjectWithTypeC casted = (ObjectWithTypeC) object;
        Assert.assertEquals(casted.base, "baseValue");
        Assert.assertEquals(casted.c, "cValue");
    }

    private Map<String, Object> createObjectWithTypeAA(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithTypeAA");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithTypeAA");
        }
        map.put("base", "baseValue");
        map.put("a", "aValue");
        map.put("aa", "aaValue");
        return map;
    }

    private void assertObjectWithTypeAA(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithTypeAA.class);
        ObjectWithTypeAA casted = (ObjectWithTypeAA) object;
        Assert.assertEquals(casted.base, "baseValue");
        Assert.assertEquals(casted.a, "aValue");
        Assert.assertEquals(casted.aa, "aaValue");
    }

    private Map<String, Object> createObjectWithTypeButNoSubtypes(boolean includeType,
            boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithTypeButNoSubtypes");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithTypeButNoSubtypes");
        }
        map.put("a", "aValue");
        map.put("b", "bValue");
        return map;
    }

    private void assertObjectWithTypeButNoSubtypes(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithTypeButNoSubtypes.class);
        ObjectWithTypeButNoSubtypes casted = (ObjectWithTypeButNoSubtypes) object;
        Assert.assertEquals(casted.a, "aValue");
        Assert.assertEquals(casted.b, "bValue");
    }

    private Map<String, Object> createObjectWithIgnoredProperties(boolean includeType,
            boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithIgnoredProperties");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithIgnoredProperties");
        }
        map.put("property", "propertyValue");
        map.put("propertyWithGetterAndSetter", "propertyWithGetterAndSetterValue");
        map.put("propertyIgnored", "propertyIgnoredValue");
        map.put("propertyWithGetterAndSetterIgnored", "propertyWithGetterAndSetterIgnoredValue");
        return map;
    }

    private void assertObjectWithIgnoredProperties(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithIgnoredProperties.class);
        ObjectWithIgnoredProperties casted = (ObjectWithIgnoredProperties) object;
        Assert.assertEquals(casted.property, "propertyValue");
        Assert.assertEquals(casted.getPropertyWithGetterAndSetter(),
                "propertyWithGetterAndSetterValue");
        Assert.assertNull(casted.propertyIgnored);
        Assert.assertNull(casted.getPropertyWithGetterAndSetterIgnored());
    }

    private Map<String, Object> createObjectWithRenamedProperties(boolean includeType,
            boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithRenamedProperties");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithRenamedProperties");
        }
        map.put("property", "propertyValue");
        map.put("propertyWithGetterAndSetter", "propertyWithGetterAndSetterValue");
        map.put("propertyRenamed", "propertyRenamedValue");
        map.put("propertyWithGetterAndSetterRenamed", "propertyWithGetterAndSetterRenamedValue");
        return map;
    }

    private void assertObjectWithRenamedProperties(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithRenamedProperties.class);
        ObjectWithRenamedProperties casted = (ObjectWithRenamedProperties) object;
        Assert.assertEquals(casted.property, "propertyValue");
        Assert.assertEquals(casted.getPropertyWithGetterAndSetter(),
                "propertyWithGetterAndSetterValue");
        Assert.assertEquals(casted.x, "propertyRenamedValue");
        Assert.assertEquals(casted.getY(), "propertyWithGetterAndSetterRenamedValue");
    }

    private Map<String, Object> createObjectWithPrivateAccess(boolean includeType,
            boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithPrivateAccess");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithPrivateAccess");
        }
        map.put("field", "fieldValue");
        return map;
    }

    private void assertObjectWithPrivateAccess(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithPrivateAccess.class);
        ObjectWithPrivateAccess casted = (ObjectWithPrivateAccess) object;
        Assert.assertEquals(casted.getField(), "fieldValue");
    }

    private Map<String, Object> createObjectWithPrimitiveTypes(boolean includeType,
            boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithPrimitiveTypes");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithPrimitiveTypes");
        }
        map.put("stringField", "stringValue");
        map.put("integerObjectField", new Integer(1));
        map.put("floatObjectField", new Float(2.5f));
        map.put("doubleObjectField", new Double(3.5f));
        map.put("integerField", 4);
        map.put("floatField", 5.5f);
        map.put("doubleField", 6.5d);
        return map;
    }

    private void assertObjectWithPrimitiveTypes(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithPrimitiveTypes.class);
        ObjectWithPrimitiveTypes casted = (ObjectWithPrimitiveTypes) object;
        Assert.assertEquals(casted.stringField, "stringValue");
        Assert.assertEquals(casted.integerObjectField, new Integer(1));
        Assert.assertEquals(casted.floatObjectField, new Float(2.5f));
        Assert.assertEquals(casted.doubleObjectField, new Double(3.5f));
        Assert.assertEquals(casted.integerField, 4);
        Assert.assertEquals(casted.floatField, 5.5f);
        Assert.assertEquals(casted.doubleField, 6.5d);
    }

    private Map<String, Object> createObjectWithDateTypes(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithDateTypes");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithDateTypes");
        }

        long DAY_IN_MILLIS = 24 * 3600 * 1000;

        map.put("utilDate", new java.util.Date(DAY_IN_MILLIS));
        map.put("sqlDate", new java.sql.Date(DAY_IN_MILLIS * 2));
        map.put("sqlTimestamp", new java.sql.Timestamp(DAY_IN_MILLIS * 3));
        return map;
    }

    private void assertObjectWithDateTypes(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithDateTypes.class);
        ObjectWithDateTypes casted = (ObjectWithDateTypes) object;

        long DAY_IN_MILLIS = 24 * 3600 * 1000;

        Assert.assertEquals(casted.utilDate.getTime(), new java.util.Date(DAY_IN_MILLIS).getTime());
        Assert.assertEquals(casted.sqlDate.getTime(),
                new java.sql.Date(DAY_IN_MILLIS * 2).getTime());
        Assert.assertEquals(casted.sqlTimestamp.getTime(),
                new java.sql.Timestamp(DAY_IN_MILLIS * 3).getTime());
    }

    private Map<String, Object> createObjectWithEnumTypes(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithEnumTypes");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithEnumTypes");
        }
        EnumSet<NestedEnum> enumSet = EnumSet.of(NestedEnum.VALUE1, NestedEnum.VALUE3);
        EnumMap<NestedEnum, Object> enumMap =
                new EnumMap<NestedEnum, Object>(Collections.singletonMap(NestedEnum.VALUE2,
                        "value2"));

        map.put("enumField", "VALUE1");
        map.put("enumSet", enumSet);
        map.put("enumMap", enumMap);
        return map;
    }

    private void assertObjectWithEnumTypes(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithEnumTypes.class);
        ObjectWithEnumTypes casted = (ObjectWithEnumTypes) object;
        Assert.assertEquals(casted.enumField, NestedEnum.VALUE1);

        EnumSet<NestedEnum> enumSet = EnumSet.of(NestedEnum.VALUE1, NestedEnum.VALUE3);
        EnumMap<NestedEnum, Object> enumMap =
                new EnumMap<NestedEnum, Object>(Collections.singletonMap(NestedEnum.VALUE2,
                        "value2"));

        Assert.assertEquals(casted.enumSet, enumSet);
        Assert.assertEquals(casted.enumMap, enumMap);
    }

    private Map<String, Object> createObjectWithNestedTypes(boolean includeType,
            boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectWithNestedTypes");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectWithNestedTypes");
        }
        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put("@type", "ObjectNested");
        nested.put("nested", "nestedValue");

        Map<String, Object> nestedChild = new HashMap<String, Object>();
        nestedChild.put("@type", "ObjectNestedChild");
        nestedChild.put("nested", "nestedValue");
        nestedChild.put("nestedChild", "nestedChildValue");

        map.put("propertyNested", nestedChild);
        map.put("propertyNestedChild", nestedChild);
        return map;
    }

    private void assertObjectWithNestedTypes(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectWithNestedTypes.class);
        ObjectWithNestedTypes casted = (ObjectWithNestedTypes) object;

        ObjectNestedChild propertyNested = (ObjectNestedChild) casted.propertyNested;
        Assert.assertEquals("nestedValue", propertyNested.nested);
        Assert.assertEquals("nestedChildValue", propertyNested.nestedChild);

        ObjectNestedChild propertyNestedChild = casted.propertyNestedChild;
        Assert.assertEquals("nestedValue", propertyNestedChild.nested);
        Assert.assertEquals("nestedChildValue", propertyNestedChild.nestedChild);
    }

    private Map<String, Object> createObjectNested(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectNested");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectNested");
        }
        map.put("nested", "nestedValue");
        return map;
    }

    private void assertObjectNested(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectNested.class);
        ObjectNested casted = (ObjectNested) object;
        Assert.assertEquals(casted.nested, "nestedValue");
    }

    private Map<String, Object> createObjectNestedChild(boolean includeType, boolean includeClass)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        if (includeType)
        {
            map.put("@type", "ObjectNestedChild");
        }
        if (includeClass)
        {
            map.put("@class", ".LegacyObjectNestedChild");
        }
        map.put("nested", "nestedValue");
        map.put("nestedChild", "nestedChildValue");
        return map;
    }

    private void assertObjectNestedChild(Object object)
    {
        Assert.assertNotNull(object);
        Assert.assertEquals(object.getClass(), ObjectNestedChild.class);
        ObjectNestedChild casted = (ObjectNestedChild) object;
        Assert.assertEquals(casted.nested, "nestedValue");
        Assert.assertEquals(casted.nestedChild, "nestedChildValue");
    }

    private Map<String, Object> createObjectWithKnownType()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@type", "ObjectWithTypeA");
        map.put("base", "objectWithKnownType_base");
        map.put("a", "objectWithKnownType_a");
        return map;
    }

    private void assertObjectWithKnownType(Object object)
    {
        Assert.assertEquals(object.getClass(), ObjectWithTypeA.class);
        ObjectWithTypeA casted = (ObjectWithTypeA) object;
        Assert.assertEquals("objectWithKnownType_base", casted.base);
        Assert.assertEquals("objectWithKnownType_a", casted.a);
    }

    private Map<String, Object> createObjectWithKnownUniqueClass()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@class", ".LegacyObjectWithTypeAA");
        map.put("base", "objectWithKnownUniqueClass_base");
        map.put("a", "objectWithKnownUniqueClass_a");
        map.put("aa", "objectWithKnownUniqueClass_aa");
        return map;
    }

    private void assertObjectWithKnownUniqueClass(Object object)
    {
        Assert.assertEquals(object.getClass(), ObjectWithTypeAA.class);
        ObjectWithTypeAA casted = (ObjectWithTypeAA) object;
        Assert.assertEquals("objectWithKnownUniqueClass_base", casted.base);
        Assert.assertEquals("objectWithKnownUniqueClass_a", casted.a);
        Assert.assertEquals("objectWithKnownUniqueClass_aa", casted.aa);
    }

    private Map<String, Object> createObjectWithKnownButNotUniqueClass()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@class", ".LegacyObjectWithTypeA");
        map.put("base", "objectWithKnownButNotUniqueClass_base");
        map.put("a", "objectWithKnownButNotUniqueClass_a");
        return map;
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private void assertObjectWithKnownButNotUniqueClass(Object object)
    {
        Assert.assertTrue(object instanceof Map);
        Assert.assertEquals(new HashMap((Map) object), createObjectWithKnownButNotUniqueClass());
    }

    private Map<String, Object> createObjectWithUnknownType()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@type", "ObjectWithTypeUnknown");
        map.put("base", "objectWithUnknownType_base");
        map.put("a", "objectWithUnknownType_a");
        return map;
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private void assertObjectWithUnknownType(Object object)
    {
        Assert.assertTrue(object instanceof Map);
        Assert.assertEquals(new HashMap((Map) object), createObjectWithUnknownType());
    }

    private Map<String, Object> createObjectWithUnknownClass()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@class", ".LegacyObjectWithTypeUnknown");
        map.put("base", "objectWithUnknownClass_base");
        map.put("a", "objectWithUnknownClass_a");
        return map;
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private void assertObjectWithUnknownClass(Object object)
    {
        Assert.assertTrue(object instanceof Map);
        Assert.assertEquals(new HashMap((Map) object), createObjectWithUnknownClass());
    }

    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    private Map<String, Object> createObjectWithContainerTypes(boolean includeType,
            boolean includeClass)
    {
        Map map = new LinkedHashMap();
        map.put("itemWithKnownType", createObjectWithKnownType());
        map.put("itemWithKnownUniqueClass", createObjectWithKnownUniqueClass());
        map.put("itemWithKnownButNotUniqueClass", createObjectWithKnownButNotUniqueClass());
        map.put("itemWithUnknownType", createObjectWithUnknownType());
        map.put("itemWithUnknownClass", createObjectWithUnknownClass());
        map.put("itemWithPrivateAccess", createObjectWithPrivateAccess(includeType, includeClass));
        map.put("itemWithPrimitiveTypes", createObjectWithPrimitiveTypes(includeType, includeClass));
        map.put("itemWithNestedTypes", createObjectWithNestedTypes(includeType, includeClass));
        map.put("itemWithEnumTypes", createObjectWithEnumTypes(includeType, includeClass));
        map.put("itemWithDateTypes", createObjectWithDateTypes(includeType, includeClass));
        map.put("itemWithIgnoredProperties",
                createObjectWithIgnoredProperties(includeType, includeClass));
        map.put("itemWithRenamedProperties",
                createObjectWithRenamedProperties(includeType, includeClass));

        Collection collection = new ArrayList(map.values());
        List list = new ArrayList(map.values());
        Object[] array = map.values().toArray(new Object[map.size()]);

        Map specificMap = new LinkedHashMap();
        specificMap.put("itemWithKnownType", createObjectWithKnownType());
        specificMap.put("itemWithKnownUniqueClass", createObjectWithKnownUniqueClass());

        Collection specificCollection = new ArrayList(specificMap.values());
        List specificList = new ArrayList(specificMap.values());
        Object[] specificArray = specificMap.values().toArray(new Object[specificMap.size()]);

        Map<String, Object> object = new HashMap<String, Object>();

        object.put("@type", "ObjectWithContainerTypes");
        object.put("collectionWithoutType", collection);
        object.put("collectionWithObjectType", collection);
        object.put("collectionWithSpecificType", specificCollection);

        object.put("linkedHashSetWithoutType", collection);
        object.put("linkedHashSetWithObjectType", collection);
        object.put("linkedHashSetWithSpecificType", specificCollection);

        object.put("mapWithoutType", map);
        object.put("mapWithObjectType", map);
        object.put("mapWithSpecificType", specificMap);

        object.put("linkedHashMapWithoutType", map);
        object.put("linkedHashMapWithObjectType", map);
        object.put("linkedHashMapWithSpecificType", specificMap);

        object.put("listWithoutType", list);
        object.put("listWithObjectType", list);
        object.put("listWithSpecificType", specificList);

        object.put("linkedListWithoutType", list);
        object.put("linkedListWithObjectType", list);
        object.put("linkedListWithSpecificType", specificList);

        object.put("arrayWithObjectType", array);
        object.put("arrayWithSpecificType", specificArray);

        return object;
    }

    private void assertObjectWithContainerTypes(Object object)
    {
        Assert.assertEquals(object.getClass(), ObjectWithContainerTypes.class);
        ObjectWithContainerTypes objectWithContainers = (ObjectWithContainerTypes) object;

        assertObjectsCollection(objectWithContainers.collectionWithObjectType);
        assertObjectsCollection(objectWithContainers.collectionWithoutType);
        assertObjectsSpecificCollection(objectWithContainers.collectionWithSpecificType);

        assertObjectsCollection(objectWithContainers.linkedHashSetWithObjectType);
        assertObjectsCollection(objectWithContainers.linkedHashSetWithoutType);
        assertObjectsSpecificCollection(objectWithContainers.linkedHashSetWithSpecificType);

        assertObjectsMap(objectWithContainers.mapWithObjectType);
        assertObjectsMap(objectWithContainers.mapWithoutType);
        assertObjectsSpecificMap(objectWithContainers.mapWithSpecificType);

        assertObjectsMap(objectWithContainers.linkedHashMapWithObjectType);
        assertObjectsMap(objectWithContainers.linkedHashMapWithoutType);
        assertObjectsSpecificMap(objectWithContainers.linkedHashMapWithSpecificType);

        assertObjectsCollection(objectWithContainers.listWithObjectType);
        assertObjectsCollection(objectWithContainers.listWithoutType);
        assertObjectsSpecificCollection(objectWithContainers.listWithSpecificType);

        assertObjectsCollection(objectWithContainers.linkedListWithObjectType);
        assertObjectsCollection(objectWithContainers.linkedListWithoutType);
        assertObjectsSpecificCollection(objectWithContainers.linkedListWithSpecificType);

        assertObjectsArray(objectWithContainers.arrayWithObjectType);
        assertObjectsSpecificArray(objectWithContainers.arrayWithSpecificType);
    }

    @SuppressWarnings(
        { "rawtypes" })
    private void assertObjectsCollection(Collection collection)
    {
        Assert.assertEquals(collection.size(), 12);
        Iterator iterator = collection.iterator();
        assertObjectWithKnownType(iterator.next());
        assertObjectWithKnownUniqueClass(iterator.next());
        assertObjectWithKnownButNotUniqueClass(iterator.next());
        assertObjectWithUnknownType(iterator.next());
        assertObjectWithUnknownClass(iterator.next());
        assertObjectWithPrivateAccess(iterator.next());
        assertObjectWithPrimitiveTypes(iterator.next());
        assertObjectWithNestedTypes(iterator.next());
        assertObjectWithEnumTypes(iterator.next());
        assertObjectWithDateTypes(iterator.next());
        assertObjectWithIgnoredProperties(iterator.next());
        assertObjectWithRenamedProperties(iterator.next());
    }

    @SuppressWarnings(
        { "rawtypes" })
    private void assertObjectsSpecificCollection(Collection collection)
    {
        Assert.assertEquals(collection.size(), 2);
        Iterator iterator = collection.iterator();
        assertObjectWithKnownType(iterator.next());
        assertObjectWithKnownUniqueClass(iterator.next());
    }

    @SuppressWarnings(
        { "rawtypes" })
    private void assertObjectsMap(Map map)
    {
        Assert.assertEquals(map.size(), 12);
        assertObjectWithKnownType(map.get("itemWithKnownType"));
        assertObjectWithKnownUniqueClass(map.get("itemWithKnownUniqueClass"));
        assertObjectWithKnownButNotUniqueClass(map.get("itemWithKnownButNotUniqueClass"));
        assertObjectWithUnknownType(map.get("itemWithUnknownType"));
        assertObjectWithUnknownClass(map.get("itemWithUnknownClass"));
        assertObjectWithPrivateAccess(map.get("itemWithPrivateAccess"));
        assertObjectWithPrimitiveTypes(map.get("itemWithPrimitiveTypes"));
        assertObjectWithNestedTypes(map.get("itemWithNestedTypes"));
        assertObjectWithEnumTypes(map.get("itemWithEnumTypes"));
        assertObjectWithDateTypes(map.get("itemWithDateTypes"));
        assertObjectWithIgnoredProperties(map.get("itemWithIgnoredProperties"));
        assertObjectWithRenamedProperties(map.get("itemWithRenamedProperties"));
    }

    @SuppressWarnings(
        { "rawtypes" })
    private void assertObjectsSpecificMap(Map map)
    {
        Assert.assertEquals(map.size(), 2);
        assertObjectWithKnownType(map.get("itemWithKnownType"));
        assertObjectWithKnownUniqueClass(map.get("itemWithKnownUniqueClass"));
    }

    private void assertObjectsArray(Object[] array)
    {
        assertObjectsCollection(Arrays.asList(array));
    }

    private void assertObjectsSpecificArray(Object[] array)
    {
        assertObjectsSpecificCollection(Arrays.asList(array));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(Object object, Class<?> rootClass) throws Exception
    {
        ObjectMapper mapper = new JsonTestObjectMapper();
        Object result = mapper.readValue(mapper.writeValueAsString(object), rootClass);
        return (T) result;
    }

}
