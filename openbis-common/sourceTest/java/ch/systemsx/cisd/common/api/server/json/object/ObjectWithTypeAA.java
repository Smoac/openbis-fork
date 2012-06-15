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
@JsonObject(ObjectWithTypeAA.TYPE)
public class ObjectWithTypeAA extends ObjectWithTypeA
{

    public static final String TYPE = "ObjectWithTypeAA";

    public static final String CLASS = ".LegacyObjectWithTypeAA";

    public static final String AA = "aa";

    public static final String AA_VALUE = "aaValue";

    public String aa;

    public static ObjectWithTypeAA createObject()
    {
        ObjectWithTypeAA object = new ObjectWithTypeAA();
        object.base = BASE_VALUE;
        object.a = A_VALUE;
        object.aa = AA_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(BASE, BASE_VALUE);
        map.putField(A, A_VALUE);
        map.putField(AA, AA_VALUE);
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithTypeAA casted = (ObjectWithTypeAA) obj;
        Assert.assertEquals(base, casted.base);
        Assert.assertEquals(a, casted.a);
        Assert.assertEquals(aa, casted.aa);
        return true;
    }

}
