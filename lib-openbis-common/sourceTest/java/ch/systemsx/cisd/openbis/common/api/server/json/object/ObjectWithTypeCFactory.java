/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.common.api.server.json.object;

import static ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeFactory.BASE;
import static ch.systemsx.cisd.openbis.common.api.server.json.object.ObjectWithTypeFactory.BASE_VALUE;

import java.util.Map;

import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.openbis.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
public class ObjectWithTypeCFactory extends ObjectFactory<ObjectWithTypeC>
{

    public static final String TYPE = "ObjectWithTypeC";

    public static final String CLASS = ".LegacyObjectWithTypeC";

    public static final String C = "c";

    public static final String C_VALUE = "cValue";

    public String c;

    @Override
    public ObjectWithTypeC createObject()
    {
        ObjectWithTypeC object = new ObjectWithTypeC();
        object.base = BASE_VALUE;
        object.c = C_VALUE;
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(BASE, BASE_VALUE);
        map.putField(C, C_VALUE);
        return map.toMap();
    }

}
