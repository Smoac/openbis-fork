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

@JsonObject(ObjectWithPrivateAccess.TYPE)
public class ObjectWithPrivateAccess
{

    public static final String TYPE = "ObjectWithPrivateAccess";

    public static final String CLASS = ".LegacyObjectWithPrivateAccess";

    public static final String FIELD = "field";

    public static final String FIELD_VALUE = "fieldValue";

    private String field;

    private ObjectWithPrivateAccess()
    {
    }

    public String getField()
    {
        return field;
    }

    @SuppressWarnings("unused")
    private void setField(String field)
    {
        this.field = field;
    }

    public static ObjectWithPrivateAccess createObject()
    {
        ObjectWithPrivateAccess object = new ObjectWithPrivateAccess();
        object.field = FIELD_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField("field", "fieldValue");
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithPrivateAccess casted = (ObjectWithPrivateAccess) obj;
        Assert.assertEquals(field, casted.field);
        return true;
    }

}
