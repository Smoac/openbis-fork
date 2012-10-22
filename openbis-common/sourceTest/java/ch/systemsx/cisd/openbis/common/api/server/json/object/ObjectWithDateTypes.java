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

@JsonObject(ObjectWithDateTypesFactory.TYPE)
public class ObjectWithDateTypes
{

    public java.util.Date utilDate;

    public java.sql.Date sqlDate;

    public java.sql.Timestamp sqlTimestamp;

    @Override
    public boolean equals(Object obj)
    {
        Assert.assertNotNull(obj);
        Assert.assertEquals(obj.getClass(), getClass());

        ObjectWithDateTypes casted = (ObjectWithDateTypes) obj;
        Assert.assertEquals(casted.utilDate, utilDate);
        Assert.assertEquals(casted.sqlDate, sqlDate);
        Assert.assertEquals(casted.sqlTimestamp, sqlTimestamp);
        return true;
    }

}
