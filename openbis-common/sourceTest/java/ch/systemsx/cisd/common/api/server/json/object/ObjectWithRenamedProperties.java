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

import com.fasterxml.jackson.annotation.JsonProperty;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
@JsonObject(ObjectWithRenamedProperties.TYPE)
public class ObjectWithRenamedProperties
{

    public static final String TYPE = "ObjectWithRenamedProperties";

    public static final String CLASS = ".LegacyObjectWithRenamedProperties";

    public static final String PROPERTY = "property";

    public static final String PROPERTY_VALUE = "propertyValue";

    public static final String PROPERTY_RENAMED = "propertyRenamed";

    public static final String PROPERTY_RENAMED_VALUE = "propertyRenamedValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER = "propertyWithGetterAndSetter";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_VALUE =
            "propertyWithGetterAndSetterValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_RENAMED =
            "propertyWithGetterAndSetterRenamed";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_RENAMED_VALUE =
            "propertyWithGetterAndSetterRenamedValue";

    public String property;

    private String propertyWithGetterAndSetter;

    @JsonProperty("propertyRenamed")
    public String x;

    private String y;

    public String getPropertyWithGetterAndSetter()
    {
        return propertyWithGetterAndSetter;
    }

    public void setPropertyWithGetterAndSetter(String propertyWithGetterAndSetter)
    {
        this.propertyWithGetterAndSetter = propertyWithGetterAndSetter;
    }

    public String getY()
    {
        return y;
    }

    @JsonProperty("propertyWithGetterAndSetterRenamed")
    public void setY(String y)
    {
        this.y = y;
    }

    public static ObjectWithRenamedProperties createObject()
    {
        ObjectWithRenamedProperties object = new ObjectWithRenamedProperties();
        object.property = PROPERTY_VALUE;
        object.x = PROPERTY_RENAMED_VALUE;
        object.propertyWithGetterAndSetter = PROPERTY_WITH_GETTER_AND_SETTER_VALUE;
        object.y = PROPERTY_WITH_GETTER_AND_SETTER_RENAMED_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(PROPERTY, PROPERTY_VALUE);
        map.putField(PROPERTY_RENAMED, PROPERTY_RENAMED_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER, PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER_RENAMED,
                PROPERTY_WITH_GETTER_AND_SETTER_RENAMED_VALUE);
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithRenamedProperties casted = (ObjectWithRenamedProperties) obj;
        Assert.assertEquals(property, casted.property);
        Assert.assertEquals(x, casted.x);
        Assert.assertEquals(propertyWithGetterAndSetter, casted.propertyWithGetterAndSetter);
        Assert.assertEquals(y, casted.y);
        return true;
    }

}
