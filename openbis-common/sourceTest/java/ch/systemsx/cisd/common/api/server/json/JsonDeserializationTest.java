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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithContainerTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithDateTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes;
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
        testDeserializeObjectWithType(ObjectType.TYPE, ObjectWithTypeInterface1.class);
        testDeserializeObjectWithType(ObjectType.CLASS, ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithType(ObjectType.TYPE, ObjectWithTypeInterface2.class);
        testDeserializeObjectWithType(ObjectType.CLASS, ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithRootTypeToRootType() throws Exception
    {
        testDeserializeObjectWithType(ObjectType.TYPE, ObjectWithType.class);
        testDeserializeObjectWithType(ObjectType.CLASS, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.TYPE, ObjectWithTypeInterface1.class);
        testDeserializeObjectWithTypeA(ObjectType.CLASS, ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.TYPE, ObjectWithTypeInterface2.class);
        testDeserializeObjectWithTypeA(ObjectType.CLASS, ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToRootType() throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.TYPE, ObjectWithType.class);
        testDeserializeObjectWithTypeA(ObjectType.CLASS, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithFirstLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.TYPE, ObjectWithTypeA.class);
        testDeserializeObjectWithTypeA(ObjectType.CLASS, ObjectWithTypeA.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithFirstLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.TYPE, ObjectWithTypeAA.class);
        testDeserializeObjectWithTypeA(ObjectType.CLASS, ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface1() throws Exception
    {
        testDeserializeObjectWithTypeAA(ObjectType.TYPE, ObjectWithTypeInterface1.class);
        testDeserializeObjectWithTypeAA(ObjectType.CLASS, ObjectWithTypeInterface1.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootInterface2() throws Exception
    {
        testDeserializeObjectWithTypeAA(ObjectType.TYPE, ObjectWithTypeInterface2.class);
        testDeserializeObjectWithTypeAA(ObjectType.CLASS, ObjectWithTypeInterface2.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToRootType() throws Exception
    {
        testDeserializeObjectWithTypeAA(ObjectType.TYPE, ObjectWithType.class);
        testDeserializeObjectWithTypeAA(ObjectType.CLASS, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToFirstLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeAA(ObjectType.TYPE, ObjectWithTypeA.class);
        testDeserializeObjectWithTypeAA(ObjectType.CLASS, ObjectWithTypeA.class);
    }

    @Test
    public void testDeserializeJsonWithSecondLevelSubTypeToSecondLevelSubType() throws Exception
    {
        testDeserializeObjectWithTypeAA(ObjectType.TYPE, ObjectWithTypeAA.class);
        testDeserializeObjectWithTypeAA(ObjectType.CLASS, ObjectWithTypeAA.class);
    }

    @Test
    public void testDeserializeJsonWithNestedRootTypeToNestedRootType() throws Exception
    {
        testDeserializeObjectNested(ObjectType.TYPE, ObjectNested.class);
        testDeserializeObjectNested(ObjectType.CLASS, ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedRootType() throws Exception
    {
        testDeserializeObjectNestedChild(ObjectType.TYPE, ObjectNested.class);
        testDeserializeObjectNestedChild(ObjectType.CLASS, ObjectNested.class);
    }

    @Test
    public void testDeserializeJsonWithNestedSubTypeToNestedSubType() throws Exception
    {
        testDeserializeObjectNestedChild(ObjectType.TYPE, ObjectNestedChild.class);
        testDeserializeObjectNestedChild(ObjectType.CLASS, ObjectNestedChild.class);
    }

    @Test
    public void testDeserializeJsonWithObjectWithPrimitiveTypes() throws Exception
    {
        testDeserializeObjectWithPrimitiveTypes(ObjectType.TYPE);
        testDeserializeObjectWithPrimitiveTypes(ObjectType.CLASS);
    }

    @Test
    public void testDeserializeJsonWithObjectWithNestedTypes() throws Exception
    {
        testDeserializeObjectWithNestedTypes(ObjectType.TYPE);
        testDeserializeObjectWithNestedTypes(ObjectType.CLASS);
    }

    @Test
    public void testDeserializeJsonWithObjectWithEnumTypes() throws Exception
    {
        testDeserializeObjectWithEnumTypes(ObjectType.TYPE);
        testDeserializeObjectWithEnumTypes(ObjectType.CLASS);
    }

    @Test
    public void testDeserializeJsonWithObjectWithDateTypes() throws Exception
    {
        testDeserializeObjectWithDateTypes(ObjectType.TYPE);
        testDeserializeObjectWithDateTypes(ObjectType.CLASS);
    }

    @Test
    public void testDeserializeJsonWithObjectWithContainerTypes() throws Exception
    {
        testDeserializeObjectWithContainerTypes(ObjectType.TYPE);
        testDeserializeObjectWithContainerTypes(ObjectType.CLASS);
    }

    @Test
    public void testDeserializeJsonWithIgnoredProperties() throws Exception
    {
        testDeserializeObjectWithIgnoredProperties(ObjectType.TYPE);
        testDeserializeObjectWithIgnoredProperties(ObjectType.CLASS);
    }

    @Test
    public void testDeserializeJsonWithRenamedProperties() throws Exception
    {
        testDeserializeObjectWithRenamedProperties(ObjectType.TYPE);
        testDeserializeObjectWithRenamedProperties(ObjectType.CLASS);
    }

    @Test
    public void testDeserializeJsonWithPrivateAccess() throws Exception
    {
        testDeserializeObjectWithPrivateAccess(ObjectType.TYPE);
        testDeserializeObjectWithPrivateAccess(ObjectType.CLASS);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithoutTypeToPolymorphicType() throws Exception
    {
        testDeserializeObjectWithType(ObjectType.NONE, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToPolymorphicType() throws Exception
    {
        testDeserializeObjectWithType(ObjectType.TYPE, ObjectWithType.class);
        testDeserializeObjectWithType(ObjectType.CLASS, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithoutTypeToNotPolymorphicType() throws Exception
    {
        testDeserializeObjectWithTypeButNoSubtypes(ObjectType.NONE,
                ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithTypeToNotPolymorphicType() throws Exception
    {
        testDeserializeObjectWithTypeButNoSubtypes(ObjectType.TYPE,
                ObjectWithTypeButNoSubtypes.class);
        testDeserializeObjectWithTypeButNoSubtypes(ObjectType.CLASS,
                ObjectWithTypeButNoSubtypes.class);
    }

    @Test
    public void testDeserializeJsonWithLegacyClassDefinedInClassMapping() throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.CLASS, ObjectWithType.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMapping() throws Exception
    {
        testDeserializeObjectWithTypeC(ObjectType.CLASS, ObjectWithType.class);
    }

    @Test(expectedExceptions = Exception.class)
    public void testDeserializeJsonWithLegacyClassNotDefinedInClassMappingButMatchingType()
            throws Exception
    {
        ObjectMap map = ObjectWithTypeC.createMap(null, ObjectType.NONE);
        map.putType(null, ".ObjectWithTypeC", ObjectType.CLASS);
        deserialize(map.toMap(), ObjectWithTypeC.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInDifferentInheritanceTrees()
            throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.CLASS, ObjectWithType.class);
    }

    @Test(expectedExceptions = JsonMappingException.class)
    public void testDeserializeJsonWithNotUniqueLegacyClassUsedInSameInheritanceTree()
            throws Exception
    {
        ObjectMap map = ObjectWithTypeB.createMap(null, ObjectType.CLASS);
        deserialize(map.toMap(), ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueTypeUsedInDifferentInheritanceTrees()
            throws Exception
    {
        testDeserializeObjectWithTypeA(ObjectType.TYPE, ObjectWithType.class);
    }

    @Test
    public void testDeserializeJsonWithNotUniqueTypeUsedInSameInheritanceTree() throws Exception
    {
        ObjectMap map = ObjectWithTypeB.createMap(null, ObjectType.TYPE);
        Object result = deserialize(map.toMap(), ObjectWithType.class);
        Assert.assertFalse(ObjectWithTypeB.class.equals(result.getClass()));
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInCollection() throws Exception
    {
        testDeserializeJsonWithNestedMapsInCollection(ObjectType.TYPE);
        testDeserializeJsonWithNestedMapsInCollection(ObjectType.CLASS);
    }

    private void testDeserializeJsonWithNestedMapsInCollection(ObjectType includeType)
            throws Exception
    {
        ObjectWithContainerTypes object =
                ObjectWithContainerTypes.createObjectWithIncorrectObjectsAsMaps();
        ObjectMap map = ObjectWithContainerTypes.createMap(null, includeType);

        List<Object> collectionWithObject = new ArrayList<Object>();
        collectionWithObject.add(object);

        List<Object> collectionWithMap = new ArrayList<Object>();
        collectionWithMap.add(map.toMap());

        Object result = deserialize(collectionWithMap, List.class);
        Assert.assertTrue(collectionWithObject.equals(result));
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInMap() throws Exception
    {
        testDeserializeJsonWithNestedMapsInMap(ObjectType.TYPE);
        testDeserializeJsonWithNestedMapsInMap(ObjectType.CLASS);
    }

    private void testDeserializeJsonWithNestedMapsInMap(ObjectType includeType) throws Exception
    {
        ObjectWithContainerTypes object =
                ObjectWithContainerTypes.createObjectWithIncorrectObjectsAsMaps();
        ObjectMap map = ObjectWithContainerTypes.createMap(null, includeType);

        Map<String, Object> mapWithObject = new HashMap<String, Object>();
        mapWithObject.put("object", object);

        Map<String, Object> mapWithMap = new HashMap<String, Object>();
        mapWithMap.put("object", map.toMap());

        Object result = deserialize(mapWithMap, Map.class);
        Assert.assertTrue(mapWithObject.equals(result));
    }

    @Test
    public void testDeserializeJsonWithNestedMapsInList() throws Exception
    {
        testDeserializeJsonWithNestedMapsInList(ObjectType.TYPE);
        testDeserializeJsonWithNestedMapsInList(ObjectType.CLASS);
    }

    private void testDeserializeJsonWithNestedMapsInList(ObjectType includeType) throws Exception
    {
        ObjectWithContainerTypes object =
                ObjectWithContainerTypes.createObjectWithIncorrectObjectsAsMaps();
        ObjectMap map = ObjectWithContainerTypes.createMap(null, includeType);

        List<Object> listWithObject = new ArrayList<Object>();
        listWithObject.add(object);

        List<Object> listWithMap = new ArrayList<Object>();
        listWithMap.add(map.toMap());

        Object result = deserialize(listWithMap, List.class);
        Assert.assertTrue(listWithObject.equals(result));
    }

    private void testDeserializeObjectWithType(ObjectType includeType, Class<?> rootClass)
            throws Exception
    {
        ObjectWithType object = ObjectWithType.createObject();
        ObjectMap map = ObjectWithType.createMap(null, includeType);

        Object result = deserialize(map.toMap(), rootClass);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithTypeA(ObjectType includeType, Class<?> rootClass)
            throws Exception
    {
        ObjectWithTypeA object = ObjectWithTypeA.createObject();
        ObjectMap map = ObjectWithTypeA.createMap(null, includeType);

        Object result = deserialize(map.toMap(), rootClass);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithTypeC(ObjectType includeType, Class<?> rootClass)
            throws Exception
    {
        ObjectWithTypeC object = ObjectWithTypeC.createObject();
        ObjectMap map = ObjectWithTypeC.createMap(null, includeType);

        Object result = deserialize(map.toMap(), rootClass);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithTypeAA(ObjectType includeType, Class<?> rootClass)
            throws Exception
    {
        ObjectWithTypeAA object = ObjectWithTypeAA.createObject();
        ObjectMap map = ObjectWithTypeAA.createMap(null, includeType);

        Object result = deserialize(map.toMap(), rootClass);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithTypeButNoSubtypes(ObjectType includeType,
            Class<?> rootClass) throws Exception
    {
        ObjectWithTypeButNoSubtypes object = ObjectWithTypeButNoSubtypes.createObject();
        ObjectMap map = ObjectWithTypeButNoSubtypes.createMap(null, includeType);

        Object result = deserialize(map.toMap(), rootClass);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithIgnoredProperties(ObjectType includeType)
            throws Exception
    {
        ObjectWithIgnoredProperties object =
                ObjectWithIgnoredProperties.createObjectWithIgnoredPropertiesNull();
        ObjectMap map = ObjectWithIgnoredProperties.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithIgnoredProperties.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithRenamedProperties(ObjectType includeType)
            throws Exception
    {
        ObjectWithRenamedProperties object = ObjectWithRenamedProperties.createObject();
        ObjectMap map = ObjectWithRenamedProperties.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithRenamedProperties.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithPrivateAccess(ObjectType includeType) throws Exception
    {
        ObjectWithPrivateAccess object = ObjectWithPrivateAccess.createObject();
        ObjectMap map = ObjectWithPrivateAccess.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithPrivateAccess.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithPrimitiveTypes(ObjectType includeType) throws Exception
    {
        ObjectWithPrimitiveTypes object = ObjectWithPrimitiveTypes.createObject();
        ObjectMap map = ObjectWithPrimitiveTypes.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithPrimitiveTypes.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithEnumTypes(ObjectType includeType) throws Exception
    {
        ObjectWithEnumTypes object = ObjectWithEnumTypes.createObject();
        ObjectMap map = ObjectWithEnumTypes.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithEnumTypes.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithDateTypes(ObjectType includeType) throws Exception
    {
        ObjectWithDateTypes object = ObjectWithDateTypes.createObject();
        ObjectMap map = ObjectWithDateTypes.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithDateTypes.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithContainerTypes(ObjectType includeType) throws Exception
    {
        ObjectWithContainerTypes object =
                ObjectWithContainerTypes.createObjectWithIncorrectObjectsAsMaps();
        ObjectMap map = ObjectWithContainerTypes.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithContainerTypes.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectWithNestedTypes(ObjectType includeType) throws Exception
    {
        ObjectWithNestedTypes object = ObjectWithNestedTypes.createObject();
        ObjectMap map = ObjectWithNestedTypes.createMap(null, includeType);

        Object result = deserialize(map.toMap(), ObjectWithNestedTypes.class);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectNested(ObjectType includeType, Class<?> rootClass)
            throws Exception
    {
        ObjectNested object = ObjectNested.createObject();
        ObjectMap map = ObjectNested.createMap(null, includeType);

        Object result = deserialize(map.toMap(), rootClass);
        Assert.assertTrue(object.equals(result));
    }

    private void testDeserializeObjectNestedChild(ObjectType includeType, Class<?> rootClass)
            throws Exception
    {
        ObjectNestedChild object = ObjectNestedChild.createObject();
        ObjectMap map = ObjectNestedChild.createMap(null, includeType);

        Object result = deserialize(map.toMap(), rootClass);
        Assert.assertTrue(object.equals(result));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(Object object, Class<?> rootClass) throws Exception
    {
        ObjectMapper mapper = new JsonTestObjectMapper();
        Object result = mapper.readValue(mapper.writeValueAsString(object), rootClass);
        return (T) result;
    }

}
