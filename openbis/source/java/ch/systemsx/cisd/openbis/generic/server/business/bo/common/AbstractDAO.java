/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.translator.DatabaseInstanceTranslator;

/**
 * 
 *
 * @author    Franz-Josef Elmer
 */
public abstract class AbstractDAO
{
    private final long databaseInstanceId;

    private final DatabaseInstance databaseInstance;
    
    protected AbstractDAO(final DatabaseInstancePE databaseInstance)
    {
        this.databaseInstanceId = databaseInstance.getId();
        this.databaseInstance = DatabaseInstanceTranslator.translate(databaseInstance);
    }

    public final long getDatabaseInstanceId()
    {
        return databaseInstanceId;
    }

    public final DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

}
