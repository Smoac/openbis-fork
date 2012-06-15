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

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes;
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
        ObjectWithType object = ObjectWithType.createObject();
        ObjectMap map = ObjectWithType.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeFirstLevelSubType() throws Exception
    {
        ObjectWithTypeA object = ObjectWithTypeA.createObject();
        ObjectMap map = ObjectWithTypeA.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeSecondLevelSubType() throws Exception
    {
        ObjectWithTypeAA object = ObjectWithTypeAA.createObject();
        ObjectMap map = ObjectWithTypeAA.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeNestedRootType() throws Exception
    {
        ObjectNested object = ObjectNested.createObject();
        ObjectMap map = ObjectNested.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeNestedSubType() throws Exception
    {
        ObjectNestedChild object = ObjectNestedChild.createObject();
        ObjectMap map = ObjectNestedChild.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializePolymorphicType() throws Exception
    {
        ObjectWithType object = ObjectWithType.createObject();
        ObjectMap map = ObjectWithType.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeNotPolymorphicType() throws Exception
    {
        ObjectWithTypeButNoSubtypes object = ObjectWithTypeButNoSubtypes.createObject();
        ObjectMap map = ObjectWithTypeButNoSubtypes.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeObjectWithPrimitiveTypes() throws Exception
    {
        ObjectWithPrimitiveTypes object = ObjectWithPrimitiveTypes.createObject();
        ObjectMap map = ObjectWithPrimitiveTypes.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeObjectWithNestedTypes() throws Exception
    {
        ObjectWithNestedTypes object = ObjectWithNestedTypes.createObject();
        ObjectMap map = ObjectWithNestedTypes.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    @Test
    public void testSerializeObjectWithEnumTypes() throws Exception
    {
        ObjectWithEnumTypes object = ObjectWithEnumTypes.createObject();
        ObjectMap map = ObjectWithEnumTypes.createMap(new ObjectCounter(), ObjectType.TYPE);
        serializeObjectAndMapAndCompare(object, map.toMap());
    }

    private void serializeObjectAndMapAndCompare(Object object, Map<String, Object> expectedMap)
            throws Exception
    {
        String jsonFromObject = new JsonTestObjectMapper().writeValueAsString(object);
        String jsonFromExpectedMap = new ObjectMapper().writeValueAsString(expectedMap);
        Assert.assertEquals(jsonFromObject, jsonFromExpectedMap);
    }
}
