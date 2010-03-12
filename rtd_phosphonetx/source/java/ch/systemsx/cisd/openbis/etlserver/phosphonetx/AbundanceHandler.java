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
import java.util.List;
import java.util.Map;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Experiment;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Parameter;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Handler of {@link Parameter} objects of type 'abundance'.
 * 
 * @author Franz-Josef Elmer
 */
class AbundanceHandler extends AbstractHandler
{
    @Private
    static final String MZXML_FILENAME = "MZXML_FILENAME";

    private static final class SampleOrError
    {
        Sample sample;

        String error;
    }

    private final IEncapsulatedOpenBISService openbisService;

    private final GroupIdentifier groupIdentifier;

    private final Experiment experiment;

    private final Map<String, SampleOrError> samplesOrErrors = new HashMap<String, SampleOrError>();

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
        Sample sample = getOrCreateSample(parameter.getName(), proteinName);
        try
        {
            dao
                    .createAbundance(proteinID, sample.getId(), Double.parseDouble(parameter
                            .getValue()));
        } catch (NumberFormatException ex)
        {
            throw new UserFailureException("Abundance of sample '" + parameter.getName()
                    + "' of protein '" + proteinName + "' is not a number: " + parameter.getValue());
        }
    }

    private Sample getOrCreateSample(String parameterName, String proteinName)
    {
        SampleOrError sampleOrError = samplesOrErrors.get(parameterName);
        if (sampleOrError == null)
        {
            SampleIdentifier sampleIdentifier =
                    new SampleIdentifier(groupIdentifier, parameterName);
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                    openbisService.tryGetSampleWithExperiment(sampleIdentifier);
            sampleOrError = new SampleOrError();
            if (sample != null)
            {
                sampleOrError.sample = getOrCreateSample(experiment, sample.getPermId());
            } else
            {
                List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> list =
                        openbisService
                                .listSamplesByCriteria(new ListSamplesByPropertyCriteria(
                                        MZXML_FILENAME, parameterName, groupIdentifier
                                                .getSpaceCode(), null));
                if (list == null || list.size() == 0)
                {
                    sampleOrError.error = "an unidentified sample: " + parameterName;
                } else if (list.size() > 1)
                {
                    sampleOrError.error =
                            "a not uniquely specified sample (" + list.size()
                                    + " samples are found): " + parameterName;
                } else
                {
                    sampleOrError.sample = getOrCreateSample(experiment, list.get(0).getPermId());
                }
            }
            samplesOrErrors.put(parameterName, sampleOrError);
        }
        if (sampleOrError.error != null)
        {
            throw new UserFailureException("Protein '" + proteinName
                    + "' has an abundance value for " + sampleOrError.error);
        }
        return sampleOrError.sample;
    }
}
