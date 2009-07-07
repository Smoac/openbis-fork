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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.AminoAcidMass;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ModificationType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ModificationHandler extends AbstractHandler
{
    private final Iterable<ModificationType> modificationTypes;

    ModificationHandler(IProtDAO dao)
    {
        super(dao);
        modificationTypes = dao.listModificationTypes();
    }
    
    void createModification(long peptideID, String peptideSequence, AminoAcidMass aminoAcidMass)
    {
        double mass = aminoAcidMass.getMass();
        int position = aminoAcidMass.getPosition();
        char aminoAcid = peptideSequence.charAt(position - 1);
        ModificationType modificationType = findBestMatchingModificationType(aminoAcid, mass);
        dao.createModification(peptideID, modificationType.getId(), position,
                mass);
    }

    private ModificationType findBestMatchingModificationType(char aminoAcid, double mass)
    {
        ModificationType result = null;
        for (ModificationType modificationType : modificationTypes)
        {
            if (modificationType.matches(aminoAcid, mass))
            {
                if (result == null
                        || modificationType.getMassTolerance() < result.getMassTolerance())
                {
                    result = modificationType;
                }
            }
        }
        return result;
    }
}
