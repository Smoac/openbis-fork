package ch.systemsx.cisd.openbis.common.api.server.json.object;

import org.testng.Assert;

import ch.systemsx.cisd.base.annotation.JsonObject;

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

/**
 * @author pkupczyk
 */
@JsonObject(ObjectWithTypeAFactory.TYPE)
public class ObjectWithTypeA extends ObjectWithType
{

    public String a;

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj.getClass(), getClass());

        ObjectWithTypeA casted = (ObjectWithTypeA) obj;
        Assert.assertEquals(casted.base, base);
        Assert.assertEquals(casted.a, a);
        return true;
    }

}
