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

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Parameter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
class AbundanceHandler extends AbstractHandler
{
    private final IEncapsulatedOpenBISService openbisService;
    private final GroupIdentifier groupIdentifier;
    private final Experiment experiment;
    private final Map<String, Sample> samples = new HashMap<String, Sample>();

    AbundanceHandler(IEncapsulatedOpenBISService openbisService, IProtDAO dao,
            GroupIdentifier groupIdentifier, Experiment experiment)
    {
        super(dao);
        this.openbisService = openbisService;
        this.groupIdentifier = groupIdentifier;
        this.experiment = experiment;
    }

    void addAbundancesToDatabase(Parameter parameter, long proteinID, String proteinName)
    {
        Sample sample = getOrCreateSample(parameter.getName().toUpperCase(), proteinName);
        try
        {
            dao.createAbundance(proteinID, sample.getId(), Double.parseDouble(parameter.getValue()));
        } catch (NumberFormatException ex)
        {
            throw new UserFailureException("Abundance of sample '" + parameter.getName()
                    + "' of protein '" + proteinName + "' is not a number: "
                    + parameter.getValue());
        }
    }

    private Sample getOrCreateSample(String sampleCode, String proteinName)
    {
        Sample sample = samples.get(sampleCode);
        if (sample == null)
        {
            SampleIdentifier sampleIdentifier =
                new SampleIdentifier(groupIdentifier, sampleCode);
            SamplePE samplePE = openbisService.tryGetSampleWithExperiment(sampleIdentifier);
            if (samplePE == null)
            {
                throw new UserFailureException("Protein '" + proteinName
                        + "' has an abundance value for a non-existing sample: " + sampleIdentifier);
            }
            sample = getOrCreateSample(experiment, samplePE.getPermId());
            samples.put(sampleCode, sample);
        }
        return sample;
    }
}
