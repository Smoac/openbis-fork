/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

/**
 * @author Izabela Adamczyk
 */
public class DatabaseInstanceTranslater
{

    public static DatabaseInstance translate(DatabaseInstancePE databaseInstance)
    {
        if (databaseInstance == null)
        {
            return null;
        }
        DatabaseInstance result = new DatabaseInstance();
        result.setCode(databaseInstance.getCode());
        result.setUuid(databaseInstance.getUuid());
        result.setIdentifier(IdentifierHelper.createIdentifier(databaseInstance).toString());
        return result;
    }

    public static DatabaseInstancePE translate(DatabaseInstance databaseInstance)
    {
        if (databaseInstance == null)
        {
            return null;
        }
        DatabaseInstancePE result = new DatabaseInstancePE();
        result.setCode(databaseInstance.getCode());
        result.setUuid(databaseInstance.getUuid());
        return result;
    }

}
