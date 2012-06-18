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

import java.util.Map;

import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

import static ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeFactory.*;

/**
 * @author pkupczyk
 */
public class ObjectWithTypeBFactory extends ObjectFactory<ObjectWithTypeB>
{

    public static final String TYPE = "ObjectWithTypeB";

    public static final String CLASS = ".LegacyObjectWithTypeB";

    public static final String B = "b";

    public static final String B_VALUE = "bValue";

    @Override
    public ObjectWithTypeB createObject()
    {
        ObjectWithTypeB object = new ObjectWithTypeB();
        object.base = BASE_VALUE;
        object.b = B_VALUE;
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(BASE, BASE_VALUE);
        map.putField(B, B_VALUE);
        return map.toMap();
    }

}
