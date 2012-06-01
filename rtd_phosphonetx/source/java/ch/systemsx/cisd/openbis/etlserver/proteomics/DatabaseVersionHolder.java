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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import ch.systemsx.cisd.openbis.dss.generic.shared.IDatabaseVersionHolder;

/**
 * Holds the version of the proteomics database.
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseVersionHolder implements IDatabaseVersionHolder
{
    @Override
    public String getDatabaseVersion()
    {
        return "005"; // changed in S124
    }
}

