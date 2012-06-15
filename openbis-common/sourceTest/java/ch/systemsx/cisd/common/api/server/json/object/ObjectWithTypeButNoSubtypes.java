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
@JsonObject(ObjectWithTypeButNoSubtypes.TYPE)
public class ObjectWithTypeButNoSubtypes
{

    public static final String TYPE = "ObjectWithTypeButNoSubtypes";

    public static final String CLASS = ".LegacyObjectWithTypeButNoSubtypes";

    public static final String A = "a";

    public static final String A_VALUE = "aValue";

    public static final String B = "b";

    public static final String B_VALUE = "bValue";

    public String a;

    public String b;

    public static ObjectWithTypeButNoSubtypes createObject()
    {
        ObjectWithTypeButNoSubtypes object = new ObjectWithTypeButNoSubtypes();
        object.a = A_VALUE;
        object.b = B_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField("a", "aValue");
        map.putField("b", "bValue");
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithTypeButNoSubtypes casted = (ObjectWithTypeButNoSubtypes) obj;
        Assert.assertEquals(a, casted.a);
        Assert.assertEquals(b, casted.b);
        return true;
    }

}
