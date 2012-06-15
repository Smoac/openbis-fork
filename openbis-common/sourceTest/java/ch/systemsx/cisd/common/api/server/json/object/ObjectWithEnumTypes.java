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

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;

import org.testng.Assert;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */

@JsonObject(ObjectWithEnumTypes.TYPE)
public class ObjectWithEnumTypes
{

    public static final String TYPE = "ObjectWithEnumTypes";

    public static final String CLASS = ".LegacyObjectWithEnumTypes";

    public static final String ENUM_FIELD = "enumField";

    public static final NestedEnum ENUM_FIELD_VALUE = NestedEnum.VALUE1;

    public static final String ENUM_SET = "enumSet";

    public static final EnumSet<NestedEnum> ENUM_SET_VALUE = EnumSet.of(NestedEnum.VALUE1,
            NestedEnum.VALUE3);

    public static final String ENUM_MAP = "enumMap";

    public static final EnumMap<NestedEnum, Object> ENUM_MAP_VALUE =
            new EnumMap<NestedEnum, Object>(Collections.singletonMap(NestedEnum.VALUE2, "value2"));

    public NestedEnum enumField;

    public EnumSet<NestedEnum> enumSet;

    public EnumMap<NestedEnum, Object> enumMap;

    @JsonObject("NestedEnum")
    public enum NestedEnum
    {
        VALUE1, VALUE2, VALUE3
    }

    public static ObjectWithEnumTypes createObject()
    {
        ObjectWithEnumTypes object = new ObjectWithEnumTypes();
        object.enumField = ENUM_FIELD_VALUE;
        object.enumSet = ENUM_SET_VALUE;
        object.enumMap = ENUM_MAP_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(ENUM_FIELD, ENUM_FIELD_VALUE);
        map.putField(ENUM_SET, ENUM_SET_VALUE);
        map.putField(ENUM_MAP, ENUM_MAP_VALUE);
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithEnumTypes casted = (ObjectWithEnumTypes) obj;
        Assert.assertEquals(enumField, casted.enumField);
        Assert.assertEquals(enumSet, casted.enumSet);
        Assert.assertEquals(enumMap, casted.enumMap);
        return true;
    }

}
