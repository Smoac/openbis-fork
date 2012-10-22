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

package ch.systemsx.cisd.openbis.common.api.server.json.object;

import org.testng.Assert;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject(ObjectWithNestedTypesFactory.TYPE)
public class ObjectWithNestedTypes
{

    public Object propertyObject;

    public ObjectNested propertyNested;

    public ObjectNestedChild propertyNestedChild;

    @JsonObject(ObjectNestedFactory.TYPE)
    public static class ObjectNested
    {

        public String nested;

        @Override
        public boolean equals(Object obj)
        {
            Assert.assertNotNull(obj);
            Assert.assertEquals(obj.getClass(), getClass());

            ObjectNested casted = (ObjectNested) obj;
            Assert.assertEquals(casted.nested, nested);
            return true;
        }

    }

    @JsonObject(ObjectNestedChildFactory.TYPE)
    public static class ObjectNestedChild extends ObjectNested
    {

        public String nestedChild;

        @Override
        public boolean equals(Object obj)
        {
            Assert.assertNotNull(obj);
            Assert.assertEquals(obj.getClass(), getClass());

            ObjectNestedChild casted = (ObjectNestedChild) obj;
            Assert.assertEquals(casted.nested, nested);
            Assert.assertEquals(casted.nestedChild, nestedChild);
            return true;
        }

    }

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj.getClass(), getClass());

        ObjectWithNestedTypes casted = (ObjectWithNestedTypes) obj;
        Assert.assertEquals(casted.propertyObject, propertyObject);
        Assert.assertEquals(casted.propertyNested, propertyNested);
        Assert.assertEquals(casted.propertyNestedChild, propertyNestedChild);
        return true;
    }

}
