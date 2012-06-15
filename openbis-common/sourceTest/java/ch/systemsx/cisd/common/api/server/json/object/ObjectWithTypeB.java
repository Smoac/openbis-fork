package ch.systemsx.cisd.common.api.server.json.object;

import org.testng.Assert;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

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

/**
 * @author pkupczyk
 */
@SuppressWarnings("hiding")
@JsonObject(ObjectWithTypeB.TYPE)
public class ObjectWithTypeB extends ObjectWithType
{

    public static final String TYPE = "ObjectWithTypeB";

    public static final String CLASS = ".LegacyObjectWithTypeB";

    public static final String B = "b";

    public static final String B_VALUE = "bValue";

    public String b;

    public static ObjectWithTypeB createObject()
    {
        ObjectWithTypeB object = new ObjectWithTypeB();
        object.base = BASE_VALUE;
        object.b = B_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(BASE, BASE_VALUE);
        map.putField(B, B_VALUE);
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithTypeB casted = (ObjectWithTypeB) obj;
        Assert.assertEquals(base, casted.base);
        Assert.assertEquals(b, casted.b);
        return true;
    }

}
