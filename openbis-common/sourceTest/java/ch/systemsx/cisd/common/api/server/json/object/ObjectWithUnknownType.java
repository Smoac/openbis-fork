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

import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;

/**
 * @author pkupczyk
 */
public class ObjectWithUnknownType
{

    public static final String TYPE = "ObjectWithUnknownType";

    public static final String CLASS = ".LegacyObjectWithUnknownType";

    public static final String PROPERTY = "property";

    public static final String PROPERTY_VALUE = "propertyValue";

    public String property;

    public static ObjectWithUnknownType createObject()
    {
        ObjectWithUnknownType object = new ObjectWithUnknownType();
        object.property = PROPERTY_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(PROPERTY, PROPERTY_VALUE);
        return map;
    }

}
