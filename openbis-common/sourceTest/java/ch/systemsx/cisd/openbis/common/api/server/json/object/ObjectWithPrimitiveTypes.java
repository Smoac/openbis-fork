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
@JsonObject(ObjectWithPrimitiveTypesFactory.TYPE)
public class ObjectWithPrimitiveTypes
{

    public String stringField;

    public Integer integerObjectField;

    public Float floatObjectField;

    public Double doubleObjectField;

    public int integerField;

    public float floatField;

    public double doubleField;

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj.getClass(), getClass());

        ObjectWithPrimitiveTypes casted = (ObjectWithPrimitiveTypes) obj;
        Assert.assertEquals(casted.stringField, stringField);
        Assert.assertEquals(casted.integerObjectField, integerObjectField);
        Assert.assertEquals(casted.floatObjectField, floatObjectField);
        Assert.assertEquals(casted.doubleObjectField, doubleObjectField);
        Assert.assertEquals(casted.integerField, integerField);
        Assert.assertEquals(casted.floatField, floatField);
        Assert.assertEquals(casted.doubleField, doubleField);
        return true;
    }
}
