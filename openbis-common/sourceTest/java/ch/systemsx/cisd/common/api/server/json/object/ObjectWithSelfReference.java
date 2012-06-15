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
@JsonObject(ObjectWithSelfReference.TYPE)
public class ObjectWithSelfReference
{

    public static final String TYPE = "ObjectWithSelfReference";

    public static final String CLASS = ".LegacyObjectWithSelfReference";

    public static final String SELF_REFERENCE = "selfReference";

    public ObjectWithSelfReference selfReference;

    public static ObjectWithSelfReference createObject()
    {
        ObjectWithSelfReference object = new ObjectWithSelfReference();
        object.selfReference = new ObjectWithSelfReference();
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);

        ObjectMap selfReference = new ObjectMap();
        selfReference.putId(objectCounter);
        selfReference.putType(TYPE, CLASS, objectType);
        selfReference.putField(SELF_REFERENCE, null);

        map.putField(SELF_REFERENCE, selfReference.toMap());

        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithSelfReference casted = (ObjectWithSelfReference) obj;
        Assert.assertEquals(selfReference, casted.selfReference);
        return true;
    }

}
