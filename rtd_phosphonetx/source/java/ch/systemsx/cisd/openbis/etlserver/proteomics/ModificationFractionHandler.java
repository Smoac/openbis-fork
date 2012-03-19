/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Handler of modification fractions.
 *
 * @author Franz-Josef Elmer
 */
class ModificationFractionHandler extends AbstractSampleHandler
{
    public ModificationFractionHandler(IEncapsulatedOpenBISService openbisService, IProtDAO dao,
            ExperimentIdentifier experimentIdentifier, Experiment experiment, String delimiter,
            boolean restrictedSampleResolving)
    {
        super(openbisService, dao, experimentIdentifier, experiment, delimiter,
                restrictedSampleResolving);
    }

    @Override
    protected void handleSample(String parameterName,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample)
    {
        // Do nothing
    }
    
    public void addModificationFractions(String peptideSequence, long modID,
            List<ModificationFraction> modificationFractions)
    {
        for (ModificationFraction modificationFraction : modificationFractions)
        {
            Sample sample = getOrCreateSample(modificationFraction.getSample(), peptideSequence);
            double fraction = modificationFraction.getFraction();
            dao.createModificationFraction(modID, sample.getId(), fraction);
        }
    }

    private Sample getOrCreateSample(String sampleName, String peptideSequence)
    {
        SampleOrError sampleOrError = getOrCreateSampleOrError(sampleName);
        if (sampleOrError.error != null)
        {
            throw new UserFailureException("Protein '" + peptideSequence
                    + "' has modification for " + sampleOrError.error);
        }
        return sampleOrError.sample;
    }

}
