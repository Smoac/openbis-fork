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

package ch.systemsx.cisd.common.api.server.json.object;

import org.testng.Assert;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
@SuppressWarnings("hiding")
@JsonObject(ObjectWithNestedTypes.TYPE)
public class ObjectWithNestedTypes
{

    public static final String TYPE = "ObjectWithNestedTypes";

    public static final String CLASS = ".LegacyObjectWithNestedTypes";

    public static final String PROPERTY_NESTED = "propertyNested";

    public static final String PROPERTY_NESTED_CHILD = "propertyNestedChild";

    // TODO: check why it doesn't work properly during both serialization and deserialization
    // public Object propertyObject;

    public ObjectNested propertyNested;

    public ObjectNestedChild propertyNestedChild;

    @JsonObject(ObjectNested.TYPE)
    public static class ObjectNested
    {
        public static final String TYPE = "ObjectNested";

        public static final String CLASS = ".LegacyObjectNested";

        public static final String NESTED = "nested";

        public static final String NESTED_VALUE = "nestedValue";

        public String nested;

        public static ObjectNested createObject()
        {
            ObjectNested object = new ObjectNested();
            object.nested = NESTED_VALUE;
            return object;
        }

        public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
        {
            ObjectMap map = new ObjectMap();
            map.putId(objectCounter);
            map.putType(TYPE, CLASS, objectType);
            map.putField(NESTED, NESTED_VALUE);
            return map;
        }

        @Override
        public boolean equals(Object obj)
        {
            Assert.assertNotNull(obj);
            Assert.assertEquals(getClass(), obj.getClass());

            ObjectNested casted = (ObjectNested) obj;
            Assert.assertEquals(nested, casted.nested);
            return true;
        }

    }

    @JsonObject(ObjectNestedChild.TYPE)
    public static class ObjectNestedChild extends ObjectNested
    {
        public static final String TYPE = "ObjectNestedChild";

        public static final String CLASS = ".LegacyObjectNestedChild";

        public static final String NESTED_CHILD = "nestedChild";

        public static final String NESTED_CHILD_VALUE = "nestedChildValue";

        public String nestedChild;

        public static ObjectNestedChild createObject()
        {
            ObjectNestedChild object = new ObjectNestedChild();
            object.nested = NESTED_VALUE;
            object.nestedChild = NESTED_CHILD_VALUE;
            return object;
        }

        public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
        {
            ObjectMap map = new ObjectMap();
            map.putId(objectCounter);
            map.putType(TYPE, CLASS, objectType);
            map.putField(NESTED, NESTED_VALUE);
            map.putField(NESTED_CHILD, NESTED_CHILD_VALUE);
            return map;
        }

        @Override
        public boolean equals(Object obj)
        {
            Assert.assertNotNull(obj);
            Assert.assertEquals(getClass(), obj.getClass());

            ObjectNestedChild casted = (ObjectNestedChild) obj;
            Assert.assertEquals(nested, casted.nested);
            Assert.assertEquals(nestedChild, casted.nestedChild);
            return true;
        }

    }

    public static ObjectWithNestedTypes createObject()
    {
        ObjectWithNestedTypes object = new ObjectWithNestedTypes();
        object.propertyNested = ObjectNested.createObject();
        object.propertyNestedChild = ObjectNestedChild.createObject();
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(PROPERTY_NESTED, ObjectNested.createMap(objectCounter, objectType).toMap());
        map.putField(PROPERTY_NESTED_CHILD, ObjectNestedChild.createMap(objectCounter, objectType)
                .toMap());
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithNestedTypes casted = (ObjectWithNestedTypes) obj;
        Assert.assertEquals(propertyNested, casted.propertyNested);
        Assert.assertEquals(propertyNestedChild, casted.propertyNestedChild);
        return true;
    }

}
