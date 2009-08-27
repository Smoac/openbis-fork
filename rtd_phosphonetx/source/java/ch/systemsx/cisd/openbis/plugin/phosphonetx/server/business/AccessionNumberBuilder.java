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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AccessionNumberBuilder
{
    private final String typeOrNull;
    
    private final String accessionNumber;
    
    public AccessionNumberBuilder(String fullAccessionNumber)
    {
        String[] parts = fullAccessionNumber.split("\\|");
        if (parts.length > 1)
        {
            typeOrNull = parts[0];
            accessionNumber = parts[1];
        } else
        {
            typeOrNull = null;
            accessionNumber = parts[0];
        }
    }

    public final String getTypeOrNull()
    {
        return typeOrNull;
    }

    public final String getAccessionNumber()
    {
        return accessionNumber;
    }
}
