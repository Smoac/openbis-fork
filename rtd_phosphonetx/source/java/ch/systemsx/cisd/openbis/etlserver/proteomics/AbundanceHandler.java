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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Parameter;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Handler of {@link Parameter} objects of type 'abundance'.
 * 
 * @author Franz-Josef Elmer
 */
class AbundanceHandler extends AbstractSampleHandler
{
    private final SampleType sampleType;

    AbundanceHandler(IEncapsulatedOpenBISService openbisService, IProtDAO dao,
            ExperimentIdentifier experimentIdentifier, Experiment experiment, String delimiter,
            boolean restrictedSampleResolving)
    {
        super(openbisService, dao, experimentIdentifier, experiment, delimiter,
                restrictedSampleResolving);
        sampleType = new SampleType();
        sampleType.setCode(Constants.SEARCH_SAMPLE_TYPE);
    }

    void addAbundancesToDatabase(Parameter parameter, long proteinID, String proteinName)
    {
        Sample sample = getOrCreateSample(parameter.getName(), proteinName);
        try
        {
            double abundance = Double.parseDouble(parameter.getValue());
            dao.createAbundance(proteinID, sample.getId(), abundance);
        } catch (NumberFormatException ex)
        {
            throw new UserFailureException("Abundance of sample '" + parameter.getName()
                    + "' of protein '" + proteinName + "' is not a number: " + parameter.getValue());
        }
    }

    private Sample getOrCreateSample(String parameterName, String proteinName)
    {
        SampleOrError sampleOrError = getOrCreateSampleOrError(parameterName);
        if (sampleOrError.error != null)
        {
            throw new UserFailureException("Protein '" + proteinName
                    + "' has an abundance value for " + sampleOrError.error);
        }
        return sampleOrError.sample;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void handleSample(String parameterName,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample)
    {
        NewSample searchSample = new NewSample();
        searchSample.setSampleType(sampleType);
        SpaceIdentifier spaceIdentifier =
                new SpaceIdentifier(experimentIdentifier.getDatabaseInstanceCode(),
                        experimentIdentifier.getSpaceCode());
        SampleIdentifier identifier =
                new SampleIdentifier(spaceIdentifier, parameterName + "_"
                        + experimentIdentifier.getExperimentCode());
        searchSample.setIdentifier(identifier.toString());
        searchSample.setExperimentIdentifier(experimentIdentifier.toString());
        searchSample.setParentIdentifier(sample.getIdentifier());
        openbisService.registerSample(searchSample, null);
    }
}
