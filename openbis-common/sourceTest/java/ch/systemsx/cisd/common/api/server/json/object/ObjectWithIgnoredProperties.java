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

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
@JsonObject(ObjectWithIgnoredProperties.TYPE)
public class ObjectWithIgnoredProperties
{

    public static final String TYPE = "ObjectWithIgnoredProperties";

    public static final String CLASS = ".LegacyObjectWithIgnoredProperties";

    public static final String PROPERTY = "property";

    public static final String PROPERTY_VALUE = "propertyValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER = "propertyWithGetterAndSetter";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_VALUE =
            "propertyWithGetterAndSetterValue";

    public static final String PROPERTY_IGNORED = "propertyIgnored";

    public static final String PROPERTY_IGNORED_VALUE = "propertyIgnoredValue";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_IGNORED =
            "propertyWithGetterAndSetterIgnored";

    public static final String PROPERTY_WITH_GETTER_AND_SETTER_IGNORED_VALUE =
            "propertyWithGetterAndSetterIgnoredValue";

    public String property;

    private String propertyWithGetterAndSetter;

    @JsonIgnore
    public String propertyIgnored;

    private String propertyWithGetterAndSetterIgnored;

    public void setPropertyWithGetterAndSetter(String propertyWithGetterAndSetter)
    {
        this.propertyWithGetterAndSetter = propertyWithGetterAndSetter;
    }

    public String getPropertyWithGetterAndSetter()
    {
        return propertyWithGetterAndSetter;
    }

    @JsonIgnore
    public void setPropertyWithGetterAndSetterIgnored(String propertyWithGetterAndSetterIgnored)
    {
        this.propertyWithGetterAndSetterIgnored = propertyWithGetterAndSetterIgnored;
    }

    public String getPropertyWithGetterAndSetterIgnored()
    {
        return propertyWithGetterAndSetterIgnored;
    }

    public static ObjectWithIgnoredProperties createObject()
    {
        ObjectWithIgnoredProperties object = new ObjectWithIgnoredProperties();
        object.property = PROPERTY_VALUE;
        object.propertyIgnored = PROPERTY_IGNORED_VALUE;
        object.propertyWithGetterAndSetter = PROPERTY_WITH_GETTER_AND_SETTER_VALUE;
        object.propertyWithGetterAndSetterIgnored = PROPERTY_WITH_GETTER_AND_SETTER_IGNORED_VALUE;
        return object;
    }

    public static ObjectWithIgnoredProperties createObjectWithIgnoredPropertiesNull()
    {
        ObjectWithIgnoredProperties object = new ObjectWithIgnoredProperties();
        object.property = PROPERTY_VALUE;
        object.propertyWithGetterAndSetter = PROPERTY_WITH_GETTER_AND_SETTER_VALUE;
        return object;
    }

    public static ObjectMap createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(PROPERTY, PROPERTY_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER, PROPERTY_WITH_GETTER_AND_SETTER_VALUE);
        map.putField(PROPERTY_IGNORED, PROPERTY_IGNORED_VALUE);
        map.putField(PROPERTY_WITH_GETTER_AND_SETTER_IGNORED,
                PROPERTY_WITH_GETTER_AND_SETTER_IGNORED_VALUE);
        return map;
    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(getClass(), obj.getClass());

        ObjectWithIgnoredProperties casted = (ObjectWithIgnoredProperties) obj;
        Assert.assertEquals(property, casted.property);
        Assert.assertEquals(propertyIgnored, casted.propertyIgnored);
        Assert.assertEquals(propertyWithGetterAndSetter, casted.propertyWithGetterAndSetter);
        Assert.assertEquals(propertyWithGetterAndSetterIgnored,
                casted.propertyWithGetterAndSetterIgnored);
        return true;
    }

}
